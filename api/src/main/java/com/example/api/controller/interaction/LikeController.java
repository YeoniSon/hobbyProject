package com.example.api.controller.interaction;

import com.example.api.security.CustomUserDetails;
import com.example.common.enums.TargetType;
import com.example.interaction.dto.response.CountResponse;
import com.example.interaction.dto.response.LikeDataResponse;
import com.example.interaction.service.LikeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/like")
@Tag(name = "좋아요", description = "게시글·댓글 좋아요/취소 및 개수·목록 조회. 쿼리 targetType으로 대상 구분")
@SecurityRequirement(name = "JWTAuth")
@RequiredArgsConstructor
public class LikeController {

    private final LikeService likeService;

    /*
    게시글 좋아요, 취소
    댓글 좋아요, 취소
    게시글, 댓글별 개수
     */
    @Operation(summary = "게시글 좋아요", description = "targetType 쿼리로 대상 타입을 지정합니다 (예: POST).")
    @PostMapping("/post/{postId}")
    public ResponseEntity<String> likePost(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long postId,
            @RequestParam TargetType targetType
    ) {
        likeService.setLike(userDetails.getId(), postId, targetType);
        return ResponseEntity.ok("좋아요 success");
    }

    @Operation(summary = "게시글 좋아요 취소", description = "게시글에 누른 좋아요를 취소합니다.")
    @PutMapping("/post/{postId}/un-like")
    public ResponseEntity<String> unLikePost(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long postId,
            @RequestParam TargetType targetType
    ) {
        likeService.setUnLike(userDetails.getId(), postId, targetType);
        return ResponseEntity.ok("좋아요 취소 success");
    }

    @Operation(summary = "댓글 좋아요", description = "댓글에 좋아요를 등록합니다.")
    @PostMapping("/comment/{commentId}")
    public ResponseEntity<String> likeComment(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long commentId,
            @RequestParam TargetType targetType
    ){
        likeService.setLike(userDetails.getId(), commentId, targetType);
        return ResponseEntity.ok("comment like success");
    }

    @Operation(summary = "댓글 좋아요 취소", description = "댓글 좋아요를 취소합니다.")
    @PutMapping("/comment/{commentId}/un-like")
    public ResponseEntity<String> unLikeComment(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long commentId,
            @RequestParam TargetType targetType
    ) {
        likeService.setUnLike(userDetails.getId(), commentId, targetType);
        return ResponseEntity.ok("comment unlike success");
    }

    @Operation(summary = "좋아요 수 조회", description = "특정 대상(postId 등)의 좋아요 개수를 조회합니다.")
    @PostMapping("/post/{postId}/count")
    public ResponseEntity<CountResponse> countPostLike(
            @PathVariable Long postId,
            @RequestParam TargetType targetType
    ) {
        return ResponseEntity.ok(likeService.countLike(postId, targetType));
    }

    @Operation(summary = "내가 좋아요한 게시글", description = "로그인 사용자가 좋아요한 게시글 목록입니다.")
    @GetMapping("/posts")
    public ResponseEntity<List<LikeDataResponse>> getPostLikes(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam TargetType targetType
    ) {
        return ResponseEntity.ok(likeService.allLikeView(userDetails.getId(), targetType));
    }

    @Operation(summary = "내가 좋아요한 댓글", description = "로그인 사용자가 좋아요한 댓글 목록입니다.")
    @GetMapping("/comments")
    public ResponseEntity<List<LikeDataResponse>> getCommentLikes(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam TargetType targetType
    ) {
        return ResponseEntity.ok(likeService.allLikeView(userDetails.getId(), targetType));
    }
}
