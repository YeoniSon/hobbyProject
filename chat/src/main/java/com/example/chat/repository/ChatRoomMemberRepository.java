package com.example.chat.repository;

import com.example.chat.domain.ChatRoomMember;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ChatRoomMemberRepository extends JpaRepository<ChatRoomMember, Long> {

    List<ChatRoomMember> findByChatRoom_Id(Long chatRoomId);

    List<ChatRoomMember> findByUser_Id(Long userId);

    Optional<ChatRoomMember> findByChatRoom_IdAndUser_Id(Long chatRoomId, Long userId);

    boolean existsByChatRoom_IdAndUser_Id(Long chatRoomId, Long userId);
}
