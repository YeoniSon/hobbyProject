package com.example.chat.dto.response;

import com.example.chat.domain.ChatRoom;
import com.example.chat.domain.ChatRoomMember;
import com.example.user.domain.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChatRoomListItemResponse {
  private Long chatRoomId;
  private Long partnerUserId;
  private String partnerNickname;

  public static ChatRoomListItemResponse of(ChatRoom room, Long myUserId) {
    User partner = room.getMembers().stream()
            .map(ChatRoomMember::getUser)
            .filter(u -> !u.getId().equals(myUserId))
            .findFirst()
            .orElseThrow();
    return ChatRoomListItemResponse.builder()
            .chatRoomId(room.getId())
            .partnerUserId(partner.getId())
            .partnerNickname(partner.getNickname())
            .build();
  }
}
