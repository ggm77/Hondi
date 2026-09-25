package com.seohamin.hondi.domain.chat.dto;

import com.seohamin.hondi.domain.chat.entity.ChatMessage;
import com.seohamin.hondi.domain.user.dto.UserSimpleResponseDto;
import lombok.Getter;

import java.time.Instant;

@Getter
public class ChatMessageResponseDto {

    private final Long id;
    private final UserSimpleResponseDto sender;
    private final String content;
    private final Instant createdAt;

    public ChatMessageResponseDto(final ChatMessage message) {
        this.id = message.getId();
        this.sender = new UserSimpleResponseDto(message.getSender());
        this.content = message.getContent();
        this.createdAt = message.getCreatedAt();
    }
}
