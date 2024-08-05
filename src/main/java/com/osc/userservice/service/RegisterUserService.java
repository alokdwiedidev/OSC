package com.osc.userservice.service;

import com.osc.userservice.dto.RegisterUserDto;
import com.osc.userservice.excetion.JsonProcessingCustomException;
import com.osc.userservice.responce.ApiResponse;
import org.springframework.http.ResponseEntity;

public interface RegisterUserService {

    String  registerUser(RegisterUserDto userRegistrationDTO) throws JsonProcessingCustomException;

}
