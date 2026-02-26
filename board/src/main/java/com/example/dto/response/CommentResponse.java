package com.example.dto.response;

import com.example.domain.Comment;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class CommentResponse {
    private Long commentId;
    private Long postId;
    private Long writerId;
    private String content;
    private boolean show;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static CommentResponse from(Comment comment) {
        return CommentResponse.builder()
                .commentId(comment.getId())
                .postId(comment.getPost().getId())
                .writerId(comment.getUser().getId())
                .content(comment.getContent())
                .show(comment.isShow())
                .createdAt(comment.getCreateTime())
                .updatedAt(comment.getUpdateTime())
                .build();
    }
}
