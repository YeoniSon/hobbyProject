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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PostService {
    /*
    게시판 구현
    - 게시글 작성
    - 게시글 조회 (회원별 조회, 전체, 비공개 처리된 것들, 공개되어있는 것들)
    - 게시글 수정
    - 게시글 삭제
    - 게시글 공개/ 비공개 (관리자 -> 신고20개 이상일 경우)
     */

    private final PostRepository postRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;

    // 게시글 작성
    @Transactional
    public void uploadPost(PostUploadRequest request) {
        Post post = createPost(request);
        postRepository.save(post);
    }

    private Post createPost(PostUploadRequest request) {
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_EXIST_CATEGORY));
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        return Post.builder()
                .categoryId(category)
                .userId(user)
                .title(request.getTitle())
                .content(request.getContent())
                .build();
    }

    // 게시글 조회(회원별) - userId(Long)로 조회
    @Transactional(readOnly = true)
    public List<PostDataResponse> getPostsByUserId(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        return postRepository.findAllByUserId(user)
                .stream()
                .map(PostDataResponse::from)
                .toList();
    }

    // 게시글 조회(카테고리별)
    @Transactional
    public List<PostDataResponse> getAllByCategoryIdPost(Long categoryId) {
        return postRepository.findAllByCategoryId(categoryId)
                .stream()
                .map(PostDataResponse::from)
                .toList();
    }

    // 게시글 조회(공개)
    @Transactional
    public List<PostDataResponse> getAllByShowPost() {
        return postRepository.findAllByShowTrue()
                .stream()
                .map(PostDataResponse::from)
                .toList();
    }

    // 게시글 조회(삭제)
    @Transactional
    public List<PostDataResponse> getAllByShowFalsePost() {
        return postRepository.findAllByShowFalse()
                .stream()
                .map(PostDataResponse::from)
                .toList();
    }

    // 게시글 상세 정보 조회
    @Transactional
    public PostDataResponse getPostById(Long id) {
        return PostDataResponse.from(
                postRepository.findById(id)
                        .orElseThrow(() -> new BusinessException(ErrorCode.NOT_EXIST_POST)));
    }

    // 게시글 수정 - 입력된 필드만 변경, 입력 안 한 필드는 기존 값 유지
    @Transactional
    public PostDataResponse editPost(Long postId, PostEditRequest request) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_EXIST_POST));

        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.NOT_EXIST_CATEGORY));
            post.updateCategory(category);
        }
        if (request.getTitle() != null && !request.getTitle().isBlank()) {
            post.updateTitle(request.getTitle());
        }
        if (request.getContent() != null && !request.getContent().isBlank()) {
            post.updateContent(request.getContent());
        }

        return PostDataResponse.from(post);
    }
}
