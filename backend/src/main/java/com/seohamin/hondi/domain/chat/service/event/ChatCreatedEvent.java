package com.seohamin.hondi.domain.chat.service.event;

import com.seohamin.hondi.domain.chat.dto.ChatResponseDto;

/**
 * 채팅이 저장되었을 때 발행되는 이벤트
 * 트랜잭션 커밋 후 채팅방 구독자에게 전송됨
 */
public record ChatCreatedEvent(ChatResponseDto chat) {}
