package com.osc.userservice.service;

import com.osc.userservice.dto.LoginDTO;
import com.osc.userservice.dto.LoginResponseDto;
import com.osc.userservice.responce.ApiResponse;
import org.springframework.http.ResponseEntity;

public interface LoginService {

   public LoginResponseDto loginUser(LoginDTO userLoginDTO);
}
