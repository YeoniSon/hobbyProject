package com.example.service;

import com.example.board.service.NoticeService;
import com.example.common.exception.BusinessException;
import com.example.common.exception.ErrorCode;
import com.example.board.domain.Notice;
import com.example.board.dto.request.notice.NoticeEditRequest;
import com.example.board.dto.request.notice.NoticeUploadRequest;
import com.example.board.dto.response.NoticeResponse;
import com.example.board.repository.NoticeRepository;
import com.example.user.domain.User;
import com.example.user.repository.UserRepository;
import com.example.common.enums.Role;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("NoticeService")
class NoticeServiceTest {

    @Mock
    private NoticeRepository noticeRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private NoticeService noticeService;

    private static User createUser() {
        return User.builder()
                .email("admin@test.com")
                .name("관리자")
                .password("password")
                .nickname("admin")
                .phone("01011112222")
                .birth(LocalDate.of(1990, 1, 1))
                .role(Role.ADMIN)
                .build();
    }

    @Test
    @DisplayName("uploadNotice - 공지사항 작성 성공")
    void uploadNoticeSuccess() {
        NoticeUploadRequest request = new NoticeUploadRequest(1L, "공지 제목", "공지 내용");
        User user = createUser();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        noticeService.uploadNotice(request);

        ArgumentCaptor<Notice> captor = ArgumentCaptor.forClass(Notice.class);
        verify(noticeRepository).save(captor.capture());
        Notice saved = captor.getValue();
        assertThat(saved.getTitle()).isEqualTo("공지 제목");
        assertThat(saved.getContent()).isEqualTo("공지 내용");
        assertThat(saved.getUser()).isEqualTo(user);
        assertThat(saved.isShow()).isTrue();
    }

