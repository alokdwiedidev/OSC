package com.osc.userservice.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor

public class OtpValidationDTO {

    @NotNull
    private String userId;;

    @NotNull
    private String otp;
}


