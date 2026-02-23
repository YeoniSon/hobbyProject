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
import com.example.user.service.EmailVerificationService;


@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailVerificationService emailVerificationService;

    /*
    회원가입 로직 구현
     */
    @Transactional
    public void signUp(SignUpRequest request) {
        validateEmail(request.getEmail());

        User user = createUser(request);
        userRepository.save(user);

        emailVerificationService.sendVerificationEmail(user);
    }

    private void validateEmail(String email) {
        if (userRepository.existsByEmail(email)) {
            throw new BusinessException(ErrorCode.DUPLICATE_EMAIL);
        }
    }

    private User createUser(SignUpRequest request) {
        return User.builder()
                .email(request.getEmail())
                .name(request.getName())
                .password(passwordEncoder.encode(request.getPassword()))
                .phone(request.getPhone())
                .emailVerified(false)
                .nickname(request.getNickname())
                .birth(request.getBirth())
                .role(Role.USER)
                .build();
    }

}
