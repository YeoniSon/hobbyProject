package com.example.service;

import com.example.board.service.CommentService;
import com.example.common.exception.BusinessException;
import com.example.common.exception.ErrorCode;
import com.example.board.domain.Category;
import com.example.board.domain.Comment;
import com.example.board.domain.Post;
import com.example.board.dto.request.comment.CommentEditRequest;
import com.example.board.dto.request.comment.CommentUploadRequest;
import com.example.board.dto.response.CommentResponse;
import com.example.board.repository.CommentRepository;
import com.example.board.repository.PostRepository;
import com.example.user.domain.User;
import com.example.user.repository.UserRepository;
import com.example.common.enums.Role;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CommentService")
class CommentServiceTest {

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private PostRepository postRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CommentService commentService;

    private static Category createCategory() {
        return Category.builder().name("baseball").build();
    }

    private static User createUser() {
        return User.builder()
                .email("test@test.com")
                .name("테스트")
                .password("password")
                .nickname("nick")
                .phone("01012345678")
                .birth(LocalDate.of(1990, 1, 1))
                .role(Role.USER)
                .build();
    }

    private static Post createPost() {
        return Post.builder()
                .categoryId(createCategory())
                .userId(createUser())
                .title("제목")
                .content("내용")
                .build();
    }

    @Test
    @DisplayName("uploadComment - 댓글 작성 성공")
    void uploadCommentSuccess() {
        CommentUploadRequest request = new CommentUploadRequest(1L, 1L, "댓글 내용");
        Post post = createPost();
        User user = createUser();
        when(commentRepository.existsByPostId(1L)).thenReturn(false);
        when(commentRepository.existsByUserId(1L)).thenReturn(false);
        when(postRepository.findById(1L)).thenReturn(Optional.of(post));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        commentService.uploadComment(request);

        ArgumentCaptor<Comment> captor = ArgumentCaptor.forClass(Comment.class);
        verify(commentRepository).save(captor.capture());
        Comment saved = captor.getValue();
        assertThat(saved.getContent()).isEqualTo("댓글 내용");
        assertThat(saved.getPost()).isEqualTo(post);
        assertThat(saved.getUser()).isEqualTo(user);
        assertThat(saved.isShow()).isTrue();
    }