    @Test
    @DisplayName("uploadNotice - 존재하지 않는 사용자면 USER_NOT_FOUND 예외")
    void uploadNoticeUserNotFound() {
        NoticeUploadRequest request = new NoticeUploadRequest(999L, "제목", "내용");
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> noticeService.uploadNotice(request))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode())
                        .isEqualTo(ErrorCode.USER_NOT_FOUND));

        verify(noticeRepository, never()).save(any());
    }

    @Test
    @DisplayName("getAllNotice - 전체 공지 목록 반환")
    void getAllNoticeSuccess() {
        Notice notice = Notice.builder()
                .user(createUser())
                .title("제목")
                .content("내용")
                .build();
        when(noticeRepository.findAll()).thenReturn(List.of(notice));

        List<NoticeResponse> result = noticeService.getAllNotice();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).isEqualTo("제목");
        verify(noticeRepository).findAll();
    }

    @Test
    @DisplayName("getShowNotice - 공개 공지 목록 반환")
    void getShowNoticeSuccess() {
        Notice notice = Notice.builder()
                .user(createUser())
                .title("공개 공지")
                .content("내용")
                .build();
        when(noticeRepository.findAllByShowTrue()).thenReturn(List.of(notice));

        List<NoticeResponse> result = noticeService.getShowNotice();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).isEqualTo("공개 공지");
        verify(noticeRepository).findAllByShowTrue();
    }

    @Test
    @DisplayName("getPrivateNotice - 비공개 공지 목록 반환")
    void getPrivateNoticeSuccess() {
        Notice notice = Notice.builder()
                .user(createUser())
                .title("비공개 공지")
                .content("내용")
                .build();
        notice.deleteShow();
        when(noticeRepository.findAllByShowFalse()).thenReturn(List.of(notice));

        List<NoticeResponse> result = noticeService.getPrivateNotice();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).isEqualTo("비공개 공지");
        verify(noticeRepository).findAllByShowFalse();
    }

    @Test
    @DisplayName("editNotice - 제목만 수정 시 제목만 변경된다")
    void editNoticeOnlyTitle() {
        Notice notice = Notice.builder()
                .user(createUser())
                .title("기존 제목")
                .content("기존 내용")
                .build();
        when(noticeRepository.findById(1L)).thenReturn(Optional.of(notice));
        NoticeEditRequest request = new NoticeEditRequest("새 제목", null);

        NoticeResponse result = noticeService.editNotice(1L, request);

        assertThat(notice.getTitle()).isEqualTo("새 제목");
        assertThat(notice.getContent()).isEqualTo("기존 내용");
        assertThat(result.getTitle()).isEqualTo("새 제목");
    }

    @Test
    @DisplayName("editNotice - 내용만 수정 시 내용만 변경된다")
    void editNoticeOnlyContent() {
        Notice notice = Notice.builder()
                .user(createUser())
                .title("제목")
                .content("기존 내용")
                .build();
        when(noticeRepository.findById(1L)).thenReturn(Optional.of(notice));
        NoticeEditRequest request = new NoticeEditRequest(null, "새 내용");

        NoticeResponse result = noticeService.editNotice(1L, request);

        assertThat(notice.getContent()).isEqualTo("새 내용");
        assertThat(result.getContent()).isEqualTo("새 내용");
    }

    @Test
    @DisplayName("editNotice - 제목·내용 모두 수정")
    void editNoticeMultipleFields() {
        Notice notice = Notice.builder()
                .user(createUser())
                .title("기존")
                .content("기존내용")
                .build();
        when(noticeRepository.findById(1L)).thenReturn(Optional.of(notice));
        NoticeEditRequest request = new NoticeEditRequest("새 제목", "새 내용");

        NoticeResponse result = noticeService.editNotice(1L, request);

        assertThat(notice.getTitle()).isEqualTo("새 제목");
        assertThat(notice.getContent()).isEqualTo("새 내용");
        assertThat(result.getTitle()).isEqualTo("새 제목");
        assertThat(result.getContent()).isEqualTo("새 내용");
    }

    @Test
    @DisplayName("editNotice - 수정 내용이 없으면 NO_CHANGE 예외")
    void editNoticeNoChange() {
        Notice notice = Notice.builder()
                .user(createUser())
                .title("제목")
                .content("내용")
                .build();
        when(noticeRepository.findById(1L)).thenReturn(Optional.of(notice));
        NoticeEditRequest request = new NoticeEditRequest("제목", "내용");

        assertThatThrownBy(() -> noticeService.editNotice(1L, request))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode())
                        .isEqualTo(ErrorCode.NO_CHANGE));
    }

    @Test
    @DisplayName("editNotice - 빈 문자열만 있으면 NO_CHANGE 예외")
    void editNoticeBlankOnly() {
        Notice notice = Notice.builder()
                .user(createUser())
                .title("제목")
                .content("내용")
                .build();
        when(noticeRepository.findById(1L)).thenReturn(Optional.of(notice));
        NoticeEditRequest request = new NoticeEditRequest("  ", "");

        assertThatThrownBy(() -> noticeService.editNotice(1L, request))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode())
                        .isEqualTo(ErrorCode.NO_CHANGE));
    }

    @Test
    @DisplayName("editNotice - 존재하지 않는 공지면 NOT_EXIST_NOTICE 예외")
    void editNoticeNotFound() {
        when(noticeRepository.findById(999L)).thenReturn(Optional.empty());
        NoticeEditRequest request = new NoticeEditRequest("제목", null);

        assertThatThrownBy(() -> noticeService.editNotice(999L, request))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode())
                        .isEqualTo(ErrorCode.NOT_EXIST_NOTICE));
    }

    @Test
    @DisplayName("deleteNotice - 공지 삭제 성공")
    void deleteNoticeSuccess() {
        Notice notice = Notice.builder()
                .user(createUser())
                .title("제목")
                .content("내용")
                .build();
        when(noticeRepository.findById(1L)).thenReturn(Optional.of(notice));

        noticeService.deleteNotice(1L);

        verify(noticeRepository).findById(1L);
        verify(noticeRepository).delete(notice);
    }

    @Test
    @DisplayName("deleteNotice - 존재하지 않는 공지면 NOT_EXIST_NOTICE 예외")
    void deleteNoticeNotFound() {
        when(noticeRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> noticeService.deleteNotice(999L))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode())
                        .isEqualTo(ErrorCode.NOT_EXIST_NOTICE));

        verify(noticeRepository, never()).delete(any());
    }

    @Test
    @DisplayName("privateNotice - 공지 비공개 처리 성공")
    void privateNoticeSuccess() {
        Notice notice = Notice.builder()
                .user(createUser())
                .title("제목")
                .content("내용")
                .build();
        when(noticeRepository.findById(1L)).thenReturn(Optional.of(notice));

        noticeService.privateNotice(1L);

        assertThat(notice.isShow()).isFalse();
        verify(noticeRepository).findById(1L);
    }

    @Test
    @DisplayName("privateNotice - 존재하지 않는 공지면 NOT_EXIST_NOTICE 예외")
    void privateNoticeNotFound() {
        when(noticeRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> noticeService.privateNotice(999L))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode())
                        .isEqualTo(ErrorCode.NOT_EXIST_NOTICE));
    }

    @Test
    @DisplayName("showNotice - 공지 공개 복구 성공")
    void showNoticeSuccess() {
        Notice notice = Notice.builder()
                .user(createUser())
                .title("제목")
                .content("내용")
                .build();
        notice.deleteShow();
        when(noticeRepository.findById(1L)).thenReturn(Optional.of(notice));

        noticeService.showNotice(1L);

        assertThat(notice.isShow()).isTrue();
        verify(noticeRepository).findById(1L);
    }

    @Test
    @DisplayName("showNotice - 존재하지 않는 공지면 NOT_EXIST_NOTICE 예외")
    void showNoticeNotFound() {
        when(noticeRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> noticeService.showNotice(999L))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode())
                        .isEqualTo(ErrorCode.NOT_EXIST_NOTICE));
    }
}
