package com.osc.userservice.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.osc.userservice.dto.ForgotPassOtpValidatioDto;
import com.osc.userservice.responce.ApiResponse;
import com.osc.userservice.service.ForgotPasswordOtpService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/users")
@RestController
@CrossOrigin(origins = "*")
public class ForgotPasswordOtpValidation {


    private ForgotPasswordOtpService forgotPasswordOtpService;

    @Autowired
    public ForgotPasswordOtpValidation(ForgotPasswordOtpService forgotPasswordOtpService) {
        this.forgotPasswordOtpService = forgotPasswordOtpService;
    }

    @PostMapping("/validateOtp")
    private ResponseEntity<ApiResponse<Object>> otpValidatioForgotPassword(@RequestBody ForgotPassOtpValidatioDto otpValidationDTO)  {
        forgotPasswordOtpService.validateOtp(otpValidationDTO.getEmail(), otpValidationDTO.getOtp());
        return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse(200, null));

    }
}
