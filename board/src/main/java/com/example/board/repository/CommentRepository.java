package com.example.board.repository;

import com.example.board.domain.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    List<Comment> findAllByUserId(Long writerId);
    List<Comment> findAll();
    List<Comment> findAllByPostId(Long postId);
    List<Comment> findAllByShowTrue();
    List<Comment> findAllByShowFalse();
    List<Comment> findAllByPostIdAndUserId(Long postId, Long userId);

    boolean existsByPostId(Long postId);
    boolean existsByUserId(Long writerId);
}
