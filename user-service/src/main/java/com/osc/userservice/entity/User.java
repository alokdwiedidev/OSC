package com.osc.userservice.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "userdb")
public class User {


    @NotNull

    @Id
    private String userId;
    @Email
    private String email;


    @NotNull
    private String name;

    @NotNull
    private String contact;


    @NotNull
    private String dob;

    private String password;

}

