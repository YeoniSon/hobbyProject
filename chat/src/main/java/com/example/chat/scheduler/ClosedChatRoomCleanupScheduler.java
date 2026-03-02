package com.example.chat.scheduler;

import com.example.chat.domain.ChatRoom;
import com.example.chat.repository.ChatRoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 닫힌 지 1주일이 지난 채팅방을 삭제한다.
 */
@Component
@RequiredArgsConstructor
public class ClosedChatRoomCleanupScheduler {

    private static final int CLOSED_DAYS_BEFORE_DELETE = 7;

    private final ChatRoomRepository chatRoomRepository;

    @Scheduled(cron = "0 0 3 * * ?") // 매일 새벽 3시
    @Transactional
    public void deleteClosedRoomsOlderThanOneWeek() {
        LocalDateTime threshold = LocalDateTime.now().minusDays(CLOSED_DAYS_BEFORE_DELETE);
        List<ChatRoom> toDelete = chatRoomRepository.findByClosedAtNotNullAndClosedAtBefore(threshold);
        chatRoomRepository.deleteAll(toDelete);
    }
}
