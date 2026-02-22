package com.example.user.repository;

import com.example.user.domain.AuthToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;

public interface AuthTokenRepository extends JpaRepository<AuthToken, Long> {
    Optional<AuthToken> findByToken(String token);


    // 토큰 정리용
    void deleteByExpiredAtBefore(LocalDateTime now);
}
