package com.example.api.controller.chat;

import com.example.api.security.CustomUserDetails;
import com.example.chat.dto.request.ChatMessageSendRequest;
import com.example.chat.dto.request.ChatRoomCreateRequest;
import com.example.chat.dto.response.ChatMessageResponse;
import com.example.chat.dto.response.ChatRoomResponse;
import com.example.chat.service.ChatMessageService;
import com.example.chat.service.ChatRoomService;
import com.example.common.exception.BusinessException;
import com.example.common.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ChatController")
class ChatControllerTest {

    @Mock
    private ChatRoomService chatRoomService;
    @Mock
    private ChatMessageService chatMessageService;

    @InjectMocks
    private ChatController chatController;

    private CustomUserDetails userDetails() {
        CustomUserDetails details = mock(CustomUserDetails.class);
        when(details.getId()).thenReturn(1L);
        return details;
    }

    @Test
    @DisplayName("createOrGetRoom - 성공")
    void createOrGetRoomSuccess() {
        ChatRoomCreateRequest request = new ChatRoomCreateRequest(2L);
        ChatRoomResponse response = ChatRoomResponse.builder()
                .chatRoomId(10L)
                .partnerUserId(2L)
                .partnerNickname("partner")
                .build();
        when(chatRoomService.createOrGetChatRoom(1L, 2L)).thenReturn(response);

        var result = chatController.createOrGetRoom(userDetails(), request);

        assertThat(result.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(result.getBody().getChatRoomId()).isEqualTo(10L);
        assertThat(result.getBody().getPartnerNickname()).isEqualTo("partner");
        verify(chatRoomService).createOrGetChatRoom(1L, 2L);
    }

    @Test
    @DisplayName("getMyRooms - 성공")
    void getMyRoomsSuccess() {
        when(chatRoomService.getChatRoomList(1L)).thenReturn(List.of());

        var result = chatController.getMyRooms(userDetails());

        assertThat(result.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(result.getBody()).isEmpty();
        verify(chatRoomService).getChatRoomList(1L);
    }

    @Test
    @DisplayName("getRoom - 성공")
    void getRoomSuccess() {
        ChatRoomResponse response = ChatRoomResponse.builder()
                .chatRoomId(10L)
                .partnerUserId(2L)
                .partnerNickname("partner")
                .build();
        when(chatRoomService.getRoom(10L, 1L)).thenReturn(response);

        var result = chatController.getRoom(userDetails(), 10L);

        assertThat(result.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(result.getBody().getChatRoomId()).isEqualTo(10L);
        verify(chatRoomService).getRoom(10L, 1L);
    }

    @Test
    @DisplayName("leaveRoom - 성공")
    void leaveRoomSuccess() {
        chatController.leaveRoom(userDetails(), 10L);

        verify(chatRoomService).leaveRoom(10L, 1L);
    }

    @Test
    @DisplayName("rejoinRoom - 성공")
    void rejoinRoomSuccess() {
        chatController.rejoinRoom(userDetails(), 10L);

        verify(chatRoomService).rejoinRoom(10L, 1L);
    }

    @Test
    @DisplayName("sendMessage - 성공")
    void sendMessageSuccess() {
        ChatMessageSendRequest request = new ChatMessageSendRequest("안녕");
        ChatMessageResponse response = ChatMessageResponse.builder()
                .messageId(100L)
                .senderId(1L)
                .senderNickname("me")
                .content("안녕")
                .sentAt(LocalDateTime.now())
                .readAt(null)
                .build();
        when(chatMessageService.sendMessage(10L, 1L, "안녕")).thenReturn(response);

        var result = chatController.sendMessage(userDetails(), 10L, request);

        assertThat(result.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(result.getBody().getContent()).isEqualTo("안녕");
        verify(chatMessageService).sendMessage(10L, 1L, "안녕");
    }

    @Test
    @DisplayName("getMessages - 성공")
    void getMessagesSuccess() {
        when(chatMessageService.getMessages(10L, 1L)).thenReturn(List.of());

        var result = chatController.getMessages(userDetails(), 10L);

        assertThat(result.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(result.getBody()).isEmpty();
        verify(chatMessageService).getMessages(10L, 1L);
    }

    @Test
    @DisplayName("deleteMessage - 성공")
    void deleteMessageSuccess() {
        chatController.deleteMessage(userDetails(), 10L, 100L);

        verify(chatMessageService).deleteMessage(10L, 100L, 1L);
    }

    @Test
    @DisplayName("deleteMessage - 10분 초과 시 MESSAGE_DELETE_TIME_EXPIRED 전달")
    void deleteMessageTimeExpired() {
        doThrow(new BusinessException(ErrorCode.MESSAGE_DELETE_TIME_EXPIRED))
                .when(chatMessageService).deleteMessage(10L, 100L, 1L);

        assertThatThrownBy(() -> chatController.deleteMessage(userDetails(), 10L, 100L))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode())
                        .isEqualTo(ErrorCode.MESSAGE_DELETE_TIME_EXPIRED));
    }
}
