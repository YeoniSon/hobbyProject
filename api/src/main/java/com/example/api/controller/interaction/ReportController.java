package com.example.api.controller.interaction;

import com.example.api.security.CustomUserDetails;
import com.example.common.enums.TargetType;
import com.example.interaction.dto.request.ReportRequest;
import com.example.interaction.dto.response.CountResponse;
import com.example.interaction.dto.response.ReportResponse;
import com.example.interaction.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/report")
@Tag(name = "신고", description = "게시글·댓글 신고, 관리자 조회, 신고 통계")
@SecurityRequirement(name = "JWTAuth")
@RequiredArgsConstructor
public class ReportController {
    private final ReportService reportService;

    @Operation(summary = "게시글 신고", description = "게시글을 신고합니다.")
    @PostMapping("/post/{postId}")
    public ResponseEntity<String> reportPost(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long postId,
            @RequestBody ReportRequest request
    ) {
        reportService.registerReport(userDetails.getId(), postId, request);
        return ResponseEntity.ok("post report success");
    }

    @Operation(summary = "댓글 신고", description = "댓글을 신고합니다.")
    @PostMapping("/comment/{commentId}")
    public ResponseEntity<String> reportComment(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long commentId,
            @RequestBody ReportRequest request
    ){
        reportService.registerReport(userDetails.getId(), commentId, request);
        return ResponseEntity.ok("comment report success");
    }

    @Operation(summary = "[관리자] 신고 전체", description = "모든 신고 내역을 조회합니다.")
    @GetMapping("/manage/all-reports")
    public ResponseEntity<List<ReportResponse>> getAllReports(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ResponseEntity.ok(reportService.getAllReports());
    }

    @Operation(summary = "[관리자] 게시글 신고만", description = "게시글 관련 신고만 조회합니다.")
    @GetMapping("/manage/all-reports/post")
    public ResponseEntity<List<ReportResponse>> getAllPostReports(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ResponseEntity.ok(reportService.getPostReports());
    }

    @Operation(summary = "[관리자] 댓글 신고만", description = "댓글 관련 신고만 조회합니다.")
    @GetMapping("/manage/all-reports/comment")
    public ResponseEntity<List<ReportResponse>> getAllCommentReports(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ResponseEntity.ok(reportService.getCommentReports());
    }

    @Operation(summary = "[관리자] 대상별 신고", description = "특정 대상(targetId)에 대한 신고 목록을 조회합니다.")
    @GetMapping("/manage/all-reports/{targetId}")
    public ResponseEntity<List<ReportResponse>> getAllReportsByTargetId(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long targetId
    ) {
        return ResponseEntity.ok(reportService.getTargetPostOrCommentReports(targetId));
    }

    @Operation(summary = "내 신고 목록", description = "로그인 사용자가 제출한 신고 목록입니다.")
    @GetMapping("/all-reports")
    public ResponseEntity<List<ReportResponse>> getAllUserReports(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ResponseEntity.ok(reportService.getAllReportByUserId(userDetails.getId()));
    }

    @Operation(summary = "신고 상세", description = "reportId로 신고 단건 상세를 조회합니다.")
    @GetMapping("/details/{reportId}")
    public ResponseEntity<ReportResponse> getReportById(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long reportId
    ){
        return ResponseEntity.ok(reportService.getReportDetail(reportId));
    }


    @Operation(summary = "신고 취소", description = "본인이 제출한 신고를 취소합니다.")
    @PutMapping("/delete/{reportId}")
    public ResponseEntity<String> deleteReport(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long reportId
    ) {
        reportService.deleteReport(reportId);
        return ResponseEntity.ok("delete report success");
    }

    @Operation(summary = "[관리자] 전체 신고 수", description = "시스템 전체 신고 건수입니다.")
    @GetMapping("/manage/count/all")
    public ResponseEntity<CountResponse> getCountAllReports(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ResponseEntity.ok(reportService.countAllReports());
    }

    @Operation(summary = "[관리자] 게시글 신고 수", description = "게시글 타입 신고 건수입니다.")
    @GetMapping("/manage/count/post")
    public ResponseEntity<CountResponse> getCountPostReports(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ResponseEntity.ok(reportService.countTargetTypeReports(TargetType.POST));
    }

    @Operation(summary = "[관리자] 댓글 신고 수", description = "댓글 타입 신고 건수입니다.")
    @GetMapping("/manage/count/comment")
    public ResponseEntity<CountResponse> getCountCommentReports(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ){
        return ResponseEntity.ok(reportService.countTargetTypeReports(TargetType.COMMENT));
    }

    @Operation(summary = "내 신고 건수", description = "로그인 사용자의 신고 총 개수입니다.")
    @GetMapping("/count/all")
    public ResponseEntity<CountResponse> getCountAllUserReports(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ){
        return ResponseEntity.ok(reportService.countUserReports(userDetails.getId()));
    }

    @Operation(summary = "내 게시글 신고 수", description = "사용자가 신고한 게시글 건수입니다.")
    @GetMapping("/count/post")
    public ResponseEntity<CountResponse> getCountPostUserReports(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ResponseEntity.ok(reportService.countUserTargetTypeReports(userDetails.getId(), TargetType.POST));
    }

    @Operation(summary = "내 댓글 신고 수", description = "사용자가 신고한 댓글 건수입니다.")
    @GetMapping("/count/comment")
    public ResponseEntity<CountResponse> getCountCommentUserReports(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ResponseEntity.ok(reportService.countUserTargetTypeReports(userDetails.getId(), TargetType.COMMENT));
    }
}
