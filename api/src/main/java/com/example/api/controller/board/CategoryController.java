package com.example.api.controller.board;

import com.example.api.security.CustomUserDetails;
import com.example.board.dto.request.CategoryRegisterRequest;
import com.example.board.dto.response.CategoryResponse;
import com.example.board.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/category")
@Tag(name = "카테고리", description = "게시판 카테고리 등록·조회·공개/비공개 (관리)")
@SecurityRequirement(name = "JWTAuth")
@RequiredArgsConstructor
public class CategoryController {
    
    private final CategoryService categoryService;

    @Operation(summary = "카테고리 등록", description = "새 카테고리를 등록합니다.")
    @PostMapping("/register")
    public ResponseEntity<String> registerCategory(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody CategoryRegisterRequest request
    ){
        categoryService.register(request);
        return ResponseEntity.ok().body("카테고리 등록 완료");
    }

    @Operation(summary = "[관리자] 카테고리 전체", description = "모든 카테고리를 조회합니다.")
    @GetMapping("/manage/all-categories")
    public ResponseEntity<List<CategoryResponse>> getAllCategories(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ResponseEntity.ok(categoryService.getAllCategory());
    }

    @Operation(summary = "[관리자] 공개 카테고리", description = "노출(show) 중인 카테고리만 조회합니다.")
    @GetMapping("/manage/show-categories")
    public ResponseEntity<List<CategoryResponse>> getShowCategories(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ){
        return ResponseEntity.ok(categoryService.getShowTrueCategory());
    }

    @Operation(summary = "[관리자] 비공개 카테고리", description = "비노출 카테고리만 조회합니다.")
    @GetMapping("/manage/not-show-categories")
    public ResponseEntity<List<CategoryResponse>> getNotShowCategories(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ){
        return ResponseEntity.ok(categoryService.getShowFalseCategory());
    }

    @Operation(summary = "[관리자] 카테고리 비공개", description = "카테고리를 비공개 처리합니다.")
    @PatchMapping("/manage/private")
    public ResponseEntity<String> categoryPrivate(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long categoryId
    ){
        categoryService.privateCategory(categoryId);
        return ResponseEntity.ok().body("카테고리 비공개 처리완료");
    }

    @Operation(summary = "[관리자] 카테고리 공개", description = "카테고리를 다시 공개합니다.")
    @PatchMapping("/manage/release")
    public ResponseEntity<String> categoryRelease(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long categoryId
    ) {
        categoryService.releaseCategory(categoryId);
        return ResponseEntity.ok().body("카테고리 공개 처리완료");
    }


}
