package com.example.api.controller.chat;

import com.example.api.security.CustomUserDetails;
import com.example.chat.dto.request.ChatMessageSendRequest;
import com.example.chat.dto.response.ChatMessageResponse;
import com.example.chat.service.ChatMessageService;
import com.example.chat.service.ChatRoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.util.Optional;

/**
 * 채팅 메시지 전송 WebSocket (STOMP).
 * - 전송: /app/chat/rooms/{roomId}/send
 * - 수신자에게만 전달: convertAndSendToUser(상대 username, "/queue/chat", payload)
 * - 클라이언트 구독: /user/queue/chat
 */
@Controller
@RequiredArgsConstructor
public class ChatWebSocketController {

    private static final String USER_QUEUE_CHAT = "/queue/chat";

    private final ChatMessageService chatMessageService;
    private final ChatRoomService chatRoomService;
    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/chat/rooms/{roomId}/send")
    public void sendMessage(
            @DestinationVariable Long roomId,
            @Payload ChatMessageSendRequest request,
            java.security.Principal principal
    ) {
        if (!(principal instanceof CustomUserDetails userDetails)) {
            return;
        }
        ChatMessageResponse response = chatMessageService.sendMessage(roomId, userDetails.getId(), request.getContent());
        Optional<String> recipientUsername = chatRoomService.getOtherMemberUsername(roomId, userDetails.getId());
        recipientUsername.ifPresent(username ->
                messagingTemplate.convertAndSendToUser(username, USER_QUEUE_CHAT, response));
    }
}
