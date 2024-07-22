package com.osc.userservice.controller;

import com.osc.userservice.dto.LoginDTO;
import com.osc.userservice.dto.LoginResponseDto;
import com.osc.userservice.responce.ApiResponse;
import com.osc.userservice.service.LoginService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/users")
@CrossOrigin(origins = "*")
public class LoginController {
    private LoginService loginService;

    public LoginController(LoginService loginService) {
        this.loginService = loginService;
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<Object>> loginUser(@Valid @RequestBody LoginDTO userLoginDTO) {
        log.info("inside login controller");
        LoginResponseDto loginResponseDto = loginService.loginUser(userLoginDTO);
        return ResponseEntity.ok().body(new ApiResponse<>(200, loginResponseDto));
    }
}
