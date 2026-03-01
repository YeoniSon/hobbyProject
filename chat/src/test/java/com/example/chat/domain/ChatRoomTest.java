package com.example.chat.domain;

import com.example.common.enums.Role;
import com.example.user.domain.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ChatRoom 엔티티")
class ChatRoomTest {

    private static User createUser() {
        return User.builder()
                .email("test@test.com")
                .name("테스트")
                .password("password")
                .nickname("nick")
                .phone("01012345678")
                .birth(LocalDate.of(1990, 1, 1))
                .role(Role.USER)
                .build();
    }

    @Test
    @DisplayName("create(userA, userB) 시 멤버 2명이 추가되고 closedAt은 null이다.")
    void create() {
        User userA = createUser();
        User userB = createUser();

        ChatRoom room = ChatRoom.create(userA, userB);

        assertThat(room.getMembers()).hasSize(2);
        assertThat(room.getClosedAt()).isNull();
        assertThat(room.getMembers().get(0).isShow()).isTrue();
        assertThat(room.getMembers().get(1).isShow()).isTrue();
    }

    @Test
    @DisplayName("close() 호출 시 closedAt이 설정되고 모든 멤버가 hide된다.")
    void close() {
        User userA = createUser();
        User userB = createUser();
        ChatRoom room = ChatRoom.create(userA, userB);

        room.close();

        assertThat(room.getClosedAt()).isNotNull();
        assertThat(room.getMembers().get(0).isShow()).isFalse();
        assertThat(room.getMembers().get(1).isShow()).isFalse();
    }

    @Test
    @DisplayName("reopen() 호출 시 closedAt이 null이 되고 모든 멤버가 rejoin된다.")
    void reopen() {
        User userA = createUser();
        User userB = createUser();
        ChatRoom room = ChatRoom.create(userA, userB);
        room.close();

        room.reopen();

        assertThat(room.getClosedAt()).isNull();
        assertThat(room.getMembers().get(0).isShow()).isTrue();
        assertThat(room.getMembers().get(1).isShow()).isTrue();
    }
}
