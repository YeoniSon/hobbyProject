package com.example.chat.repository;


import com.example.chat.domain.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {

    // 1:1 방 찾기를 DB 한 번에 하고 싶을 때 사용
    @Query("SELECT DISTINCT cr FROM ChatRoom cr " +
            "JOIN cr.members m WHERE m.user.id IN :userIds " +
            "GROUP BY cr HAVING COUNT(m) = 2")
    Optional<ChatRoom> findOneToOneRoomByUserIds(@Param("userIds") List<Long> userIds);

    /** 닫힌 지 일정 시간이 지난 방 조회 (스케줄 삭제용) */
    List<ChatRoom> findByClosedAtNotNullAndClosedAtBefore(LocalDateTime threshold);
}
