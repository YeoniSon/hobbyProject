package com.example.api.controller.board;

import com.example.api.security.CustomUserDetails;
import com.example.common.enums.TargetType;
import com.example.common.exception.BusinessException;
import com.example.common.exception.ErrorCode;
import com.example.board.dto.request.comment.CommentEditRequest;
import com.example.board.dto.request.comment.CommentUploadRequest;
import com.example.board.dto.response.CommentResponse;
import com.example.interaction.service.ReportService;
import com.example.board.service.CommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/comment")
@Tag(name = "댓글", description = "댓글 CRUD·관리자 조회·신고 기반 비공개")
@SecurityRequirement(name = "JWTAuth")
@RequiredArgsConstructor
public class CommentController {

    private static final int MIN_REPORTS_FOR_PRIVATE = 20;

    private final CommentService commentService;
    private final ReportService reportService;

    @Operation(summary = "댓글 등록", description = "게시글에 댓글을 등록합니다.")
    @PostMapping("/upload")
    public ResponseEntity<String> uploadComment(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody CommentUploadRequest request
    ) {
        return ResponseEntity.ok("success");
    }


    @Operation(summary = "회원별 댓글 목록", description = "특정 userId가 작성한 댓글을 조회합니다.")
    @GetMapping("/{userId}/all-comments")
    public ResponseEntity<List<CommentResponse>> getAllComments(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long userId
    ) {
        return ResponseEntity.ok(commentService.getAllCommentByWriterId(userId));
    }

    @Operation(summary = "게시글별 내 댓글", description = "로그인 사용자가 해당 postId에 쓴 댓글을 조회합니다.")
    @GetMapping("/{postId}/all-comments")
    public ResponseEntity<List<CommentResponse>> getAllCommentsByPostIdAndWriterId(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long postId
    ){
        return ResponseEntity.ok(commentService.getAllCommentByWriterIdAndPostId(userDetails.getId(), postId));
    }

    @Operation(summary = "[관리자] 댓글 전체", description = "모든 댓글을 조회합니다.")
    @GetMapping("/manage/all-comments")
    public ResponseEntity<List<CommentResponse>> getComments(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ResponseEntity.ok(commentService.getAllComment());
    }

    @Operation(summary = "[관리자] 게시글별 댓글", description = "특정 postId의 댓글을 조회합니다.")
    @GetMapping("/manage/{postId}/all-comments")
    public ResponseEntity<List<CommentResponse>> getCommentsByPostId(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long postId
    ){
        return ResponseEntity.ok(commentService.getAllCommentByPostId(postId));
    }

    @Operation(summary = "[관리자] 비공개 댓글", description = "비공개 처리된 댓글만 조회합니다.")
    @GetMapping("/manage/private-comments")
    public ResponseEntity<List<CommentResponse>> getPrivateComments(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ){
        return ResponseEntity.ok(commentService.getPrivateComment());
    }

    @Operation(summary = "[관리자] 공개 댓글", description = "공개 상태 댓글만 조회합니다.")
    @GetMapping("/manage/show-comments")
    public ResponseEntity<List<CommentResponse>> getShowComments(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ){
        return ResponseEntity.ok(commentService.getShowComment());
    }

    @Operation(summary = "댓글 수정", description = "댓글 내용을 수정합니다.")
    @PatchMapping("/{commentId}/edit-comment")
    public ResponseEntity<CommentResponse> editComment(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long commentId,
            @RequestBody CommentEditRequest request
    ){
        return ResponseEntity.ok(commentService.editComment(commentId, request));
    }

    @Operation(summary = "댓글 삭제", description = "본인 댓글을 삭제합니다.")
    @PutMapping("/{commentId}/delete")
    public ResponseEntity<String> deleteComment(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long commentId
    ){
        commentService.deleteComment(userDetails.getId(), commentId);
        return ResponseEntity.ok("success");
    }

    @Operation(summary = "[관리자] 댓글 비공개", description = "신고 20건 이상인 댓글만 비공개 처리합니다.")
    @PatchMapping("/manage/{commentId}/private-comment")
    public ResponseEntity<String> editPrivateComment(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long commentId
    ) {
        int reportCount = reportService.countForTarget(TargetType.COMMENT, commentId);
        if (reportCount < MIN_REPORTS_FOR_PRIVATE) {
            throw new BusinessException(ErrorCode.NOT_ENOUGH_REPORTS_FOR_PRIVATE);
        }
        commentService.privateComment(commentId);
        return ResponseEntity.ok("success");
    }

    @Operation(summary = "[관리자] 댓글 공개 복구", description = "비공개 댓글을 다시 공개합니다.")
    @PatchMapping("/manage/{commentId}/show-comment")
    public ResponseEntity<String> editShowComment(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long commentId
    ){
        commentService.showComment(commentId);
        return ResponseEntity.ok("success");
    }
}

