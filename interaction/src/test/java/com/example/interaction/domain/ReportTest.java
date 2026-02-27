package com.example.interaction.domain;

import com.example.common.enums.Role;
import com.example.common.enums.TargetType;
import com.example.user.domain.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Report 엔티티")
class ReportTest {

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
    @DisplayName("빌더로 생성 시 user, targetType, targetId, reason이 설정된다.")
    void builder() {
        User user = createUser();
        Long targetId = 1L;
        String reason = "스팸입니다";

        Report report = Report.builder()
                .user(user)
                .targetType(TargetType.POST)
                .targetId(targetId)
                .reason(reason)
                .build();

        assertThat(report.getUser()).isEqualTo(user);
        assertThat(report.getTargetType()).isEqualTo(TargetType.POST);
        assertThat(report.getTargetId()).isEqualTo(targetId);
        assertThat(report.getReason()).isEqualTo(reason);
    }

    @Test
    @DisplayName("빌더로 댓글 신고 생성 시 targetType이 COMMENT로 설정된다.")
    void builderComment() {
        User user = createUser();

        Report report = Report.builder()
                .user(user)
                .targetType(TargetType.COMMENT)
                .targetId(10L)
                .reason("욕설입니다")
                .build();

        assertThat(report.getTargetType()).isEqualTo(TargetType.COMMENT);
        assertThat(report.getTargetId()).isEqualTo(10L);
        assertThat(report.getReason()).isEqualTo("욕설입니다");
    }
}
