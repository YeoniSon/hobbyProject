package com.example.service;

import com.example.board.service.CategoryService;
import com.example.common.exception.BusinessException;
import com.example.common.exception.ErrorCode;
import com.example.board.domain.Category;
import com.example.board.dto.request.CategoryRegisterRequest;
import com.example.board.repository.CategoryRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CategoryService")
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CategoryService categoryService;

    private static Category createCategory(String name, boolean show) {
        Category category = Category.builder()
                .name(name)
                .build();
        if (!show) {
            category.deleteShow();
        }
        return category;
    }

    @Test
    @DisplayName("Register - 카테고리명이 중복이면 DUPLICATE_CATEGORY 예외")
    void register() {
        CategoryRegisterRequest request = new CategoryRegisterRequest(
                "baseball"
        );

        when(categoryRepository.existsByName(request.getName())).thenReturn(true);

        assertThatThrownBy(() -> categoryService.register(request))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex)
                        .getErrorCode())
                        .isEqualTo(ErrorCode.DUPLICATE_CATEGORY));

        verify(categoryRepository, never()).save(any());
    }

    @Test
    @DisplayName("getAllCategory - 전체 카테고리 목록 반환")
    void getAllCategory() {
        Category category1 = createCategory("baseball", true);
        Category category2 = createCategory("soccer", false);

        when(categoryRepository.findAll()).thenReturn(List.of(category1, category2));

        var result = categoryService.getAllCategory();

        assertThat(result).hasSize(2);
        assertThat(result)
                .extracting("name")
                .containsExactly("baseball", "soccer");
    }

    @Test
    @DisplayName("register - 카테고리 등록 성공")
    void registerSuccess() {
        CategoryRegisterRequest request = new CategoryRegisterRequest("baseball");

        when(categoryRepository.existsByName(request.getName())).thenReturn(false);

        categoryService.register(request);

        verify(categoryRepository).save(argThat(category ->
                "baseball".equals(category.getName()) && category.isShow()));
    }

    @Test
    @DisplayName("getShowTrueCategory - show=true 인 카테고리만 반환")
    void getShowTrueCategory() {
        Category category1 = createCategory("baseball", true);
        Category category2 = createCategory("soccer", true);

        when(categoryRepository.findAllByShowTrue()).thenReturn(List.of(category1, category2));

        var result = categoryService.getShowTrueCategory();

        assertThat(result).hasSize(2);
        assertThat(result)
                .extracting("show")
                .containsOnly(true);
    }

    @Test
    @DisplayName("getShowFalseCategory - show=false 인 카테고리만 반환")
    void getShowFalseCategory() {
        Category category1 = createCategory("baseball", false);
        Category category2 = createCategory("soccer", false);

        when(categoryRepository.findAllByShowFalse()).thenReturn(List.of(category1, category2));

        var result = categoryService.getShowFalseCategory();

        assertThat(result).hasSize(2);
        assertThat(result)
                .extracting("show")
                .containsOnly(false);
    }

    

    @Test
    @DisplayName("privateCategory - 카테고리 비공개 처리 성공 (show true -> false)")
    void privateCategorySuccess() {
        Category category = createCategory("baseball", true);
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));

        categoryService.privateCategory(1L);

        assertThat(category.isShow()).isFalse();
        verify(categoryRepository).findById(1L);
    }

    @Test
    @DisplayName("privateCategory - 존재하지 않는 카테고리면 NOT_EXIST_CATEGORY 예외")
    void privateCategoryNotFound() {
        when(categoryRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> categoryService.privateCategory(999L))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode())
                        .isEqualTo(ErrorCode.NOT_EXIST_CATEGORY));
    }

    @Test
    @DisplayName("privateCategory - 이미 비공개 상태면 ALREADY_DELETE_CATEGORY 예외")
    void privateCategoryAlreadyDeleted() {
        Category category = createCategory("baseball", false);
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));

        assertThatThrownBy(() -> categoryService.privateCategory(1L))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode())
                        .isEqualTo(ErrorCode.ALREADY_DELETE_CATEGORY));
    }

    @Test
    @DisplayName("releaseCategory - 카테고리 복구 성공 (show false -> true)")
    void releaseCategorySuccess() {
        Category category = createCategory("baseball", false);
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));

        categoryService.releaseCategory(1L);

        assertThat(category.isShow()).isTrue();
        verify(categoryRepository).findById(1L);
    }

    @Test
    @DisplayName("releaseCategory - 존재하지 않는 카테고리면 NOT_EXIST_CATEGORY 예외")
    void releaseCategoryNotFound() {
        when(categoryRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> categoryService.releaseCategory(999L))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode())
                        .isEqualTo(ErrorCode.NOT_EXIST_CATEGORY));
    }

    @Test
    @DisplayName("releaseCategory - 이미 공개 상태면 ALREADY_SHOW_CATEGORY 예외")
    void releaseCategoryAlreadyShow() {
        Category category = createCategory("baseball", true);
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));

        assertThatThrownBy(() -> categoryService.releaseCategory(1L))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode())
                        .isEqualTo(ErrorCode.ALREADY_SHOW_CATEGORY));
    }
}