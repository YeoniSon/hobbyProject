package com.example.user.repository;

import com.example.common.enums.TokenType;
import com.example.user.domain.AuthToken;
import com.example.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;

public interface AuthTokenRepository extends JpaRepository<AuthToken, Long> {
    Optional<AuthToken> findByToken(String token);
    Optional<AuthToken> findByTokenAndType(String token, TokenType type);

    void deleteByUserAndType(User user, TokenType type);
    void deleteByExpireAtBefore(LocalDateTime now);
}
