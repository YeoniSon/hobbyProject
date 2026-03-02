package com.example.chat.domain;

import com.example.common.entity.BaseEntity;
import com.example.user.domain.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "chat_room")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatRoom extends BaseEntity {

    @OneToMany(mappedBy = "chatRoom", cascade = CascadeType.ALL)
    private List<ChatRoomMember> members = new ArrayList<>();

    private LocalDateTime closedAt;

    public static ChatRoom create(User userA, User userB) {
        ChatRoom room = new ChatRoom();
        room.addMember(userA);
        room.addMember(userB);
        return room;
    }

    private void addMember(User user) {
        ChatRoomMember member = new ChatRoomMember(this, user);
        members.add(member);
    }

    public void close() {
        this.closedAt = LocalDateTime.now();
        members.forEach(ChatRoomMember::hide);
    }

    public void reopen() {
        this.closedAt = null;
        members.forEach(ChatRoomMember::rejoin);
    }
}
