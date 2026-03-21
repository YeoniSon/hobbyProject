package com.example.api.controller.board;

import com.example.api.security.CustomUserDetails;
import com.example.common.enums.TargetType;
import com.example.common.exception.BusinessException;
import com.example.common.exception.ErrorCode;
import com.example.board.dto.request.post.PostEditRequest;
import com.example.board.dto.request.post.PostUploadRequest;
import com.example.board.dto.response.PostDataResponse;
import com.example.interaction.service.ReportService;
import com.example.board.service.PostService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/post")
@SecurityRequirement(name = "JWTAuth")
@RequiredArgsConstructor
public class PostController {

    private static final int MIN_REPORTS_FOR_PRIVATE = 20;

    private final PostService postService;
    private final ReportService reportService;

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
    @GetMapping("/manage/view/{userId}")
    public ResponseEntity<List<PostDataResponse>> getPostsByUserId(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long userId
    ) {
        return ResponseEntity.ok(postService.getPostsByUserId(userId));
    }

    // 관리자: 카테고리별 게시글 조회
    @GetMapping("/manage/view/{categoryId}")
    public ResponseEntity<List<PostDataResponse>> getPostsByCategoryId(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long categoryId
    ) {
        return ResponseEntity.ok(postService.getAllByCategoryIdPost(categoryId));
    }

    // 관리자: 게시글 조회 (공개)
    @GetMapping("/manage/view/show")
    public ResponseEntity<List<PostDataResponse>> getPostsByShow(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ResponseEntity.ok(postService.getAllByShowPost());
    }

    // 관리자: 게시글 조회 (비공개)
    @GetMapping("/manage/view/private")
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

    // 게시글 수정
    @PatchMapping("/{postId}/edit")
    public ResponseEntity<PostDataResponse> editPost(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long postId,
            @RequestBody PostEditRequest request
    ){
        return ResponseEntity.ok(postService.editPost(postId, request));
    }

    // 회원: 게시글 삭제
    @PutMapping("/{postId}/delete")
    public ResponseEntity<String> deletePost(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long postId
    ) {
        postService.deletePost(postId);
        return ResponseEntity.ok(postId + "삭제 완료했습니다.");
    }

    // 관리자: 게시글 삭제(비공개) - 신고 20건 이상일 때만 처리
    @PatchMapping("/manage/{postId}/delete")
    public ResponseEntity<String> privatePost(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long postId
    ) {
        int reportCount = reportService.countForTarget(TargetType.POST, postId);
        if (reportCount < MIN_REPORTS_FOR_PRIVATE) {
            throw new BusinessException(ErrorCode.NOT_ENOUGH_REPORTS_FOR_PRIVATE);
        }
        postService.privatePost(postId);
        return ResponseEntity.ok("게시글 삭제(비공개) success");
    }

    // 관리자: 게시글 복구(공개)
    @PatchMapping("/manage/{postId}/release")
    public ResponseEntity<String> releasePost(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long postId
    ){
        postService.releasePost(postId);
        return ResponseEntity.ok("게시글 복구(공개) success");
    }
}
