package com.example.chat.repository;

import com.example.chat.domain.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    /** 해당 방에서 특정 시각 이후 메시지 조회 (재입장 시 clearAt 이후, 삭제되지 않은 것만) */
    List<ChatMessage> findByChatRoom_IdAndCreateTimeAfterAndDeletedAtIsNullOrderByCreateTimeAsc(Long chatRoomId, LocalDateTime after);

    /** 상대가 보낸 메시지 중 아직 안 읽은 것 (읽음 처리 대상) */
    List<ChatMessage> findByChatRoom_IdAndReadAtIsNullAndSender_IdNot(Long chatRoomId, Long senderId);

    Optional<ChatMessage> findByChatRoom_IdAndId(Long chatRoomId, Long messageId);
}
