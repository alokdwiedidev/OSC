package com.osc.userservice.service;

import com.osc.userservice.dto.ChangePasswordRequestDTO;
import com.osc.userservice.responce.ApiResponse;
import org.springframework.http.ResponseEntity;

public interface PasswordChangeService {
   void changePassword(ChangePasswordRequestDTO changePasswordRequestDTO);
}
