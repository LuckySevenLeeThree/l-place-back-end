package com.lslt.l_place.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lslt.l_place.dto.ChatMessageDTO;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatServiceImpl implements ChatService {

    private final SimpMessagingTemplate messagingTemplate;
    private final ObjectMapper objectMapper;

    @Override
    public void sendMessage(ChatMessageDTO message) {
        // 타임스탬프 추가
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        message.setTimestamp(LocalDateTime.now().format(formatter));
        log.info("Sending message: {}", message);
        messagingTemplate.convertAndSend("/topic/chat", serialize(message));
    }

    private String serialize(Object data) {
        try {
            return objectMapper.writeValueAsString(data);
        } catch (JsonProcessingException e) {
            log.error("데이터 직렬화 중 오류 발생", e);
            throw new RuntimeException("메시지 변환 실패", e);
        }
    }
}
