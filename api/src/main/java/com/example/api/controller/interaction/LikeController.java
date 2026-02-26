package com.example.api.controller.interaction;

import com.example.api.security.CustomUserDetails;
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
    @PostMapping("/{postId}")
    public ResponseEntity<String> likePost(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long postId
    ) {
        likeService.postLike(userDetails.getId(), postId);
        return ResponseEntity.ok("좋아요 success");
    }

    // 게시글 좋아요 취소
    @PutMapping("/unLike/{postId}")
    public ResponseEntity<String> unLikePost(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long postId
    ) {
        likeService.postUnLike(userDetails.getId(), postId);
        return ResponseEntity.ok("좋아요 취소 success");
    }
}
