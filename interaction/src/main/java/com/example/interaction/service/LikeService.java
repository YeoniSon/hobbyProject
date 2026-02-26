package com.example.interaction.service;

import com.example.common.enums.TargetType;
import com.example.common.exception.BusinessException;
import com.example.common.exception.ErrorCode;
import com.example.domain.Post;
import com.example.interaction.domain.Like;
import com.example.interaction.repository.LikeRepository;
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

    /*
    게시판/ 댓글 좋아요, 취소
    각 게시판 / 댓글에 대한 좋아요 수
     */

    // 게시판 좋아요 등록
    @Transactional
    public void postLike(Long userId, Long postId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_EXIST_POST));

        if (likeRepository.existsByUser_IdAndTargetId(user.getId(), post.getId())) {
            throw new BusinessException(ErrorCode.ALREADY_EXIST_LIKE);
        }

        likeRepository.save(createLike(postId, user));
    }

    private Like createLike(Long postId, User user) {
        return Like.builder()
                .user(user)
                .targetType(TargetType.POST)
                .targetId(postId)
                .build();
    }

    // 게시판 좋아요 취소
    @Transactional
    public void postUnLike(Long userId, Long postId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_EXIST_POST));

        if (!likeRepository.existsByUser_IdAndTargetId(user.getId(), post.getId())) {
            throw new BusinessException(ErrorCode.NOT_EXIST_LIKE);
        }

        Like like = likeRepository.findByTargetIdAndUser_Id(post.getId(), user.getId())
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_EXIST_LIKE));

        likeRepository.delete(like);
    }
}
