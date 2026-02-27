package com.example.api.controller.board;

import com.example.api.security.CustomUserDetails;
import com.example.common.enums.TargetType;
import com.example.common.exception.BusinessException;
import com.example.common.exception.ErrorCode;
import com.example.dto.request.comment.CommentEditRequest;
import com.example.dto.request.comment.CommentUploadRequest;
import com.example.dto.response.CommentResponse;
import com.example.interaction.service.ReportService;
import com.example.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/comment")
@RequiredArgsConstructor
public class CommentController {

    private static final int MIN_REPORTS_FOR_PRIVATE = 20;

    private final CommentService commentService;
    private final ReportService reportService;

    // 댓글 등록
    @PostMapping("/upload")
    public ResponseEntity<String> uploadComment(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody CommentUploadRequest request
    ) {
        return ResponseEntity.ok("success");
    }


    // 댓글 회원 전체 조회(회원)
    @GetMapping("/{userId}/all-comments")
    public ResponseEntity<List<CommentResponse>> getAllComments(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long userId
    ) {
        return ResponseEntity.ok(commentService.getAllCommentByWriterId(userId));
    }

    // 댓글 회원 아이디, 게시글 조회(회원)
    @GetMapping("/{postId}/all-comments")
    public ResponseEntity<List<CommentResponse>> getAllCommentsByPostIdAndWriterId(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long postId
    ){
        return ResponseEntity.ok(commentService.getAllCommentByWriterIdAndPostId(userDetails.getId(), postId));
    }

    // 댓글 전체 조회(관리자)
    @GetMapping("/manage/all-comments")
    public ResponseEntity<List<CommentResponse>> getComments(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ResponseEntity.ok(commentService.getAllComment());
    }

    // 댓글 게시글별 조회(관리자)
    @GetMapping("/manage/{postId}/all-comments")
    public ResponseEntity<List<CommentResponse>> getCommentsByPostId(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long postId
    ){
        return ResponseEntity.ok(commentService.getAllCommentByPostId(postId));
    }

    // 댓글 비공개 조회(관리자)
    @GetMapping("/manage/private-comments")
    public ResponseEntity<List<CommentResponse>> getPrivateComments(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ){
        return ResponseEntity.ok(commentService.getPrivateComment());
    }

    // 댓글 공개 조회(관리자)
    @GetMapping("/manage/show-comments")
    public ResponseEntity<List<CommentResponse>> getShowComments(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ){
        return ResponseEntity.ok(commentService.getShowComment());
    }

    // 댓글 수정
    @PatchMapping("/{commentId}/edit-comment")
    public ResponseEntity<CommentResponse> editComment(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long commentId,
            @RequestBody CommentEditRequest request
    ){
        return ResponseEntity.ok(commentService.editComment(commentId, request));
    }

    // 댓글 삭제
    @PutMapping("/{commentId}/delete")
    public ResponseEntity<String> deleteComment(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long commentId
    ){
        commentService.deleteComment(userDetails.getId(), commentId);
        return ResponseEntity.ok("success");
    }

    // 댓글 비공개처리 - 신고 20건 이상일 때만 처리
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

    // 댓글 공개처리
    @PatchMapping("/manage/{commentId}/show-comment")
    public ResponseEntity<String> editShowComment(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long commentId
    ){
        commentService.showComment(commentId);
        return ResponseEntity.ok("success");
    }
}

