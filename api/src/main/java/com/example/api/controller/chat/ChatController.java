package com.example.api.controller.chat;

import com.example.api.security.CustomUserDetails;
import com.example.chat.dto.request.ChatRoomCreateRequest;
import com.example.chat.dto.response.ChatRoomListItemResponse;
import com.example.chat.dto.response.ChatRoomResponse;
import com.example.chat.service.ChatRoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatRoomService chatRoomService;

    /** 1:1 채팅방 생성 또는 기존 방 반환 */
    @PostMapping("/rooms")
    public ResponseEntity<ChatRoomResponse> createOrGetRoom(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody ChatRoomCreateRequest request
    ) {
        ChatRoomResponse response = chatRoomService.createOrGetChatRoom(userDetails.getId(), request.getPartnerId());
        return ResponseEntity.ok(response);
    }

    /** 내 채팅방 목록 조회 */
    @GetMapping("/rooms")
    public ResponseEntity<List<ChatRoomListItemResponse>> getMyRooms(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        List<ChatRoomListItemResponse> list = chatRoomService.getChatRoomList(userDetails.getId());
        return ResponseEntity.ok(list);
    }

    /** 채팅방 단건 조회 (권한 확인) */
    @GetMapping("/rooms/{roomId}")
    public ResponseEntity<ChatRoomResponse> getRoom(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long roomId
    ) {
        ChatRoomResponse response = chatRoomService.getRoom(roomId, userDetails.getId());
        return ResponseEntity.ok(response);
    }

    /** 채팅방 나가기 */
    @PatchMapping("/rooms/{roomId}/leave")
    public ResponseEntity<String> leaveRoom(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long roomId
    ) {
        chatRoomService.leaveRoom(roomId, userDetails.getId());
        return ResponseEntity.ok("success");
    }

    /** 채팅방 다시 들어오기 */
    @PatchMapping("/rooms/{roomId}/rejoin")
    public ResponseEntity<String> rejoinRoom(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long roomId
    ) {
        chatRoomService.rejoinRoom(roomId, userDetails.getId());
        return ResponseEntity.ok("success");
    }
}
