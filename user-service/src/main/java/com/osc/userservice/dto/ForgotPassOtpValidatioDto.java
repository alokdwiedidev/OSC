package com.osc.userservice.dto;


import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ForgotPassOtpValidatioDto {

    @NotNull
    private String email;;

    @NotNull
    private String otp;
}


