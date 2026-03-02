package com.example.api.controller.chat;

import com.example.api.security.CustomUserDetails;
import com.example.chat.dto.request.ChatMessageSendRequest;
import com.example.chat.dto.response.ChatMessageResponse;
import com.example.chat.service.ChatMessageService;
import com.example.chat.service.ChatRoomService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ChatWebSocketController")
class ChatWebSocketControllerTest {

    @Mock
    private ChatMessageService chatMessageService;
    @Mock
    private ChatRoomService chatRoomService;
    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @InjectMocks
    private ChatWebSocketController chatWebSocketController;

    @Test
    @DisplayName("sendMessage - 전송 후 상대에게 convertAndSendToUser 호출")
    void sendMessageSuccessAndNotifyRecipient() {
        Long roomId = 10L;
        ChatMessageSendRequest request = new ChatMessageSendRequest("안녕");
        CustomUserDetails userDetails = mock(CustomUserDetails.class);
        when(userDetails.getId()).thenReturn(1L);

        ChatMessageResponse response = ChatMessageResponse.builder()
                .messageId(100L)
                .senderId(1L)
                .senderNickname("me")
                .content("안녕")
                .sentAt(LocalDateTime.now())
                .readAt(null)
                .build();
        when(chatMessageService.sendMessage(roomId, 1L, "안녕")).thenReturn(response);
        when(chatRoomService.getOtherMemberUsername(roomId, 1L)).thenReturn(Optional.of("other@test.com"));

        chatWebSocketController.sendMessage(roomId, request, userDetails);

        verify(chatMessageService).sendMessage(roomId, 1L, "안녕");
        verify(chatRoomService).getOtherMemberUsername(roomId, 1L);

        ArgumentCaptor<String> userCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> destCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<ChatMessageResponse> payloadCaptor = ArgumentCaptor.forClass(ChatMessageResponse.class);
        verify(messagingTemplate).convertAndSendToUser(userCaptor.capture(), destCaptor.capture(), payloadCaptor.capture());
        assertThat(userCaptor.getValue()).isEqualTo("other@test.com");
        assertThat(destCaptor.getValue()).isEqualTo("/queue/chat");
        assertThat(payloadCaptor.getValue().getContent()).isEqualTo("안녕");
    }

    @Test
    @DisplayName("sendMessage - 상대 없으면 convertAndSendToUser 미호출")
    void sendMessageNoRecipient() {
        Long roomId = 10L;
        ChatMessageSendRequest request = new ChatMessageSendRequest("안녕");
        CustomUserDetails userDetails = mock(CustomUserDetails.class);
        when(userDetails.getId()).thenReturn(1L);

        ChatMessageResponse response = ChatMessageResponse.builder()
                .messageId(100L)
                .senderId(1L)
                .senderNickname("me")
                .content("안녕")
                .sentAt(LocalDateTime.now())
                .readAt(null)
                .build();
        when(chatMessageService.sendMessage(roomId, 1L, "안녕")).thenReturn(response);
        when(chatRoomService.getOtherMemberUsername(roomId, 1L)).thenReturn(Optional.empty());

        chatWebSocketController.sendMessage(roomId, request, userDetails);

        verify(chatMessageService).sendMessage(roomId, 1L, "안녕");
        verify(chatRoomService).getOtherMemberUsername(roomId, 1L);
        verify(messagingTemplate, never()).convertAndSendToUser(anyString(), anyString(), any());
    }

    @Test
    @DisplayName("sendMessage - Principal이 CustomUserDetails 아니면 전송만 하고 convertAndSendToUser 미호출")
    void sendMessagePrincipalNotCustomUserDetails() {
        Long roomId = 10L;
        ChatMessageSendRequest request = new ChatMessageSendRequest("안녕");
        java.security.Principal principal = () -> "anonymous";

        chatWebSocketController.sendMessage(roomId, request, principal);

        verify(chatMessageService, never()).sendMessage(anyLong(), anyLong(), anyString());
        verify(chatRoomService, never()).getOtherMemberUsername(anyLong(), anyLong());
        verify(messagingTemplate, never()).convertAndSendToUser(anyString(), anyString(), any());
    }
}
