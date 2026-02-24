package com.example.user.service;

import com.example.common.exception.BusinessException;
import com.example.common.exception.ErrorCode;
import com.example.common.token.JwtTokenProvider;
import com.example.user.domain.User;
import com.example.user.dto.request.LoginRequest;
import com.example.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LoginService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    public String login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BusinessException(ErrorCode.DUPLICATE_EMAIL));

        if (!user.isEmailVerified()) {
            throw new BusinessException(ErrorCode.NOT_VERIFIED);
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException(ErrorCode.INVALID_PASSWORD);
        }

        if (user.isDeleted()){
            throw new BusinessException(ErrorCode.WITHDRAW_EMAIL);
        }

        return jwtTokenProvider.createToken(
                user.getId(),
                user.getEmail(),
                user.getRole().name()
        );
    }
    
}
