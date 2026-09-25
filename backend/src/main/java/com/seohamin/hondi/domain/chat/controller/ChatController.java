package com.seohamin.hondi.domain.chat.controller;

import com.seohamin.hondi.domain.chat.dto.ChatMessageListResponseDto;
import com.seohamin.hondi.domain.chat.dto.ChatMessageRequestDto;
import com.seohamin.hondi.domain.chat.dto.ChatMessageResponseDto;
import com.seohamin.hondi.domain.chat.dto.ChatRoomSummaryListResponseDto;
import com.seohamin.hondi.domain.chat.service.ChatService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/chats")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    //내 채팅방 목록 API (모집글별 마지막 메시지, 안 읽은 개수)
    @GetMapping("/rooms")
    public ResponseEntity<ChatRoomSummaryListResponseDto> getMyChatRooms(
            @AuthenticationPrincipal final String userIdStr
    ){

        final Long userId = Long.parseLong(userIdStr);

        return ResponseEntity.ok(chatService.getMyChatRooms(userId));
    }

    //채팅 메시지 조회 API
    //afterId만 주면 폴링용(그 이후 메시지), beforeId만 주면 과거 메시지 스크롤용, 둘 다 없으면 최신 메시지
    @GetMapping("/{rideId}/messages")
    public ResponseEntity<ChatMessageListResponseDto> getMessages(
            @PathVariable final Long rideId,
            @RequestParam(required = false) final Long afterId,
            @RequestParam(required = false) final Long beforeId,
            @RequestParam(required = false, defaultValue = "50") final Integer size,
            @AuthenticationPrincipal final String userIdStr
    ){

        final Long userId = Long.parseLong(userIdStr);

        return ResponseEntity.ok(chatService.getMessages(rideId, afterId, beforeId, size, userId));
    }

    //채팅 메시지 전송 API
    @PostMapping("/{rideId}/messages")
    public ResponseEntity<ChatMessageResponseDto> sendMessage(
            @PathVariable final Long rideId,
            @Valid @RequestBody final ChatMessageRequestDto requestDto,
            @AuthenticationPrincipal final String userIdStr
    ){

        final Long userId = Long.parseLong(userIdStr);

        return ResponseEntity.ok(chatService.sendMessage(rideId, requestDto, userId));
    }

    //채팅 읽음 처리 API
    @PostMapping("/{rideId}/read")
    public ResponseEntity<Void> markRead(
            @PathVariable final Long rideId,
            @RequestParam final Long lastMessageId,
            @AuthenticationPrincipal final String userIdStr
    ){

        final Long userId = Long.parseLong(userIdStr);

        chatService.markRead(rideId, lastMessageId, userId);

        return ResponseEntity.noContent().build();
    }
}
