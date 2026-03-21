package com.example.api.controller.chat;

import com.example.api.security.CustomUserDetails;
import com.example.chat.dto.request.ChatMessageSendRequest;
import com.example.chat.dto.request.ChatRoomCreateRequest;
import com.example.chat.dto.response.ChatMessageResponse;
import com.example.chat.dto.response.ChatRoomListItemResponse;
import com.example.chat.dto.response.ChatRoomResponse;
import com.example.chat.service.ChatMessageService;
import com.example.chat.service.ChatRoomService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/chat")
@SecurityRequirement(name = "JWTAuth")
@RequiredArgsConstructor
public class ChatController {

    private final ChatRoomService chatRoomService;
    private final ChatMessageService chatMessageService;

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

    /** 메시지 전송 */
    @PostMapping("/rooms/{roomId}/messages")
    public ResponseEntity<ChatMessageResponse> sendMessage(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long roomId,
            @RequestBody ChatMessageSendRequest request
    ) {
        ChatMessageResponse response = chatMessageService.sendMessage(roomId, userDetails.getId(), request.getContent());
        return ResponseEntity.ok(response);
    }

    /** 메시지 목록 조회 (재입장 시 clearAt 이후 메시지만 노출) */
    @GetMapping("/rooms/{roomId}/messages")
    public ResponseEntity<List<ChatMessageResponse>> getMessages(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long roomId
    ) {
        List<ChatMessageResponse> list = chatMessageService.getMessages(roomId, userDetails.getId());
        return ResponseEntity.ok(list);
    }

    /** 메시지 삭제 (전송 후 10분 이내, 본인 메시지만) */
    @DeleteMapping("/rooms/{roomId}/messages/{messageId}")
    public ResponseEntity<String> deleteMessage(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long roomId,
            @PathVariable Long messageId
    ) {
        chatMessageService.deleteMessage(roomId, messageId, userDetails.getId());
        return ResponseEntity.ok("success");
    }
}
