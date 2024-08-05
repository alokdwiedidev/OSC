package com.osc.userservice.serviceImpl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.osc.userservice.config.KafkaProducerConfig;
import com.osc.userservice.dto.ForgotPasswordDto;
import com.osc.userservice.dto.ForgotPasswordMessageDto;
import com.osc.userservice.entity.User;
import com.osc.userservice.excetion.ForgotPasswordException;
import com.osc.userservice.repository.UserRepository;
import com.osc.userservice.service.ForgotPassService;
import com.osc.userservice.service.RegisterOtpService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
public class ForgotPassServiceImpl implements ForgotPassService {

    private final UserRepository userRepository;
    private final RegisterOtpService otpService;
    private final KafkaProducerConfig kafkaConfig;

    @Value("${kafka.topic.forgotPassOtp}")
    private String forgotPassOtpTopic;

    @Autowired
    public ForgotPassServiceImpl(UserRepository userRepository, RegisterOtpService otpService, KafkaProducerConfig kafkaConfig) {
        this.userRepository = userRepository;
        this.otpService = otpService;
        this.kafkaConfig = kafkaConfig;
    }

    @Override
    public void validateEmailAndOtp(ForgotPasswordDto forgotPasswordDto) {
        String email = forgotPasswordDto.getEmail();
        log.info("Validating email: {}", email);

        Optional<User> userOptional = userRepository.findByEmail(email);
        if (userOptional.isEmpty()) {
            log.warn("No user found with email: {}", email);
            throw new ForgotPasswordException.UserNotFoundException("No user found with email: " + email);
        }

        User user = userOptional.get();
        log.info("User found: {}", user);

        String otp = otpService.generateOtp();
        log.info("Generated OTP for email {}: {}", email, otp);

        ForgotPasswordMessageDto messageDto = new ForgotPasswordMessageDto(otp, email, 0);

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            String message = objectMapper.writeValueAsString(messageDto);
            kafkaConfig.sendMessage(forgotPassOtpTopic, email, message);
            log.info("OTP sent successfully to Kafka topic for email: {}", email);
        } catch (Exception e) {
            log.error("Error processing OTP for email {}: {}", email, e.getMessage());
            throw new ForgotPasswordException.OTPProcessingException("Error processing OTP");
        }
    }
}