    @Test
    @DisplayName("uploadComment - 존재하지 않는 게시글이면 NOT_EXIST_POST 예외 (createComment)")
    void uploadCommentPostNotFound() {
        CommentUploadRequest request = new CommentUploadRequest(999L, 1L, "댓글");
        when(commentRepository.existsByPostId(999L)).thenReturn(false);
        when(commentRepository.existsByUserId(1L)).thenReturn(false);
        when(postRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> commentService.uploadComment(request))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode())
                        .isEqualTo(ErrorCode.NOT_EXIST_POST));

        verify(commentRepository, never()).save(any());
    }

    @Test
    @DisplayName("uploadComment - 존재하지 않는 사용자면 USER_NOT_FOUND 예외 (createComment)")
    void uploadCommentUserNotFound() {
        CommentUploadRequest request = new CommentUploadRequest(1L, 999L, "댓글");
        when(commentRepository.existsByPostId(1L)).thenReturn(false);
        when(commentRepository.existsByUserId(999L)).thenReturn(false);
        when(postRepository.findById(1L)).thenReturn(Optional.of(createPost()));
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> commentService.uploadComment(request))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode())
                        .isEqualTo(ErrorCode.USER_NOT_FOUND));

        verify(commentRepository, never()).save(any());
    }

    @Test
    @DisplayName("getAllCommentByWriterId - 작성자별 댓글 조회 성공")
    void getAllCommentByWriterIdSuccess() {
        User user = mock(User.class);
        when(user.getId()).thenReturn(1L);
        Comment comment = Comment.builder()
                .post(createPost())
                .user(user)
                .content("댓글")
                .build();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(commentRepository.findAllByUserId(1L)).thenReturn(List.of(comment));

        List<CommentResponse> result = commentService.getAllCommentByWriterId(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getContent()).isEqualTo("댓글");
        verify(commentRepository).findAllByUserId(1L);
    }

    @Test
    @DisplayName("getAllCommentByWriterId - 존재하지 않는 사용자면 USER_NOT_FOUND 예외")
    void getAllCommentByWriterIdUserNotFound() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> commentService.getAllCommentByWriterId(999L))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode())
                        .isEqualTo(ErrorCode.USER_NOT_FOUND));

        verify(commentRepository, never()).findAllByUserId(anyLong());
    }

    @Test
    @DisplayName("getAllComment - 전체 댓글 목록 반환")
    void getAllCommentSuccess() {
        Comment comment = Comment.builder()
                .post(createPost())
                .user(createUser())
                .content("댓글")
                .build();
        when(commentRepository.findAll()).thenReturn(List.of(comment));

        List<CommentResponse> result = commentService.getAllComment();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getContent()).isEqualTo("댓글");
        verify(commentRepository).findAll();
    }

    @Test
    @DisplayName("getAllCommentByPostId - 게시글별 댓글 목록 반환")
    void getAllCommentByPostIdSuccess() {
        Post post = mock(Post.class);
        when(post.getId()).thenReturn(1L);
        Comment comment = Comment.builder()
                .post(post)
                .user(createUser())
                .content("댓글")
                .build();
        when(postRepository.findById(1L)).thenReturn(Optional.of(post));
        when(commentRepository.findAllByPostId(1L)).thenReturn(List.of(comment));

        List<CommentResponse> result = commentService.getAllCommentByPostId(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getContent()).isEqualTo("댓글");
        verify(commentRepository).findAllByPostId(1L);
    }

    @Test
    @DisplayName("getAllCommentByPostId - 존재하지 않는 게시글이면 NOT_EXIST_POST 예외")
    void getAllCommentByPostIdPostNotFound() {
        when(postRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> commentService.getAllCommentByPostId(999L))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode())
                        .isEqualTo(ErrorCode.NOT_EXIST_POST));

        verify(commentRepository, never()).findAllByPostId(anyLong());
    }

    @Test
    @DisplayName("getAllCommentByWriterIdAndPostId - 작성자·게시글별 댓글 조회 성공")
    void getAllCommentByWriterIdAndPostIdSuccess() {
        Post post = mock(Post.class);
        when(post.getId()).thenReturn(1L);
        User user = mock(User.class);
        when(user.getId()).thenReturn(1L);
        Comment comment = Comment.builder().post(post).user(user).content("댓글").build();
        when(postRepository.findById(1L)).thenReturn(Optional.of(post));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(commentRepository.findAllByPostIdAndUserId(1L, 1L)).thenReturn(List.of(comment));

        List<CommentResponse> result = commentService.getAllCommentByWriterIdAndPostId(1L, 1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getContent()).isEqualTo("댓글");
        verify(commentRepository).findAllByPostIdAndUserId(1L, 1L);
    }

    @Test
    @DisplayName("getShowComment - 공개 댓글 목록 반환")
    void getShowCommentSuccess() {
        Comment comment = Comment.builder()
                .post(createPost())
                .user(createUser())
                .content("공개 댓글")
                .build();
        when(commentRepository.findAllByShowTrue()).thenReturn(List.of(comment));

        List<CommentResponse> result = commentService.getShowComment();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getContent()).isEqualTo("공개 댓글");
        verify(commentRepository).findAllByShowTrue();
    }

    @Test
    @DisplayName("getPrivateComment - 비공개 댓글 목록 반환")
    void getPrivateCommentSuccess() {
        Comment comment = Comment.builder()
                .post(createPost())
                .user(createUser())
                .content("비공개 댓글")
                .build();
        comment.deleteShow();
        when(commentRepository.findAllByShowFalse()).thenReturn(List.of(comment));

        List<CommentResponse> result = commentService.getPrivateComment();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getContent()).isEqualTo("비공개 댓글");
        assertThat(result.get(0).isShow()).isFalse();
        verify(commentRepository).findAllByShowFalse();
    }

    @Test
    @DisplayName("editComment - 댓글 수정 성공")
    void editCommentSuccess() {
        Comment comment = Comment.builder()
                .post(createPost())
                .user(createUser())
                .content("기존 내용")
                .build();
        when(commentRepository.findById(1L)).thenReturn(Optional.of(comment));
        CommentEditRequest request = new CommentEditRequest("새 내용");

        CommentResponse result = commentService.editComment(1L, request);

        assertThat(comment.getContent()).isEqualTo("새 내용");
        assertThat(result.getContent()).isEqualTo("새 내용");
        verify(commentRepository).findById(1L);
    }

    @Test
    @DisplayName("editComment - 수정 내용이 없으면 NO_CHANGE 예외")
    void editCommentNoChange() {
        Comment comment = Comment.builder()
                .post(createPost())
                .user(createUser())
                .content("내용")
                .build();
        when(commentRepository.findById(1L)).thenReturn(Optional.of(comment));
        CommentEditRequest request = new CommentEditRequest("내용");

        assertThatThrownBy(() -> commentService.editComment(1L, request))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode())
                        .isEqualTo(ErrorCode.NO_CHANGE));
    }

    @Test
    @DisplayName("editComment - 빈 문자열만 있으면 NO_CHANGE 예외")
    void editCommentBlankOnly() {
        Comment comment = Comment.builder()
                .post(createPost())
                .user(createUser())
                .content("내용")
                .build();
        when(commentRepository.findById(1L)).thenReturn(Optional.of(comment));
        CommentEditRequest request = new CommentEditRequest("  ");

        assertThatThrownBy(() -> commentService.editComment(1L, request))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode())
                        .isEqualTo(ErrorCode.NO_CHANGE));
    }

    @Test
    @DisplayName("editComment - 존재하지 않는 댓글이면 NOT_EXIST_COMMENT 예외")
    void editCommentNotFound() {
        when(commentRepository.findById(999L)).thenReturn(Optional.empty());
        CommentEditRequest request = new CommentEditRequest("내용");

        assertThatThrownBy(() -> commentService.editComment(999L, request))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode())
                        .isEqualTo(ErrorCode.NOT_EXIST_COMMENT));
    }

    @Test
    @DisplayName("deleteComment - 본인 댓글 삭제 성공")
    void deleteCommentSuccess() {
        User writer = mock(User.class);
        when(writer.getId()).thenReturn(1L);
        Comment comment = Comment.builder()
                .post(createPost())
                .user(writer)
                .content("댓글")
                .build();
        when(commentRepository.findById(1L)).thenReturn(Optional.of(comment));

        commentService.deleteComment(1L, 1L);

        verify(commentRepository).findById(1L);
        verify(commentRepository).delete(comment);
    }

    @Test
    @DisplayName("deleteComment - 존재하지 않는 댓글이면 NOT_EXIST_COMMENT 예외")
    void deleteCommentNotFound() {
        when(commentRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> commentService.deleteComment(1L, 999L))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode())
                        .isEqualTo(ErrorCode.NOT_EXIST_COMMENT));

        verify(commentRepository, never()).delete(any());
    }

    @Test
    @DisplayName("deleteComment - 작성자가 아니면 NOT_MATCH_WRITER 예외")
    void deleteCommentNotMatchWriter() {
        User writer = mock(User.class);
        when(writer.getId()).thenReturn(1L);
        Comment comment = Comment.builder()
                .post(createPost())
                .user(writer)
                .content("댓글")
                .build();
        when(commentRepository.findById(1L)).thenReturn(Optional.of(comment));

        assertThatThrownBy(() -> commentService.deleteComment(999L, 1L))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode())
                        .isEqualTo(ErrorCode.NOT_MATCH_WRITER));

        verify(commentRepository, never()).delete(any());
    }

    @Test
    @DisplayName("getAllCommentByWriterIdAndPostId - 존재하지 않는 게시글이면 NOT_EXIST_POST 예외")
    void getAllCommentByWriterIdAndPostIdPostNotFound() {
        when(postRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> commentService.getAllCommentByWriterIdAndPostId(1L, 999L))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode())
                        .isEqualTo(ErrorCode.NOT_EXIST_POST));

        verify(userRepository, never()).findById(anyLong());
        verify(commentRepository, never()).findAllByPostIdAndUserId(anyLong(), anyLong());
    }

    @Test
    @DisplayName("getAllCommentByWriterIdAndPostId - 존재하지 않는 사용자면 USER_NOT_FOUND 예외")
    void getAllCommentByWriterIdAndPostIdUserNotFound() {
        when(postRepository.findById(1L)).thenReturn(Optional.of(createPost()));
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> commentService.getAllCommentByWriterIdAndPostId(999L, 1L))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode())
                        .isEqualTo(ErrorCode.USER_NOT_FOUND));

        verify(commentRepository, never()).findAllByPostIdAndUserId(anyLong(), anyLong());
    }

    @Test
    @DisplayName("privateComment - 댓글 비공개 처리 성공")
    void privateCommentSuccess() {
        Comment comment = Comment.builder()
                .post(createPost())
                .user(createUser())
                .content("댓글")
                .build();
        when(commentRepository.findById(1L)).thenReturn(Optional.of(comment));

        commentService.privateComment(1L);

        assertThat(comment.isShow()).isFalse();
        verify(commentRepository).findById(1L);
    }

    @Test
    @DisplayName("privateComment - 존재하지 않는 댓글이면 NOT_EXIST_COMMENT 예외")
    void privateCommentNotFound() {
        when(commentRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> commentService.privateComment(999L))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode())
                        .isEqualTo(ErrorCode.NOT_EXIST_COMMENT));
    }

    @Test
    @DisplayName("showComment - 댓글 공개 복구 성공")
    void showCommentSuccess() {
        Comment comment = Comment.builder()
                .post(createPost())
                .user(createUser())
                .content("댓글")
                .build();
        comment.deleteShow();
        when(commentRepository.findById(1L)).thenReturn(Optional.of(comment));

        commentService.showComment(1L);

        assertThat(comment.isShow()).isTrue();
        verify(commentRepository).findById(1L);
    }

    @Test
    @DisplayName("showComment - 존재하지 않는 댓글이면 NOT_EXIST_COMMENT 예외")
    void showCommentNotFound() {
        when(commentRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> commentService.showComment(999L))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode())
                        .isEqualTo(ErrorCode.NOT_EXIST_COMMENT));
    }
}
