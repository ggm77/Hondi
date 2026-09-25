package com.seohamin.hondi.domain.chat.dto;

import com.seohamin.hondi.domain.chat.entity.ChatMessage;
import com.seohamin.hondi.domain.ride.entity.Ride;
import lombok.Getter;

import java.time.Instant;

/**
 * 채팅방 목록의 아이템 (모집글 1개 = 채팅방 1개)
 */
@Getter
public class ChatRoomSummaryResponseDto {

    private final Long rideId;
    private final String originName;
    private final String destName;
    private final Instant departureAt;
    private final String lastMessage;
    private final Instant lastMessageAt;
    private final long unreadCount;

    public ChatRoomSummaryResponseDto(
            final Ride ride,
            final ChatMessage lastMessage,
            final long unreadCount
    ) {
        this.rideId = ride.getId();
        this.originName = ride.getOriginName();
        this.destName = ride.getDestName();
        this.departureAt = ride.getDepartureAt();
        this.lastMessage = lastMessage != null ? lastMessage.getContent() : null;
        this.lastMessageAt = lastMessage != null ? lastMessage.getCreatedAt() : null;
        this.unreadCount = unreadCount;
    }
}
