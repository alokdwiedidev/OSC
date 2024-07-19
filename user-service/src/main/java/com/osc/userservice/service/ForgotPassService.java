package com.osc.userservice.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.osc.userservice.dto.ForgotPasswordDto;
import com.osc.userservice.responce.ApiResponse;
import org.springframework.http.ResponseEntity;

public interface ForgotPassService {

    void   validateEmailAndOtp(ForgotPasswordDto forgotPasswordDto) throws JsonProcessingException;
}
