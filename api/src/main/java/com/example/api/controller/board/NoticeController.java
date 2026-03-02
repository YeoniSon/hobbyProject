package com.example.api.controller.board;

import com.example.api.security.CustomUserDetails;
import com.example.board.dto.request.notice.NoticeEditRequest;
import com.example.board.dto.request.notice.NoticeUploadRequest;
import com.example.board.dto.response.NoticeResponse;
import com.example.board.service.NoticeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/notice/manage")
@RequiredArgsConstructor
public class NoticeController {

    private final NoticeService noticeService;

    // 공지사항 등록
    @PostMapping("/register")
    public ResponseEntity<String> uploadNotice(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody NoticeUploadRequest request
    ) {
        noticeService.uploadNotice(request);
        return ResponseEntity.ok("notice Upload success");
    }

    // 전체 공지사항 조회
    @GetMapping("/all-notices")
    public ResponseEntity<List<NoticeResponse>> getAllNotices(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ResponseEntity.ok(noticeService.getAllNotice());
    }

    // 공개되어있는 공지사항 조회
    @GetMapping("/show-all-notices")
    public ResponseEntity<List<NoticeResponse>> getAllShowNotices(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ResponseEntity.ok(noticeService.getShowNotice());
    }

    // 비공개되어있는 공지사항 조회
    @GetMapping("/private-all-notices")
    public ResponseEntity<List<NoticeResponse>> getPrivateAllNotices(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ResponseEntity.ok(noticeService.getPrivateNotice());
    }

    // 공지사항 세부 내용 조회
    @GetMapping("/{noticeId}/details")
    public ResponseEntity<NoticeResponse> getNoticeDetails(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long noticeId
    ) {
        return ResponseEntity.ok(noticeService.getNoticeInfo(noticeId));
    }

    //공지사항 수정
    @PatchMapping("/{noticeId}/edit-notice")
    public ResponseEntity<NoticeResponse> editNotice(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long noticeId,
            @RequestBody NoticeEditRequest request
    ){
        return ResponseEntity.ok(noticeService.editNotice(noticeId, request));
    }

    // 공지사항 삭제
    @PutMapping("/{noticeId}/delete-notice")
    public ResponseEntity<String> deleteNotice(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long noticeId
    ) {
        noticeService.deleteNotice(noticeId);
        return ResponseEntity.ok("notice Delete success");
    }

    // 공지사항 비공개
    @PatchMapping("/{noticeId}/private-notice")
    public ResponseEntity<String> privateNotice(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long noticeId
    ) {
        noticeService.privateNotice(noticeId);
        return ResponseEntity.ok("notice Private success");
    }

    // 공지사항 공개
    @PatchMapping("/{noticeId}/show-notice")
    public ResponseEntity<String> showNotice(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long noticeId
    ) {
        noticeService.showNotice(noticeId);
        return ResponseEntity.ok("notice Show success");
    }


}
