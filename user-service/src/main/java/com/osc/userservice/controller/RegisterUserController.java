package com.osc.userservice.controller;

import com.osc.userservice.dto.RegisterUserDto;
import com.osc.userservice.excetion.JsonProcessingCustomException;
import com.osc.userservice.responce.ApiResponse;
import com.osc.userservice.service.RegisterUserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/users")
public class RegisterUserController {

    @Autowired
    private RegisterUserService userService;

    public RegisterUserController(RegisterUserService userService) {
        this.userService = userService;
    }

    @PostMapping("/")
    public ResponseEntity<ApiResponse<Object>> registerUser(@Valid @RequestBody RegisterUserDto userRegistrationDTO) throws JsonProcessingCustomException {
       String userId=userService.registerUser(userRegistrationDTO);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(200, Map.of("userId", userId)));
    }
}
