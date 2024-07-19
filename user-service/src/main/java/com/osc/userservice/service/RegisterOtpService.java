package com.osc.userservice.service;

import com.osc.userservice.responce.ApiResponse;
import org.springframework.http.ResponseEntity;

public interface RegisterOtpService {

    String generateOtp();

    void validateOtp(String userId, String otp);

    public String getOtpStore(String userId);
}
