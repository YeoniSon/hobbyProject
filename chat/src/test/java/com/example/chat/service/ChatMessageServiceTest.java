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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ChatMessageService")
class ChatMessageServiceTest {

    @Mock
    private ChatMessageRepository chatMessageRepository;
    @Mock
    private ChatRoomRepository chatRoomRepository;
    @Mock
    private ChatRoomMemberRepository chatRoomMemberRepository;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ChatMessageService chatMessageService;

    // ---- sendMessage ----
    @Test
    @DisplayName("sendMessage - 전송 성공")
    void sendMessageSuccess() {
        ChatRoom room = mock(ChatRoom.class);
        when(room.getClosedAt()).thenReturn(null);
        User sender = mock(User.class);
        when(sender.getId()).thenReturn(1L);
        when(sender.getNickname()).thenReturn("me");
        when(chatRoomRepository.findById(10L)).thenReturn(Optional.of(room));
        ChatRoomMember member = mock(ChatRoomMember.class);
        when(member.isShow()).thenReturn(true);
        when(chatRoomMemberRepository.findByChatRoom_IdAndUser_Id(10L, 1L)).thenReturn(Optional.of(member));
        when(userRepository.findById(1L)).thenReturn(Optional.of(sender));

        ChatMessageResponse result = chatMessageService.sendMessage(10L, 1L, "안녕");

        assertThat(result.getContent()).isEqualTo("안녕");
        assertThat(result.getSenderId()).isEqualTo(1L);
        assertThat(result.getSenderNickname()).isEqualTo("me");
        ArgumentCaptor<ChatMessage> captor = ArgumentCaptor.forClass(ChatMessage.class);
        verify(chatMessageRepository).save(captor.capture());
        assertThat(captor.getValue().getContent()).isEqualTo("안녕");
    }

