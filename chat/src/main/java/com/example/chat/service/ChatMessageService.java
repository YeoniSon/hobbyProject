package com.example.chat.service;

import com.example.chat.domain.ChatMessage;
import com.example.chat.domain.ChatRoom;
import com.example.chat.domain.ChatRoomMember;
import com.example.chat.dto.response.ChatMessageResponse;
import com.example.chat.repository.ChatMessageRepository;
import com.example.chat.repository.ChatRoomMemberRepository;
import com.example.chat.repository.ChatRoomRepository;
import com.example.common.exception.BusinessException;
import com.example.common.exception.ErrorCode;
import com.example.user.domain.User;
import com.example.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatMessageService {

    private final ChatMessageRepository chatMessageRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final ChatRoomMemberRepository chatRoomMemberRepository;
    private final UserRepository userRepository;

    /**
     * 메시지 전송. 방 멤버이고 방이 닫히지 않았을 때만 가능.
     */
    @Transactional
    public ChatMessageResponse sendMessage(Long roomId, Long userId, String content) {
        ChatRoom room = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_EXIST_ROOM));
        if (room.getClosedAt() != null) {
            throw new BusinessException(ErrorCode.NOT_EXIST_ROOM);
        }
        ChatRoomMember member = chatRoomMemberRepository.findByChatRoom_IdAndUser_Id(roomId, userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_MATCH_WRITER));
        if (!member.isShow()) {
            throw new BusinessException(ErrorCode.NOT_MATCH_WRITER);
        }
        User sender = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        ChatMessage message = new ChatMessage(room, sender, content);
        chatMessageRepository.save(message);
        return ChatMessageResponse.from(message);
    }

    /**
     * 메시지 목록 조회. 재입장한 사용자는 clearAt 이후 메시지만 조회됨.
     * 호출한 사용자가 수신자인 메시지는 이 시점에 읽음 처리된다.
     */
    @Transactional
    public List<ChatMessageResponse> getMessages(Long roomId, Long userId) {
        ChatRoom room = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_EXIST_ROOM));
        if (room.getClosedAt() != null) {
            throw new BusinessException(ErrorCode.NOT_EXIST_ROOM);
        }
        ChatRoomMember member = chatRoomMemberRepository.findByChatRoom_IdAndUser_Id(roomId, userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_MATCH_WRITER));

        markAsReadByUser(roomId, userId);

        LocalDateTime after = member.getClearAt() != null ? member.getClearAt() : LocalDateTime.MIN;
        List<ChatMessage> messages = chatMessageRepository.findByChatRoom_IdAndCreateTimeAfterAndDeletedAtIsNullOrderByCreateTimeAsc(roomId, after);
        return messages.stream()
                .map(ChatMessageResponse::from)
                .toList();
    }

    /** 해당 방에서 상대가 보낸 메시지를 현재 사용자 기준으로 읽음 처리 */
    @Transactional
    public void markAsReadByUser(Long roomId, Long userId) {
        List<ChatMessage> unread = chatMessageRepository.findByChatRoom_IdAndReadAtIsNullAndSender_IdNot(roomId, userId);
        unread.forEach(ChatMessage::markAsRead);
    }

    private static final int DELETE_ALLOW_MINUTES = 10;

    /**
     * 메시지 삭제(소프트 삭제). 전송 후 10분 이내이고 본인 메시지일 때만 가능.
     */
    @Transactional
    public void deleteMessage(Long roomId, Long messageId, Long userId) {
        ChatMessage message = chatMessageRepository.findByChatRoom_IdAndId(roomId, messageId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_EXIST_MESSAGE));
        if (message.getDeletedAt() != null) {
            throw new BusinessException(ErrorCode.NOT_EXIST_MESSAGE);
        }
        if (!message.getSender().getId().equals(userId)) {
            throw new BusinessException(ErrorCode.NOT_MATCH_WRITER);
        }
        long minutes = ChronoUnit.MINUTES.between(message.getCreateTime(), LocalDateTime.now());
        if (minutes > DELETE_ALLOW_MINUTES) {
            throw new BusinessException(ErrorCode.MESSAGE_DELETE_TIME_EXPIRED);
        }
        message.delete();
    }
}
