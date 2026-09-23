package com.seohamin.hondi.domain.chat.dto.room;

import com.seohamin.hondi.domain.chat.dto.ChatResponseDto;
import com.seohamin.hondi.domain.ride.entity.Ride;
import com.seohamin.hondi.domain.ride.entity.RideStatus;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 채팅방 목록의 아이템 (채팅방 = 모집글)
 */
@Getter
public class ChatRoomResponseDto {
    private final Long rideId;
    private final String originName;
    private final String destName;
    private final LocalDateTime departureAt;
    private final RideStatus status;
    private final Integer memberCount;
    //채팅이 없으면 null
    private final ChatResponseDto lastChat;

    public ChatRoomResponseDto(final Ride ride, final ChatResponseDto lastChat) {
        this.rideId = ride.getId();
        this.originName = ride.getOriginName();
        this.destName = ride.getDestName();
        this.departureAt = ride.getDepartureAt();
        this.status = ride.getStatus();
        this.memberCount = ride.getCurrentCount();
        this.lastChat = lastChat;
    }
}
