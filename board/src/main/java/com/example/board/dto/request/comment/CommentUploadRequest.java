package com.example.board.dto.request.comment;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CommentUploadRequest {
    private Long postId;
    private Long writerId;
    private String content;
}
