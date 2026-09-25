package com.seohamin.hondi.domain.chat.dto;

import lombok.Getter;

import java.util.List;

@Getter
public class ChatMessageListResponseDto {

    private final List<ChatMessageResponseDto> messages;
    private final boolean hasNext;

    public ChatMessageListResponseDto(
            final List<ChatMessageResponseDto> messages,
            final boolean hasNext
    ) {
        this.messages = messages;
        this.hasNext = hasNext;
    }
}
