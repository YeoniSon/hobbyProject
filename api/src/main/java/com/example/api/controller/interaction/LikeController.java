package com.example.api.controller.interaction;

import com.example.api.security.CustomUserDetails;
import com.example.common.enums.TargetType;
import com.example.interaction.service.LikeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/like")
@RequiredArgsConstructor
public class LikeController {

    private final LikeService likeService;

    /*
    게시글 좋아요, 취소
    댓글 좋아요, 취소
    게시글, 댓글별 개수
     */
    // 게시글 좋아요
    @PostMapping("/post/{postId}")
    public ResponseEntity<String> likePost(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long postId,
            @RequestParam TargetType targetType
    ) {
        likeService.postLike(userDetails.getId(), postId, targetType);
        return ResponseEntity.ok("좋아요 success");
    }

    // 게시글 좋아요 취소
    @PutMapping("/post/{postId}/un-like")
    public ResponseEntity<String> unLikePost(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long postId,
            @RequestParam TargetType targetType
    ) {
        likeService.postUnLike(userDetails.getId(), postId, targetType);
        return ResponseEntity.ok("좋아요 취소 success");
    }

    // 댓글 좋아요
    @PostMapping("/comment/{commentId}")
    public ResponseEntity<String> likeComment(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long commentId,
            @RequestParam TargetType targetType
    ){
        likeService.postLike(userDetails.getId(), commentId, targetType);
        return ResponseEntity.ok("comment like success");
    }

    // 댓글 좋아요 취소
    @PutMapping("/comment/{commentId}/un-like")
    public ResponseEntity<String> unLikeComment(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long commentId,
            @RequestParam TargetType targetType
    ) {
        likeService.postUnLike(userDetails.getId(), commentId, targetType);
        return ResponseEntity.ok("comment unlike success");
    }

}
