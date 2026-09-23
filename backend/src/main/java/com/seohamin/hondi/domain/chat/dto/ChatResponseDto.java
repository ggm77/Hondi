package com.seohamin.hondi.domain.chat.dto;

import com.seohamin.hondi.domain.chat.entity.Chat;
import com.seohamin.hondi.domain.chat.entity.ChatType;
import com.seohamin.hondi.domain.user.dto.UserSimpleResponseDto;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class ChatResponseDto {
    private final Long id;
    private final Long rideId;
    private final ChatType type;
    //시스템 메세지면 null
    private final UserSimpleResponseDto sender;
    private final String message;
    private final LocalDateTime createdAt;

    public ChatResponseDto(final Chat chat){
        this.id = chat.getId();
        this.rideId = chat.getRide().getId();
        this.type = chat.getType();
        this.sender = chat.getSender() != null ? new UserSimpleResponseDto(chat.getSender()) : null;
        this.message = chat.getMessage();
        this.createdAt = chat.getCreatedAt();
    }
}
