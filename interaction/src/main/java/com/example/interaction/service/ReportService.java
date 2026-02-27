package com.example.interaction.service;

import com.example.common.enums.TargetType;
import com.example.common.exception.BusinessException;
import com.example.common.exception.ErrorCode;
import com.example.interaction.domain.Report;
import com.example.interaction.dto.request.ReportRequest;
import com.example.interaction.repository.ReportRepository;
import com.example.repository.CommentRepository;
import com.example.repository.PostRepository;
import com.example.user.domain.User;
import com.example.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReportService {
    private final ReportRepository reportRepository;
    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;

    /*

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
        }else {
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
}
