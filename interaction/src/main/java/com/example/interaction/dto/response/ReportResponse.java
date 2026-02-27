package com.example.interaction.dto.response;

import com.example.common.enums.TargetType;
import com.example.interaction.domain.Report;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ReportResponse {
    private Long reportId;
    private Long targetId;
    private TargetType targetType;
    private Long reporterId;
    private String reason;
    private LocalDateTime reportTime;
    /** POST면 게시글 제목, COMMENT면 댓글 내용 */
    private String targetSummary;


    /**
     * Report 엔티티와 대상 요약(게시글 제목 또는 댓글 내용)으로 응답 생성.
     */
    public static ReportResponse of(Report report, String targetSummary) {
        return ReportResponse.builder()
                .reportId(report.getId())
                .targetId(report.getTargetId())
                .targetType(report.getTargetType())
                .reporterId(report.getUser().getId())
                .reason(report.getReason())
                .reportTime(report.getCreateTime())
                .targetSummary(targetSummary)
                .build();
    }
}
