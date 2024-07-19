package com.osc.userservice.service;

import com.osc.userservice.dto.LogoutDTO;
import com.osc.userservice.responce.ApiResponse;
import org.springframework.http.ResponseEntity;

public interface LogoutService {

    public void logoutUser(LogoutDTO logoutDTO);
}
