package com.example.interaction.repository;

import com.example.common.enums.TargetType;
import com.example.interaction.domain.Report;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReportRepository extends JpaRepository<Report, Long> {

    boolean existsByUser_IdAndTargetId(Long userId, Long targetId);

    List<Report> findAllByTargetType(TargetType targetType);
    List<Report> findAllByTargetId(Long targetId);
    List<Report> findAllByUser_Id(Long userId);

    boolean existsByUser_Id(Long userId);
    boolean existsByUser_IdAndTargetType(Long userId, TargetType targetType);

    int countByTargetType(TargetType targetType);
    int countByTargetId(Long targetId);
    int countByUser_Id(Long userId);
    int countByUser_IdAndTargetType(Long userId, TargetType targetType);
}
