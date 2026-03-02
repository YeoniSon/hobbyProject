package com.example.chat.domain;

import com.example.common.enums.Role;
import com.example.user.domain.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ChatRoomMember 엔티티")
class ChatRoomMemberTest {

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
    @DisplayName("생성자(chatRoom, user)로 생성 시 chatRoom, user가 설정되고 show는 true이다.")
    void constructor() {
        ChatRoom room = ChatRoom.create(createUser(), createUser());
        User user = createUser();

        ChatRoomMember member = new ChatRoomMember(room, user);

        assertThat(member.getChatRoom()).isEqualTo(room);
        assertThat(member.getUser()).isEqualTo(user);
        assertThat(member.isShow()).isTrue();
    }

    @Test
    @DisplayName("hide() 호출 시 show가 false이고 clearAt이 설정된다.")
    void hide() {
        ChatRoom room = ChatRoom.create(createUser(), createUser());
        ChatRoomMember member = room.getMembers().get(0);

        member.hide();

        assertThat(member.isShow()).isFalse();
        assertThat(member.getClearAt()).isNotNull();
    }

    @Test
    @DisplayName("rejoin() 호출 시 show가 true이고 clearAt이 갱신된다.")
    void rejoin() {
        ChatRoom room = ChatRoom.create(createUser(), createUser());
        ChatRoomMember member = room.getMembers().get(0);
        member.hide();

        member.rejoin();

        assertThat(member.isShow()).isTrue();
        assertThat(member.getClearAt()).isNotNull();
    }
}
