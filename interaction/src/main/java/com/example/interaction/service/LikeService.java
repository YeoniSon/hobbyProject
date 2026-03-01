package com.example.interaction.service;

import com.example.common.enums.TargetType;
import com.example.common.exception.BusinessException;
import com.example.common.exception.ErrorCode;
import com.example.board.domain.Comment;
import com.example.board.domain.Post;
import com.example.interaction.domain.Like;
import com.example.interaction.dto.response.CountResponse;
import com.example.interaction.dto.response.LikeDataResponse;
import com.example.interaction.repository.LikeRepository;
import com.example.board.repository.CommentRepository;
import com.example.board.repository.PostRepository;
import com.example.user.domain.User;
import com.example.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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
    public void setLike(Long userId, Long targetId, TargetType targetType) {
        User user = findUser(userId);
        validateTargetExists(targetId, targetType);

        if (likeRepository.existsByTargetTypeAndUser_IdAndTargetId(targetType, user.getId(), targetId)) {
            throw new BusinessException(ErrorCode.ALREADY_EXIST_LIKE);
        }

        likeRepository.save(createLike(targetId, user, targetType));
    }

    // 게시판/댓글 좋아요 취소
    @Transactional
    public void setUnLike(Long userId, Long targetId, TargetType targetType) {
        User user = findUser(userId);
        validateTargetExists(targetId, targetType);

        if (!likeRepository.existsByTargetTypeAndUser_IdAndTargetId(targetType, user.getId(), targetId)) {
            throw new BusinessException(ErrorCode.NOT_EXIST_LIKE);
        }

        Like like = likeRepository.findByTargetTypeAndTargetIdAndUser_Id(targetType, targetId, user.getId())
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

    // 게시판 / 댓글 별 좋아요 수
    @Transactional
    public CountResponse countLike(Long targetId, TargetType targetType) {
        validateTargetExists(targetId, targetType);

        int count = likeRepository.countByTargetTypeAndTargetId(targetType, targetId);

        return CountResponse.from(count);
    }

    // 사용자가 좋아요를 누른 게시물/댓글 전체 조회
    @Transactional(readOnly = true)
    public List<LikeDataResponse> allLikeView(Long userId, TargetType targetType) {
        List<Like> likes = likeRepository.findAllByUser_IdAndTargetType(userId, targetType);

        if (TargetType.POST.equals(targetType)) {
            return likes.stream()
                    .map(like -> {
                        Post post = postRepository.findById(like.getTargetId())
                                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_EXIST_POST));
                        return LikeDataResponse.fromPost(post);
                    })
                    .toList();
        } else {
            return likes.stream()
                    .map(like -> {
                        Comment comment = commentRepository.findById(like.getTargetId())
                                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_EXIST_COMMENT));
                        return LikeDataResponse.fromComment(comment);
                    })
                    .toList();
        }
    }
}
