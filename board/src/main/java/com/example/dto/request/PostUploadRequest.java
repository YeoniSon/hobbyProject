package com.example.dto.request;

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
