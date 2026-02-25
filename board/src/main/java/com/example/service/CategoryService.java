package com.example.service;

import com.example.common.exception.BusinessException;
import com.example.common.exception.ErrorCode;
import com.example.domain.Category;
import com.example.dto.request.CategoryRegisterRequest;
import com.example.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public void register(CategoryRegisterRequest request) {
        if (categoryRepository.findByName(request.getName()).isPresent()) {
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


}
