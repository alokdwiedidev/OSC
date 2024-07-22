package com.osc.userservice.controller;

import com.osc.userservice.dto.ChangePasswordRequestDTO;
import com.osc.userservice.responce.ApiResponse;
import com.osc.userservice.service.PasswordChangeService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/users")
@RestController
@CrossOrigin(origins = "*")
public class PasswordChangeController {
    private PasswordChangeService passwordChangeService;

    public PasswordChangeController(PasswordChangeService passwordChangeService) {
        this.passwordChangeService = passwordChangeService;
    }

    @PatchMapping("/changePassword")
    public ResponseEntity<ApiResponse<Object>> changePassword(@Validated @RequestBody ChangePasswordRequestDTO changePasswordRequestDTO) {
         passwordChangeService.changePassword(changePasswordRequestDTO);
        return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse(200, null));
    }
}
