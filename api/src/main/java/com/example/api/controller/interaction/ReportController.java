package com.example.api.controller.interaction;

import com.example.api.security.CustomUserDetails;
import com.example.common.enums.TargetType;
import com.example.interaction.dto.request.ReportRequest;
import com.example.interaction.dto.response.ReportResponse;
import com.example.interaction.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/report")
@RequiredArgsConstructor
public class ReportController {
    private final ReportService reportService;

    // post 신고
    @PostMapping("/post/{postId}")
    public ResponseEntity<String> reportPost(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long postId,
            @RequestBody ReportRequest request
    ) {
        reportService.registerReport(userDetails.getId(), postId, request);
        return ResponseEntity.ok("post report success");
    }

    // comment 신고
    @PostMapping("/comment/{commentId}")
    public ResponseEntity<String> reportComment(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long commentId,
            @RequestBody ReportRequest request
    ){
        reportService.registerReport(userDetails.getId(), commentId, request);
        return ResponseEntity.ok("comment report success");
    }

    //신고 내용 전체 조회 -> 관리자
    @GetMapping("/manage/all-reports")
    public ResponseEntity<List<ReportResponse>> getAllReports(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ResponseEntity.ok(reportService.getAllReports());
    }

    // 신고 내용 조회(게시글 별 신고 조회) -> 관리자
    @GetMapping("/manage/all-reports/post")
    public ResponseEntity<List<ReportResponse>> getAllPostReports(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ResponseEntity.ok(reportService.getPostReports());
    }

    // 신고 내용 조회(댓글 별 신고 조회) -> 관리자
    @GetMapping("/manage/all-reports/comment")
    public ResponseEntity<List<ReportResponse>> getAllCommentReports(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ResponseEntity.ok(reportService.getCommentReports());
    }

    // 게시글 / 댓글 하나에 대한 모든 신고 조회 -> 관리자
    @GetMapping("/manage/all-reports/{targetId}")
    public ResponseEntity<List<ReportResponse>> getAllReportsByTargetId(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long targetId
    ) {
        return ResponseEntity.ok(reportService.getTargetPostOrCommentReports(targetId));
    }

    // 신고 내용 조회( 신고한 모든 게시글, 댓글 전체 조회) -> 유저
    @GetMapping("/all-reports")
    public ResponseEntity<List<ReportResponse>> getAllUserReports(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ResponseEntity.ok(reportService.getAllReportByUserId(userDetails.getId()));
    }

    // 신고 내용 조회(신고 상세 내역)
    @GetMapping("/details/{reportId}")
    public ResponseEntity<ReportResponse> getReportById(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long reportId
    ){
        return ResponseEntity.ok(reportService.getReportDetail(reportId));
    }


    //신고 취소
    @PutMapping("/delete/{reportId}")
    public ResponseEntity<String> deleteReport(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long reportId
    ) {
        reportService.deleteReport(reportId);
        return ResponseEntity.ok("delete report success");
    }

}
