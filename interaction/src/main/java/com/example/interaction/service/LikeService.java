package com.example.interaction.service;

import com.example.common.enums.TargetType;
import com.example.common.exception.BusinessException;
import com.example.common.exception.ErrorCode;
import com.example.interaction.domain.Like;
import com.example.interaction.repository.LikeRepository;
import com.example.repository.CommentRepository;
import com.example.repository.PostRepository;
import com.example.user.domain.User;
import com.example.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LikeService {

    private final LikeRepository likeRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final CommentRepository commentRepository;

    /*
    게시판/ 댓글 좋아요, 취소
    각 게시판 / 댓글에 대한 좋아요 수
     */

    // 게시판/댓글 좋아요
    @Transactional
    public void postLike(Long userId, Long targetId, TargetType targetType) {
        User user = findUser(userId);
        validateTargetExists(targetId, targetType);

        if (likeRepository.existsByUser_IdAndTargetId(user.getId(), targetId)) {
            throw new BusinessException(ErrorCode.ALREADY_EXIST_LIKE);
        }

        likeRepository.save(createLike(targetId, user, targetType));
    }

    // 게시판/댓글 좋아요 취소
    @Transactional
    public void postUnLike(Long userId, Long targetId, TargetType targetType) {
        User user = findUser(userId);
        validateTargetExists(targetId, targetType);

        if (!likeRepository.existsByUser_IdAndTargetId(user.getId(), targetId)) {
            throw new BusinessException(ErrorCode.NOT_EXIST_LIKE);
        }

        Like like = likeRepository.findByTargetIdAndUser_Id(targetId, user.getId())
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_EXIST_LIKE));

        likeRepository.delete(like);
    }

    private User findUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
    }

    private void validateTargetExists(Long targetId, TargetType targetType) {
        if (TargetType.POST.equals(targetType)) {
            postRepository.findById(targetId)
                    .orElseThrow(() -> new BusinessException(ErrorCode.NOT_EXIST_POST));
        } else {
            commentRepository.findById(targetId)
                    .orElseThrow(() -> new BusinessException(ErrorCode.NOT_EXIST_COMMENT));
        }
    }

    private Like createLike(Long targetId, User user, TargetType targetType) {
        return Like.builder()
                .user(user)
                .targetType(targetType)
                .targetId(targetId)
                .build();
    }
}
