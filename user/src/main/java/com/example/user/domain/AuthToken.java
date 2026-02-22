package com.example.user.domain;

import com.example.common.entity.BaseEntity;
import com.example.common.enums.TokenType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "auth_tokens")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class AuthToken extends BaseEntity {
    @Column(nullable = false, unique = true)
    private String token;

    //1:n으로 연결 되어야함
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = false, name = "expired_at")
    private LocalDateTime expiredAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TokenType type;

    @Builder.Default
    @Column(nullable = false)
    private boolean used = false;


    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiredAt);
    }

    public void isUsed() {
        this.used = true;
    }
}
