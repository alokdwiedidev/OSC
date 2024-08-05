package com.osc.userservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterUserDto {

    @NotNull
    @Size(min = 1, message = "Name is required")
    private String name;

    @NotNull
    @Pattern(regexp = "^\\d{10}$", message = "Invalid contact number")
    private String contact;

    @NotNull
    @Email(message = "Invalid email format")
    private String email;


    @NotNull
    @JsonProperty("DOB")
    private String DOB;


}
