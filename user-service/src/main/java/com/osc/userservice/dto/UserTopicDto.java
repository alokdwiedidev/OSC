package com.osc.userservice.dto;

import lombok.Data;

@Data
public class UserTopicDto {
    private String userId;
    private String otp;
    private String email;
    private String name;
    private String contact;
    private String dob;
}
