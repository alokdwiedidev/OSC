package com.osc.userservice.service;

import com.osc.userservice.dto.PasswordDTO;
import com.osc.userservice.responce.ApiResponse;
import org.springframework.http.ResponseEntity;

public interface RegisterPasswordService {
    void createPassword(PasswordDTO passwordDTO);
}
