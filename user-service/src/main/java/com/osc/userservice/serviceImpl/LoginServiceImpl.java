package com.osc.userservice.serviceImpl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.osc.userservice.config.KafkaProducerConfig;
import com.osc.userservice.dto.LoginDTO;
import com.osc.userservice.dto.LoginResponseDto;
import com.osc.userservice.dto.SessionData;
import com.osc.userservice.entity.User;
import com.osc.userservice.excetion.LoginException;
import com.osc.userservice.grpcclient.LoginServiceClient;
import com.osc.userservice.repository.UserRepository;
import com.osc.userservice.responce.ApiResponse;
import com.osc.userservice.service.LoginService;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

@Slf4j
@Service
public class LoginServiceImpl implements LoginService {
    @Value("${kafka.topic.session}")
    private String SESSION_TOPIC;
    private LoginServiceClient loginServiceClient;
    private SessionData sessionData;
    private KafkaProducer<String, String> kafkaProducer;
    private UserRepository userRepository;
    private KafkaProducerConfig kafkaConfig;
    ObjectMapper objectMapper = new ObjectMapper();
    private static final int MAX_ATTEMPTS = 3;
    private final Map<String, Integer> loginAttempts = new HashMap<>();

    @Autowired
    public LoginServiceImpl(LoginServiceClient loginServiceClient, ModelMapper modelMapper, SessionData sessionData, UserRepository userRepository, KafkaProducerConfig kafkaConfig) {
        this.loginServiceClient = loginServiceClient;
        this.sessionData = sessionData;
        this.userRepository = userRepository;
        this.kafkaConfig = kafkaConfig;
    }

    @Override
    public LoginResponseDto loginUser(LoginDTO userLoginDTO) {
        String userId = userLoginDTO.getUserId();

        Optional<User> userOptional = userRepository.findByUserId(userLoginDTO.getUserId());
        User user = userOptional.orElseThrow(() -> new LoginException.UserNotFoundException("User not found with userId: " + userId));


        // Validating password
        if (!user.getPassword().equals(userLoginDTO.getPassword())) {
            incrementLoginAttempts(userId);
            throw new LoginException.IncorrectPasswordException("Incorrect password for user: " + userId);
        }

        if (loginServiceClient.getLoginStatus(userId, userLoginDTO.getLoginDevice())) {
            throw new LoginException.UserAlreadyLoggedInException("User is already logged in somewhere else: " + userId);
        }
        resetLoginAttempts(userLoginDTO.getUserId());

        // Generate session ID
        String sessionId = generateSessionId();
        String action = "login";

        SessionData sessionData = new SessionData(userLoginDTO.getUserId(), sessionId, userLoginDTO.getLoginDevice(), action);
        try {
            // Convert session object to JSON string
            String jsonSessionData = objectMapper.writeValueAsString(sessionData);
            // Send session data to Kafka topic
            kafkaConfig.sendMessage(SESSION_TOPIC, userLoginDTO.getUserId(), jsonSessionData);
            log.info("Data sent to Kafka topic session");
        } catch (JsonProcessingException e) {
            throw new LoginException.SessionDataProcessingException("Error processing session data", e);
        }
        if (loginAttempts.getOrDefault(userLoginDTO.getUserId(), 0) >= MAX_ATTEMPTS) {
            throw new LoginException.TooManyRequestsException("Maximum login attempts exceeded for userId: ");

        }

return new LoginResponseDto(sessionId,user.getName());

    }

    private void incrementLoginAttempts(String email) {
        loginAttempts.put(email, loginAttempts.getOrDefault(email, 1) + 1);
    }

    private void resetLoginAttempts(String email) {
        loginAttempts.remove(email);
    }

    public String generateSessionId() {
        Random random = new Random();
        int sessionId = 1000000 + random.nextInt(9000000);
        return String.valueOf(sessionId);


    }
}