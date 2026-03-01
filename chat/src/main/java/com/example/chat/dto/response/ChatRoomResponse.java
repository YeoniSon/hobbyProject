package com.example.chat.dto.response;

import com.example.chat.domain.ChatRoom;
import com.example.user.domain.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChatRoomResponse {
  private Long chatRoomId;
  private Long partnerUserId;
  private String partnerNickname;

  public static ChatRoomResponse from(ChatRoom chatRoom, User partnerUser) {
    return ChatRoomResponse.builder()
      .chatRoomId(chatRoom.getId())
      .partnerUserId(partnerUser.getId())
      .partnerNickname(partnerUser.getNickname())
      .build();
  }
  
}