    @Test
    @DisplayName("sendMessage - 방 없으면 NOT_EXIST_ROOM")
    void sendMessageRoomNotFound() {
        when(chatRoomRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> chatMessageService.sendMessage(999L, 1L, "안녕"))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode()).isEqualTo(ErrorCode.NOT_EXIST_ROOM));
        verify(chatMessageRepository, never()).save(any());
    }

    @Test
    @DisplayName("sendMessage - 방 닫혀 있으면 NOT_EXIST_ROOM")
    void sendMessageRoomClosed() {
        ChatRoom room = mock(ChatRoom.class);
        when(room.getClosedAt()).thenReturn(LocalDateTime.now());
        when(chatRoomRepository.findById(10L)).thenReturn(Optional.of(room));

        assertThatThrownBy(() -> chatMessageService.sendMessage(10L, 1L, "안녕"))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode()).isEqualTo(ErrorCode.NOT_EXIST_ROOM));
        verify(chatMessageRepository, never()).save(any());
    }

    @Test
    @DisplayName("sendMessage - 멤버 아니면 NOT_MATCH_WRITER")
    void sendMessageNotMember() {
        ChatRoom room = mock(ChatRoom.class);
        when(room.getClosedAt()).thenReturn(null);
        when(chatRoomRepository.findById(10L)).thenReturn(Optional.of(room));
        when(chatRoomMemberRepository.findByChatRoom_IdAndUser_Id(10L, 1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> chatMessageService.sendMessage(10L, 1L, "안녕"))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode()).isEqualTo(ErrorCode.NOT_MATCH_WRITER));
        verify(chatMessageRepository, never()).save(any());
    }

    // ---- getMessages ----
    @Test
    @DisplayName("getMessages - 목록 조회 성공, 읽음 처리 호출")
    void getMessagesSuccess() {
        ChatRoom room = mock(ChatRoom.class);
        when(room.getClosedAt()).thenReturn(null);
        ChatRoomMember member = mock(ChatRoomMember.class);
        when(member.getClearAt()).thenReturn(null);
        when(chatRoomRepository.findById(10L)).thenReturn(Optional.of(room));
        when(chatRoomMemberRepository.findByChatRoom_IdAndUser_Id(10L, 1L)).thenReturn(Optional.of(member));
        when(chatMessageRepository.findByChatRoom_IdAndReadAtIsNullAndSender_IdNot(10L, 1L)).thenReturn(List.of());
        when(chatMessageRepository.findByChatRoom_IdAndCreateTimeAfterAndDeletedAtIsNullOrderByCreateTimeAsc(eq(10L), any(LocalDateTime.class)))
                .thenReturn(List.of());

        List<ChatMessageResponse> result = chatMessageService.getMessages(10L, 1L);

        assertThat(result).isEmpty();
        verify(chatMessageRepository).findByChatRoom_IdAndReadAtIsNullAndSender_IdNot(10L, 1L);
    }

    @Test
    @DisplayName("getMessages - 방 없으면 NOT_EXIST_ROOM")
    void getMessagesRoomNotFound() {
        when(chatRoomRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> chatMessageService.getMessages(999L, 1L))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode()).isEqualTo(ErrorCode.NOT_EXIST_ROOM));
    }

    // ---- markAsReadByUser ----
    @Test
    @DisplayName("markAsReadByUser - 안 읽은 메시지 읽음 처리")
    void markAsReadByUserSuccess() {
        ChatMessage msg = mock(ChatMessage.class);
        when(chatMessageRepository.findByChatRoom_IdAndReadAtIsNullAndSender_IdNot(10L, 1L)).thenReturn(List.of(msg));

        chatMessageService.markAsReadByUser(10L, 1L);

        verify(msg).markAsRead();
    }

    // ---- deleteMessage ----
    @Test
    @DisplayName("deleteMessage - 10분 이내 본인 메시지 삭제 성공")
    void deleteMessageSuccess() {
        ChatRoom room = mock(ChatRoom.class);
        User sender = mock(User.class);
        when(sender.getId()).thenReturn(1L);
        ChatMessage message = mock(ChatMessage.class);
        when(message.getDeletedAt()).thenReturn(null);
        when(message.getSender()).thenReturn(sender);
        when(message.getCreateTime()).thenReturn(LocalDateTime.now().minusMinutes(5));
        when(chatMessageRepository.findByChatRoom_IdAndId(10L, 100L)).thenReturn(Optional.of(message));

        chatMessageService.deleteMessage(10L, 100L, 1L);

        verify(message).delete();
    }

    @Test
    @DisplayName("deleteMessage - 메시지 없으면 NOT_EXIST_MESSAGE")
    void deleteMessageNotFound() {
        when(chatMessageRepository.findByChatRoom_IdAndId(10L, 999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> chatMessageService.deleteMessage(10L, 999L, 1L))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode()).isEqualTo(ErrorCode.NOT_EXIST_MESSAGE));
    }

    @Test
    @DisplayName("deleteMessage - 본인 메시지 아니면 NOT_MATCH_WRITER")
    void deleteMessageNotSender() {
        User sender = mock(User.class);
        when(sender.getId()).thenReturn(2L);
        ChatMessage message = mock(ChatMessage.class);
        when(message.getDeletedAt()).thenReturn(null);
        when(message.getSender()).thenReturn(sender);
        when(chatMessageRepository.findByChatRoom_IdAndId(10L, 100L)).thenReturn(Optional.of(message));

        assertThatThrownBy(() -> chatMessageService.deleteMessage(10L, 100L, 1L))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode()).isEqualTo(ErrorCode.NOT_MATCH_WRITER));
        verify(message, never()).delete();
    }

    @Test
    @DisplayName("deleteMessage - 10분 초과 시 MESSAGE_DELETE_TIME_EXPIRED")
    void deleteMessageTimeExpired() {
        User sender = mock(User.class);
        when(sender.getId()).thenReturn(1L);
        ChatMessage message = mock(ChatMessage.class);
        when(message.getDeletedAt()).thenReturn(null);
        when(message.getSender()).thenReturn(sender);
        when(message.getCreateTime()).thenReturn(LocalDateTime.now().minusMinutes(11));
        when(chatMessageRepository.findByChatRoom_IdAndId(10L, 100L)).thenReturn(Optional.of(message));

        assertThatThrownBy(() -> chatMessageService.deleteMessage(10L, 100L, 1L))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode()).isEqualTo(ErrorCode.MESSAGE_DELETE_TIME_EXPIRED));
        verify(message, never()).delete();
    }
}
