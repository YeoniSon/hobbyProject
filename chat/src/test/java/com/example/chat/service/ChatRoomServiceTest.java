package com.example.chat.service;

import com.example.chat.domain.ChatRoom;
import com.example.chat.domain.ChatRoomMember;
import com.example.chat.dto.response.ChatRoomListItemResponse;
import com.example.chat.dto.response.ChatRoomResponse;
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

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ChatRoomService")
class ChatRoomServiceTest {

    @Mock
    private ChatRoomRepository chatRoomRepository;

    @Mock
    private ChatRoomMemberRepository chatRoomMemberRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ChatRoomService chatRoomService;

    // ---- createOrGetChatRoom ----
    @Test
    @DisplayName("createOrGetChatRoom - 새 채팅방 생성 성공")
    void createOrGetChatRoomNewRoomSuccess() {
        User me = mock(User.class);
        User partner = mock(User.class);
        when(partner.getId()).thenReturn(2L);
        when(partner.getNickname()).thenReturn("partner");
        when(userRepository.findById(1L)).thenReturn(Optional.of(me));
        when(userRepository.findById(2L)).thenReturn(Optional.of(partner));
        when(chatRoomMemberRepository.findByUser_Id(1L)).thenReturn(List.of());

        ChatRoomResponse result = chatRoomService.createOrGetChatRoom(1L, 2L);

        assertThat(result.getPartnerUserId()).isEqualTo(2L);
        assertThat(result.getPartnerNickname()).isEqualTo("partner");
        ArgumentCaptor<ChatRoom> captor = ArgumentCaptor.forClass(ChatRoom.class);
        verify(chatRoomRepository).save(captor.capture());
        assertThat(captor.getValue().getMembers()).hasSize(2);
    }

    @Test
    @DisplayName("createOrGetChatRoom - 기존 1:1 방 있으면 해당 방 반환")
    void createOrGetChatRoomExistingRoomReturnsSame() {
        User me = mock(User.class);
        User partner = mock(User.class);
        when(partner.getId()).thenReturn(2L);
        when(partner.getNickname()).thenReturn("partner");
        when(userRepository.findById(1L)).thenReturn(Optional.of(me));
        when(userRepository.findById(2L)).thenReturn(Optional.of(partner));

        ChatRoom existingRoom = mock(ChatRoom.class);
        when(existingRoom.getId()).thenReturn(100L);
        ChatRoomMember myMember = mock(ChatRoomMember.class);
        when(myMember.getChatRoom()).thenReturn(existingRoom);
        when(chatRoomMemberRepository.findByUser_Id(1L)).thenReturn(List.of(myMember));
        when(chatRoomMemberRepository.existsByChatRoom_IdAndUser_Id(100L, 2L)).thenReturn(true);

        ChatRoomResponse result = chatRoomService.createOrGetChatRoom(1L, 2L);

        assertThat(result.getChatRoomId()).isEqualTo(100L);
        assertThat(result.getPartnerUserId()).isEqualTo(2L);
        assertThat(result.getPartnerNickname()).isEqualTo("partner");
        verify(chatRoomRepository, never()).save(any());
    }

