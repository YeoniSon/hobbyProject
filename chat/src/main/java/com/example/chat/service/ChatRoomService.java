package com.example.chat.service;

import com.example.chat.domain.ChatRoomMember;
import com.example.chat.dto.response.ChatRoomListItemResponse;
import com.example.chat.repository.ChatRoomMemberRepository;
import com.example.chat.repository.ChatRoomRepository;
import com.example.common.exception.BusinessException;
import com.example.user.domain.User;
import com.example.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.example.chat.domain.ChatRoom;
import com.example.chat.dto.response.ChatRoomResponse;
import com.example.common.exception.ErrorCode;

import jakarta.transaction.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatRoomService {
  
  private final ChatRoomRepository chatRoomRepository;
  private final ChatRoomMemberRepository chatRoomMemberRepository;
  private final UserRepository userRepository;

  // 1:1 채팅방 생성
  @Transactional
  public ChatRoomResponse createOrGetChatRoom(Long myUserId, Long partnerId) {

    if (myUserId.equals(partnerId)) {
      throw new BusinessException(ErrorCode.INVALID_REQUEST_CHAT_ROOM);
    }

    User myUser = userRepository.findById(myUserId)
      .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

    User partnerUser = userRepository.findById(partnerId)
      .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

    // 채팅방 조회 (이미 둘이 있는 1:1 방)
    List<ChatRoomMember> myMembers = chatRoomMemberRepository.findByUser_Id(myUserId);
    for (ChatRoomMember member : myMembers) {
      ChatRoom chatRoom = member.getChatRoom();
      boolean partnerExists = chatRoomMemberRepository
          .existsByChatRoom_IdAndUser_Id(chatRoom.getId(), partnerId);
      if (partnerExists) {
        if (chatRoom.getClosedAt() != null) {
          chatRoom.reopen();
          chatRoomRepository.save(chatRoom);
        }
        return ChatRoomResponse.from(chatRoom, partnerUser);
      }
    }

    // 채팅방 생성
    ChatRoom room = ChatRoom.create(myUser, partnerUser);
    chatRoomRepository.save(room);
    return ChatRoomResponse.from(room, partnerUser);
  }

  // 채팅방 목록 조회
  @Transactional
  public List<ChatRoomListItemResponse> getChatRoomList(Long myUserId) {
    List<ChatRoomMember> myMembers = chatRoomMemberRepository.findByUser_Id(myUserId);
    return myMembers.stream()
        .filter(member -> member.isShow())
        .map(member -> ChatRoomListItemResponse.of(member.getChatRoom(), member.getUser().getId()))
        .toList();
  }

  // 채팅방 권한 확인 (닫힌 방은 조회 불가)
  public ChatRoomResponse getRoom(Long roomId, Long userId) {
    ChatRoom room = chatRoomRepository.findById(roomId)
            .orElseThrow(() -> new BusinessException(ErrorCode.NOT_EXIST_ROOM));
    if (room.getClosedAt() != null) {
        throw new BusinessException(ErrorCode.NOT_EXIST_ROOM);
    }
    if (!chatRoomMemberRepository.existsByChatRoom_IdAndUser_Id(roomId, userId)) {
        throw new BusinessException(ErrorCode.NOT_MATCH_WRITER);
    }
    User partner = room.getMembers().stream()
            .map(ChatRoomMember::getUser)
            .filter(u -> !u.getId().equals(userId))
            .findFirst()
            .orElseThrow();
    return ChatRoomResponse.from(room, partner);
  }

  /**
   * 채팅방 나가기.
   * 멤버를 hide 처리하고, 두 명 모두 show=false면 방을 close한다.
   */
  @Transactional
  public void leaveRoom(Long roomId, Long userId) {
    ChatRoomMember member = chatRoomMemberRepository.findByChatRoom_IdAndUser_Id(roomId, userId)
            .orElseThrow(() -> new BusinessException(ErrorCode.NOT_EXIST_ROOM));
    member.hide();

    ChatRoom room = member.getChatRoom();
    if (room.getMembers().stream().allMatch(m -> !m.isShow())) {
      room.close();
      chatRoomRepository.save(room);
    }
  }

  /**
   * 채팅방 다시 들어오기.
   * show=true, clearAt=now 로 설정. 메시지 조회 시 member.getClearAt() 이후 메시지만 반환하면 재입장 전 내용은 안 보임.
   */
  @Transactional
  public void rejoinRoom(Long roomId, Long userId) {
    ChatRoomMember member = chatRoomMemberRepository.findByChatRoom_IdAndUser_Id(roomId, userId)
            .orElseThrow(() -> new BusinessException(ErrorCode.NOT_EXIST_ROOM));
    member.rejoin();
  }

  /** 1:1 방에서 상대방의 이메일(Principal name) 반환. WebSocket convertAndSendToUser용 */
  @Transactional
  public java.util.Optional<String> getOtherMemberUsername(Long roomId, Long excludeUserId) {
    ChatRoom room = chatRoomRepository.findById(roomId).orElse(null);
    if (room == null) return java.util.Optional.empty();
    return room.getMembers().stream()
        .map(ChatRoomMember::getUser)
        .filter(u -> !u.getId().equals(excludeUserId))
        .findFirst()
        .map(User::getEmail);
  }
}
