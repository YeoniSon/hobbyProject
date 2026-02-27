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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ReportService")
class ReportServiceTest {

    @Mock
    private ReportRepository reportRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PostRepository postRepository;

    @Mock
    private CommentRepository commentRepository;

    @InjectMocks
    private ReportService reportService;

    private ReportRequest createRequest(TargetType targetType, String reason) {
        return new ReportRequest(targetType, reason);
    }

    @Test
    @DisplayName("registerReport - 게시글 신고 성공")
    void registerReportPostSuccess() {
        User user = mock(User.class);
        when(user.getId()).thenReturn(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(postRepository.findById(10L)).thenReturn(Optional.of(mock(Post.class)));
        when(reportRepository.existsByUser_IdAndTargetId(1L, 10L)).thenReturn(false);

        ReportRequest request = createRequest(TargetType.POST, "스팸입니다");

        reportService.registerReport(1L, 10L, request);

        ArgumentCaptor<Report> captor = ArgumentCaptor.forClass(Report.class);
        verify(reportRepository).save(captor.capture());
        Report saved = captor.getValue();
        assertThat(saved.getUser()).isEqualTo(user);
        assertThat(saved.getTargetId()).isEqualTo(10L);
        assertThat(saved.getTargetType()).isEqualTo(TargetType.POST);
        assertThat(saved.getReason()).isEqualTo("스팸입니다");
    }

    @Test
    @DisplayName("registerReport - 댓글 신고 성공")
    void registerReportCommentSuccess() {
        User user = mock(User.class);
        when(user.getId()).thenReturn(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(commentRepository.findById(20L)).thenReturn(Optional.of(mock(Comment.class)));
        when(reportRepository.existsByUser_IdAndTargetId(1L, 20L)).thenReturn(false);

        ReportRequest request = createRequest(TargetType.COMMENT, "욕설입니다");

        reportService.registerReport(1L, 20L, request);

        ArgumentCaptor<Report> captor = ArgumentCaptor.forClass(Report.class);
        verify(reportRepository).save(captor.capture());
        Report saved = captor.getValue();
        assertThat(saved.getUser()).isEqualTo(user);
        assertThat(saved.getTargetId()).isEqualTo(20L);
        assertThat(saved.getTargetType()).isEqualTo(TargetType.COMMENT);
        assertThat(saved.getReason()).isEqualTo("욕설입니다");
    }

    @Test
    @DisplayName("registerReport - 존재하지 않는 사용자면 USER_NOT_FOUND 예외")
    void registerReportUserNotFound() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());
        ReportRequest request = createRequest(TargetType.POST, "사유");

        assertThatThrownBy(() -> reportService.registerReport(999L, 1L, request))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode())
                        .isEqualTo(ErrorCode.USER_NOT_FOUND));

        verify(reportRepository, never()).save(any());
    }

    @Test
    @DisplayName("registerReport - 존재하지 않는 게시글이면 NOT_EXIST_POST 예외")
    void registerReportPostNotFound() {
        User user = mock(User.class);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(postRepository.findById(999L)).thenReturn(Optional.empty());

        ReportRequest request = createRequest(TargetType.POST, "사유");

        assertThatThrownBy(() -> reportService.registerReport(1L, 999L, request))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode())
                        .isEqualTo(ErrorCode.NOT_EXIST_POST));

        verify(reportRepository, never()).save(any());
    }

    @Test
    @DisplayName("registerReport - 존재하지 않는 댓글이면 NOT_EXIST_COMMENT 예외")
    void registerReportCommentNotFound() {
        User user = mock(User.class);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(commentRepository.findById(999L)).thenReturn(Optional.empty());

        ReportRequest request = createRequest(TargetType.COMMENT, "사유");

        assertThatThrownBy(() -> reportService.registerReport(1L, 999L, request))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode())
                        .isEqualTo(ErrorCode.NOT_EXIST_COMMENT));

        verify(reportRepository, never()).save(any());
    }

    @Test
    @DisplayName("registerReport - 이미 신고한 경우 ALREADY_EXIST_REPORT 예외")
    void registerReportAlreadyExist() {
        User user = mock(User.class);
        when(user.getId()).thenReturn(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(postRepository.findById(10L)).thenReturn(Optional.of(mock(Post.class)));
        when(reportRepository.existsByUser_IdAndTargetId(1L, 10L)).thenReturn(true);

        ReportRequest request = createRequest(TargetType.POST, "사유");

        assertThatThrownBy(() -> reportService.registerReport(1L, 10L, request))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode())
                        .isEqualTo(ErrorCode.ALREADY_EXIST_REPORT));

        verify(reportRepository, never()).save(any());
    }

    // ---- getAllReports ----
    @Test
    @DisplayName("getAllReports - 신고 전체 조회 성공 (POST/COMMENT 혼합)")
    void getAllReportsSuccess() {
        User user = mock(User.class);
        when(user.getId()).thenReturn(1L);
        Report reportPost = Report.builder().user(user).targetType(TargetType.POST).targetId(10L).reason("사유1").build();
        Report reportComment = Report.builder().user(user).targetType(TargetType.COMMENT).targetId(20L).reason("사유2").build();
        when(reportRepository.findAll()).thenReturn(List.of(reportPost, reportComment));

        Post post = mock(Post.class);
        when(post.getTitle()).thenReturn("게시글 제목");
        when(postRepository.findById(10L)).thenReturn(Optional.of(post));
        Comment comment = mock(Comment.class);
        when(comment.getContent()).thenReturn("댓글 내용");
        when(commentRepository.findById(20L)).thenReturn(Optional.of(comment));

        List<ReportResponse> result = reportService.getAllReports();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getTargetType()).isEqualTo(TargetType.POST);
        assertThat(result.get(0).getTargetSummary()).isEqualTo("게시글 제목");
        assertThat(result.get(1).getTargetType()).isEqualTo(TargetType.COMMENT);
        assertThat(result.get(1).getTargetSummary()).isEqualTo("댓글 내용");
    }

    @Test
    @DisplayName("getAllReports - 신고가 없으면 NOT_EXIST_REPORT 예외")
    void getAllReportsEmpty() {
        when(reportRepository.findAll()).thenReturn(List.of());

        assertThatThrownBy(() -> reportService.getAllReports())
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode())
                        .isEqualTo(ErrorCode.NOT_EXIST_REPORT));
    }

    // ---- getPostReports ----
    @Test
    @DisplayName("getPostReports - 게시글 신고 목록 조회 성공")
    void getPostReportsSuccess() {
        User user = mock(User.class);
        when(user.getId()).thenReturn(1L);
        Report report = Report.builder().user(user).targetType(TargetType.POST).targetId(10L).reason("사유").build();
        when(reportRepository.findAllByTargetType(TargetType.POST)).thenReturn(List.of(report));
        Post post = mock(Post.class);
        when(post.getTitle()).thenReturn("제목");
        when(postRepository.findById(10L)).thenReturn(Optional.of(post));

        List<ReportResponse> result = reportService.getPostReports();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTargetSummary()).isEqualTo("제목");
        assertThat(result.get(0).getTargetType()).isEqualTo(TargetType.POST);
    }

    @Test
    @DisplayName("getPostReports - 게시글 신고가 없으면 NOT_EXIST_REPORT 예외")
    void getPostReportsEmpty() {
        when(reportRepository.findAllByTargetType(TargetType.POST)).thenReturn(List.of());

        assertThatThrownBy(() -> reportService.getPostReports())
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode())
                        .isEqualTo(ErrorCode.NOT_EXIST_REPORT));
    }

    @Test
    @DisplayName("getPostReports - 대상 게시글이 없으면 NOT_EXIST_POST 예외")
    void getPostReportsPostNotFound() {
        User user = mock(User.class);
        Report report = Report.builder().user(user).targetType(TargetType.POST).targetId(999L).reason("사유").build();
        when(reportRepository.findAllByTargetType(TargetType.POST)).thenReturn(List.of(report));
        when(postRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reportService.getPostReports())
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode())
                        .isEqualTo(ErrorCode.NOT_EXIST_POST));
    }

    // ---- getCommentReports ----
    @Test
    @DisplayName("getCommentReports - 댓글 신고 목록 조회 성공")
    void getCommentReportsSuccess() {
        User user = mock(User.class);
        when(user.getId()).thenReturn(1L);
        Report report = Report.builder().user(user).targetType(TargetType.COMMENT).targetId(5L).reason("욕설").build();
        when(reportRepository.findAllByTargetType(TargetType.COMMENT)).thenReturn(List.of(report));
        Comment comment = mock(Comment.class);
        when(comment.getContent()).thenReturn("댓글본문");
        when(commentRepository.findById(5L)).thenReturn(Optional.of(comment));

        List<ReportResponse> result = reportService.getCommentReports();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTargetSummary()).isEqualTo("댓글본문");
        assertThat(result.get(0).getTargetType()).isEqualTo(TargetType.COMMENT);
    }

    @Test
    @DisplayName("getCommentReports - 댓글 신고가 없으면 NOT_EXIST_REPORT 예외")
    void getCommentReportsEmpty() {
        when(reportRepository.findAllByTargetType(TargetType.COMMENT)).thenReturn(List.of());

        assertThatThrownBy(() -> reportService.getCommentReports())
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode())
                        .isEqualTo(ErrorCode.NOT_EXIST_REPORT));
    }

    // ---- getTargetPostOrCommentReports ----
    @Test
    @DisplayName("getTargetPostOrCommentReports - 특정 대상 신고 목록 조회 성공")
    void getTargetPostOrCommentReportsSuccess() {
        User user = mock(User.class);
        when(user.getId()).thenReturn(1L);
        Report report = Report.builder().user(user).targetType(TargetType.POST).targetId(10L).reason("사유").build();
        when(reportRepository.findAllByTargetId(10L)).thenReturn(List.of(report));
        Post post = mock(Post.class);
        when(post.getTitle()).thenReturn("제목");
        when(postRepository.findById(10L)).thenReturn(Optional.of(post));

        List<ReportResponse> result = reportService.getTargetPostOrCommentReports(10L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTargetId()).isEqualTo(10L);
        assertThat(result.get(0).getTargetSummary()).isEqualTo("제목");
    }

    @Test
    @DisplayName("getTargetPostOrCommentReports - 해당 대상 신고 없으면 NOT_EXIST_REPORT 예외")
    void getTargetPostOrCommentReportsEmpty() {
        when(reportRepository.findAllByTargetId(1L)).thenReturn(List.of());

        assertThatThrownBy(() -> reportService.getTargetPostOrCommentReports(1L))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode())
                        .isEqualTo(ErrorCode.NOT_EXIST_REPORT));
    }

    // ---- getAllReportByUserId ----
    @Test
    @DisplayName("getAllReportByUserId - 유저별 신고 목록 조회 성공")
    void getAllReportByUserIdSuccess() {
        User user = mock(User.class);
        when(user.getId()).thenReturn(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        Report report = Report.builder().user(user).targetType(TargetType.POST).targetId(10L).reason("사유").build();
        when(reportRepository.findAllByUser_Id(1L)).thenReturn(List.of(report));
        Post post = mock(Post.class);
        when(post.getTitle()).thenReturn("제목");
        when(postRepository.findById(10L)).thenReturn(Optional.of(post));

        List<ReportResponse> result = reportService.getAllReportByUserId(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getReporterId()).isEqualTo(1L);
        assertThat(result.get(0).getTargetSummary()).isEqualTo("제목");
    }

    @Test
    @DisplayName("getAllReportByUserId - 존재하지 않는 유저면 USER_NOT_FOUND 예외")
    void getAllReportByUserIdUserNotFound() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reportService.getAllReportByUserId(999L))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode())
                        .isEqualTo(ErrorCode.USER_NOT_FOUND));
    }

    @Test
    @DisplayName("getAllReportByUserId - 해당 유저 신고 없으면 NOT_EXIST_REPORT 예외")
    void getAllReportByUserIdEmpty() {
        User user = mock(User.class);
        when(user.getId()).thenReturn(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(reportRepository.findAllByUser_Id(1L)).thenReturn(List.of());

        assertThatThrownBy(() -> reportService.getAllReportByUserId(1L))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode())
                        .isEqualTo(ErrorCode.NOT_EXIST_REPORT));
    }

    // ---- getReportDetail ----
    @Test
    @DisplayName("getReportDetail - 신고 상세 조회 성공 (POST)")
    void getReportDetailSuccessPost() {
        User user = mock(User.class);
        when(user.getId()).thenReturn(1L);
        Report report = Report.builder().user(user).targetType(TargetType.POST).targetId(10L).reason("사유").build();
        when(reportRepository.findById(1L)).thenReturn(Optional.of(report));
        Post post = mock(Post.class);
        when(post.getTitle()).thenReturn("제목");
        when(postRepository.findById(10L)).thenReturn(Optional.of(post));

        ReportResponse result = reportService.getReportDetail(1L);

        assertThat(result.getTargetId()).isEqualTo(10L);
        assertThat(result.getTargetType()).isEqualTo(TargetType.POST);
        assertThat(result.getTargetSummary()).isEqualTo("제목");
        assertThat(result.getReason()).isEqualTo("사유");
    }

    @Test
    @DisplayName("getReportDetail - 신고 상세 조회 성공 (COMMENT)")
    void getReportDetailSuccessComment() {
        User user = mock(User.class);
        when(user.getId()).thenReturn(1L);
        Report report = Report.builder().user(user).targetType(TargetType.COMMENT).targetId(20L).reason("욕설").build();
        when(reportRepository.findById(2L)).thenReturn(Optional.of(report));
        Comment comment = mock(Comment.class);
        when(comment.getContent()).thenReturn("댓글내용");
        when(commentRepository.findById(20L)).thenReturn(Optional.of(comment));

        ReportResponse result = reportService.getReportDetail(2L);

        assertThat(result.getTargetId()).isEqualTo(20L);
        assertThat(result.getTargetType()).isEqualTo(TargetType.COMMENT);
        assertThat(result.getTargetSummary()).isEqualTo("댓글내용");
    }

    @Test
    @DisplayName("getReportDetail - 존재하지 않는 신고면 NOT_EXIST_REPORT 예외")
    void getReportDetailNotFound() {
        when(reportRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reportService.getReportDetail(999L))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode())
                        .isEqualTo(ErrorCode.NOT_EXIST_REPORT));
    }

    // ---- deleteReport (신고 취소) ----
    @Test
    @DisplayName("deleteReport - 신고 취소 성공")
    void deleteReportSuccess() {
        User user = mock(User.class);
        Report report = Report.builder()
                .user(user)
                .targetType(TargetType.POST)
                .targetId(10L)
                .reason("사유")
                .build();
        when(reportRepository.findById(1L)).thenReturn(Optional.of(report));

        reportService.deleteReport(1L);

        verify(reportRepository).findById(1L);
        verify(reportRepository).delete(report);
    }

    @Test
    @DisplayName("deleteReport - 존재하지 않는 신고면 NOT_EXIST_REPORT 예외")
    void deleteReportNotFound() {
        when(reportRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reportService.deleteReport(999L))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode())
                        .isEqualTo(ErrorCode.NOT_EXIST_REPORT));

        verify(reportRepository, never()).delete(any());
    }
}

