package com.example.interaction.service;

import com.example.common.enums.TargetType;
import com.example.common.exception.BusinessException;
import com.example.common.exception.ErrorCode;
import com.example.domain.Comment;
import com.example.domain.Post;
import com.example.interaction.domain.Report;
import com.example.interaction.dto.request.ReportRequest;
import com.example.interaction.dto.response.ReportResponse;
import com.example.interaction.repository.ReportRepository;
import com.example.repository.CommentRepository;
import com.example.repository.PostRepository;
import com.example.user.domain.User;
import com.example.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReportService {
    private final ReportRepository reportRepository;
    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;

    /*
    - 게시판 / 댓글 신고
    - 신고 내용 조회 (신고 내용 전체 조회) -> 관리자
    - 신고 내용 조회 (게시글/댓글 별 신고 조회) -> 관리자
    - 신고 내용 조회 (유저별 신고 조회) -> 유저
    - 신고 상세 내용 조회 -> 유저
    - 신고 취소 -> 유저
     */
    // post / comment 신고
    @Transactional
    public void registerReport(
            Long reporterId,
            Long targetId,
            ReportRequest request
    ) {
        User user = userRepository.findById(reporterId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        validateTargetExists(targetId, request.getTargetType());

        //신고는 한 유저당 한 글에 대해서 한번만 가능
        if (reportRepository.existsByUser_IdAndTargetId(user.getId(), targetId)) {
            throw new BusinessException(ErrorCode.ALREADY_EXIST_REPORT);
        }

        reportRepository.save(createReport(user, targetId, request));
    }

    private void validateTargetExists(Long targetId, TargetType targetType) {
        if (TargetType.POST.equals(targetType)) {
            postRepository.findById(targetId)
                    .orElseThrow(() -> new BusinessException(ErrorCode.NOT_EXIST_POST));
        } else {
            commentRepository.findById(targetId)
                    .orElseThrow(() -> new BusinessException(ErrorCode.NOT_EXIST_COMMENT));
        }
    }

    private Report createReport(User reporter, Long targetId, ReportRequest request) {
        return Report.builder()
                .user(reporter)
                .targetId(targetId)
                .targetType(request.getTargetType())
                .reason(request.getReason())
                .build();
    }

    // 신고 내용 전체 조회 -> 관리자
    // POST면 게시글 제목, COMMENT면 댓글 내용을 targetSummary에 담아 반환
    @Transactional(readOnly = true)
    public List<ReportResponse> getAllReports() {
        List<Report> reports = reportRepository.findAll();
        if (reports.isEmpty()) {
            throw new BusinessException(ErrorCode.NOT_EXIST_REPORT);
        }
        return reports.stream()
                .map(this::toReportResponse)
                .toList();
    }

    private ReportResponse toReportResponse(Report report) {
        String targetSummary = TargetType.POST.equals(report.getTargetType())
                ? postRepository.findById(report.getTargetId()).map(Post::getTitle).orElse("")
                : commentRepository.findById(report.getTargetId()).map(Comment::getContent).orElse("");
        return ReportResponse.of(report, targetSummary);
    }

    // 신고 내용 조회(게시글 신고 전체 조회) -> 관리자
    // targetSummary = 게시글 제목
    @Transactional(readOnly = true)
    public List<ReportResponse> getPostReports() {
        List<Report> reports = reportRepository.findAllByTargetType(TargetType.POST);
        if (reports.isEmpty()) {
            throw new BusinessException(ErrorCode.NOT_EXIST_REPORT);
        }
        return reports.stream()
                .map(report -> {
                    Post post = postRepository.findById(report.getTargetId())
                            .orElseThrow(() -> new BusinessException(ErrorCode.NOT_EXIST_POST));
                    return ReportResponse.of(report, post.getTitle());
                })
                .toList();
    }

    // 신고 내용 조회(댓글 신고 전체 조회) -> 관리자
    // targetSummary = 댓글
    @Transactional(readOnly = true)
    public List<ReportResponse> getCommentReports() {
        List<Report> reports = reportRepository.findAllByTargetType(TargetType.COMMENT);
        if (reports.isEmpty()) {
            throw new BusinessException(ErrorCode.NOT_EXIST_REPORT);
        }
        return reports.stream()
                .map(report -> {
                    Comment comment = commentRepository.findById(report.getTargetId())
                            .orElseThrow(() -> new BusinessException(ErrorCode.NOT_EXIST_COMMENT));

                    return ReportResponse.of(report, comment.getContent());
                })
                .toList();
    }

    // 게시글/댓글 하나에 대한 신고 전체 조회
    @Transactional(readOnly = true)
    public List<ReportResponse> getTargetPostOrCommentReports(Long targetId) {
        List<Report> reports = reportRepository.findAllByTargetId(targetId);

        if (reports.isEmpty()) {
            throw new BusinessException(ErrorCode.NOT_EXIST_REPORT);
        }

        return reports.stream()
                .map(report -> {
                    if (TargetType.POST.equals(report.getTargetType())) {
                        Post post = postRepository.findById(report.getTargetId())
                                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_EXIST_POST));

                        return ReportResponse.of(report, post.getTitle());
                    } else {
                        Comment comment = commentRepository.findById(report.getTargetId())
                                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_EXIST_COMMENT));

                        return ReportResponse.of(report, comment.getContent());
                    }
                })
                .toList();
    }

    // 신고 내용 조회( 신고한 모든 게시글, 댓글 전체 조회) -> 유저
    @Transactional(readOnly = true)
    public List<ReportResponse> getAllReportByUserId(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        List<Report> reports = reportRepository.findAllByUser_Id(user.getId());
        if (reports.isEmpty()) {
            throw new BusinessException(ErrorCode.NOT_EXIST_REPORT);
        }

        return reports.stream()
                .map(this::toReportResponse)
                .toList();
    }

    // 신고 내용 조회(신고 상세 내역). targetSummary 포함
    @Transactional(readOnly = true)
    public ReportResponse getReportDetail(Long reportId) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_EXIST_REPORT));
        return toReportResponse(report);
    }
}
