package com.example.service;

import com.example.common.exception.BusinessException;
import com.example.common.exception.ErrorCode;
import com.example.domain.Notice;
import com.example.dto.request.notice.NoticeEditRequest;
import com.example.dto.request.notice.NoticeUploadRequest;
import com.example.dto.response.NoticeResponse;
import com.example.repository.NoticeRepository;
import com.example.user.domain.User;
import com.example.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NoticeService {

    private final NoticeRepository noticeRepository;
    private final UserRepository userRepository;

    /*
    - 공지사항 작성 (관리자)
    - 공지사항 조회 (관리자)
    - 공지사항 수정 (관리자)
    - 공지사항 삭제 (관리자)
    - 공지사항 공개여부 (관리자)
     */

    // 공지사항 작성
    @Transactional
    public void uploadNotice(NoticeUploadRequest request) {
        Notice notice = createNotice(request);
        noticeRepository.save(notice);
    }

    private Notice createNotice(NoticeUploadRequest request) {
        User user = userRepository.findById(request.getWriterId())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        return Notice.builder()
                .title(request.getTitle())
                .content(request.getContent())
                .user(user)
                .build();
    }

    // 공지사항 조회
    @Transactional
    public List<NoticeResponse> getAllNotice() {
        return noticeRepository.findAll()
                .stream()
                .map(NoticeResponse::from)
                .toList();
    }

    //공지사항 조회(공개만)
    @Transactional
    public List<NoticeResponse> getShowNotice() {
        return noticeRepository.findAllByShowTrue()
                .stream()
                .map(NoticeResponse::from)
                .toList();
    }

    // 공지사항 조회 (비공개만)
    @Transactional
    public List<NoticeResponse> getPrivateNotice() {
        return noticeRepository.findAllByShowFalse()
                .stream()
                .map(NoticeResponse::from)
                .toList();
    }

    // 공지사항 세부 조회
    @Transactional
    public NoticeResponse getNoticeInfo(Long noticeId) {
        Notice notice = noticeRepository.findById(noticeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_EXIST_NOTICE));

        return NoticeResponse.from(notice);
    }

    // 공지사항 수정 - 입력된 필드만 변경, 입력 안 한 필드는 기존 값 유지
    @Transactional
    public NoticeResponse editNotice(Long noticeId, NoticeEditRequest request) {
        Notice notice = noticeRepository.findById(noticeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_EXIST_NOTICE));

        boolean titleUnchanged = request.getTitle() == null || request.getTitle().isBlank()
                || request.getTitle().equals(notice.getTitle());
        boolean contentUnchanged = request.getContent() == null || request.getContent().isBlank()
                || request.getContent().equals(notice.getContent());

        if (titleUnchanged && contentUnchanged) {
            throw new BusinessException(ErrorCode.NO_CHANGE);
        }

        if (request.getTitle() != null && !request.getTitle().isBlank()) {
            notice.updateTitle(request.getTitle());
        }
        if (request.getContent() != null && !request.getContent().isBlank()) {
            notice.updateContent(request.getContent());
        }

        return NoticeResponse.from(notice);
    }

    // 공지사항 삭제
    @Transactional
    public void deleteNotice(Long noticeId) {
        Notice notice = noticeRepository.findById(noticeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_EXIST_NOTICE));
        noticeRepository.delete(notice);
    }

    // 공지사항 비공개
    @Transactional
    public void privateNotice(Long noticeId) {
        Notice notice = noticeRepository.findById(noticeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_EXIST_NOTICE));
        notice.deleteShow();
    }

    // 공지사항 공개 복구
    @Transactional
    public void showNotice(Long noticeId) {
        Notice notice = noticeRepository.findById(noticeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_EXIST_NOTICE));
        notice.depositShow();
    }
}
