package com.osc.userservice.dto;


import com.fasterxml.jackson.annotation.JsonProperty;
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
    @JsonProperty("OTP")
    private String otp;
}


