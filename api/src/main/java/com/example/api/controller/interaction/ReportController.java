package com.example.api.controller.interaction;

import com.example.api.security.CustomUserDetails;
import com.example.interaction.dto.request.ReportRequest;
import com.example.interaction.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/report")
@RequiredArgsConstructor
public class ReportController {
    private final ReportService reportService;

    // post 신고
    @PostMapping("/post/{postId}")
    public ResponseEntity<String> reportPost(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long postId,
            @RequestBody ReportRequest request
    ) {
        reportService.registerReport(userDetails.getId(), postId, request);
        return ResponseEntity.ok("post report success");
    }

    // comment 신고
    @PostMapping("/comment/{commentId}")
    public ResponseEntity<String> reportComment(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long commentId,
            @RequestBody ReportRequest request
    ){
        reportService.registerReport(userDetails.getId(), commentId, request);
        return ResponseEntity.ok("comment report success");
    }


}
