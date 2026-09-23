package com.seohamin.hondi.domain.chat.dto.list;

import com.seohamin.hondi.domain.chat.dto.ChatResponseDto;
import lombok.Getter;

import java.util.List;

@Getter
public class ChatListResponseDto {
    private final List<ChatResponseDto> chats;
    //다음 요청에 lastChatId로 넣을 값
    private final Long nextCursor;
    private final boolean hasMore;

    public ChatListResponseDto(
            final List<ChatResponseDto> chats,
            final Long nextCursor,
            final boolean hasMore
    ) {
        this.chats = chats;
        this.nextCursor = nextCursor;
        this.hasMore = hasMore;
    }
}
