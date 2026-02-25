package com.example.service;

import com.example.common.exception.BusinessException;
import com.example.common.exception.ErrorCode;
import com.example.domain.Comment;
import com.example.domain.Post;
import com.example.dto.request.comment.CommentUploadRequest;
import com.example.dto.response.CommentResponse;
import com.example.repository.CommentRepository;
import com.example.repository.PostRepository;
import com.example.user.domain.User;
import com.example.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    /*
    - 댓글 작성 (회원 / 관리자)
    - 댓글 수정 (회원 / 관리자)
    - 댓글 조회 (회원/관리자)
    - 댓글 삭제 (회원)
    - 댓글 숨김 (관리자 -> 신고 20개 이상일 경우)
     */
    // 댓글 작성
    @Transactional
    public void uploadComment(CommentUploadRequest request) {
        validateComment(request.getPostId(), request.getWriterId());

        Comment comment = createComment(request);
        commentRepository.save(comment);
    }

    private void validateComment(Long postId, Long userId) {
        if (commentRepository.existsByPostId(postId)) {
            throw new BusinessException(ErrorCode.NOT_EXIST_POST);
        }

        if (commentRepository.existsByUserId(userId)){
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }
    }

    private Comment createComment(CommentUploadRequest request) {
        Post post= postRepository.findById(request.getPostId())
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_EXIST_POST));
        User user = userRepository.findById(request.getWriterId())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        return Comment.builder()
                .post(post)
                .user(user)
                .content(request.getContent())
                .build();
    }

    // 댓글 조회(회원)
    @Transactional
    public List<CommentResponse> getAllCommentByWriterId(Long writerId) {
        User user = userRepository.findById(writerId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        return commentRepository.findAllByUserId(user.getId())
                .stream()
                .map(CommentResponse::from)
                .toList();
    }

    // 댓글 조회 회원 아이디, 게시글 별(회원)
    @Transactional
    public List<CommentResponse> getAllCommentByWriterIdAndPostId(Long writerId, Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_EXIST_POST));
        User user = userRepository.findById(writerId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        return commentRepository.findAllByPostIdAndUserId(post.getId(), user.getId())
                .stream()
                .map(CommentResponse::from)
                .toList();

    }

    // 댓글 전체 조회(관리자)
    @Transactional
    public List<CommentResponse> getAllComment() {
        return commentRepository.findAll()
                .stream()
                .map(CommentResponse::from)
                .toList();
    }

    // 댓글 조회(게시글 별)
    @Transactional
    public List<CommentResponse> getAllCommentByPostId(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_EXIST_POST));

        return commentRepository.findAllByPostId(post.getId())
                .stream()
                .map(CommentResponse::from)
                .toList();
    }

    // 댓글 조회(비공개)
    @Transactional
    public List<CommentResponse> getPrivateComment() {
        return commentRepository.findAllByShowFalse()
                .stream()
                .map(CommentResponse::from)
                .toList();
    }

    //댓글 조회(공개)
    @Transactional
    public List<CommentResponse> getShowComment() {
        return commentRepository.findAllByShowTrue()
                .stream()
                .map(CommentResponse::from)
                .toList();
    }



}
