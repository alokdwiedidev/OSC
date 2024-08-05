package com.osc.userservice.service;

import com.osc.userservice.dto.ChangePasswordRequestDTO;

public interface PasswordChangeService {
   void changePassword(ChangePasswordRequestDTO changePasswordRequestDTO);
}
