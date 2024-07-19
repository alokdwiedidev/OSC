package com.osc.sessionservice.repository;

import com.osc.sessionservice.entity.SessionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SessionRepository extends JpaRepository<SessionEntity,Long> {
    SessionEntity findByUserIdAndLogoutTimeIsNull(String userId);
    SessionEntity findByUserId(String userId);
}
