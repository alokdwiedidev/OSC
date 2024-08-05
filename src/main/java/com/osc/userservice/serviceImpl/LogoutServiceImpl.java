package com.osc.userservice.serviceImpl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.osc.userservice.config.KafkaProducerConfig;
import com.osc.userservice.dto.LogoutDTO;
import com.osc.userservice.dto.SessionData;
import com.osc.userservice.entity.User;
import com.osc.userservice.excetion.LogoutException;
import com.osc.userservice.grpcclient.LoginServiceClient;
import com.osc.userservice.repository.UserRepository;
import com.osc.userservice.service.LogoutService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
public class LogoutServiceImpl implements LogoutService {

    @Value("${kafka.topic.session}")
    private String SESSION_TOPIC;

    private final UserRepository userRepository;
    private final LoginServiceClient loginServiceClient;
    private final KafkaProducerConfig kafkaProducerConfig;

    @Autowired
    public LogoutServiceImpl(UserRepository userRepository, LoginServiceClient loginServiceClient, KafkaProducerConfig kafkaProducerConfig) {
        this.userRepository = userRepository;
        this.kafkaProducerConfig = kafkaProducerConfig;
        this.loginServiceClient = loginServiceClient;
    }

    @Override
    public void logoutUser(LogoutDTO logoutDTO) {
        String userId = logoutDTO.getUserId();
        String device =logoutDTO.getLogoutDevice();
        log.info("Attempting to log out user with ID: {}", userId);

        Optional<User> userOptional = userRepository.findByUserId(userId);
        if (userOptional.isEmpty()) {
            log.warn("User not found with userId: {}", userId);
            throw new LogoutException.UserNotFoundException("User not found with userId: " + userId);
        }

        if (loginServiceClient.isSessionIdValid(userId+":"+device, logoutDTO.getSessionId())) {
            log.warn("Session ID mismatch for user: {}", userId);
            String action = "logout";
            SessionData sessionData = new SessionData(userId, null, null, action,false);

            try {
                ObjectMapper objectMapper = new ObjectMapper();
                // Convert session object to JSON string
                String jsonSessionData = objectMapper.writeValueAsString(sessionData);
                // Send session data to Kafka topic
                kafkaProducerConfig.sendMessage(SESSION_TOPIC, userId+":"+device, null);
                log.info("Logout data sent to Kafka topic: {}", SESSION_TOPIC);

            } catch (Exception e) {
                log.error("Error processing session data for user: {}", userId, e);
                throw new LogoutException.SessionDataProcessingException("Error processing session data", e);
            }

            log.info("User logged out successfully with ID: {}", userId);
        }else
            throw new LogoutException.SessionIdMismatchException("Session ID mismatch for user: " + userId);
        }


}
