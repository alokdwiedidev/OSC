package com.osc.userservice.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.osc.userservice.dto.ForgotPasswordDto;

public interface ForgotPassService {

    void   validateEmailAndOtp(ForgotPasswordDto forgotPasswordDto) throws JsonProcessingException;
}
