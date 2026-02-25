package com.example.api.controller.board;

import com.example.api.security.CustomUserDetails;
import com.example.dto.request.CategoryRegisterRequest;
import com.example.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
