package com.example.api.controller.board;

import com.example.api.security.CustomUserDetails;
import com.example.board.dto.request.CategoryRegisterRequest;
import com.example.board.dto.response.CategoryResponse;
import com.example.board.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/category")
@RequiredArgsConstructor
public class CategoryController {
    
    private final CategoryService categoryService;

    // 카테고리 등록
    @PostMapping("/register")
    public ResponseEntity<String> registerCategory(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody CategoryRegisterRequest request
    ){
        categoryService.register(request);
        return ResponseEntity.ok().body("카테고리 등록 완료");
    }

    // 카테고리 조회(전체)
    @GetMapping("/manage/all-categories")
    public ResponseEntity<List<CategoryResponse>> getAllCategories(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ResponseEntity.ok(categoryService.getAllCategory());
    }

    // 카테고리 조회(show true)
    @GetMapping("/manage/show-categories")
    public ResponseEntity<List<CategoryResponse>> getShowCategories(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ){
        return ResponseEntity.ok(categoryService.getShowTrueCategory());
    }

    // 카테고리 조회(show false)
    @GetMapping("/manage/not-show-categories")
    public ResponseEntity<List<CategoryResponse>> getNotShowCategories(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ){
        return ResponseEntity.ok(categoryService.getShowFalseCategory());
    }

    // 카테고리 비공개 처리
    @PatchMapping("/manage/private")
    public ResponseEntity<String> categoryPrivate(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long categoryId
    ){
        categoryService.privateCategory(categoryId);
        return ResponseEntity.ok().body("카테고리 비공개 처리완료");
    }

    // 카테고리 공개 처리
    @PatchMapping("/manage/release")
    public ResponseEntity<String> categoryRelease(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long categoryId
    ) {
        categoryService.releaseCategory(categoryId);
        return ResponseEntity.ok().body("카테고리 공개 처리완료");
    }


}
