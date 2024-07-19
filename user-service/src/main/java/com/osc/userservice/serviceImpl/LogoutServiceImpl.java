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
import com.osc.userservice.responce.ApiResponse;
import com.osc.userservice.service.LogoutService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
public class LogoutServiceImpl implements LogoutService {
    @Value("${kafka.topic.session}")
    private String SESSION_TOPIC;
    private UserRepository userRepository;
    private LoginServiceClient loginServiceClient;
    private KafkaProducerConfig kafkaProducerConfig;

    @Autowired
    public LogoutServiceImpl(UserRepository userRepository, LoginServiceClient loginServiceClient, KafkaProducerConfig kafkaProducerConfig) {
        this.userRepository = userRepository;
        this.kafkaProducerConfig = kafkaProducerConfig;
        this.loginServiceClient = loginServiceClient;
    }


    public void logoutUser(LogoutDTO logoutDTO) {

        log.info("Method Invoked");
        Optional<User> user = userRepository.findByUserId(logoutDTO.getUserId());
        if (user.isEmpty()) {
            log.warn("User not found with email: {}", logoutDTO.getUserId());
            throw new LogoutException.UserNotFoundException("User not found with email: " + logoutDTO.getUserId());

        }

        if (loginServiceClient.isSessionIdValid(logoutDTO.getUserId(), logoutDTO.getSessionId())) {
            log.warn("Session ID mismatch for user: {}", logoutDTO.getUserId());
            throw new LogoutException.SessionIdMismatchException("Session ID mismatch for user: " + logoutDTO.getUserId());
        }

        String action = "logout";

        SessionData sessionData = new SessionData(logoutDTO.getUserId(), null, null, action);
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            // Convert session object to JSON string
            String jsonSessionData = objectMapper.writeValueAsString(sessionData);
            // Send session data to Kafka topic
            kafkaProducerConfig.sendMessage(SESSION_TOPIC, logoutDTO.getUserId(), jsonSessionData);
            log.info("logout data send to kafka topic session");


        } catch (JsonProcessingException e) {
            log.error("Error processing session data for user: {}", logoutDTO.getUserId(), e);
            throw new LogoutException.SessionDataProcessingException("Error processing session data", e);

        }
    }}