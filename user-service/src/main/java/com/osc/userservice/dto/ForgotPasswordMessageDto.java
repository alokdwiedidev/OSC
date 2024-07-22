package com.osc.userservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class ForgotPasswordMessageDto {
    private String otp;
    private String email;
    private  Integer count;
}
