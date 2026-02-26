package com.example.repository;

import com.example.domain.Notice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NoticeRepository extends JpaRepository<Notice, Long> {

    List<Notice> findAll();
    List<Notice> findAllByShowTrue();
    List<Notice> findAllByShowFalse();
}
