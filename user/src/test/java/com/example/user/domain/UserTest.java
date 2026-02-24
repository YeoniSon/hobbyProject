package com.example.user.domain;

import com.example.common.enums.Role;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("User 엔티티")
class UserTest {

    private static User createUser() {
        return User.builder()
                .email("test@test.com")
                .name("테스트")
                .password("encodedPassword")
                .nickname("닉네임")
                .phone("010-1234-5678")
                .birth(LocalDate.of(1990, 1, 1))
                .emailVerified(false)
                .deleted(false)
                .role(Role.USER)
                .build();
    }

    @Test
    @DisplayName("withdraw() 호출 시 deleted가 true가 된다")
    void withdraw_setsDeletedTrue() {
        User user = createUser();
        assertThat(user.isDeleted()).isFalse();

        user.withdraw();
        assertThat(user.isDeleted()).isTrue();
    }

    @Test
    @DisplayName("deposit() 호출 시 deleted가 false가 된다")
    void deposit_setsDeletedFalse() {
        User user = createUser();
        user.withdraw();
        assertThat(user.isDeleted()).isTrue();

        user.deposit();
        assertThat(user.isDeleted()).isFalse();
    }

    @Test
    @DisplayName("updateProfile에 nickname만 넘기면 nickname만 변경된다")
    void updateProfile_updatesOnlyNickname() {
        User user = createUser();
        String originalPhone = user.getPhone();

        user.updateProfile("새닉네임", null);
        assertThat(user.getNickname()).isEqualTo("새닉네임");
        assertThat(user.getPhone()).isEqualTo(originalPhone);
    }

    @Test
    @DisplayName("updateProfile에 phone만 넘기면 phone만 변경된다")
    void updateProfile_updatesOnlyPhone() {
        User user = createUser();
        String originalNickname = user.getNickname();

        user.updateProfile(null, "010-9999-8888");
        assertThat(user.getPhone()).isEqualTo("010-9999-8888");
        assertThat(user.getNickname()).isEqualTo(originalNickname);
    }

    @Test
    @DisplayName("verifyEmail() 호출 시 emailVerified가 true가 된다")
    void verifyEmail_setsEmailVerifiedTrue() {
        User user = createUser();
        assertThat(user.isEmailVerified()).isFalse();

        user.verifyEmail();
        assertThat(user.isEmailVerified()).isTrue();
    }

    @Test
    @DisplayName("changeRole() 호출 시 role이 변경된다")
    void changeRole_changesRole() {
        User user = createUser();
        assertThat(user.getRole()).isEqualTo(Role.USER);

        user.changeRole(Role.ADMIN);
        assertThat(user.getRole()).isEqualTo(Role.ADMIN);
    }

    @Test
    @DisplayName("changePassword() 호출 시 password가 변경된다")
    void changePassword_changesPassword() {
        User user = createUser();
        String newEncoded = "newEncodedPassword";

        user.changePassword(newEncoded);
        assertThat(user.getPassword()).isEqualTo(newEncoded);
    }
}
