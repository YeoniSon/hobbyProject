package com.example.service;

import com.example.common.exception.BusinessException;
import com.example.common.exception.ErrorCode;
import com.example.domain.Category;
import com.example.domain.Post;
import com.example.dto.request.post.PostEditRequest;
import com.example.dto.request.post.PostUploadRequest;
import com.example.dto.response.PostDataResponse;
import com.example.repository.CategoryRepository;
import com.example.repository.PostRepository;
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
@DisplayName("PostService")
class PostServiceTest {

    @Mock
    private PostRepository postRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private PostService postService;

    private static Category createCategory() {
        return Category.builder()
                .name("baseball")
                .build();
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

    @Test
    @DisplayName("uploadPost - 게시글 작성 성공")
    void uploadPostSuccess() {
        PostUploadRequest request = new PostUploadRequest(1L, 1L, "제목", "내용");
        Long categoryId = request.getCategoryId();
        Long userId = request.getUserId();

        Category category = createCategory();
        User user = createUser();
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        postService.uploadPost(request);

        ArgumentCaptor<Post> postCaptor = ArgumentCaptor.forClass(Post.class);
        verify(postRepository).save(postCaptor.capture());
        Post saved = postCaptor.getValue();
        assertThat(saved.getTitle()).isEqualTo("제목");
        assertThat(saved.getContent()).isEqualTo("내용");
        assertThat(saved.getCategoryId()).isEqualTo(category);
        assertThat(saved.getUserId()).isEqualTo(user);
        assertThat(saved.isShow()).isTrue();
    }

    @Test
    @DisplayName("uploadPost - 존재하지 않는 카테고리면 NOT_EXIST_CATEGORY 예외")
    void uploadPostCategoryNotFound() {
        PostUploadRequest request = new PostUploadRequest(999L, 1L, "제목", "내용");
        when(categoryRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> postService.uploadPost(request))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode())
                        .isEqualTo(ErrorCode.NOT_EXIST_CATEGORY));

