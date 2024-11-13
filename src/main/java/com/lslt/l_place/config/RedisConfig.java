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
     * Redis 메시지 리스너 컨테이너 설정.
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
     * Redis 메시지 리스너 어댑터 설정.
     */
    @Bean
    public MessageListenerAdapter listenerAdapter(RedisMessageListener listener) {
        return new MessageListenerAdapter(listener); // 기본 메서드 handleMessage 자동 매핑
    }
}
