package com.lslt.l_place.service;

import com.lslt.l_place.dto.PixelDTO;
import com.lslt.l_place.entity.Pixel;
import com.lslt.l_place.repository.PixelRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PixelUpdateHandler {

    private static final Logger logger = LoggerFactory.getLogger(PixelUpdateHandler.class);
    private final PixelRepository pixelRepository;

    @RabbitListener(queues = "pixel.update.queue")
    public void handlePixelUpdate(PixelDTO pixelDTO) {
        try {
            Pixel pixel = new Pixel(pixelDTO.getX(), pixelDTO.getY(), pixelDTO.getColor());
            pixelRepository.save(pixel);
            logger.info("RabbitMQ 메시지 처리 완료: {}", pixelDTO);
        } catch (Exception e) {
            logger.error("RabbitMQ 메시지 처리 중 오류 발생", e);
        }
    }
}
