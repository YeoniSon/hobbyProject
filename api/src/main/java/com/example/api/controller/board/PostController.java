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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/post")
@Tag(name = "게시글", description = "게시글 CRUD·내 글 조회·관리자 조회·비공개 처리")
@SecurityRequirement(name = "JWTAuth")
@RequiredArgsConstructor
public class PostController {

    private static final int MIN_REPORTS_FOR_PRIVATE = 20;

    private final PostService postService;
    private final ReportService reportService;

    @Operation(summary = "게시글 등록", description = "새 게시글을 등록합니다.")
    @PostMapping("/upload")
    public ResponseEntity<String> uploadPost(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody PostUploadRequest request
    ) {
        postService.uploadPost(request);
        return ResponseEntity.ok("success");
    }

    @Operation(summary = "내 게시글 목록", description = "로그인한 사용자가 작성한 게시글만 조회합니다.")
    @GetMapping("/my-posts")
    public ResponseEntity<List<PostDataResponse>> getMyPosts(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ResponseEntity.ok(postService.getPostsByUserId(userDetails.getId()));
    }

    @Operation(summary = "[관리자] 회원별 게시글", description = "특정 userId의 게시글 목록을 조회합니다.")
    @GetMapping("/manage/view/{userId}")
    public ResponseEntity<List<PostDataResponse>> getPostsByUserId(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long userId
    ) {
        return ResponseEntity.ok(postService.getPostsByUserId(userId));
    }

    @Operation(summary = "[관리자] 카테고리별 게시글", description = "특정 categoryId의 게시글 목록을 조회합니다.")
    @GetMapping("/manage/view/{categoryId}")
    public ResponseEntity<List<PostDataResponse>> getPostsByCategoryId(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long categoryId
    ) {
        return ResponseEntity.ok(postService.getAllByCategoryIdPost(categoryId));
    }

    @Operation(summary = "[관리자] 공개 게시글 전체", description = "공개(show) 상태인 게시글을 조회합니다.")
    @GetMapping("/manage/view/show")
    public ResponseEntity<List<PostDataResponse>> getPostsByShow(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ResponseEntity.ok(postService.getAllByShowPost());
    }

    @Operation(summary = "[관리자] 비공개 게시글 전체", description = "비공개 처리된 게시글을 조회합니다.")
    @GetMapping("/manage/view/private")
    public ResponseEntity<List<PostDataResponse>> getPostsByPrivate(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ResponseEntity.ok(postService.getAllByShowFalsePost());
    }

    @Operation(summary = "게시글 상세", description = "postId로 게시글 상세 정보를 조회합니다.")
    @GetMapping("/{postId}/detail")
    public ResponseEntity<PostDataResponse> getPostDetail(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long postId
    ) {
        return ResponseEntity.ok(postService.getPostById(postId));
    }

    @Operation(summary = "게시글 수정", description = "게시글 내용을 수정합니다.")
    @PatchMapping("/{postId}/edit")
    public ResponseEntity<PostDataResponse> editPost(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long postId,
            @RequestBody PostEditRequest request
    ){
        return ResponseEntity.ok(postService.editPost(postId, request));
    }

    @Operation(summary = "게시글 삭제", description = "본인 게시글을 삭제합니다.")
    @PutMapping("/{postId}/delete")
    public ResponseEntity<String> deletePost(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long postId
    ) {
        postService.deletePost(postId);
        return ResponseEntity.ok(postId + "삭제 완료했습니다.");
    }

    @Operation(summary = "[관리자] 게시글 비공개 처리", description = "신고가 20건 이상인 게시글만 비공개 처리합니다.")
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

    @Operation(summary = "[관리자] 게시글 공개 복구", description = "비공개 게시글을 다시 공개합니다.")
    @PatchMapping("/manage/{postId}/release")
    public ResponseEntity<String> releasePost(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long postId
    ){
        postService.releasePost(postId);
        return ResponseEntity.ok("게시글 복구(공개) success");
    }
}
