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

        authTokenRepository.delete(token); // 사용 완료한 토큰 삭제
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

    /** 비밀번호 재설정 링크의 토큰 검증. PASSWORD_RESET 타입만 허용, 사용 후에는 change-password에서 처리. */
    @Transactional
    public void verifyResetPasswordToken(String tokenValue) {
        AuthToken token = authTokenRepository.findByToken(tokenValue)
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_TOKEN));

        if (token.isUsed()) {
            throw new BusinessException(ErrorCode.ALREADY_USED_TOKEN);
        }
        if (token.getType() != TokenType.PASSWORD_RESET) {
            throw new BusinessException(ErrorCode.INVALID_TOKEN_TYPE);
        }
        if (token.getExpireAt().isBefore(LocalDateTime.now())) {
            throw new BusinessException(ErrorCode.EXPIRED_TOKEN);
        }
        // 검증만 수행, 실제 사용(삭제)는 change-password 시점에 수행
        token.markUsed();
    }


    @Transactional
    public void sendResetPasswordEmail(User user) {

        AuthToken token = createResetToken(user);
        authTokenRepository.save(token);

        String link  = "http://localhost:8080/users/reset-password/email-verify?token="
                + token.getToken();

        String subject = "HabitProject 비밀번호 재설정 인증";
        String text = "<p>" + user.getName() + "님의 HabitProject 비밀번호 재설정을 하기 위한 메일입니다.</p>"
                + "<p><a href=\"" + link + "\"> 재설정 </a>을 눌러 비밀번호를 변경하세요.</p>";


        mailService.sendMail(user.getEmail(), subject, text);
    }

    private AuthToken createResetToken(User user) {
        return AuthToken.builder()
                .token(UUID.randomUUID().toString())
                .user(user)
                .type(TokenType.PASSWORD_RESET)
                .expireAt(LocalDateTime.now().plusMinutes(15))
                .used(false)
                .build();
    }
}

