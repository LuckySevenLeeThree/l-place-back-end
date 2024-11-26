package com.lslt.l_place.controller;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.lslt.l_place.dto.ChatMessageDTO;
import com.lslt.l_place.service.ChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Slf4j
public class ChattingController {

    private final ChatService chatService;

    @MessageMapping("/chat/send")
    public void sendMessage(ChatMessageDTO message) {
        chatService.sendMessage(message);
    }
}