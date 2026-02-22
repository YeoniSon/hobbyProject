package com.example.user.service;

import com.example.common.enums.Role;
import com.example.common.enums.TokenType;
import com.example.common.exception.BusinessException;
import com.example.common.exception.ErrorCode;
import com.example.common.mail.MailService;
import com.example.user.domain.AuthToken;
import com.example.user.domain.User;
import com.example.user.dto.request.SignUpRequest;
import com.example.user.repository.AuthTokenRepository;
import com.example.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final AuthTokenRepository authTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final MailService mailSender;


    /*
    ======== 회원가입 ==============
    회원가입 로직
    1. 이메일 존재 여부 확인
    2. 비밀번호 암호화
    3. User 저장
    4. 이메일 인증 토큰 생성
    5. 토큰 저장
    6. 인증 메일 발송
     */
    @Transactional
    public void signUp(SignUpRequest signUpRequest) {
        validateEmail(signUpRequest.getEmail());

        User user = createUser(signUpRequest);
        userRepository.save(user);

        AuthToken token = createEmailVerifyToken(user);
        authTokenRepository.save(token);

        sendVerifyEmail(user, token);

    }

    // 이메일 중복 확인
    private void validateEmail(String email) {
        if (!userRepository.existsByEmail(email)) {
            throw new BusinessException(ErrorCode.EMAIL_EXIST);
        }
    }

    // 회원 정보 전달
    private User createUser(SignUpRequest signUpRequest) {
        return User.builder()
                .email(signUpRequest.getEmail())
                .name(signUpRequest.getName())
                .nickname(signUpRequest.getNickname())
                .password(passwordEncoder.encode(signUpRequest.getPassword()))
                .phone(signUpRequest.getPhone())
                .birth(signUpRequest.getBirth())
                .role(Role.USER)
                .build();
    }

    // 이메일 인증 토큰 발송
    private AuthToken createEmailVerifyToken(User user) {
        return AuthToken.builder()
                .token(UUID.randomUUID().toString())
                .user(user)
                .expiredAt(LocalDateTime.now().plusMinutes(10))
                .type(TokenType.EMAIL_VERIFY)
                .used(false)
                .build();
    }

    // 인증 이메일 전송
    private void sendVerifyEmail(User user, AuthToken token) {
        String link = "http://localhost:8080/user/email-auth?token="
                + token.getToken();

        String subject = "HabitProject 사이트에 가입을 축하드립니다.";
        String text = "<p>" + user.getName() + " 님 "
                + "HabitProject 사이트 가입을 축하드립니다.</p>" +
                "<p>아래 링크를 클릭하셔서 가입을 완료 하세요.</p>" +
                "<div><a href='" + link + "'> 인증완료 </a></div>";

        mailSender.sendMail(user.getEmail(), subject, text);
    }

}
