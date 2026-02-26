package com.example.interaction.domain;

import com.example.common.enums.TargetType;
import com.example.user.domain.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import com.example.common.enums.Role;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Like 엔티티")
class LikeTest {

    private static User createUser() {
        return User.builder()
                .email("test@test.com")
                .name("테스트")
                .password("password")
                .nickname("nick")
                .phone("01012345678")
                .birth(LocalDate.of(1990, 1, 1))
                .role(Role.USER)
                .build();
    }

    @Test
    @DisplayName("빌더로 생성 시 userId, targetType, targetId가 설정된다.")
    void builder() {
        User user = createUser();
        Long targetId = 1L;

        Like like = Like.builder()
                .userId(user)
                .targetType(TargetType.POST)
                .targetId(targetId)
                .build();

        assertThat(like.getUserId()).isEqualTo(user);
        assertThat(like.getTargetType()).isEqualTo(TargetType.POST);
        assertThat(like.getTargetId()).isEqualTo(targetId);
    }
}
