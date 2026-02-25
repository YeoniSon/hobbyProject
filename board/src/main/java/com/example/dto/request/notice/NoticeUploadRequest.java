package com.example.dto.request.notice;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class NoticeUploadRequest {
    private Long writerId;
    private String title;
    private String content;
}
