package com.example.user.domain;

import com.example.common.entity.BaseEntity;
import com.example.common.enums.TokenType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "auth_token")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class AuthToken extends BaseEntity {
    @Column(nullable = false, unique = true)
    private String token;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, name = "expired_at")
    private LocalDateTime expireAt;

    @Column(nullable = false)
    @Builder.Default
    private boolean used = false;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TokenType type;


    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expireAt);
    }

    public void markUsed() {
        this.used = true;
    }

}
