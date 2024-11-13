package com.lslt.l_place.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lslt.l_place.entity.Pixel;
import com.lslt.l_place.entity.PixelId; // PixelId를 올바르게 import
import com.lslt.l_place.dto.PixelDTO;
import com.lslt.l_place.repository.PixelRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PixelUpdateHandler implements MessageListener {

    private static final Logger logger = LoggerFactory.getLogger(PixelUpdateHandler.class);
    private final PixelRepository pixelRepository;
    private final ObjectMapper objectMapper;

    @Override
    public void onMessage(Message message, byte[] pattern) {
        try {
            String body = new String(message.getBody());
            PixelDTO pixelDTO = objectMapper.readValue(body, PixelDTO.class);

            PixelId pixelId = new PixelId(pixelDTO.getX(), pixelDTO.getY()); // PixelId 생성
            Pixel existingPixel = pixelRepository.findById(pixelId).orElse(null);

            if (existingPixel != null && existingPixel.getColor().equals(pixelDTO.getColor())) {
                logger.info("중복 데이터 처리 생략: X={}, Y={}, Color={}", pixelDTO.getX(), pixelDTO.getY(), pixelDTO.getColor());
                return;
            }

            Pixel pixel = new Pixel(pixelDTO.getX(), pixelDTO.getY(), pixelDTO.getColor());
            pixelRepository.save(pixel);

            logger.info("Redis Pub/Sub 메시지 처리 완료: {}", pixelDTO);
        } catch (Exception e) {
            logger.error("Redis Pub/Sub 메시지 처리 중 오류 발생", e);
        }
    }
}
