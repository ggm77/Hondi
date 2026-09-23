package com.seohamin.hondi.domain.chat.controller;

import com.seohamin.hondi.domain.chat.dto.ChatRequestDto;
import com.seohamin.hondi.domain.chat.dto.ChatResponseDto;
import com.seohamin.hondi.domain.chat.dto.list.ChatListResponseDto;
import com.seohamin.hondi.domain.chat.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class ChatRestController {

    private final ChatService chatService;

    //채팅 전송하는 API (전송된 채팅은 /topic/chat.room.{id} 구독자에게 전달됨)
    @PostMapping("/chat/room/{id}/messages")
    public ResponseEntity<ChatResponseDto> sendChat(
            @PathVariable("id") final Long rideId,
            @AuthenticationPrincipal final String userIdStr,
            @Validated @RequestBody final ChatRequestDto chatRequestDto
    ){

        final Long userId = Long.parseLong(userIdStr);

        return ResponseEntity.ok(chatService.sendChat(rideId, chatRequestDto, userId));
    }

    //lastChatId 이후의 채팅 가져오는 API (입장시, 재연결시 동기화용)
    @GetMapping("/chat/room/{id}/messages/sync")
    public ResponseEntity<ChatListResponseDto> syncChat(
            @PathVariable("id") final Long rideId,
            @RequestParam(defaultValue = "0") final Long lastChatId,
            @RequestParam(defaultValue = "100") final Integer size,
            @AuthenticationPrincipal final String userIdStr
    ){

        final Long userId = Long.parseLong(userIdStr);

        return ResponseEntity.ok(chatService.syncChat(rideId, lastChatId, size, userId));
    }
}
