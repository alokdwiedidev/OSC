package com.osc.userservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class OtpTopicDto {
    private String userId;
    private String email;
    private String otp;
    private Integer otpCount;

}
