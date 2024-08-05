package com.osc.userservice.controller;

import com.osc.userservice.dto.PasswordDTO;
import com.osc.userservice.responce.ApiResponse;
import com.osc.userservice.service.RegisterPasswordService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
@CrossOrigin(origins = "*")
public class RegisterPasswordController {

    @Autowired
    private RegisterPasswordService passwordService;

    @PostMapping("/createPassword")
    public ResponseEntity<ApiResponse<Object>> createPassword(@Valid @RequestBody PasswordDTO passwordDTO) {
        passwordService.createPassword(passwordDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponse<>(200, null));
    }
}
