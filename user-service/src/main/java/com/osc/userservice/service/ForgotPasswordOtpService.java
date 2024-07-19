package com.osc.userservice.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.osc.userservice.responce.ApiResponse;
import org.springframework.http.ResponseEntity;

public interface ForgotPasswordOtpService {

    String generateOtp();
    void validateOtp(String email, String otp) throws JsonProcessingException;


}
