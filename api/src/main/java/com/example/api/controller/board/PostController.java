package com.example.api.controller.board;

import com.example.api.security.CustomUserDetails;
import com.example.dto.request.PostUploadRequest;
import com.example.dto.response.PostDataResponse;
import com.example.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/post")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    // 게시글 등록
    @PostMapping("/upload")
    public ResponseEntity<String> uploadPost(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody PostUploadRequest request
    ) {
        postService.uploadPost(request);
        return ResponseEntity.ok("success");
    }

    // 회원: 내 게시글만 조회 (로그인한 사용자 본인 글만)
    @GetMapping("/my-posts")
    public ResponseEntity<List<PostDataResponse>> getMyPosts(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ResponseEntity.ok(postService.getPostsByUserId(userDetails.getId()));
    }

    // 관리자: 회원별 게시글 조회 (userId 선택 가능). 관리자만 호출 가능
    @GetMapping("/view/{userId}")
    public ResponseEntity<List<PostDataResponse>> getPostsByUserId(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long userId
    ) {
        return ResponseEntity.ok(postService.getPostsByUserId(userId));
    }

    // 관리자: 카테고리별 게시글 조회
    @GetMapping("/view/{categoryId}")
    public ResponseEntity<List<PostDataResponse>> getPostsByCategoryId(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long categoryId
    ) {
        return ResponseEntity.ok(postService.getAllByCategoryIdPost(categoryId));
    }

    // 관리자: 게시글 조회 (공개)
    @GetMapping("/view/show")
    public ResponseEntity<List<PostDataResponse>> getPostsByShow(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ResponseEntity.ok(postService.getAllByShowPost());
    }

    // 관리자: 게시글 조회 (비공개)
    @GetMapping("/view/private")
    public ResponseEntity<List<PostDataResponse>> getPostsByPrivate(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ResponseEntity.ok(postService.getAllByShowFalsePost());
    }

    // 게시글 상세 정보 조회
    @GetMapping("/{postId}/detail")
    public ResponseEntity<PostDataResponse> getPostDetail(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long postId
    ) {
        return ResponseEntity.ok(postService.getPostById(postId));
    }


}
