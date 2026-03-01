package com.example.domain;

import com.example.board.domain.Category;
import com.example.board.domain.Comment;
import com.example.board.domain.Post;
import com.example.user.domain.User;
import com.example.common.enums.Role;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Comment 엔티티")
class CommentTest {

    private static Category createCategory() {
        return Category.builder()
                .name("baseball")
                .build();
    }

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

    private static Post createPost() {
        return Post.builder()
                .categoryId(createCategory())
                .userId(createUser())
                .title("제목")
                .content("내용")
                .build();
    }

    private static Comment createComment() {
        return Comment.builder()
                .post(createPost())
                .user(createUser())
                .content("댓글 내용")
                .build();
    }

    @Test
    @DisplayName("빌더로 생성 시 post, user, content가 설정된다.")
    void builder() {
        Post post = createPost();
        User user = createUser();
        Comment comment = Comment.builder()
                .post(post)
                .user(user)
                .content("테스트 댓글")
                .build();

        assertThat(comment.getPost()).isEqualTo(post);
        assertThat(comment.getUser()).isEqualTo(user);
        assertThat(comment.getContent()).isEqualTo("테스트 댓글");
        assertThat(comment.isShow()).isTrue();
    }

    @Test
    @DisplayName("deleteShow() 호출시 show가 true에서 false로 변경된다.")
    void deleteShow() {
        Comment comment = createComment();
        assertThat(comment.isShow()).isTrue();

        comment.deleteShow();
        assertThat(comment.isShow()).isFalse();
    }

    @Test
    @DisplayName("depositShow() 호출시 show가 false에서 true로 변경된다.")
    void depositShow() {
        Comment comment = createComment();
        comment.deleteShow();
        assertThat(comment.isShow()).isFalse();

        comment.depositShow();
        assertThat(comment.isShow()).isTrue();
    }

    @Test
    @DisplayName("updateContent() 호출시 내용이 변경된다.")
    void updateContent() {
        Comment comment = createComment();

        comment.updateContent("수정된 댓글");

        assertThat(comment.getContent()).isEqualTo("수정된 댓글");
    }
}
