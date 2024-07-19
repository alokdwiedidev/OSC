package com.osc.userservice.controller;

import com.osc.userservice.dto.PasswordDTO;
import com.osc.userservice.responce.ApiResponse;
import com.osc.userservice.service.RegisterPasswordService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
public class RegisterPasswordController {

    @Autowired
    private RegisterPasswordService passwordService;

    @PatchMapping("/")
    public ResponseEntity<ApiResponse<Object>> createPassword(@Valid @RequestBody PasswordDTO passwordDTO) {
         passwordService.createPassword(passwordDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponse<>(HttpStatus.CREATED.value(), null));
    }
}
