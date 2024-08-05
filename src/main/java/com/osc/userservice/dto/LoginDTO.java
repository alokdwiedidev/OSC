package com.osc.userservice.dto;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginDTO {

    @NotBlank
    private String userId;

    @NotBlank
    private String password;

    @NotBlank
    private String loginDevice;
}