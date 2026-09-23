package com.seohamin.hondi.domain.chat.controller.room;

import com.seohamin.hondi.domain.chat.dto.room.ChatRoomResponseDto;
import com.seohamin.hondi.domain.chat.service.room.ChatRoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class ChatRoomController {

    private final ChatRoomService chatRoomService;

    //자신이 참가 중인 채팅방 리스트 조회하는 API
    @GetMapping("/chat/rooms")
    public ResponseEntity<List<ChatRoomResponseDto>> getChatRooms(
            @AuthenticationPrincipal final String userIdStr
    ){

        final Long userId = Long.parseLong(userIdStr);

        return ResponseEntity.ok(chatRoomService.getChatRooms(userId));
    }
}
