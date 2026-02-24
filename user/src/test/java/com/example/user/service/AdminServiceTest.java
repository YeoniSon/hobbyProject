package com.example.user.service;

import com.example.common.enums.Role;
import com.example.common.exception.BusinessException;
import com.example.common.exception.ErrorCode;
import com.example.user.domain.User;
import com.example.user.dto.request.SignUpRequest;
import com.example.user.dto.response.UserDataReponse;
import com.example.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("AdminService")
class AdminServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AdminService adminService;

    private static User createUser(Role role, boolean deleted) {
        User user = User.builder()
                .email("admin@test.com")
                .name("관리자")
                .password("encoded")
                .nickname("adminNick")
                .phone("010-0000-0000")
                .birth(LocalDate.of(1990, 1, 1))
                .emailVerified(true)
                .deleted(deleted)
                .role(role)
                .build();
        return user;
    }

    @Test
    @DisplayName("adminRegister - 정상 요청 시 ADMIN 역할로 저장")
    void adminRegister_success_savesWithAdminRole() {
        SignUpRequest request = new SignUpRequest(
                "admin@test.com", "adminNick", "관리자", "010-0000-0000", "password", LocalDate.of(1990, 1, 1)
        );
        when(passwordEncoder.encode(anyString())).thenReturn("encoded");

        adminService.adminRegister(request);

        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("getAllUsersWithoutWithdraw - 탈퇴 제외 목록만 반환")
    void getAllUsersWithoutWithdraw_returnsOnlyNotDeleted() {
        User active = createUser(Role.USER, false);
        when(userRepository.findAllByDeletedFalse()).thenReturn(List.of(active));

        List<UserDataReponse> result = adminService.getAllUsersWithoutWithdraw();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getEmail()).isEqualTo(active.getEmail());
    }

    @Test
    @DisplayName("getWithdrawUsers - 탈퇴 유저 목록만 반환")
    void getWithdrawUsers_returnsOnlyDeleted() {
        User withdrawn = createUser(Role.USER, true);
        when(userRepository.findByDeletedTrue()).thenReturn(List.of(withdrawn));

        List<UserDataReponse> result = adminService.getWithdrawUsers();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getEmail()).isEqualTo(withdrawn.getEmail());
    }

    @Test
    @DisplayName("getUserById - 존재하지 않는 id면 USER_NOT_FOUND 예외")
    void getUserById_notFound_throws() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> adminService.getUserById(999L))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode()).isEqualTo(ErrorCode.USER_NOT_FOUND));
    }

    @Test
    @DisplayName("getUserById - 존재하는 id면 UserDataReponse 반환")
    void getUserById_success_returnsResponse() {
        User user = createUser(Role.USER, false);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        UserDataReponse result = adminService.getUserById(1L);

        assertThat(result).isNotNull();
        assertThat(result.getEmail()).isEqualTo(user.getEmail());
    }

    @Test
    @DisplayName("changeRole - 동일한 역할이면 NOT_CHANGEABLE_ROLE 예외")
    void changeRole_sameRole_throws() {
        User user = createUser(Role.USER, false);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> adminService.changeRole(1L, Role.USER))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode()).isEqualTo(ErrorCode.NOT_CHANGEABLE_ROLE));
    }

    @Test
    @DisplayName("changeRole - 다른 역할이면 변경됨")
    void changeRole_success_changesRole() {
        User user = createUser(Role.USER, false);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        adminService.changeRole(1L, Role.ADMIN);

        assertThat(user.getRole()).isEqualTo(Role.ADMIN);
    }

    @Test
    @DisplayName("withdrawUser - 존재하지 않는 userId면 USER_NOT_FOUND 예외")
    void withdrawUser_notFound_throws() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> adminService.withdrawUser(999L))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode()).isEqualTo(ErrorCode.USER_NOT_FOUND));
    }

    @Test
    @DisplayName("withdrawUser - 정상 시 유저 탈퇴 처리")
    void withdrawUser_success_withdraws() {
        User user = createUser(Role.USER, false);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        adminService.withdrawUser(1L);

        assertThat(user.isDeleted()).isTrue();
    }

    @Test
    @DisplayName("depositUser - 정상 시 유저 복구")
    void depositUser_success_restores() {
        User user = createUser(Role.USER, true);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        adminService.depositUser(1L);

        assertThat(user.isDeleted()).isFalse();
    }
}
