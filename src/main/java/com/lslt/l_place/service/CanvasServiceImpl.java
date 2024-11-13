package com.lslt.l_place.service;

import com.lslt.l_place.dto.PixelDTO;
import com.lslt.l_place.entity.Pixel;
import com.lslt.l_place.repository.PixelRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CanvasServiceImpl implements CanvasService {

    private final StringRedisTemplate redisTemplate;
    private final RabbitTemplate rabbitTemplate;

    @Override
    public List<PixelDTO> getCanvas() {
        return redisTemplate.opsForHash().entries("canvas").entrySet().stream()
                .map(entry -> {
                    String[] coords = entry.getKey().toString().split("_");
                    int x = Integer.parseInt(coords[1]);
                    int y = Integer.parseInt(coords[2]);
                    String color = entry.getValue().toString();
                    return new PixelDTO(x, y, color);
                })
                .collect(Collectors.toList());
    }

    @Override
    public PixelDTO updatePixel(int x, int y, String color) {
        PixelDTO pixelDTO = new PixelDTO(x, y, color);
        String key = "pixel_" + x + "_" + y;

        // Redis에 픽셀 업데이트
        redisTemplate.opsForHash().put("canvas", key, color);

        // 변경된 데이터를 변경 목록에 추가
        redisTemplate.opsForSet().add("changed_pixels", key);

        // RabbitMQ에 메시지 전송
        rabbitTemplate.convertAndSend("pixel.update.queue", pixelDTO);

        return pixelDTO;
    }

}
