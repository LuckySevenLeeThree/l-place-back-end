package com.lslt.l_place.config;
import com.lslt.l_place.service.RedisMessageListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;

@Configuration
public class RedisConfig {

    /**
     * Redis 메시지 리스너 컨테이너를 설정합니다.
     * - canvas-update 채널을 구독하며, Redis에서 Pub/Sub 메시지를 처리합니다.
     *
     * @param connectionFactory Redis 연결 설정
     * @param listenerAdapter   메시지 리스너 어댑터
     * @return RedisMessageListenerContainer 인스턴스
     */
    @Bean
    public RedisMessageListenerContainer redisContainer(
            RedisConnectionFactory connectionFactory,
            MessageListenerAdapter listenerAdapter) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.addMessageListener(listenerAdapter, new PatternTopic("canvas-update")); // canvas-update 채널 구독
        return container;
    }

    /**
     * Redis 메시지를 처리할 리스너 어댑터를 설정합니다.
     * - RedisMessageListener의 handleMessage 메서드에 메시지를 매핑합니다.
     *
     * @param listener RedisMessageListener 인스턴스
     * @return MessageListenerAdapter 인스턴스
     */
    @Bean
    public MessageListenerAdapter listenerAdapter(RedisMessageListener listener) {
        return new MessageListenerAdapter(listener); // 기본 메서드 handleMessage 자동 매핑
    }
}
