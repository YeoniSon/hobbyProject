package com.example.board.repository;

import com.example.board.domain.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    List<Category> findAll();
    List<Category> findAllByShowFalse();
    List<Category> findAllByShowTrue();

    boolean existsByName(String name);
}