    @Test
    @DisplayName("createOrGetChatRoom - 자기 자신과 채팅 요청 시 INVALID_REQUEST_CHAT_ROOM 예외")
    void createOrGetChatRoomSelfRequestThrows() {
        assertThatThrownBy(() -> chatRoomService.createOrGetChatRoom(1L, 1L))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode())
                        .isEqualTo(ErrorCode.INVALID_REQUEST_CHAT_ROOM));
        verify(userRepository, never()).findById(anyLong());
        verify(chatRoomRepository, never()).save(any());
    }

    @Test
    @DisplayName("createOrGetChatRoom - 내 사용자 없으면 USER_NOT_FOUND 예외")
    void createOrGetChatRoomMyUserNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> chatRoomService.createOrGetChatRoom(1L, 2L))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode())
                        .isEqualTo(ErrorCode.USER_NOT_FOUND));
        verify(chatRoomMemberRepository, never()).findByUser_Id(anyLong());
        verify(chatRoomRepository, never()).save(any());
    }

    @Test
    @DisplayName("createOrGetChatRoom - 상대 사용자 없으면 USER_NOT_FOUND 예외")
    void createOrGetChatRoomPartnerNotFound() {
        User me = mock(User.class);
        when(userRepository.findById(1L)).thenReturn(Optional.of(me));
        when(userRepository.findById(2L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> chatRoomService.createOrGetChatRoom(1L, 2L))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode())
                        .isEqualTo(ErrorCode.USER_NOT_FOUND));
        verify(chatRoomRepository, never()).save(any());
    }

    @Test
    @DisplayName("createOrGetChatRoom - 기존 방이 닫혀 있으면 reopen 후 해당 방 반환")
    void createOrGetChatRoomExistingClosedRoomReopenAndReturn() {
        User me = mock(User.class);
        User partner = mock(User.class);
        when(partner.getId()).thenReturn(2L);
        when(partner.getNickname()).thenReturn("partner");
        when(userRepository.findById(1L)).thenReturn(Optional.of(me));
        when(userRepository.findById(2L)).thenReturn(Optional.of(partner));

        ChatRoom closedRoom = mock(ChatRoom.class);
        when(closedRoom.getId()).thenReturn(100L);
        when(closedRoom.getClosedAt()).thenReturn(java.time.LocalDateTime.now());
        ChatRoomMember myMember = mock(ChatRoomMember.class);
        when(myMember.getChatRoom()).thenReturn(closedRoom);
        when(chatRoomMemberRepository.findByUser_Id(1L)).thenReturn(List.of(myMember));
        when(chatRoomMemberRepository.existsByChatRoom_IdAndUser_Id(100L, 2L)).thenReturn(true);

        ChatRoomResponse result = chatRoomService.createOrGetChatRoom(1L, 2L);

        assertThat(result.getChatRoomId()).isEqualTo(100L);
        assertThat(result.getPartnerNickname()).isEqualTo("partner");
        verify(closedRoom).reopen();
        verify(chatRoomRepository).save(closedRoom);
    }

    // ---- getChatRoomList ----
    @Test
    @DisplayName("getChatRoomList - 내 채팅방 목록 조회 성공")
    void getChatRoomListSuccess() {
        ChatRoom room = mock(ChatRoom.class);
        when(room.getId()).thenReturn(10L);
        User me = mock(User.class);
        when(me.getId()).thenReturn(1L);
        User partner = mock(User.class);
        when(partner.getId()).thenReturn(2L);
        when(partner.getNickname()).thenReturn("partnerNick");
        ChatRoomMember memberMe = mock(ChatRoomMember.class);
        ChatRoomMember memberPartner = mock(ChatRoomMember.class);
        when(memberMe.getChatRoom()).thenReturn(room);
        when(memberMe.getUser()).thenReturn(me);
        when(memberMe.isShow()).thenReturn(true);
        when(memberPartner.getUser()).thenReturn(partner);
        when(room.getMembers()).thenReturn(List.of(memberMe, memberPartner));

        when(chatRoomMemberRepository.findByUser_Id(1L)).thenReturn(List.of(memberMe));

        List<ChatRoomListItemResponse> result = chatRoomService.getChatRoomList(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getChatRoomId()).isEqualTo(10L);
        assertThat(result.get(0).getPartnerUserId()).isEqualTo(2L);
        assertThat(result.get(0).getPartnerNickname()).isEqualTo("partnerNick");
    }

    @Test
    @DisplayName("getChatRoomList - show false인 방은 목록에서 제외")
    void getChatRoomListExcludesHidden() {
        ChatRoomMember member = mock(ChatRoomMember.class);
        when(member.isShow()).thenReturn(false);
        when(chatRoomMemberRepository.findByUser_Id(1L)).thenReturn(List.of(member));

        List<ChatRoomListItemResponse> result = chatRoomService.getChatRoomList(1L);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("getChatRoomList - 참여 방 없으면 빈 목록")
    void getChatRoomListEmpty() {
        when(chatRoomMemberRepository.findByUser_Id(1L)).thenReturn(List.of());

        List<ChatRoomListItemResponse> result = chatRoomService.getChatRoomList(1L);

        assertThat(result).isEmpty();
    }

    // ---- getRoom ----
    @Test
    @DisplayName("getRoom - 채팅방 단건 조회 성공")
    void getRoomSuccess() {
        ChatRoom room = mock(ChatRoom.class);
        when(room.getId()).thenReturn(10L);
        User me = mock(User.class);
        when(me.getId()).thenReturn(1L);
        User partner = mock(User.class);
        when(partner.getId()).thenReturn(2L);
        when(partner.getNickname()).thenReturn("partner");
        ChatRoomMember memberMe = mock(ChatRoomMember.class);
        ChatRoomMember memberPartner = mock(ChatRoomMember.class);
        when(memberMe.getUser()).thenReturn(me);
        when(memberPartner.getUser()).thenReturn(partner);
        when(room.getMembers()).thenReturn(List.of(memberMe, memberPartner));

        when(chatRoomRepository.findById(10L)).thenReturn(Optional.of(room));
        when(chatRoomMemberRepository.existsByChatRoom_IdAndUser_Id(10L, 1L)).thenReturn(true);

        ChatRoomResponse result = chatRoomService.getRoom(10L, 1L);

        assertThat(result.getChatRoomId()).isEqualTo(10L);
        assertThat(result.getPartnerUserId()).isEqualTo(2L);
        assertThat(result.getPartnerNickname()).isEqualTo("partner");
    }

    @Test
    @DisplayName("getRoom - 존재하지 않는 방이면 NOT_EXIST_ROOM 예외")
    void getRoomNotFound() {
        when(chatRoomRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> chatRoomService.getRoom(999L, 1L))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode())
                        .isEqualTo(ErrorCode.NOT_EXIST_ROOM));
        verify(chatRoomMemberRepository, never()).existsByChatRoom_IdAndUser_Id(anyLong(), anyLong());
    }

    @Test
    @DisplayName("getRoom - 멤버가 아니면 NOT_MATCH_WRITER 예외")
    void getRoomNotMember() {
        ChatRoom room = mock(ChatRoom.class);
        when(chatRoomRepository.findById(10L)).thenReturn(Optional.of(room));
        when(chatRoomMemberRepository.existsByChatRoom_IdAndUser_Id(10L, 1L)).thenReturn(false);

        assertThatThrownBy(() -> chatRoomService.getRoom(10L, 1L))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode())
                        .isEqualTo(ErrorCode.NOT_MATCH_WRITER));
    }

    @Test
    @DisplayName("getRoom - 닫힌 방이면 NOT_EXIST_ROOM 예외")
    void getRoomClosedRoomThrows() {
        ChatRoom room = mock(ChatRoom.class);
        when(room.getClosedAt()).thenReturn(java.time.LocalDateTime.now());
        when(chatRoomRepository.findById(10L)).thenReturn(Optional.of(room));

        assertThatThrownBy(() -> chatRoomService.getRoom(10L, 1L))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode())
                        .isEqualTo(ErrorCode.NOT_EXIST_ROOM));
        verify(chatRoomMemberRepository, never()).existsByChatRoom_IdAndUser_Id(anyLong(), anyLong());
    }

    // ---- leaveRoom ----
    @Test
    @DisplayName("leaveRoom - 한 명만 나가면 방은 close 되지 않음")
    void leaveRoomOneMemberLeavesRoomNotClosed() {
        Long roomId = 10L;
        Long userId = 1L;
        ChatRoom room = mock(ChatRoom.class);
        ChatRoomMember leavingMember = mock(ChatRoomMember.class);
        ChatRoomMember otherMember = mock(ChatRoomMember.class);
        when(otherMember.isShow()).thenReturn(true);
        when(chatRoomMemberRepository.findByChatRoom_IdAndUser_Id(roomId, userId))
                .thenReturn(Optional.of(leavingMember));
        when(leavingMember.getChatRoom()).thenReturn(room);
        when(room.getMembers()).thenReturn(List.of(leavingMember, otherMember));

        chatRoomService.leaveRoom(roomId, userId);

        verify(leavingMember).hide();
        verify(chatRoomRepository, never()).save(any());
    }

    @Test
    @DisplayName("leaveRoom - 두 명 모두 나가면 방 close 후 save")
    void leaveRoomBothLeftThenCloseAndSave() {
        Long roomId = 10L;
        Long userId = 1L;
        ChatRoom room = mock(ChatRoom.class);
        ChatRoomMember leavingMember = mock(ChatRoomMember.class);
        ChatRoomMember otherMember = mock(ChatRoomMember.class);
        when(otherMember.isShow()).thenReturn(false);
        when(chatRoomMemberRepository.findByChatRoom_IdAndUser_Id(roomId, userId))
                .thenReturn(Optional.of(leavingMember));
        when(leavingMember.getChatRoom()).thenReturn(room);
        when(room.getMembers()).thenReturn(List.of(leavingMember, otherMember));

        chatRoomService.leaveRoom(roomId, userId);

        verify(leavingMember).hide();
        verify(room).close();
        verify(chatRoomRepository).save(room);
    }

    @Test
    @DisplayName("leaveRoom - 멤버가 아니면 NOT_EXIST_ROOM 예외")
    void leaveRoomMemberNotFound() {
        when(chatRoomMemberRepository.findByChatRoom_IdAndUser_Id(10L, 1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> chatRoomService.leaveRoom(10L, 1L))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode())
                        .isEqualTo(ErrorCode.NOT_EXIST_ROOM));
    }

    // ---- rejoinRoom ----
    @Test
    @DisplayName("rejoinRoom - 재입장 성공")
    void rejoinRoomSuccess() {
        Long roomId = 10L;
        Long userId = 1L;
        ChatRoomMember member = mock(ChatRoomMember.class);
        when(chatRoomMemberRepository.findByChatRoom_IdAndUser_Id(roomId, userId))
                .thenReturn(Optional.of(member));

        chatRoomService.rejoinRoom(roomId, userId);

        verify(member).rejoin();
    }

    @Test
    @DisplayName("rejoinRoom - 멤버가 아니면 NOT_EXIST_ROOM 예외")
    void rejoinRoomMemberNotFound() {
        when(chatRoomMemberRepository.findByChatRoom_IdAndUser_Id(10L, 1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> chatRoomService.rejoinRoom(10L, 1L))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode())
                        .isEqualTo(ErrorCode.NOT_EXIST_ROOM));
    }
}
