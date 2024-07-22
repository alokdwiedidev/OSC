package com.osc.userservice.serviceImpl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.osc.userservice.config.KafkaProducerConfig;
import com.osc.userservice.dto.ForgotPasswordDto;
import com.osc.userservice.dto.ForgotPasswordMessageDto;
import com.osc.userservice.entity.User;
import com.osc.userservice.excetion.ForgotPasswordException;
import com.osc.userservice.repository.UserRepository;
import com.osc.userservice.responce.ApiResponse;
import com.osc.userservice.service.ForgotPassService;
import com.osc.userservice.service.RegisterOtpService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
public class ForgotPassServiceImpl implements ForgotPassService {
    private final UserRepository userRepository;
    private final RegisterOtpService otpService;
    private final KafkaProducerConfig kafkaConfig;

    @Autowired
    public ForgotPassServiceImpl(UserRepository userRepository, RegisterOtpService otpService, KafkaProducerConfig kafkaConfig) {
        this.userRepository = userRepository;
        this.otpService = otpService;
        this.kafkaConfig = kafkaConfig;
    }

    @Value("${kafka.topic.forgotPassOtp}")
    private String forgotPassOtpTopic;

    @Override
    public void  validateEmailAndOtp(ForgotPasswordDto forgotPasswordDto) {
        String email = forgotPasswordDto.getEmail();

        Optional<User> user = userRepository.findByEmail(email);
        if (user.isEmpty()) {
            log.warn("No user found with email: {}", email);
            throw new ForgotPasswordException.UserNotFoundException("No user found with email: " + email);
        }
        User foundUser = user.get();
        log.info("User found: {}", foundUser);

        String otp = otpService.generateOtp();
        ForgotPasswordMessageDto messageDto = new ForgotPasswordMessageDto(otp,forgotPasswordDto.getEmail(),1);
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            String message = objectMapper.writeValueAsString(messageDto);

            kafkaConfig.sendMessage(forgotPassOtpTopic, forgotPasswordDto.getEmail(), message);
            log.info("OTP sent successfully to email: {}", email);


        } catch (Exception e) {
            log.error("Error processing OTP for email {}: {}", email, e);
            throw new ForgotPasswordException.OTPProcessingException("Error processing OTP");
        }

    }
}