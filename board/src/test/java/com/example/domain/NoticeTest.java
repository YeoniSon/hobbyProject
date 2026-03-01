package com.example.domain;

import com.example.board.domain.Notice;
import com.example.user.domain.User;
import com.example.common.enums.Role;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Notice 엔티티")
class NoticeTest {

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

    private static Notice createNotice() {
        return Notice.builder()
                .user(createUser())
                .title("공지 제목")
                .content("공지 내용")
                .build();
    }

    @Test
    @DisplayName("빌더로 생성 시 title, content, user가 설정된다.")
    void builder() {
        User user = createUser();
        Notice notice = Notice.builder()
                .user(user)
                .title("테스트 제목")
                .content("테스트 내용")
                .build();

        assertThat(notice.getTitle()).isEqualTo("테스트 제목");
        assertThat(notice.getContent()).isEqualTo("테스트 내용");
        assertThat(notice.getUser()).isEqualTo(user);
        assertThat(notice.isShow()).isTrue();
    }

    @Test
    @DisplayName("deleteShow() 호출시 show가 true에서 false로 변경된다.")
    void deleteShow() {
        Notice notice = createNotice();
        assertThat(notice.isShow()).isTrue();

        notice.deleteShow();
        assertThat(notice.isShow()).isFalse();
    }

    @Test
    @DisplayName("depositShow() 호출시 show가 false에서 true로 변경된다.")
    void depositShow() {
        Notice notice = createNotice();
        notice.deleteShow();
        assertThat(notice.isShow()).isFalse();

        notice.depositShow();
        assertThat(notice.isShow()).isTrue();
    }

    @Test
    @DisplayName("updateTitle() 호출시 제목이 변경된다.")
    void updateTitle() {
        Notice notice = createNotice();

        notice.updateTitle("변경된 제목");

        assertThat(notice.getTitle()).isEqualTo("변경된 제목");
    }

    @Test
    @DisplayName("updateContent() 호출시 내용이 변경된다.")
    void updateContent() {
        Notice notice = createNotice();

        notice.updateContent("변경된 내용");

        assertThat(notice.getContent()).isEqualTo("변경된 내용");
    }
}
