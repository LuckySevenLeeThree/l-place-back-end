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
     * 메시지 브로커 구성.
     * 클라이언트로 전달할 메시지의 대상 경로를 설정.
     */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic"); // 클라이언트로 브로드캐스트할 경로
        config.setApplicationDestinationPrefixes("/app"); // 클라이언트에서 서버로 보낼 메시지의 경로 접두사
    }

    /**
     * WebSocket 연결 엔드포인트 등록.
     */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws") // WebSocket 엔드포인트
                .setAllowedOriginPatterns("http://localhost:3000") // 특정 도메인만 허용 (리액트 서버 주소)
                .withSockJS(); // SockJS 지원 활성화
    }
}
