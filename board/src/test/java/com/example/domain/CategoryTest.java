package com.example.domain;

import com.example.board.domain.Category;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Category 엔티티")
class CategoryTest {

    private static Category createCategory() {
        return Category.builder()
                .name("baseball")
                .build();
    }

    @Test
    @DisplayName("deleteShow() 호출시 show가 true에서 false로 변경된다.")
    void deleteShow() {
        Category category = createCategory();
        assertThat(category.isShow()).isTrue();

        category.deleteShow();
        assertThat(category.isShow()).isFalse();
    }

    @Test
    @DisplayName("depositShow() 호출시 show가 false에서 true로 변경된다.")
    void depositShow() {
        Category category = createCategory();
        category.deleteShow();
        assertThat(category.isShow()).isFalse();

        category.depositShow();
        assertThat(category.isShow()).isTrue();
    }
}