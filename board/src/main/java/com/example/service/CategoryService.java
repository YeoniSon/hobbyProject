package com.example.service;

import com.example.common.exception.BusinessException;
import com.example.common.exception.ErrorCode;
import com.example.domain.Category;
import com.example.dto.request.CategoryRegisterRequest;
import com.example.dto.response.CategoryResponse;
import com.example.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    /*
    카테고리 관련 내용
    - 카테고리 등록
    - 카테고리 조회 (전체, 숨겨진 것들, 보여지는 것들)
    - 카테고리 삭제
    - 카테고리 수정
     */

    // 카테고리 등록
    @Transactional
    public void register(CategoryRegisterRequest request) {
        if (categoryRepository.existsByName(request.getName())) {
            throw new BusinessException(ErrorCode.DUPLICATE_CATEGORY);
        }

        Category category = createCategory(request);

        categoryRepository.save(category);
    }

    private Category createCategory(CategoryRegisterRequest request) {
        return Category.builder()
                .name(request.getName())
                .build();
    }

    // 카테고리 조회 (전체)
    @Transactional
    public List<CategoryResponse> getAllCategory() {
        return categoryRepository.findAll()
                .stream()
                .map(CategoryResponse::from)
                .toList();
    }

    // 카테고리 조회(show true)
    @Transactional
    public List<CategoryResponse> getShowTrueCategory() {
        return categoryRepository.findAllByShowTrue()
                .stream()
                .map(CategoryResponse::from)
                .toList();
    }

    // 카테고리 조회(show false)
    @Transactional
    public List<CategoryResponse> getShowFalseCategory() {
        return categoryRepository.findAllByShowFalse()
                .stream()
                .map(CategoryResponse::from)
                .toList();
    }



}
