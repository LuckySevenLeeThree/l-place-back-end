package com.lslt.l_place.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    /**
     * 메시지 브로커 설정.
     * 클라이언트로 전달할 메시지의 브로커 경로와 서버로 보낼 메시지 경로를 설정.
     */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic"); // 클라이언트 구독 경로
        config.setApplicationDestinationPrefixes("/app"); // 클라이언트가 서버로 전송할 경로 접두사
    }

    /**
     * WebSocket 엔드포인트 설정.
     * 클라이언트가 WebSocket 연결을 초기화할 수 있는 경로를 설정.
     *
     * @param registry StompEndpointRegistry 객체
     */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws") // WebSocket 엔드포인트
                .setAllowedOriginPatterns("http://localhost:3000") // React 개발 서버 허용
                .withSockJS(); // SockJS 사용 활성화
    }
}

