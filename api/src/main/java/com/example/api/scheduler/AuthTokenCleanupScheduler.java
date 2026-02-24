package com.example.api.scheduler;

import com.example.user.repository.AuthTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 만료된 AuthToken을 주기적으로 삭제한다.
 */
@Component
@RequiredArgsConstructor
public class AuthTokenCleanupScheduler {

    private final AuthTokenRepository authTokenRepository;

    /** 매일 새벽 3시에 만료된 토큰 삭제 */
    @Scheduled(cron = "0 0 3 * * *")
    public void deleteExpiredTokens() {
        authTokenRepository.deleteByExpireAtBefore(LocalDateTime.now());
    }
}
