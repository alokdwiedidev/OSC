package com.osc.userservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class PasswordDTO {

    @NotBlank
    private String userId;

    @NotBlank
    @Size(min = 8, max = 16)
    private String password;


}
