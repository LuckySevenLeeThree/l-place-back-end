package com.lslt.l_place.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lslt.l_place.dto.PixelDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class RedisMessageListener implements MessageListener {

    private static final Logger logger = LoggerFactory.getLogger(RedisMessageListener.class);
    private final SimpMessagingTemplate messagingTemplate;
    private final ObjectMapper objectMapper;

    public RedisMessageListener(SimpMessagingTemplate messagingTemplate, ObjectMapper objectMapper) {
        this.messagingTemplate = messagingTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public void onMessage(Message message, byte[] pattern) {
        try {
            String payload = new String(message.getBody());
            PixelDTO pixelDTO = objectMapper.readValue(payload, PixelDTO.class);

            // WebSocket으로 데이터 전송
            messagingTemplate.convertAndSend("/topic/canvas", pixelDTO);
            logger.info("Redis Pub/Sub -> WebSocket 전송 완료: {}", pixelDTO);

        } catch (Exception e) {
            handleException(message, e);
        }
    }

    /**
     * 예외 처리 및 로깅.
     *
     * @param message 실패한 Redis 메시지
     * @param e       발생한 예외
     */
    private void handleException(Message message, Exception e) {
        String payload = new String(message.getBody());
        logger.error("Redis 메시지 처리 중 오류 발생. 메시지 내용: {}, 예외: {}", payload, e.getMessage(), e);
        // TODO: 실패 메시지를 재처리하거나 별도의 로그 저장 로직 추가
    }
}
