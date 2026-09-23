package com.seohamin.hondi.global.config;

import com.seohamin.hondi.global.auth.interceptor.StompAuthChannelInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import java.util.List;

@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    // 허용할 프론트 주소 목록 (application.yml에서 설정)
    @Value("${cors.allowed-origins}")
    private List<String> allowedOrigins;

    private final StompAuthChannelInterceptor stompAuthChannelInterceptor;

    // 엔드포인트 등록
    @Override
    public void registerStompEndpoints(final StompEndpointRegistry stompEndpointRegistry){
        stompEndpointRegistry.addEndpoint("/ws")
                .setAllowedOriginPatterns(allowedOrigins.toArray(String[]::new));
    }

    @Override
    public void configureMessageBroker(final MessageBrokerRegistry messageBrokerRegistry){

        // 클라이언트 -> 서버
        messageBrokerRegistry.setApplicationDestinationPrefixes("/app");

        // 서버 -> 클라이언트 (서버 1대 기준 인메모리 브로커)
        messageBrokerRegistry.setUserDestinationPrefix("/user"); //개인 에러 전송용
        messageBrokerRegistry.enableSimpleBroker("/topic", "/queue");
    }

    // 웹소켓에서 인증 인가를 처리하기 위함
    // 필터와 비슷한 역할
    @Override
    public void configureClientInboundChannel(final ChannelRegistration registration) {
        registration.interceptors(stompAuthChannelInterceptor);
    }
}
