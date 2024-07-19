package com.osc.userservice.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Component
public class SessionData {

    private  String userId;

    @NotBlank
    private String sessionId;

    @NotBlank
    private String device;
    private String action;
}

