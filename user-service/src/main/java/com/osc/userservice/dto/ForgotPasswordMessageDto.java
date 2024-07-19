package com.osc.userservice.dto;

import lombok.Data;

@Data
public class ForgotPasswordMessageDto {
    private String otp;
    private String email;
}
