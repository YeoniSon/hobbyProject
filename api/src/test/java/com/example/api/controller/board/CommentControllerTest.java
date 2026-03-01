package com.example.api.controller.board;

import com.example.api.security.CustomUserDetails;
import com.example.common.enums.TargetType;
import com.example.common.exception.BusinessException;
import com.example.common.exception.ErrorCode;
import com.example.interaction.service.ReportService;
import com.example.board.service.CommentService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CommentController - 비공개 처리")
class CommentControllerTest {

    @Mock
    private CommentService commentService;

    @Mock
    private ReportService reportService;

    @InjectMocks
    private CommentController commentController;

    @Test
    @DisplayName("editPrivateComment - 신고 20건 이상이면 댓글 비공개 처리 성공")
    void privateCommentSuccessWhenReportCountGe20() {
        Long commentId = 1L;
        when(reportService.countForTarget(TargetType.COMMENT, commentId)).thenReturn(20);

        var result = commentController.editPrivateComment(mock(CustomUserDetails.class), commentId);

        assertThat(result.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(result.getBody()).isEqualTo("success");
        verify(reportService).countForTarget(TargetType.COMMENT, commentId);
        verify(commentService).privateComment(commentId);
    }

    @Test
    @DisplayName("editPrivateComment - 신고 20건 미만이면 NOT_ENOUGH_REPORTS_FOR_PRIVATE 예외")
    void privateCommentFailWhenReportCountLt20() {
        Long commentId = 1L;
        when(reportService.countForTarget(TargetType.COMMENT, commentId)).thenReturn(19);

        assertThatThrownBy(() -> commentController.editPrivateComment(mock(CustomUserDetails.class), commentId))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode())
                        .isEqualTo(ErrorCode.NOT_ENOUGH_REPORTS_FOR_PRIVATE));

        verify(reportService).countForTarget(TargetType.COMMENT, commentId);
        verify(commentService, never()).privateComment(anyLong());
    }
}
