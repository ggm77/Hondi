package com.seohamin.hondi.domain.chat.service.event;

import com.seohamin.hondi.global.stomp.constants.StompConstants;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * 저장된 채팅을 채팅방 구독자에게 전송하는 리스너
 * 롤백된 채팅이 전송되지 않도록 커밋 후에 전송
 */
@Component
@RequiredArgsConstructor
public class ChatPushListener {

    private final SimpMessagingTemplate simpMessagingTemplate;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void push(final ChatCreatedEvent event) {
        simpMessagingTemplate.convertAndSend(
                StompConstants.DEST_CHAT_ROOM_PREFIX + event.chat().getRideId(),
                event.chat()
        );
    }
}
