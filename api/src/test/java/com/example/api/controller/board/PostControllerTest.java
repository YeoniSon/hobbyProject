package com.example.api.controller.board;

import com.example.api.security.CustomUserDetails;
import com.example.common.enums.TargetType;
import com.example.common.exception.BusinessException;
import com.example.common.exception.ErrorCode;
import com.example.interaction.service.ReportService;
import com.example.service.PostService;
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
@DisplayName("PostController - 비공개 처리")
class PostControllerTest {

    @Mock
    private PostService postService;

    @Mock
    private ReportService reportService;

    @InjectMocks
    private PostController postController;

    @Test
    @DisplayName("privatePost - 신고 20건 이상이면 비공개 처리 성공")
    void privatePostSuccessWhenReportCountGe20() {
        Long postId = 1L;
        when(reportService.countForTarget(TargetType.POST, postId)).thenReturn(20);

        var result = postController.privatePost(mock(CustomUserDetails.class), postId);

        assertThat(result.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(result.getBody()).contains("success");
        verify(reportService).countForTarget(TargetType.POST, postId);
        verify(postService).privatePost(postId);
    }

    @Test
    @DisplayName("privatePost - 신고 20건 미만이면 NOT_ENOUGH_REPORTS_FOR_PRIVATE 예외")
    void privatePostFailWhenReportCountLt20() {
        Long postId = 1L;
        when(reportService.countForTarget(TargetType.POST, postId)).thenReturn(19);

        assertThatThrownBy(() -> postController.privatePost(mock(CustomUserDetails.class), postId))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode())
                        .isEqualTo(ErrorCode.NOT_ENOUGH_REPORTS_FOR_PRIVATE));

        verify(reportService).countForTarget(TargetType.POST, postId);
        verify(postService, never()).privatePost(anyLong());
    }
}
