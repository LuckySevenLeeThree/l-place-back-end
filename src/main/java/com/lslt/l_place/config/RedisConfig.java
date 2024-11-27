package com.lslt.l_place.config;
import com.lslt.l_place.service.RedisMessageListener;
import jakarta.annotation.PostConstruct;
import java.util.Arrays;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class RedisConfig {

    private static final int CANVAS_HEIGHT = 256;
    private static final int CANVAS_WIDTH= 256;
    private static final String CANVAS_KEY = "canvas";
    private final RedisTemplate redisTemplate;

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
     * 서버 실행시 redis에 Canvas가 없을때 Canvas를 만들어주고
     * 모두 흰색으로 채워준다.
     *
     * /@PostConstruct는 빈생명주기에서 초기화 콜백 단계에서 실행되는 메서드
     *
     */
    @PostConstruct
    public void initializeCanvas() {
        RedisConnection connection = Objects.requireNonNull(redisTemplate.getConnectionFactory()).getConnection();
        byte initColor = (byte) 0xFF; //초기화 할 색상 : 흰색

        // 캔버스 데이터가 없는 경우에만 초기화
        if (connection.get(CANVAS_KEY.getBytes()) == null) {
            // 전체 캔버스 크기만큼의 바이트 배열 생성 (3바이트 * 가로 * 세로)
            byte[] initialData = new byte[CANVAS_WIDTH * CANVAS_HEIGHT * 3];
            // 모든 픽셀을 흰색(0xFF)으로 초기화
            Arrays.fill(initialData, initColor);

            // Redis에 초기 데이터 저장
            connection.set(CANVAS_KEY.getBytes(), initialData);
            log.info("Initialize Canvas!!!!");
        } else {
            log.info("Canvas data already exists!!!!");
        }
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
