package com.seohamin.hondi.global.stomp.subscribe;

import com.seohamin.hondi.domain.chat.service.room.ChatRoomService;
import com.seohamin.hondi.global.exception.CustomException;
import com.seohamin.hondi.global.exception.constants.ExceptionCode;
import com.seohamin.hondi.global.stomp.constants.StompConstants;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;

import java.security.Principal;

@Component
@RequiredArgsConstructor
public class StompSubscribeGuard {

    private final ChatRoomService chatRoomService;

    /**
     * 유저가 요청한 채팅방에 구독(입장)이 가능함을 보장하는 메서드
     * @param accessor 검사를 진행할 STOMP 메세지의 헤더 정보를 포함한 객체
     * @param destination 구독하려는 주소 (/topic/chat.room.{rideId})
     */
    public void assertCanSubscribeChatRoom(
            final StompHeaderAccessor accessor,
            final String destination
    ) {
        // 1) accessor에서 Principal 추출
        final Principal principal = accessor.getUser();
        if(principal == null || principal.getName() == null) {
            throw new CustomException(ExceptionCode.UNAUTHORIZED);
        }

        // 2) 주소에서 모집글 ID 추출
        final Long rideId;
        try {
            rideId = Long.parseLong(destination.substring(StompConstants.DEST_CHAT_ROOM_PREFIX.length()));
        } catch (NumberFormatException ex) {
            throw new CustomException(ExceptionCode.CHAT_ROOM_NOT_EXIST);
        }

        // 3) 채팅방 입장 가능한지 확인
        final Long userId = Long.parseLong(principal.getName());
        if(!chatRoomService.isMember(rideId, userId)) {
            throw new CustomException(ExceptionCode.CHAT_ROOM_NOT_JOINED);
        }
    }
}
