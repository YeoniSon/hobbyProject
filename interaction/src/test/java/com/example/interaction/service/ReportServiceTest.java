package com.example.interaction.service;

import com.example.common.enums.TargetType;
import com.example.common.exception.BusinessException;
import com.example.common.exception.ErrorCode;
import com.example.domain.Comment;
import com.example.domain.Post;
import com.example.interaction.domain.Report;
import com.example.interaction.dto.request.ReportRequest;
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
}

