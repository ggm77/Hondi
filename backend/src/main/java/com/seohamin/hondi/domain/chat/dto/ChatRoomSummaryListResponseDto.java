package com.seohamin.hondi.domain.chat.dto;

import lombok.Getter;

import java.util.List;

@Getter
public class ChatRoomSummaryListResponseDto {

    private final List<ChatRoomSummaryResponseDto> rooms;

    public ChatRoomSummaryListResponseDto(final List<ChatRoomSummaryResponseDto> rooms) {
        this.rooms = rooms;
    }
}
