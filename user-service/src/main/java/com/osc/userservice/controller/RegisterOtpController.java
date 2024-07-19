package com.osc.userservice.controller;

import com.osc.userservice.dto.OtpValidationDTO;
import com.osc.userservice.responce.ApiResponse;
import com.osc.userservice.service.RegisterOtpService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
public class RegisterOtpController {

    @Autowired
    private RegisterOtpService otpService;

    @GetMapping("/")
    public ResponseEntity<ApiResponse<Object>> validateOtp(@Valid @RequestBody OtpValidationDTO otpValidationDTO) {
      otpService.validateOtp(otpValidationDTO.getUserId(), otpValidationDTO.getOtp());
      return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse<> (HttpStatus.OK.value(), "OTP validated successfully"));
    }
}
