package com.example.interaction.repository;

import com.example.interaction.domain.Report;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReportRepository extends JpaRepository<Report, Long> {

    boolean existsByUser_IdAndTargetId(Long userId, Long targetId);
}
