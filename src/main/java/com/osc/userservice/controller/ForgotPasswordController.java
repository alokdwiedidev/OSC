package com.osc.userservice.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.osc.userservice.dto.ForgotPasswordDto;
import com.osc.userservice.responce.ApiResponse;
import com.osc.userservice.service.ForgotPassService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RequestMapping("/users")
@RestController
@CrossOrigin(origins = "*")
public class ForgotPasswordController {
    @Autowired
    private ForgotPassService forgotPassOtpService;


    @PostMapping("/forgotPassword")
    public ResponseEntity<ApiResponse<Object>> sendOtpForForgotPass(@RequestBody ForgotPasswordDto forgotPasswordDto) throws JsonProcessingException {
        log.info("inside the meathod :{}" + forgotPasswordDto.getEmail());
        forgotPassOtpService.validateEmailAndOtp(forgotPasswordDto);
        return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse(200, null));


    }
}
