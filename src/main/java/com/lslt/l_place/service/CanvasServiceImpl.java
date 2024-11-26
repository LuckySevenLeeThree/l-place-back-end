package com.lslt.l_place.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lslt.l_place.dto.PixelDTO;
import io.micrometer.core.annotation.Timed;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class CanvasServiceImpl implements CanvasService {

    private static final int CANVAS_HEIGHT = 1024;
    private static final int CANVAS_WIDTH = 1024;
    private static final String CANVAS_KEY = "canvas";

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    public CanvasServiceImpl(StringRedisTemplate redisTemplate, ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    @Timed("canvas.getCanvas") // 실행 시간 측정
    @Override
    public List<PixelDTO> getCanvas() {
        List<PixelDTO> canvas = new ArrayList<>();
        RedisConnection connection = redisTemplate.getConnectionFactory().getConnection();
        byte[] data = connection.get(CANVAS_KEY.getBytes());

        if (data == null) {
            data = new byte[CANVAS_HEIGHT * CANVAS_WIDTH * 3];
            Arrays.fill(data, (byte) 0xFF);
            connection.set(CANVAS_KEY.getBytes(), data);
        }

        for (int y = 0; y < CANVAS_HEIGHT; y++) {
            for (int x = 0; x < CANVAS_WIDTH; x++) {
                int offset = (y * CANVAS_WIDTH + x) * 3;
                if (offset + 3 <= data.length) {
                    int color = ((data[offset] & 0xFF) << 16) |
                            ((data[offset + 1] & 0xFF) << 8) |
                            (data[offset + 2] & 0xFF);
                    String hexColor = String.format("#%06X", color);
                    canvas.add(new PixelDTO(x, y, hexColor));
                }
            }
        }

        return canvas;
    }

    @Timed("canvas.updatePixel") // 실행 시간 측정
    @Override
    public PixelDTO updatePixel(int x, int y, String color) {
        int colorValue = Integer.parseInt(color.substring(1), 16);
        int offset = (y * CANVAS_WIDTH + x) * 3;

        byte[] colorBytes = new byte[]{
                (byte) ((colorValue >> 16) & 0xFF), // R
                (byte) ((colorValue >> 8) & 0xFF),  // G
                (byte) (colorValue & 0xFF)          // B
        };

        RedisConnection connection = redisTemplate.getConnectionFactory().getConnection();
        try {
            byte[] existingData = connection.get(CANVAS_KEY.getBytes());
            if (existingData == null) {
                existingData = new byte[CANVAS_HEIGHT * CANVAS_WIDTH * 3];
                Arrays.fill(existingData, (byte) 0xFF); // 흰색으로 초기화
            }

            // 해당 위치에 3바이트 데이터 삽입
            System.arraycopy(colorBytes, 0, existingData, offset, 3);
            connection.set(CANVAS_KEY.getBytes(), existingData);
        } finally {
            connection.close();
        }

        PixelDTO pixelDTO = new PixelDTO(x, y, color);
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
