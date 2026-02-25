package com.example.dto.response;

import com.example.domain.Notice;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class NoticeResponse {
    private Long noticeId;
    private Long writerId;
    private String title;
    private String content;
    private LocalDateTime createdAt;

    public static NoticeResponse from(Notice notice) {
        return NoticeResponse.builder()
                .noticeId(notice.getId())
                .writerId(notice.getUser().getId())
                .title(notice.getTitle())
                .content(notice.getContent())
                .createdAt(notice.getCreateTime())
                .build();
    }

}
