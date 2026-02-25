package com.example.repository;

import com.example.domain.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    Optional<Category> findByName(String name);

    List<Category> findAll();
    List<Category> findAllByShowFalse();
    List<Category> findAllByShowTrue();

    boolean existsByName(String name);
}
