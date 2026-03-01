package com.example.chat.domain;

import com.example.common.entity.BaseEntity;
import com.example.common.entity.BaseShowEntity;
import com.example.user.domain.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "chat_room_member")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatRoomMember extends BaseShowEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    private ChatRoom chatRoom;

    @ManyToOne(fetch = FetchType.LAZY)
    private User user;

    private boolean show;

    /** 나갔다가 다시 들어온 시점. 이 시각 이후 메시지만 해당 멤버에게 보이면 됨. */
    private LocalDateTime clearAt;

    public ChatRoomMember(ChatRoom chatRoom, User user) {
        this.chatRoom = chatRoom;
        this.user = user;
        this.show = true;
    }

    public void hide() {
        this.show = false;
        this.clearAt = LocalDateTime.now();
    }

    public void rejoin() {
        this.show = true;
        this.clearAt = LocalDateTime.now();
    }
}
