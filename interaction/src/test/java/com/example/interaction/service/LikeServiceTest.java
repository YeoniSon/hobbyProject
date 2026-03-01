package com.example.interaction.service;

import com.example.common.enums.TargetType;
import com.example.common.exception.BusinessException;
import com.example.common.exception.ErrorCode;
import com.example.board.domain.Comment;
import com.example.board.domain.Post;
import com.example.interaction.domain.Like;
import com.example.interaction.dto.response.LikeDataResponse;
import com.example.interaction.repository.LikeRepository;
import com.example.board.repository.CommentRepository;
import com.example.board.repository.PostRepository;
import com.example.user.domain.User;
import com.example.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
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
    void setLikeSuccess() {
        User user = mock(User.class);
        when(user.getId()).thenReturn(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(postRepository.findById(1L)).thenReturn(Optional.of(mock(Post.class)));
        when(likeRepository.existsByTargetTypeAndUser_IdAndTargetId(TargetType.POST, 1L, 1L)).thenReturn(false);

        likeService.setLike(1L, 1L, TargetType.POST);

        ArgumentCaptor<Like> captor = ArgumentCaptor.forClass(Like.class);
        verify(likeRepository).save(captor.capture());
        Like saved = captor.getValue();
        assertThat(saved.getTargetType()).isEqualTo(TargetType.POST);
        assertThat(saved.getTargetId()).isEqualTo(1L);
        assertThat(saved.getUser()).isEqualTo(user);
        verify(likeRepository).existsByTargetTypeAndUser_IdAndTargetId(TargetType.POST, 1L, 1L);
    }

    @Test
    @DisplayName("postLike - 존재하지 않는 사용자면 USER_NOT_FOUND 예외")
    void setLikeUserNotFound() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> likeService.setLike(999L, 1L, TargetType.POST))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode())
                        .isEqualTo(ErrorCode.USER_NOT_FOUND));

        verify(postRepository, never()).findById(anyLong());
        verify(likeRepository, never()).save(any());
    }

    @Test
    @DisplayName("postLike - 존재하지 않는 게시글이면 NOT_EXIST_POST 예외")
    void postLikeSetNotFound() {
        User user = mock(User.class);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(postRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> likeService.setLike(1L, 999L, TargetType.POST))
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
        when(likeRepository.existsByTargetTypeAndUser_IdAndTargetId(TargetType.COMMENT, 1L, 1L)).thenReturn(false);

        likeService.setLike(1L, 1L, TargetType.COMMENT);

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

        assertThatThrownBy(() -> likeService.setLike(1L, 999L, TargetType.COMMENT))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode())
                        .isEqualTo(ErrorCode.NOT_EXIST_COMMENT));

        verify(likeRepository, never()).save(any());
    }

    @Test
    @DisplayName("postLike - 이미 좋아요 했으면 ALREADY_EXIST_LIKE 예외")
    void setLikeAlreadyExist() {
        User user = mock(User.class);
        when(user.getId()).thenReturn(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(postRepository.findById(1L)).thenReturn(Optional.of(mock(Post.class)));
        when(likeRepository.existsByTargetTypeAndUser_IdAndTargetId(TargetType.POST, 1L, 1L)).thenReturn(true);

        assertThatThrownBy(() -> likeService.setLike(1L, 1L, TargetType.POST))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode())
                        .isEqualTo(ErrorCode.ALREADY_EXIST_LIKE));

        verify(likeRepository, never()).save(any());
    }

    @Test
    @DisplayName("postUnLike - 좋아요 취소 성공")
    void setUnLikeSuccess() {
        User user = mock(User.class);
        when(user.getId()).thenReturn(1L);
        Like like = Like.builder()
                .user(user)
                .targetType(TargetType.POST)
                .targetId(1L)
                .build();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(postRepository.findById(1L)).thenReturn(Optional.of(mock(Post.class)));
        when(likeRepository.existsByTargetTypeAndUser_IdAndTargetId(TargetType.POST, 1L, 1L)).thenReturn(true);
        when(likeRepository.findByTargetTypeAndTargetIdAndUser_Id(TargetType.POST, 1L, 1L)).thenReturn(Optional.of(like));

        likeService.setUnLike(1L, 1L, TargetType.POST);

        verify(likeRepository).findByTargetTypeAndTargetIdAndUser_Id(TargetType.POST, 1L, 1L);
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
        when(likeRepository.existsByTargetTypeAndUser_IdAndTargetId(TargetType.COMMENT, 1L, 1L)).thenReturn(true);
        when(likeRepository.findByTargetTypeAndTargetIdAndUser_Id(TargetType.COMMENT, 1L, 1L)).thenReturn(Optional.of(like));

        likeService.setUnLike(1L, 1L, TargetType.COMMENT);

        verify(likeRepository).findByTargetTypeAndTargetIdAndUser_Id(TargetType.COMMENT, 1L, 1L);
        verify(likeRepository).delete(like);
        verify(commentRepository).findById(1L);
        verify(postRepository, never()).findById(anyLong());
    }

    @Test
    @DisplayName("postUnLike - 존재하지 않는 사용자면 USER_NOT_FOUND 예외")
    void setUnLikeUserNotFound() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> likeService.setUnLike(999L, 1L, TargetType.POST))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode())
                        .isEqualTo(ErrorCode.USER_NOT_FOUND));

        verify(likeRepository, never()).delete(any());
    }

    @Test
    @DisplayName("postUnLike - 존재하지 않는 게시글이면 NOT_EXIST_POST 예외")
    void postUnLikeSetNotFound() {
        User user = mock(User.class);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(postRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> likeService.setUnLike(1L, 999L, TargetType.POST))
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

        assertThatThrownBy(() -> likeService.setUnLike(1L, 999L, TargetType.COMMENT))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode())
                        .isEqualTo(ErrorCode.NOT_EXIST_COMMENT));

        verify(likeRepository, never()).delete(any());
    }

    @Test
    @DisplayName("postUnLike - 좋아요가 없으면 NOT_EXIST_LIKE 예외")
    void setUnLikeNotExistLike() {
        User user = mock(User.class);
        when(user.getId()).thenReturn(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(postRepository.findById(1L)).thenReturn(Optional.of(mock(Post.class)));
        when(likeRepository.existsByTargetTypeAndUser_IdAndTargetId(TargetType.POST, 1L, 1L)).thenReturn(false);

        assertThatThrownBy(() -> likeService.setUnLike(1L, 1L, TargetType.POST))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode())
                        .isEqualTo(ErrorCode.NOT_EXIST_LIKE));

        verify(likeRepository, never()).findByTargetTypeAndTargetIdAndUser_Id(any(), anyLong(), anyLong());
        verify(likeRepository, never()).delete(any());
    }

    @Test
    @DisplayName("countLike - 게시글 좋아요 수 조회 성공")
    void countPostLikeSuccess() {
        when(postRepository.findById(1L)).thenReturn(Optional.of(mock(Post.class)));
        when(likeRepository.countByTargetTypeAndTargetId(TargetType.POST, 1L)).thenReturn(3);

        var response = likeService.countLike(1L, TargetType.POST);

        assertThat(response.getCount()).isEqualTo(3);
        verify(likeRepository).countByTargetTypeAndTargetId(TargetType.POST, 1L);
    }

    @Test
    @DisplayName("countLike - 존재하지 않는 댓글이면 NOT_EXIST_COMMENT 예외")
    void countCommentLikeNotFound() {
        when(commentRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> likeService.countLike(999L, TargetType.COMMENT))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode())
                        .isEqualTo(ErrorCode.NOT_EXIST_COMMENT));

        verify(likeRepository, never()).countByTargetTypeAndTargetId(any(), anyLong());
    }

    @Test
    @DisplayName("allLikeView - 사용자가 좋아요한 게시글 목록 조회 성공")
    void allLikeViewPostSuccess() {
        User user = mock(User.class);

        Like like1 = Like.builder()
                .user(user)
                .targetType(TargetType.POST)
                .targetId(1L)
                .build();
        Like like2 = Like.builder()
                .user(user)
                .targetType(TargetType.POST)
                .targetId(2L)
                .build();

        when(likeRepository.findAllByUser_IdAndTargetType(1L, TargetType.POST))
                .thenReturn(List.of(like1, like2));

        Post post1 = mock(Post.class);
        when(post1.getId()).thenReturn(1L);
        when(post1.getTitle()).thenReturn("title1");
        Post post2 = mock(Post.class);
        when(post2.getId()).thenReturn(2L);
        when(post2.getTitle()).thenReturn("title2");

        when(postRepository.findById(1L)).thenReturn(Optional.of(post1));
        when(postRepository.findById(2L)).thenReturn(Optional.of(post2));

        List<LikeDataResponse> result = likeService.allLikeView(1L, TargetType.POST);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getTargetId()).isEqualTo(1L);
        assertThat(result.get(0).getTargetType()).isEqualTo(TargetType.POST);
        assertThat(result.get(0).getTitle()).isEqualTo("title1");
        assertThat(result.get(1).getTargetId()).isEqualTo(2L);
        assertThat(result.get(1).getTargetType()).isEqualTo(TargetType.POST);
        assertThat(result.get(1).getTitle()).isEqualTo("title2");

        verify(likeRepository).findAllByUser_IdAndTargetType(1L, TargetType.POST);
        verify(postRepository).findById(1L);
        verify(postRepository).findById(2L);
        verify(commentRepository, never()).findById(anyLong());
    }

    @Test
    @DisplayName("allLikeView - 사용자가 좋아요한 댓글 목록 조회 성공")
    void allLikeViewCommentSuccess() {
        User user = mock(User.class);

        Like like1 = Like.builder()
                .user(user)
                .targetType(TargetType.COMMENT)
                .targetId(1L)
                .build();

        when(likeRepository.findAllByUser_IdAndTargetType(1L, TargetType.COMMENT))
                .thenReturn(List.of(like1));

        Comment comment = mock(Comment.class);
        when(comment.getId()).thenReturn(1L);
        when(comment.getContent()).thenReturn("content1");
        when(commentRepository.findById(1L)).thenReturn(Optional.of(comment));

        List<LikeDataResponse> result = likeService.allLikeView(1L, TargetType.COMMENT);

        assertThat(result).hasSize(1);
        LikeDataResponse data = result.get(0);
        assertThat(data.getTargetId()).isEqualTo(1L);
        assertThat(data.getTargetType()).isEqualTo(TargetType.COMMENT);
        assertThat(data.getContent()).isEqualTo("content1");

        verify(likeRepository).findAllByUser_IdAndTargetType(1L, TargetType.COMMENT);
        verify(commentRepository).findById(1L);
        verify(postRepository, never()).findById(anyLong());
    }

    @Test
    @DisplayName("allLikeView - 게시글이 존재하지 않으면 NOT_EXIST_POST 예외")
    void allLikeViewPostNotFound() {
        User user = mock(User.class);

        Like like = Like.builder()
                .user(user)
                .targetType(TargetType.POST)
                .targetId(1L)
                .build();

        when(likeRepository.findAllByUser_IdAndTargetType(1L, TargetType.POST))
                .thenReturn(List.of(like));
        when(postRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> likeService.allLikeView(1L, TargetType.POST))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode())
                        .isEqualTo(ErrorCode.NOT_EXIST_POST));

        verify(commentRepository, never()).findById(anyLong());
    }

    @Test
    @DisplayName("allLikeView - 댓글이 존재하지 않으면 NOT_EXIST_COMMENT 예외")
    void allLikeViewCommentNotFound() {
        User user = mock(User.class);

        Like like = Like.builder()
                .user(user)
                .targetType(TargetType.COMMENT)
                .targetId(1L)
                .build();

        when(likeRepository.findAllByUser_IdAndTargetType(1L, TargetType.COMMENT))
                .thenReturn(List.of(like));
        when(commentRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> likeService.allLikeView(1L, TargetType.COMMENT))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode())
                        .isEqualTo(ErrorCode.NOT_EXIST_COMMENT));

        verify(postRepository, never()).findById(anyLong());
    }
}