        verify(postRepository, never()).save(any());
    }

    @Test
    @DisplayName("uploadPost - 존재하지 않는 사용자면 USER_NOT_FOUND 예외")
    void uploadPostUserNotFound() {
        PostUploadRequest request = new PostUploadRequest(1L, 999L, "제목", "내용");
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(createCategory()));
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> postService.uploadPost(request))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode())
                        .isEqualTo(ErrorCode.USER_NOT_FOUND));

        verify(postRepository, never()).save(any());
    }

    @Test
    @DisplayName("getPostsByUserId - 회원별 게시글 조회 성공")
    void getPostsByUserIdSuccess() {
        Long userId = 1L;
        User user = createUser();
        Post post = Post.builder()
                .categoryId(createCategory())
                .userId(user)
                .title("제목")
                .content("내용")
                .build();
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(postRepository.findAllByUserId(user)).thenReturn(List.of(post));

        List<PostDataResponse> result = postService.getPostsByUserId(userId);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).isEqualTo("제목");
        assertThat(result.get(0).getContent()).isEqualTo("내용");
        verify(userRepository).findById(userId);
        verify(postRepository).findAllByUserId(user);
    }

    @Test
    @DisplayName("getPostsByUserId - 존재하지 않는 사용자면 USER_NOT_FOUND 예외")
    void getPostsByUserIdUserNotFound() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> postService.getPostsByUserId(999L))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode())
                        .isEqualTo(ErrorCode.USER_NOT_FOUND));

        verify(postRepository, never()).findAllByUserId(any());
    }

    @Test
    @DisplayName("getPostById - 게시글 상세 조회 성공")
    void getPostByIdSuccess() {
        Post post = Post.builder()
                .categoryId(createCategory())
                .userId(createUser())
                .title("제목")
                .content("내용")
                .build();
        when(postRepository.findById(1L)).thenReturn(Optional.of(post));

        PostDataResponse result = postService.getPostById(1L);

        assertThat(result.getTitle()).isEqualTo("제목");
        assertThat(result.getContent()).isEqualTo("내용");
        verify(postRepository).findById(1L);
    }

    @Test
    @DisplayName("getPostById - 존재하지 않는 게시글이면 NOT_EXIST_POST 예외")
    void getPostByIdNotFound() {
        when(postRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> postService.getPostById(999L))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode())
                        .isEqualTo(ErrorCode.NOT_EXIST_POST));
    }

    @Test
    @DisplayName("getAllByShowPost - 공개 게시글 목록 반환")
    void getAllByShowPostSuccess() {
        Post post = Post.builder()
                .categoryId(createCategory())
                .userId(createUser())
                .title("제목")
                .content("내용")
                .build();
        when(postRepository.findAllByShowTrue()).thenReturn(List.of(post));

        List<PostDataResponse> result = postService.getAllByShowPost();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).isEqualTo("제목");
        verify(postRepository).findAllByShowTrue();
    }

    @Test
    @DisplayName("getAllByShowFalsePost - 비공개 게시글 목록 반환")
    void getAllByShowFalsePostSuccess() {
        Post post = Post.builder()
                .categoryId(createCategory())
                .userId(createUser())
                .title("비공개 제목")
                .content("내용")
                .build();
        post.deleteShow();
        when(postRepository.findAllByShowFalse()).thenReturn(List.of(post));

        List<PostDataResponse> result = postService.getAllByShowFalsePost();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).isEqualTo("비공개 제목");
        assertThat(result.get(0).isShow()).isFalse();
        verify(postRepository).findAllByShowFalse();
    }

    @Test
    @DisplayName("getAllByCategoryIdPost - 카테고리별 게시글 목록 반환")
    void getAllByCategoryIdPostSuccess() {
        Long categoryId = 1L;
        Post post = Post.builder()
                .categoryId(createCategory())
                .userId(createUser())
                .title("제목")
                .content("내용")
                .build();
        when(postRepository.findAllByCategoryId_Id(categoryId)).thenReturn(List.of(post));

        List<PostDataResponse> result = postService.getAllByCategoryIdPost(categoryId);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).isEqualTo("제목");
        verify(postRepository).findAllByCategoryId_Id(categoryId);
    }

    @Test
    @DisplayName("editPost - 제목만 수정 시 제목만 변경된다")
    void editPostOnlyTitle() {
        Post post = Post.builder()
                .categoryId(createCategory())
                .userId(createUser())
                .title("기존 제목")
                .content("기존 내용")
                .build();
        when(postRepository.findById(1L)).thenReturn(Optional.of(post));
        PostEditRequest request = new PostEditRequest(null, "새 제목", null);

        PostDataResponse result = postService.editPost(1L, request);

        assertThat(post.getTitle()).isEqualTo("새 제목");
        assertThat(post.getContent()).isEqualTo("기존 내용");
        assertThat(result.getTitle()).isEqualTo("새 제목");
        verify(postRepository).findById(1L);
        verify(categoryRepository, never()).findById(any());
    }

    @Test
    @DisplayName("editPost - 내용만 수정 시 내용만 변경된다")
    void editPostOnlyContent() {
        Post post = Post.builder()
                .categoryId(createCategory())
                .userId(createUser())
                .title("기존 제목")
                .content("기존 내용")
                .build();
        when(postRepository.findById(1L)).thenReturn(Optional.of(post));
        PostEditRequest request = new PostEditRequest(null, null, "새 내용");

        PostDataResponse result = postService.editPost(1L, request);

        assertThat(post.getTitle()).isEqualTo("기존 제목");
        assertThat(post.getContent()).isEqualTo("새 내용");
        assertThat(result.getContent()).isEqualTo("새 내용");
        verify(categoryRepository, never()).findById(any());
    }

    @Test
    @DisplayName("editPost - 카테고리만 수정 시 카테고리만 변경된다")
    void editPostOnlyCategoryId() {
        Category existingCategory = createCategory();
        Category newCategory = Category.builder().name("soccer").build();
        Post post = Post.builder()
                .categoryId(existingCategory)
                .userId(createUser())
                .title("제목")
                .content("내용")
                .build();
        when(postRepository.findById(1L)).thenReturn(Optional.of(post));
        when(categoryRepository.findById(2L)).thenReturn(Optional.of(newCategory));
        PostEditRequest request = new PostEditRequest(2L, null, null);

        postService.editPost(1L, request);

        assertThat(post.getCategoryId()).isEqualTo(newCategory);
        assertThat(post.getTitle()).isEqualTo("제목");
        verify(categoryRepository).findById(2L);
    }

    @Test
    @DisplayName("editPost - 여러 필드 동시 수정")
    void editPostMultipleFields() {
        Category newCategory = Category.builder().name("soccer").build();
        Post post = Post.builder()
                .categoryId(createCategory())
                .userId(createUser())
                .title("기존")
                .content("기존내용")
                .build();
        when(postRepository.findById(1L)).thenReturn(Optional.of(post));
        when(categoryRepository.findById(2L)).thenReturn(Optional.of(newCategory));
        PostEditRequest request = new PostEditRequest(2L, "새 제목", "새 내용");

        PostDataResponse result = postService.editPost(1L, request);

        assertThat(post.getCategoryId()).isEqualTo(newCategory);
        assertThat(post.getTitle()).isEqualTo("새 제목");
        assertThat(post.getContent()).isEqualTo("새 내용");
        assertThat(result.getTitle()).isEqualTo("새 제목");
        assertThat(result.getContent()).isEqualTo("새 내용");
    }

    @Test
    @DisplayName("editPost - 입력 안 한 필드는 기존 값 유지 (빈 문자열은 무시)")
    void editPostEmptyStringNotChanged() {
        Post post = Post.builder()
                .categoryId(createCategory())
                .userId(createUser())
                .title("기존 제목")
                .content("기존 내용")
                .build();
        when(postRepository.findById(1L)).thenReturn(Optional.of(post));
        PostEditRequest request = new PostEditRequest(null, "  ", "");

        postService.editPost(1L, request);

        assertThat(post.getTitle()).isEqualTo("기존 제목");
        assertThat(post.getContent()).isEqualTo("기존 내용");
        verify(categoryRepository, never()).findById(any());
    }

    @Test
    @DisplayName("editPost - 존재하지 않는 게시글이면 NOT_EXIST_POST 예외")
    void editPostNotFound() {
        when(postRepository.findById(999L)).thenReturn(Optional.empty());
        PostEditRequest request = new PostEditRequest(null, "제목", null);

        assertThatThrownBy(() -> postService.editPost(999L, request))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode())
                        .isEqualTo(ErrorCode.NOT_EXIST_POST));

        verify(categoryRepository, never()).findById(any());
    }

    @Test
    @DisplayName("editPost - 존재하지 않는 카테고리로 변경 시 NOT_EXIST_CATEGORY 예외")
    void editPostCategoryNotFound() {
        Post post = Post.builder()
                .categoryId(createCategory())
                .userId(createUser())
                .title("제목")
                .content("내용")
                .build();
        when(postRepository.findById(1L)).thenReturn(Optional.of(post));
        when(categoryRepository.findById(999L)).thenReturn(Optional.empty());
        PostEditRequest request = new PostEditRequest(999L, null, null);

        assertThatThrownBy(() -> postService.editPost(1L, request))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode())
                        .isEqualTo(ErrorCode.NOT_EXIST_CATEGORY));
    }

    @Test
    @DisplayName("deletePost - 게시글 삭제 성공")
    void deletePostSuccess() {
        Post post = Post.builder()
                .categoryId(createCategory())
                .userId(createUser())
                .title("제목")
                .content("내용")
                .build();
        when(postRepository.findById(1L)).thenReturn(Optional.of(post));

        postService.deletePost(1L);

        verify(postRepository).findById(1L);
        verify(postRepository).delete(post);
    }

    @Test
    @DisplayName("deletePost - 존재하지 않는 게시글이면 NOT_EXIST_POST 예외")
    void deletePostNotFound() {
        when(postRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> postService.deletePost(999L))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode())
                        .isEqualTo(ErrorCode.NOT_EXIST_POST));

        verify(postRepository, never()).delete(any());
    }

    @Test
    @DisplayName("privatePost - 게시글 비공개 처리 성공")
    void privatePostSuccess() {
        Post post = Post.builder()
                .categoryId(createCategory())
                .userId(createUser())
                .title("제목")
                .content("내용")
                .build();
        when(postRepository.findById(1L)).thenReturn(Optional.of(post));

        postService.privatePost(1L);

        assertThat(post.isShow()).isFalse();
        verify(postRepository).findById(1L);
    }

    @Test
    @DisplayName("privatePost - 존재하지 않는 게시글이면 NOT_EXIST_POST 예외")
    void privatePostNotFound() {
        when(postRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> postService.privatePost(999L))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode())
                        .isEqualTo(ErrorCode.NOT_EXIST_POST));
    }

    @Test
    @DisplayName("releasePost - 게시글 공개 복구 성공")
    void releasePostSuccess() {
        Post post = Post.builder()
                .categoryId(createCategory())
                .userId(createUser())
                .title("제목")
                .content("내용")
                .build();
        post.deleteShow();
        when(postRepository.findById(1L)).thenReturn(Optional.of(post));

        postService.releasePost(1L);

        assertThat(post.isShow()).isTrue();
        verify(postRepository).findById(1L);
    }

    @Test
    @DisplayName("releasePost - 존재하지 않는 게시글이면 NOT_EXIST_POST 예외")
    void releasePostNotFound() {
        when(postRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> postService.releasePost(999L))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode())
                        .isEqualTo(ErrorCode.NOT_EXIST_POST));
    }
}
