package com.osc.userservice.serviceImpl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.osc.userservice.config.KafkaProducerConfig;
import com.osc.userservice.dto.ForgotPasswordMessageDto;
import com.osc.userservice.excetion.ForgotPasswordException;
import com.osc.userservice.excetion.OtpExceptions;
import com.osc.userservice.mapper.UserMapper;
import com.osc.userservice.repository.UserRepository;
import com.osc.userservice.service.ForgotPasswordOtpService;
import com.osc.userservice.service.RegisterOtpService;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Random;

@Slf4j
@Service
public class ForgotPasswordOtpServiceImpl implements ForgotPasswordOtpService {
    private final UserMapper userMapper;
    private final UserRepository userRepository;
    private final RegisterOtpService otpService;
    private final KafkaProducerConfig kafkaProducerConfig;
    private final ReadOnlyKeyValueStore<String, String> forgotPassOtpStore;
    private static final int MAX_ATTEMPTS = 3;

    @Value("${kafka.topic.forgotPassOtp}")
    private String forgotPassOtpTopic;

    @Autowired
    public ForgotPasswordOtpServiceImpl(UserMapper userMapper, UserRepository userRepository, RegisterOtpService otpService, KafkaProducerConfig kafkaProducerConfig, ReadOnlyKeyValueStore<String, String> forgotPassOtpStore) {
        this.userMapper = userMapper;
        this.userRepository = userRepository;
        this.otpService = otpService;
        this.kafkaProducerConfig = kafkaProducerConfig;
        this.forgotPassOtpStore = forgotPassOtpStore;
    }

    @Override
    public void validateOtp(String email, String otp)  {
        log.info("Validating OTP for email: {}", email);

        String storedData = forgotPassOtpStore.get(email);
        if (storedData == null) {
            throw new ForgotPasswordException.OTPProcessingException("Invalid OTP");
        }

        JsonNode storedDataJson = parseJson(storedData, email);
        String storedOtp = storedDataJson.get("otp").asText();
        int attempts = storedDataJson.get("count").asInt();

        if (!storedOtp.equals(otp)) {
            log.warn("Invalid OTP for userId: {}", email);

            incrementOtpAttempts(email, storedDataJson);
            attempts++;
            if (attempts >= MAX_ATTEMPTS) {
                log.warn("Maximum OTP attempts exceeded for userId: {}", email);
                throw new OtpExceptions.MaximumOtpAttemptsExceededException("Maximum OTP attempts exceeded. A new OTP has been sent.");
            }
            throw new OtpExceptions.OtpValidationException("Invalid OTP");
        }

        log.info("OTP validated successfully for email: {}", email);
        resetOtpAttempts(email, storedDataJson);
    }

    private JsonNode parseJson(String jsonData, String email) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.readTree(jsonData);
        } catch (JsonProcessingException e) {
            log.error("Error parsing JSON data for email: {}", email, e);
            throw new ForgotPasswordException.OTPProcessingException("Error extracting OTP");
        }
    }

    private void incrementOtpAttempts(String email, JsonNode storedDataJson) {
        int attempts = storedDataJson.get("count").asInt() + 1;
        log.info("Incrementing OTP attempts for email: {}. New attempt count: {}", email, attempts);
        sendNewOtp(email, storedDataJson.get("otp").asText(), attempts);
    }

    private void resetOtpAttempts(String email, JsonNode storedDataJson) {
        log.info("Resetting OTP attempts for email: {}", email);
        sendNewOtp(email, storedDataJson.get("otp").asText(), 0);
    }

    private void sendNewOtp(String email, String otp, int attempts) {
        ForgotPasswordMessageDto otpTopicDto = new ForgotPasswordMessageDto(otp, email, attempts);
        ObjectMapper objectMapper = new ObjectMapper();
        String otpData;
        try {
            otpData = objectMapper.writeValueAsString(otpTopicDto);
            log.info("Sending OTP data to Kafka for email: {}", email);
        } catch (JsonProcessingException e) {
            log.error("Error processing OTP JSON for email: {}", email, e);
            throw new RuntimeException("Error processing OTP JSON", e);
        }
        kafkaProducerConfig.sendMessage(forgotPassOtpTopic, email, otpData);
    }
}
