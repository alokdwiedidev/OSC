package com.osc.userservice.service;

import com.fasterxml.jackson.core.JsonProcessingException;

public interface ForgotPasswordOtpService {


    void validateOtp(String email, String otp) ;

}
