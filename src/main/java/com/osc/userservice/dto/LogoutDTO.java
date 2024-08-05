package com.osc.userservice.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LogoutDTO {
    @NotBlank
    private String userId;

    @NotBlank
    private String sessionId;
    @NotBlank
    private String logoutDevice;
}
