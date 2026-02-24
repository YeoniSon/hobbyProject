package com.example.user.service;

import com.example.common.enums.Role;
import com.example.common.exception.BusinessException;
import com.example.common.exception.ErrorCode;
import com.example.user.domain.User;
import com.example.user.dto.request.SignUpRequest;
import com.example.user.dto.request.UpdateUserRequest;
import com.example.user.dto.response.UserDataReponse;
import com.example.user.repository.AuthTokenRepository;
import com.example.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserService")
class UserServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private EmailVerificationService emailVerificationService;
    @Mock
    private AuthTokenRepository authTokenRepository;

    @InjectMocks
    private UserService userService;

    private static User createUser() {
        return User.builder()
                .email("test@test.com")
                .name("테스트")
                .password("encoded")
                .nickname("닉네임")
                .phone("010-1234-5678")
                .birth(LocalDate.of(1990, 1, 1))
                .emailVerified(false)
                .deleted(false)
                .role(Role.USER)
                .build();
    }

    private static User createUser(Long id) {
        User user = createUser();
        return user; // id는 BaseEntity에서 관리, 테스트에서는 동일 인스턴스만 사용
    }

    @Test
    @DisplayName("signUp - 이메일 중복이면 DUPLICATE_EMAIL 예외")
    void signUp_duplicateEmail_throws() {
        SignUpRequest request = new SignUpRequest(
                "exist@test.com", "닉네임", "이름", "010-1111-2222", "password", LocalDate.of(1990, 1, 1)
        );
        when(userRepository.existsByEmail(request.getEmail())).thenReturn(true);

        assertThatThrownBy(() -> userService.signUp(request))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode()).isEqualTo(ErrorCode.DUPLICATE_EMAIL));

        verify(userRepository, never()).save(any());
        verify(emailVerificationService, never()).sendVerificationEmail(any());
    }

    @Test
    @DisplayName("signUp - 정상 요청 시 유저 저장 후 이메일 발송 호출")
    void signUp_success_savesAndSendsEmail() {
        SignUpRequest request = new SignUpRequest(
                "new@test.com", "닉네임", "이름", "010-1111-2222", "password", LocalDate.of(1990, 1, 1)
        );
        when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");

        userService.signUp(request);

        verify(userRepository).save(any(User.class));
        verify(emailVerificationService).sendVerificationEmail(any(User.class));
    }

    @Test
    @DisplayName("getUserInfo - 존재하지 않는 userId면 USER_NOT_FOUND 예외")
    void getUserInfo_notFound_throws() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUserInfo(999L))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode()).isEqualTo(ErrorCode.USER_NOT_FOUND));
    }

    @Test
    @DisplayName("getUserInfo - 존재하는 유저면 UserDataReponse 반환")
    void getUserInfo_success_returnsResponse() {
        User user = createUser(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        UserDataReponse result = userService.getUserInfo(1L);

        assertThat(result).isNotNull();
        assertThat(result.getEmail()).isEqualTo(user.getEmail());
        assertThat(result.getNickname()).isEqualTo(user.getNickname());
    }

    @Test
    @DisplayName("updateUser - 수정 내용이 기존과 같으면 NO_CHANGE 예외")
    void updateUser_noChange_throws() throws Exception {
        User user = createUser(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        UpdateUserRequest request = createUpdateUserRequest(user.getNickname(), user.getPhone());

        assertThatThrownBy(() -> userService.updateUser(1L, request))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode()).isEqualTo(ErrorCode.NO_CHANGE));
    }

    /** DTO를 수정하지 않고 테스트용으로만 값 설정 (reflection) */
    private static UpdateUserRequest createUpdateUserRequest(String nickname, String phone) throws Exception {
        UpdateUserRequest request = new UpdateUserRequest();
        setField(request, "nickname", nickname);
        setField(request, "phone", phone);
        return request;
    }

    private static void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    @Test
    @DisplayName("changePassword - 현재 비밀번호가 틀리면 INVALID_PASSWORD 예외")
    void changePassword_wrongCurrent_throws() {
        User user = createUser(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong", user.getPassword())).thenReturn(false);

        assertThatThrownBy(() -> userService.changePassword(1L, "wrong", "newPassword"))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode()).isEqualTo(ErrorCode.INVALID_PASSWORD));
    }

    @Test
    @DisplayName("resetPassword - 존재하지 않는 이메일이면 NOT_EXIST_EMAIL 예외")
    void resetPassword_notExistEmail_throws() {
        when(userRepository.findByEmail("none@test.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.resetPassword("none@test.com"))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode()).isEqualTo(ErrorCode.NOT_EXIST_EMAIL));

        verify(emailVerificationService, never()).sendResetPasswordEmail(any());
    }

    @Test
    @DisplayName("userWithdraw - 비밀번호가 틀리면 INVALID_PASSWORD 예외")
    void userWithdraw_wrongPassword_throws() {
        User user = createUser(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong", user.getPassword())).thenReturn(false);

        assertThatThrownBy(() -> userService.userWithdraw(1L, "wrong"))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode()).isEqualTo(ErrorCode.INVALID_PASSWORD));

        assertThat(user.isDeleted()).isFalse();
    }

    @Test
    @DisplayName("userWithdraw - 비밀번호가 맞으면 탈퇴 처리")
    void userWithdraw_success_withdraws() {
        User user = createUser(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("correct", user.getPassword())).thenReturn(true);

        userService.userWithdraw(1L, "correct");

        assertThat(user.isDeleted()).isTrue();
    }
}
