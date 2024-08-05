package com.osc.userservice.serviceImpl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.osc.userservice.config.KafkaProducerConfig;
import com.osc.userservice.dto.OtpTopicDto;
import com.osc.userservice.excetion.OtpExceptions;
import com.osc.userservice.repository.UserRepository;
import com.osc.userservice.service.RegisterOtpService;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.util.Random;

@Slf4j
@Service
public class RegisterOtpServiceImpl implements RegisterOtpService {

    private final KafkaProducerConfig kafkaProducerConfig;
    private final UserRepository userRepository;
    private final ReadOnlyKeyValueStore<String, String> otpStore;
    private static final int MAX_ATTEMPTS = 3;
    private final Random random = new Random();

    @Value("${kafka.topic.otpTopic}")
    private String otpTopic;

    @Autowired
    public RegisterOtpServiceImpl(KafkaProducerConfig kafkaProducerConfig, UserRepository userRepository, ReadOnlyKeyValueStore<String, String> otpStore) {
        this.kafkaProducerConfig = kafkaProducerConfig;
        this.userRepository = userRepository;
        this.otpStore = otpStore;
    }

    @Override
    public String generateOtp() {
        int otp = 100000 + random.nextInt(900000);
        log.info("Generated OTP: {}", otp);
        return String.valueOf(otp);
    }

    @Override
    public void validateOtp(String userId, String otp)  {
        log.info("Validating OTP for userId: {}", userId);

        String storedData = otpStore.get(userId);
        if (storedData == null) {
            log.error("No OTP data found for userId: {}", userId);
            throw new OtpExceptions.OtpValidationException("Invalid OTP");
        }

        JsonNode storedDataJson = parseJson(storedData, userId);
        String storedOtp = storedDataJson.get("otp").asText();
        int attempts = storedDataJson.get("otpCount").asInt();

        if (!storedOtp.equals(otp)) {
            log.warn("Invalid OTP for userId: {}", userId);

            incrementOtpAttempts(userId, storedDataJson);
            attempts++;
            if (attempts >= MAX_ATTEMPTS) {
                log.warn("Maximum OTP attempts exceeded for userId: {}", userId);
                throw new OtpExceptions.MaximumOtpAttemptsExceededException("Maximum OTP attempts exceeded. A new OTP has been sent.");
            }
            throw new OtpExceptions.OtpValidationException("Invalid OTP");
        }

        log.info("OTP validated successfully for userId: {}", userId);
    }

    private JsonNode parseJson(String jsonData, String userId) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.readTree(jsonData);
        } catch (JsonProcessingException e) {
            log.error("Error parsing JSON data for userId: {}", userId, e);
            throw new OtpExceptions.OtpValidationException("Error extracting OTP");
        }
    }

    private void incrementOtpAttempts(String userId, JsonNode storedDataJson) {
        int attempts = storedDataJson.get("otpCount").asInt() + 1;
        log.info("Incrementing OTP attempts for userId: {} to {}", userId, attempts);
        updateAttempt(userId, storedDataJson.get("otp").asText(), attempts);
    }


    private void updateAttempt(String userId, String otp, int attempts) {
        OtpTopicDto otpTopicDto = new OtpTopicDto(userId, getEmailForUserId(userId), otp, attempts);
        ObjectMapper objectMapper = new ObjectMapper();
        String otpData;
        try {
            otpData = objectMapper.writeValueAsString(otpTopicDto);
            log.debug("Updated OTP attempts for userId: {} - Data: {}", userId, otpData);
        } catch (JsonProcessingException e) {
            log.error("Error processing OTP JSON for userId: {}", userId, e);
            throw new RuntimeException("Error processing OTP JSON", e);
        }

        kafkaProducerConfig.sendMessage(otpTopic, userId, otpData);
        log.info("Sent OTP data to Kafka for userId: {} on topic: {}", userId, otpTopic);
    }

    private String getEmailForUserId(String userId) {
        try {
            String storedData = otpStore.get(userId);
            if (storedData == null) {
                log.error("No OTP data found for userId: {}", userId);
                throw new OtpExceptions.OtpValidationException("No OTP data found for userId: " + userId);
            }

            JsonNode storedDataJson = parseJson(storedData, userId);
            return storedDataJson.get("email").asText();
        } catch (Exception e) {
            log.error("Error retrieving email for userId {}: {}", userId, e.getMessage());
            throw new RuntimeException("Error retrieving email for userId: " + userId, e);
        }
    }
}
