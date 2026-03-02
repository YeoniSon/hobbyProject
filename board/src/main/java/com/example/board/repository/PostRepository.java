package com.example.board.repository;

import com.example.board.domain.Post;
import com.example.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PostRepository extends JpaRepository<Post, Long> {
    List<Post> findAll();
    Optional<Post> findById(Long id);

    List<Post> findAllByCategoryId_Id(Long categoryId);
    List<Post> findAllByUserId(User userId);
    List<Post> findAllByShowFalse();
    List<Post> findAllByShowTrue();

    boolean existsById(Long id);
}
