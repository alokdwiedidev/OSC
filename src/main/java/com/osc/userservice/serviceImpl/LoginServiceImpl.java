package com.osc.userservice.serviceImpl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.osc.userservice.config.KafkaProducerConfig;
import com.osc.userservice.dto.LoginDTO;
import com.osc.userservice.dto.LoginResponseDto;
import com.osc.userservice.dto.SessionData;
import com.osc.userservice.entity.User;
import com.osc.userservice.excetion.LoginException;
import com.osc.userservice.grpcclient.LoginServiceClient;
import com.osc.userservice.repository.UserRepository;
import com.osc.userservice.service.LoginService;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

@Slf4j
@Service
public class LoginServiceImpl implements LoginService {

    @Value("${kafka.topic.session}")
    private String SESSION_TOPIC;

    private final LoginServiceClient loginServiceClient;

    private final UserRepository userRepository;
    private final KafkaProducerConfig kafkaConfig;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private static final int MAX_ATTEMPTS = 3;
    private final Map<String, Integer> loginAttempts = new HashMap<>();

    @Autowired
    public LoginServiceImpl(LoginServiceClient loginServiceClient, ModelMapper modelMapper, UserRepository userRepository, KafkaProducerConfig kafkaConfig) {
        this.loginServiceClient = loginServiceClient;
        this.userRepository = userRepository;
        this.kafkaConfig = kafkaConfig;
    }

    @Override
    public LoginResponseDto loginUser(LoginDTO userLoginDTO) {
        String userId = userLoginDTO.getUserId();
        String device =userLoginDTO.getLoginDevice();
        log.info("Attempting to log in user with ID: {}", userId);

        Optional<User> userOptional = userRepository.findByUserId(userLoginDTO.getUserId());
        User user = userOptional.orElseThrow(() ->
                new LoginException.UserNotFoundException("User not found with userId: " + userId)
        );

        // Validating password
        if (!user.getPassword().equals(userLoginDTO.getPassword())) {
            incrementLoginAttempts(userId);
            if (loginAttempts.getOrDefault(userId, 0) >= MAX_ATTEMPTS) {
                log.error("Maximum login attempts exceeded for userId: {}", userId);
                throw new LoginException.TooManyRequestsException("Maximum login attempts exceeded for userId: " + userId);
            }

            throw new LoginException.IncorrectPasswordException("Incorrect password " + userId);
        }

        // Checking if user is already logged in
        if (loginServiceClient.getLoginStatus( userId+":"+device)) {
            throw new LoginException.UserAlreadyLoggedInException("User is already logged in somewhere else: " + userId);
        }

        // Generate session ID
        String sessionId = generateSessionId();
        String loginTime = String.valueOf((LocalDateTime.now()));
        SessionData sessionData = new SessionData(userLoginDTO.getUserId(), sessionId, userLoginDTO.getLoginDevice(), loginTime,true);

        // Check if maximum login attempts exceeded


        try {
            // Converting session object to JSON string
            String jsonSessionData = objectMapper.writeValueAsString(sessionData);

            // Sending session data to Kafka topic
            kafkaConfig.sendMessage(SESSION_TOPIC, userId+":"+device, jsonSessionData);
            log.info("Session data sent to Kafka topic: {}", SESSION_TOPIC);
        } catch (Exception e) {
            log.error("Error processing session data for userId: {}", userId, e);
            throw new LoginException.SessionDataProcessingException("Error processing session data", e);
        }

        resetLoginAttempts(userLoginDTO.getUserId());
        log.info("Login successful for userId: {}", userId);

        return new LoginResponseDto(sessionId, user.getName());
    }

    private void incrementLoginAttempts(String userId) {
        int attempts = loginAttempts.getOrDefault(userId, 0) + 1;
        loginAttempts.put(userId, attempts);
        log.info("Incremented login attempts for userId: {}. Attempts: {}", userId, attempts);
    }

    private void resetLoginAttempts(String userId) {
        loginAttempts.remove(userId);
        log.info("Reset login attempts for userId: {}", userId);
    }

    private String generateSessionId() {
        Random random = new Random();
        int sessionId = 1000000 + random.nextInt(9000000);
        String generatedSessionId = String.valueOf(sessionId);
        log.info("Generated session ID: {}", generatedSessionId);
        return generatedSessionId;
    }
}
