package com.example.user.service;

import com.example.common.enums.Role;
import com.example.common.exception.BusinessException;
import com.example.common.exception.ErrorCode;
import com.example.user.domain.User;
import com.example.user.dto.request.SignUpRequest;
import com.example.user.dto.response.UserDataReponse;
import com.example.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /*
    - 관리자 등록
    - 회원정보 전체 조회
    - 회원정보 조회
    - 역할 변경
    - 회원 탈퇴 처리
     */

    // 관리자 등록
    @Transactional
    public void adminRegister(SignUpRequest request) {
        User user = createAdmin(request);
        userRepository.save(user);
    }

    private User createAdmin(SignUpRequest request) {
        return User.builder()
                .email(request.getEmail())
                .name(request.getName())
                .password(passwordEncoder.encode(request.getPassword()))
                .phone(request.getPhone())
                .emailVerified(true)
                .nickname(request.getNickname())
                .birth(request.getBirth())
                .role(Role.ADMIN)
                .build();
    }

    // 회원 정보 전체 조회
    public List<UserDataReponse> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(UserDataReponse::from)
                .toList();
    }

    // 회원 정보 탈퇴 유저 제외 전체 조회
    public List<UserDataReponse> getAllUsersWithoutWithdraw() {
        return userRepository.findAllByDeletedFalse()
                .stream()
                .map(UserDataReponse::from)
                .toList();
    }

    // 회원 정보 탈퇴 유저 조회
    public List<UserDataReponse> getWithdrawUsers() {
        return userRepository.findByDeletedTrue()
                .stream()
                .map(UserDataReponse::from)
                .toList();
    }

    // 특정 회원 정보 조회
    public UserDataReponse getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(()-> new BusinessException(ErrorCode.USER_NOT_FOUND));

        return UserDataReponse.from(user);
    }

    // 회원 역할 변경
    @Transactional
    public void changeRole(Long userId, Role role) {
        User user = userRepository.findById(userId)
                .orElseThrow(()-> new BusinessException(ErrorCode.USER_NOT_FOUND));

        if (user.getRole() == role) {
            throw new BusinessException(ErrorCode.NOT_CHANGEABLE_ROLE);
        }

        user.changeRole(role);

    }

    // 탈퇴 처리
    @Transactional
    public void withdrawUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(()-> new BusinessException(ErrorCode.USER_NOT_FOUND));

        user.withdraw();
    }


    // 복구
    @Transactional
    public void depositUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(()-> new BusinessException(ErrorCode.USER_NOT_FOUND));

        user.deposit();
    }

}
