package com.example.board.dto.request.post;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PostUploadRequest {
    private Long categoryId;
    private Long userId;
    private String title;
    private String content;
}
