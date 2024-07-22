package com.osc.userservice.controller;

import com.osc.userservice.dto.LogoutDTO;
import com.osc.userservice.responce.ApiResponse;
import com.osc.userservice.service.LogoutService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/users")
@RestController
@CrossOrigin(origins = "*")
public class LogoutController {
    @Autowired
    private LogoutService logoutService;

    public LogoutController(LogoutService logoutService) {
        this.logoutService = logoutService;
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Object>> logoutUser(@Valid @RequestBody LogoutDTO logoutDTO) {
        logoutService.logoutUser(logoutDTO);
        return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse<>(200, null));
    }
}
