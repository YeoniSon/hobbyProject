package com.example.board.repository;

import com.example.board.domain.Notice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NoticeRepository extends JpaRepository<Notice, Long> {

    List<Notice> findAll();
    List<Notice> findAllByShowTrue();
    List<Notice> findAllByShowFalse();
}
