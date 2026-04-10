package com.example.api.controller.board;

import com.example.api.security.CustomUserDetails;
import com.example.board.dto.request.notice.NoticeEditRequest;
import com.example.board.dto.request.notice.NoticeUploadRequest;
import com.example.board.dto.response.NoticeResponse;
import com.example.board.service.NoticeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/notice/manage")
@Tag(name = "공지사항", description = "공지 등록·조회·수정·삭제·공개/비공개 (관리)")
@SecurityRequirement(name = "JWTAuth")
@RequiredArgsConstructor
public class NoticeController {

    private final NoticeService noticeService;

    @Operation(summary = "공지 등록", description = "새 공지사항을 등록합니다.")
    @PostMapping("/register")
    public ResponseEntity<String> uploadNotice(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody NoticeUploadRequest request
    ) {
        noticeService.uploadNotice(request);
        return ResponseEntity.ok("notice Upload success");
    }

    @Operation(summary = "공지 전체 조회", description = "모든 공지사항을 조회합니다.")
    @GetMapping("/all-notices")
    public ResponseEntity<List<NoticeResponse>> getAllNotices(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ResponseEntity.ok(noticeService.getAllNotice());
    }

    @Operation(summary = "공개 공지 조회", description = "공개 상태 공지만 조회합니다.")
    @GetMapping("/show-all-notices")
    public ResponseEntity<List<NoticeResponse>> getAllShowNotices(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ResponseEntity.ok(noticeService.getShowNotice());
    }

    @Operation(summary = "비공개 공지 조회", description = "비공개 공지만 조회합니다.")
    @GetMapping("/private-all-notices")
    public ResponseEntity<List<NoticeResponse>> getPrivateAllNotices(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ResponseEntity.ok(noticeService.getPrivateNotice());
    }

    @Operation(summary = "공지 상세", description = "noticeId로 공지 상세를 조회합니다.")
    @GetMapping("/{noticeId}/details")
    public ResponseEntity<NoticeResponse> getNoticeDetails(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long noticeId
    ) {
        return ResponseEntity.ok(noticeService.getNoticeInfo(noticeId));
    }

    @Operation(summary = "공지 수정", description = "공지 내용을 수정합니다.")
    @PatchMapping("/{noticeId}/edit-notice")
    public ResponseEntity<NoticeResponse> editNotice(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long noticeId,
            @RequestBody NoticeEditRequest request
    ){
        return ResponseEntity.ok(noticeService.editNotice(noticeId, request));
    }

    @Operation(summary = "공지 삭제", description = "공지사항을 삭제합니다.")
    @PutMapping("/{noticeId}/delete-notice")
    public ResponseEntity<String> deleteNotice(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long noticeId
    ) {
        noticeService.deleteNotice(noticeId);
        return ResponseEntity.ok("notice Delete success");
    }

    @Operation(summary = "공지 비공개", description = "공지를 비공개 처리합니다.")
    @PatchMapping("/{noticeId}/private-notice")
    public ResponseEntity<String> privateNotice(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long noticeId
    ) {
        noticeService.privateNotice(noticeId);
        return ResponseEntity.ok("notice Private success");
    }

    @Operation(summary = "공지 공개", description = "공지를 다시 공개합니다.")
    @PatchMapping("/{noticeId}/show-notice")
    public ResponseEntity<String> showNotice(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long noticeId
    ) {
        noticeService.showNotice(noticeId);
        return ResponseEntity.ok("notice Show success");
    }


}
