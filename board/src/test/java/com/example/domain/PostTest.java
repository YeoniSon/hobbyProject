package com.example.domain;

import com.example.board.domain.Category;
import com.example.board.domain.Post;
import com.example.user.domain.User;
import com.example.common.enums.Role;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Post 엔티티")
class PostTest {

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

    @Test
    @DisplayName("빌더로 생성 시 title, content, categoryId, userId가 설정된다.")
    void builder() {
        Category category = createCategory();
        User user = createUser();
        Post post = Post.builder()
                .categoryId(category)
                .userId(user)
                .title("테스트 제목")
                .content("테스트 내용")
                .build();

        assertThat(post.getTitle()).isEqualTo("테스트 제목");
        assertThat(post.getContent()).isEqualTo("테스트 내용");
        assertThat(post.getCategoryId()).isEqualTo(category);
        assertThat(post.getUserId()).isEqualTo(user);
        assertThat(post.isShow()).isTrue();
    }

    @Test
    @DisplayName("deleteShow() 호출시 show가 true에서 false로 변경된다.")
    void deleteShow() {
        Post post = createPost();
        assertThat(post.isShow()).isTrue();

        post.deleteShow();
        assertThat(post.isShow()).isFalse();
    }

    @Test
    @DisplayName("depositShow() 호출시 show가 false에서 true로 변경된다.")
    void depositShow() {
        Post post = createPost();
        post.deleteShow();
        assertThat(post.isShow()).isFalse();

        post.depositShow();
        assertThat(post.isShow()).isTrue();
    }

    @Test
    @DisplayName("updateCategory() 호출시 카테고리가 변경된다.")
    void updateCategory() {
        Post post = createPost();
        Category newCategory = Category.builder().name("soccer").build();

        post.updateCategory(newCategory);

        assertThat(post.getCategoryId()).isEqualTo(newCategory);
    }

    @Test
    @DisplayName("updateTitle() 호출시 제목이 변경된다.")
    void updateTitle() {
        Post post = createPost();

        post.updateTitle("변경된 제목");

        assertThat(post.getTitle()).isEqualTo("변경된 제목");
    }

    @Test
    @DisplayName("updateContent() 호출시 내용이 변경된다.")
    void updateContent() {
        Post post = createPost();

        post.updateContent("변경된 내용");

        assertThat(post.getContent()).isEqualTo("변경된 내용");
    }
}
