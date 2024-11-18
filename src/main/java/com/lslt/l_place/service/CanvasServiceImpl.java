package com.lslt.l_place.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lslt.l_place.dto.PixelDTO;
import java.util.ArrayList;
import org.springframework.data.redis.connection.BitFieldSubCommands;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class CanvasServiceImpl implements CanvasService {

    private static final int CANVAS_HEIGHT = 20;
    private static final int CANVAS_WIDTH= 20;
    private static final String CANVAS_KEY = "canvas";

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    public CanvasServiceImpl(StringRedisTemplate redisTemplate, ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public List<PixelDTO> getCanvas() {
        List<PixelDTO> canvas = new ArrayList<>();

        RedisConnection connection = redisTemplate.getConnectionFactory().getConnection();

        // Redis에서 전체 데이터를 한 번에 읽어옴
        byte[] data = connection.get(CANVAS_KEY.getBytes());

        // 각 픽셀의 24비트 컬러값을 순차적으로 읽어옴
        // 데이터를 순차적으로 읽으며 24비트씩 분리
        for (int y = 0; y < CANVAS_HEIGHT; y++) {
            for (int x = 0; x < CANVAS_WIDTH; x++) {
                int offset = (y * CANVAS_WIDTH + x) * 3; // 바이트 단위로 3씩 이동 (24비트 = 3바이트)

                if (offset + 3 <= data.length) { // 안전하게 바이트 접근
                    int color = ((data[offset] & 0xFF) << 16) | // R
                            ((data[offset + 1] & 0xFF) << 8) | // G
                            (data[offset + 2] & 0xFF); // B

                    // 결과값을 16진수로 변환
                    String hexColor = String.format("#%06X", color);
                    canvas.add(new PixelDTO(x, y, hexColor));
                }
            }
        }

        return canvas;
    }

    @Override
    public PixelDTO updatePixel(int x, int y, String color) {
        int colorValue = Integer.parseInt(color.substring(1), 16);
        int offset = (y * CANVAS_WIDTH + x) * 24;

//        if (color.equals(existingColor)) {
//            return null;
//        }

        RedisConnection connection = redisTemplate.getConnectionFactory().getConnection();
        connection.bitField(
                CANVAS_KEY.getBytes(),
                BitFieldSubCommands.create()
                        .set(BitFieldSubCommands.BitFieldType.unsigned(24))
                        .valueAt(offset)
                        .to(colorValue)
        );

        PixelDTO pixelDTO = new PixelDTO(x, y , color);
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
