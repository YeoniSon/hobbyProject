package com.example.api.controller.board;

import com.example.api.security.CustomUserDetails;
import com.example.dto.request.comment.CommentUploadRequest;
import com.example.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/comment")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    // 댓글 등록
    @PostMapping("/upload")
    public ResponseEntity<String> uploadComment(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody CommentUploadRequest request
    ) {
        return ResponseEntity.ok("success");
    }


}
