package com.osc.userservice.service;

import com.osc.userservice.excetion.JsonProcessingCustomException;

public interface RegisterOtpService {

    String generateOtp();

    void validateOtp(String userId, String otp) ;


}
