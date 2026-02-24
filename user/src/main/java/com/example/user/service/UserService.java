package com.example.user.service;

import com.example.common.enums.Role;
import com.example.common.enums.TokenType;
import com.example.common.exception.BusinessException;
import com.example.common.exception.ErrorCode;
import com.example.common.mail.MailService;
import com.example.user.domain.AuthToken;
import com.example.user.domain.User;
import com.example.user.dto.request.SignUpRequest;
import com.example.user.dto.request.UpdateUserRequest;
import com.example.user.dto.response.UserDataReponse;
import com.example.user.repository.AuthTokenRepository;
import com.example.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.user.service.EmailVerificationService;

import java.time.LocalDateTime;
import java.util.UUID;


@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailVerificationService emailVerificationService;
    private final AuthTokenRepository authTokenRepository;
    private final MailService mailService;

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

    /*
    회원 정보
     */
    @Transactional
    public UserDataReponse getUserInfo(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        return UserDataReponse.from(user);
    }

    /*
    회원 정보 수정
     */
    @Transactional
    public UserDataReponse updateUser(Long userId, UpdateUserRequest request) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // 수정 요청값이 기존 정보와 모두 같으면 에러
        boolean nicknameUnchanged = request.getNickname() == null || request.getNickname().equals(user.getNickname());
        boolean phoneUnchanged = request.getPhone() == null || request.getPhone().equals(user.getPhone());
        
        if (nicknameUnchanged && phoneUnchanged) {
            throw new BusinessException(ErrorCode.NO_CHANGE);
        }

        if (request.getNickname() != null
                && !request.getNickname().equals(user.getNickname())
                && userRepository.existsByNickname(request.getNickname())) {
            throw new BusinessException(ErrorCode.DUPLICATE_NICKNAME);
        }
        if (request.getPhone() != null
                && !request.getPhone().equals(user.getPhone())
                && userRepository.existsByPhone(request.getPhone())) {
            throw new BusinessException(ErrorCode.DUPLICATE_PHONE);
        }

        user.updateProfile(
                request.getNickname(),
                request.getPhone()
        );

        return UserDataReponse.from(user);

    }

    // 비밀번호 재설정(로그인 상태)
    @Transactional
    public void changePassword(Long userId, String currentPassword, String newPassword ) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // 현재 비밀번호 검증
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new BusinessException(ErrorCode.INVALID_PASSWORD);
        }

        // 새 비밀번호가 이전 비밀번호와 동일한지 체크
        if (passwordEncoder.matches(newPassword, user.getPassword())) {
            throw new BusinessException(ErrorCode.SAME_PASSWORD);
        }

        // 새 비밀번호 재설정
        user.changePassword(passwordEncoder.encode(newPassword));
    }

    // 비밀번호 리셋
    @Transactional
    public void resetPassword(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_EXIST_EMAIL));

        // 기존 토큰 삭제
        authTokenRepository.deleteByUserAndType(user, TokenType.PASSWORD_RESET);

        emailVerificationService.sendResetPasswordEmail(user);
    }


    // 비밀번호 재설정(로그인 x)
    @Transactional
    public void changeResetPassword(String token, String newPassword) {
        AuthToken authToken = authTokenRepository
                .findByTokenAndType(token, TokenType.PASSWORD_RESET)
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_TOKEN_TYPE));

        if (!authToken.isUsed() || authToken.isExpired()) {
            throw new BusinessException(ErrorCode.UNUSED_EXPIRED_TOKEN);
        }

        User user = authToken.getUser();
        user.changePassword(passwordEncoder.encode(newPassword));
        authTokenRepository.delete(authToken); // 사용한 토큰 삭제
    }

}
