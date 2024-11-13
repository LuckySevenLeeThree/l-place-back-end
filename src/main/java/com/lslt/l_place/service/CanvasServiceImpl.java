package com.lslt.l_place.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lslt.l_place.dto.PixelDTO;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class CanvasServiceImpl implements CanvasService {

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    public CanvasServiceImpl(StringRedisTemplate redisTemplate, ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public List<PixelDTO> getCanvas() {
        return redisTemplate.opsForHash().entries("canvas").entrySet().stream()
                .map(entry -> {
                    String[] coords = entry.getKey().toString().split("_");
                    int x = Integer.parseInt(coords[1]);
                    int y = Integer.parseInt(coords[2]);
                    return new PixelDTO(x, y, entry.getValue().toString());
                })
                .collect(Collectors.toList());
    }

    @Override
    public PixelDTO updatePixel(int x, int y, String color) {
        String key = "pixel_" + x + "_" + y;
        String existingColor = (String) redisTemplate.opsForHash().get("canvas", key);

        if (color.equals(existingColor)) {
            return null;
        }

        PixelDTO pixelDTO = new PixelDTO(x, y, color);
        redisTemplate.opsForHash().put("canvas", key, color);
        redisTemplate.convertAndSend("canvas-update", serialize(pixelDTO));
        return pixelDTO;
    }

    private String serialize(PixelDTO pixelDTO) {
        try {
            return objectMapper.writeValueAsString(pixelDTO);
        } catch (Exception e) {
            throw new IllegalStateException("PixelDTO 직렬화 실패", e);
        }
    }
}
