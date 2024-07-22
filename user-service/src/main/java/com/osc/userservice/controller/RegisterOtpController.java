package com.osc.userservice.controller;

import com.osc.userservice.dto.OtpValidationDTO;
import com.osc.userservice.excetion.JsonProcessingCustomException;
import com.osc.userservice.responce.ApiResponse;
import com.osc.userservice.service.RegisterOtpService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
@CrossOrigin(origins = "*")
public class RegisterOtpController {
    @Autowired
    private RegisterOtpService otpService;

    @PostMapping("/validateRegisterOtp")
    public ResponseEntity<ApiResponse<Object>> validateOtp(@Valid @RequestBody OtpValidationDTO otpValidationDTO) throws JsonProcessingCustomException {
      otpService.validateOtp(otpValidationDTO.getUserId(), otpValidationDTO.getOTP());
      return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse<> (200, null));
    }
}
