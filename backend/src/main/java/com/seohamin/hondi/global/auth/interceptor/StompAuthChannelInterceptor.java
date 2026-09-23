package com.seohamin.hondi.global.auth.interceptor;

import com.seohamin.hondi.global.exception.CustomException;
import com.seohamin.hondi.global.exception.constants.ExceptionCode;
import com.seohamin.hondi.global.stomp.auth.JwtStompAuthenticator;
import com.seohamin.hondi.global.stomp.auth.StompSessionContext;
import com.seohamin.hondi.global.stomp.constants.StompConstants;
import com.seohamin.hondi.global.stomp.dto.StompAuthResultDto;
import com.seohamin.hondi.global.stomp.error.StompErrorSender;
import com.seohamin.hondi.global.stomp.subscribe.StompSubscribeGuard;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

/**
 * Stomp 통신 과정에서 인증 인가를 처리하는 인터셉터
 * CONNECT에서만 JWT를 통한 인증 후 세션 인증한다.
 * 이후 과정에서는 JWT를 사용하지 않음.
 * 세션 인증시 JWT와 같은 만료 시간을 설정해서 JWT가 만료 되면 세션도 함께 만료 되게 함
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class StompAuthChannelInterceptor implements ChannelInterceptor {

    private final JwtStompAuthenticator jwtStompAuthenticator;
    private final StompSessionContext stompSessionContext;
    private final StompSubscribeGuard stompSubscribeGuard;
    private final StompErrorSender stompErrorSender;

    @Override
    public Message<?> preSend(
            final Message<?> message,
            final MessageChannel channel
    ){
        final StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);

        try {
            // 0) HEARTBEAT, DISCONNECT 예외처리
            if(accessor.getCommand() == null || StompCommand.DISCONNECT.equals(accessor.getCommand())) {
                return message;
            }

            //STOMP가 Connect 단계일 경우
            if (StompCommand.CONNECT.equals(accessor.getCommand())) {

                // 1) jwt인증
                final StompAuthResultDto stompAuthResultDto = jwtStompAuthenticator.authenticateFromAccessor(accessor);

                // 2) 세션에 정보 저장
                stompSessionContext.store(accessor, stompAuthResultDto);

                log.info("STOMP CONNECT by userId: {}, sessionId: {}", stompAuthResultDto.getUserIdStr(), accessor.getSessionId());

                // 3) 메세지에 내용 저장을 확실히 하기 위해서 새로운 메세지로 리턴
                return MessageBuilder.createMessage(message.getPayload(), accessor.getMessageHeaders());
            }

            //STOMP가 Connect 단계가 아닌 다른 모든 경우에는 JWT검사 X
            // 1) 세션이 인증 되어있는지 검사 (Principal이 없다면 attributes에서 복원)
            if (accessor.getUser() == null) {
                stompSessionContext.restorePrincipalFromAttributes(accessor);
            }

            // 2) 세션 만료 시간 검사
            stompSessionContext.assertNotExpired(accessor);

            //STOMP가 SUBSCRIBE일 경우 구독 가능한지 검사
            if(StompCommand.SUBSCRIBE.equals(accessor.getCommand())) {
                final String destination = accessor.getDestination();

                if (destination == null || destination.isEmpty()) {
                    throw new CustomException(ExceptionCode.INVALID_REQUEST);
                }

                //채팅방 구독일 경우 채팅방 멤버인지 확인
                if(destination.startsWith(StompConstants.DEST_CHAT_ROOM_PREFIX)) {
                    stompSubscribeGuard.assertCanSubscribeChatRoom(accessor, destination);
                }
                //개인 에러 큐 외의 다른 주소는 구독 불가
                else if(!destination.equals("/user" + StompConstants.DEST_ERROR_MESSAGE)) {
                    throw new CustomException(ExceptionCode.ACCESS_DENIED);
                }
            }

            //클라이언트에서 직접 SEND는 막음 (채팅은 REST API로 전송)
            if(StompCommand.SEND.equals(accessor.getCommand())) {
                throw new CustomException(ExceptionCode.ACCESS_DENIED);
            }

            return MessageBuilder.createMessage(message.getPayload(), accessor.getMessageHeaders());
        }
        //에러 메세지를 따로 보내기 위함
        catch (CustomException ex){
            log.warn("STOMP client error: {}", ex.getExceptionCode().name());
            sendError(accessor, ex.getExceptionCode().name(), ex.getExceptionCode().getMessage());
            return null;
        }
        catch (Exception ex) {
            log.error("STOMP client error: {}", ex.getMessage(), ex);
            sendError(accessor, ExceptionCode.INTERNAL_SERVER_ERROR.name(), ExceptionCode.INTERNAL_SERVER_ERROR.getMessage());
            return null;
        }
    }

    private void sendError(final StompHeaderAccessor accessor, final String code, final String message) {
        final String sessionId = accessor.getSessionId();
        if(sessionId == null){
            return;
        }
        stompErrorSender.toSession(sessionId, code, message, accessor.getDestination());
    }
}
