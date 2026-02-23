package com.example.user.service;

import com.example.common.enums.TokenType;
import com.example.common.exception.BusinessException;
import com.example.common.exception.ErrorCode;
import com.example.common.mail.MailService;
import com.example.user.domain.AuthToken;
import com.example.user.domain.User;
import com.example.user.repository.AuthTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EmailVerificationService {

    private final AuthTokenRepository authTokenRepository;
    private final MailService mailService;

    @Transactional
    public void sendVerificationEmail(User user) {

        AuthToken token = createEmailVerifyToken(user);
        authTokenRepository.save(token);

        String link  = "http://localhost:8080/users/email-verify?token="
                + token.getToken();

        String subject = "HabitProject의 가입을 환영합니다.";
        String text = "<p>" + user.getName() + "님의 HabitProject 가입을 환영합니다. </p>"
                + "<p><a href=\"" + link + "\"> 인증하기 </a>를 눌러 가입을 완료하세요.</p>";

        mailService.sendMail(user.getEmail(), subject, text);
    }

    private AuthToken createEmailVerifyToken(User user) {
        return AuthToken.builder()
                .token(UUID.randomUUID().toString())
                .expireAt(LocalDateTime.now().plusMinutes(10))
                .used(false)
                .type(TokenType.EMAIL_VERIFY)
                .user(user)
                .build();
    }

    @Transactional
    public void verifyToken(String tokenValue) {
        AuthToken token = authTokenRepository.findByToken(tokenValue)
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_TOKEN));

        validateToken(token);

        User user = token.getUser();
        user.verifyEmail();

        token.markUsed(); // used = true
    }

    private void validateToken(AuthToken token) {
        if (token.isUsed()) {
            throw new BusinessException(ErrorCode.ALREADY_USED_TOKEN);
        }
        if (token.getType() != TokenType.EMAIL_VERIFY) {
            throw new BusinessException(ErrorCode.INVALID_TOKEN_TYPE);
        }

        if (token.getExpireAt().isBefore(LocalDateTime.now())) {
            throw new BusinessException(ErrorCode.EXPIRED_TOKEN);
        }
    }
}

