package com.example.api.controller.chat;

import com.example.api.security.CustomUserDetails;
import com.example.chat.dto.request.ChatMessageSendRequest;
import com.example.chat.dto.request.ChatRoomCreateRequest;
import com.example.chat.dto.response.ChatMessageResponse;
import com.example.chat.dto.response.ChatRoomListItemResponse;
import com.example.chat.dto.response.ChatRoomResponse;
import com.example.chat.service.ChatMessageService;
import com.example.chat.service.ChatRoomService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/chat")
@Tag(name = "채팅 (REST)", description = "1:1 채팅방·메시지 REST API. 실시간은 WebSocket(STOMP) 별도")
@SecurityRequirement(name = "JWTAuth")
@RequiredArgsConstructor
public class ChatController {

    private final ChatRoomService chatRoomService;
    private final ChatMessageService chatMessageService;

    @Operation(summary = "채팅방 생성 또는 조회", description = "상대 partnerId와 1:1 방이 없으면 만들고, 있으면 기존 방을 반환합니다.")
    @PostMapping("/rooms")
    public ResponseEntity<ChatRoomResponse> createOrGetRoom(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody ChatRoomCreateRequest request
    ) {
        ChatRoomResponse response = chatRoomService.createOrGetChatRoom(userDetails.getId(), request.getPartnerId());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "내 채팅방 목록", description = "로그인 사용자가 참여 중인 채팅방 목록입니다.")
    @GetMapping("/rooms")
    public ResponseEntity<List<ChatRoomListItemResponse>> getMyRooms(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        List<ChatRoomListItemResponse> list = chatRoomService.getChatRoomList(userDetails.getId());
        return ResponseEntity.ok(list);
    }

    @Operation(summary = "채팅방 단건 조회", description = "roomId 방 정보를 조회합니다. 참여자만 조회 가능합니다.")
    @GetMapping("/rooms/{roomId}")
    public ResponseEntity<ChatRoomResponse> getRoom(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long roomId
    ) {
        ChatRoomResponse response = chatRoomService.getRoom(roomId, userDetails.getId());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "채팅방 나가기", description = "해당 방에서 나갑니다.")
    @PatchMapping("/rooms/{roomId}/leave")
    public ResponseEntity<String> leaveRoom(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long roomId
    ) {
        chatRoomService.leaveRoom(roomId, userDetails.getId());
        return ResponseEntity.ok("success");
    }

    @Operation(summary = "채팅방 재입장", description = "나갔던 방에 다시 참여합니다.")
    @PatchMapping("/rooms/{roomId}/rejoin")
    public ResponseEntity<String> rejoinRoom(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long roomId
    ) {
        chatRoomService.rejoinRoom(roomId, userDetails.getId());
        return ResponseEntity.ok("success");
    }

    @Operation(summary = "메시지 전송 (HTTP)", description = "REST로 메시지를 보냅니다. 실시간은 STOMP 사용.")
    @PostMapping("/rooms/{roomId}/messages")
    public ResponseEntity<ChatMessageResponse> sendMessage(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long roomId,
            @RequestBody ChatMessageSendRequest request
    ) {
        ChatMessageResponse response = chatMessageService.sendMessage(roomId, userDetails.getId(), request.getContent());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "메시지 목록", description = "방의 메시지 목록입니다. 재입장 시 clearAt 이후만 노출될 수 있습니다.")
    @GetMapping("/rooms/{roomId}/messages")
    public ResponseEntity<List<ChatMessageResponse>> getMessages(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long roomId
    ) {
        List<ChatMessageResponse> list = chatMessageService.getMessages(roomId, userDetails.getId());
        return ResponseEntity.ok(list);
    }

    @Operation(summary = "메시지 삭제", description = "전송 후 10분 이내이며 본인 메시지만 삭제할 수 있습니다.")
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
