package com.osc.sessionservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "sessions")
public class SessionEntity {
    @Id
    private String userId;
    private String sessionId;
    private LocalDateTime loginTime;
    private LocalDateTime logoutTime;
    private String device;
}
