package com.example.interaction.service;

import com.example.common.enums.TargetType;
import com.example.common.exception.BusinessException;
import com.example.common.exception.ErrorCode;
import com.example.domain.Comment;
import com.example.domain.Post;
import com.example.interaction.domain.Like;
import com.example.interaction.repository.LikeRepository;
import com.example.repository.CommentRepository;
import com.example.repository.PostRepository;
import com.example.user.domain.User;
import com.example.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("LikeService")
class LikeServiceTest {

    @Mock
    private LikeRepository likeRepository;

    @Mock
    private PostRepository postRepository;

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private LikeService likeService;

    @Test
    @DisplayName("postLike - 게시글 좋아요 등록 성공")
    void postLikeSuccess() {
        User user = mock(User.class);
        when(user.getId()).thenReturn(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(postRepository.findById(1L)).thenReturn(Optional.of(mock(Post.class)));
        when(likeRepository.existsByUser_IdAndTargetId(1L, 1L)).thenReturn(false);

        likeService.postLike(1L, 1L, TargetType.POST);

        ArgumentCaptor<Like> captor = ArgumentCaptor.forClass(Like.class);
        verify(likeRepository).save(captor.capture());
        Like saved = captor.getValue();
        assertThat(saved.getTargetType()).isEqualTo(TargetType.POST);
        assertThat(saved.getTargetId()).isEqualTo(1L);
        assertThat(saved.getUser()).isEqualTo(user);
        verify(likeRepository).existsByUser_IdAndTargetId(1L, 1L);
    }

    @Test
    @DisplayName("postLike - 존재하지 않는 사용자면 USER_NOT_FOUND 예외")
    void postLikeUserNotFound() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> likeService.postLike(999L, 1L, TargetType.POST))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode())
                        .isEqualTo(ErrorCode.USER_NOT_FOUND));

        verify(postRepository, never()).findById(anyLong());
        verify(likeRepository, never()).save(any());
    }

    @Test
    @DisplayName("postLike - 존재하지 않는 게시글이면 NOT_EXIST_POST 예외")
    void postLikePostNotFound() {
        User user = mock(User.class);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(postRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> likeService.postLike(1L, 999L, TargetType.POST))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode())
                        .isEqualTo(ErrorCode.NOT_EXIST_POST));

        verify(likeRepository, never()).save(any());
    }

    @Test
    @DisplayName("postLike - 댓글 좋아요 등록 성공")
    void commentLikeSuccess() {
        User user = mock(User.class);
        when(user.getId()).thenReturn(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(commentRepository.findById(1L)).thenReturn(Optional.of(mock(Comment.class)));
        when(likeRepository.existsByUser_IdAndTargetId(1L, 1L)).thenReturn(false);

        likeService.postLike(1L, 1L, TargetType.COMMENT);

        ArgumentCaptor<Like> captor = ArgumentCaptor.forClass(Like.class);
        verify(likeRepository).save(captor.capture());
        Like saved = captor.getValue();
        assertThat(saved.getTargetType()).isEqualTo(TargetType.COMMENT);
        assertThat(saved.getTargetId()).isEqualTo(1L);
        assertThat(saved.getUser()).isEqualTo(user);
        verify(commentRepository).findById(1L);
        verify(postRepository, never()).findById(anyLong());
    }

    @Test
    @DisplayName("postLike - 존재하지 않는 댓글이면 NOT_EXIST_COMMENT 예외")
    void commentLikeCommentNotFound() {
        User user = mock(User.class);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(commentRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> likeService.postLike(1L, 999L, TargetType.COMMENT))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode())
                        .isEqualTo(ErrorCode.NOT_EXIST_COMMENT));

        verify(likeRepository, never()).save(any());
    }

    @Test
    @DisplayName("postLike - 이미 좋아요 했으면 ALREADY_EXIST_LIKE 예외")
    void postLikeAlreadyExist() {
        User user = mock(User.class);
        when(user.getId()).thenReturn(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(postRepository.findById(1L)).thenReturn(Optional.of(mock(Post.class)));
        when(likeRepository.existsByUser_IdAndTargetId(1L, 1L)).thenReturn(true);

        assertThatThrownBy(() -> likeService.postLike(1L, 1L, TargetType.POST))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode())
                        .isEqualTo(ErrorCode.ALREADY_EXIST_LIKE));

        verify(likeRepository, never()).save(any());
    }

    @Test
    @DisplayName("postUnLike - 좋아요 취소 성공")
    void postUnLikeSuccess() {
        User user = mock(User.class);
        when(user.getId()).thenReturn(1L);
        Like like = Like.builder()
                .user(user)
                .targetType(TargetType.POST)
                .targetId(1L)
                .build();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(postRepository.findById(1L)).thenReturn(Optional.of(mock(Post.class)));
        when(likeRepository.existsByUser_IdAndTargetId(1L, 1L)).thenReturn(true);
        when(likeRepository.findByTargetIdAndUser_Id(1L, 1L)).thenReturn(Optional.of(like));

        likeService.postUnLike(1L, 1L, TargetType.POST);

        verify(likeRepository).findByTargetIdAndUser_Id(1L, 1L);
        verify(likeRepository).delete(like);
    }

    @Test
    @DisplayName("postUnLike - 댓글 좋아요 취소 성공")
    void commentUnLikeSuccess() {
        User user = mock(User.class);
        when(user.getId()).thenReturn(1L);
        Like like = Like.builder()
                .user(user)
                .targetType(TargetType.COMMENT)
                .targetId(1L)
                .build();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(commentRepository.findById(1L)).thenReturn(Optional.of(mock(Comment.class)));
        when(likeRepository.existsByUser_IdAndTargetId(1L, 1L)).thenReturn(true);
        when(likeRepository.findByTargetIdAndUser_Id(1L, 1L)).thenReturn(Optional.of(like));

        likeService.postUnLike(1L, 1L, TargetType.COMMENT);

        verify(likeRepository).findByTargetIdAndUser_Id(1L, 1L);
        verify(likeRepository).delete(like);
        verify(commentRepository).findById(1L);
        verify(postRepository, never()).findById(anyLong());
    }

    @Test
    @DisplayName("postUnLike - 존재하지 않는 사용자면 USER_NOT_FOUND 예외")
    void postUnLikeUserNotFound() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> likeService.postUnLike(999L, 1L, TargetType.POST))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode())
                        .isEqualTo(ErrorCode.USER_NOT_FOUND));

        verify(likeRepository, never()).delete(any());
    }

    @Test
    @DisplayName("postUnLike - 존재하지 않는 게시글이면 NOT_EXIST_POST 예외")
    void postUnLikePostNotFound() {
        User user = mock(User.class);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(postRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> likeService.postUnLike(1L, 999L, TargetType.POST))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode())
                        .isEqualTo(ErrorCode.NOT_EXIST_POST));

        verify(likeRepository, never()).delete(any());
    }

    @Test
    @DisplayName("postUnLike - 존재하지 않는 댓글이면 NOT_EXIST_COMMENT 예외")
    void commentUnLikeCommentNotFound() {
        User user = mock(User.class);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(commentRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> likeService.postUnLike(1L, 999L, TargetType.COMMENT))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode())
                        .isEqualTo(ErrorCode.NOT_EXIST_COMMENT));

        verify(likeRepository, never()).delete(any());
    }

    @Test
    @DisplayName("postUnLike - 좋아요가 없으면 NOT_EXIST_LIKE 예외")
    void postUnLikeNotExistLike() {
        User user = mock(User.class);
        when(user.getId()).thenReturn(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(postRepository.findById(1L)).thenReturn(Optional.of(mock(Post.class)));
        when(likeRepository.existsByUser_IdAndTargetId(1L, 1L)).thenReturn(false);

        assertThatThrownBy(() -> likeService.postUnLike(1L, 1L, TargetType.POST))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode())
                        .isEqualTo(ErrorCode.NOT_EXIST_LIKE));

        verify(likeRepository, never()).findByTargetIdAndUser_Id(anyLong(), anyLong());
        verify(likeRepository, never()).delete(any());
    }
}
