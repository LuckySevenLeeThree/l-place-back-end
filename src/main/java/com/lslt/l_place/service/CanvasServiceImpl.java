package com.lslt.l_place.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lslt.l_place.dto.PixelDTO;
import io.micrometer.core.annotation.Timed;
import org.springframework.data.redis.connection.RedisConnection;
import java.util.ArrayList;
import org.springframework.data.redis.connection.BitFieldSubCommands;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class CanvasServiceImpl implements CanvasService {

    private static final int CANVAS_HEIGHT = 256;
    private static final int CANVAS_WIDTH = 256;
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
        int offset = (y * CANVAS_WIDTH + x) * 3; // 픽셀의 바이트 오프셋 계산

        byte[] pixelData = new byte[] {
                (byte) ((colorValue >> 16) & 0xFF), // Red
                (byte) ((colorValue >> 8) & 0xFF),  // Green
                (byte) (colorValue & 0xFF)          // Blue
        };

        RedisConnection connection = redisTemplate.getConnectionFactory().getConnection();
        byte[] canvasData = connection.get(CANVAS_KEY.getBytes());

        // Redis 데이터가 없으면 초기화
        if (canvasData == null || canvasData.length < (CANVAS_WIDTH * CANVAS_HEIGHT * 3)) {
            canvasData = new byte[CANVAS_WIDTH * CANVAS_HEIGHT * 3];
            Arrays.fill(canvasData, (byte) 0xFF); // 초기화: 흰색으로 설정
        }

        // 픽셀 데이터 업데이트
        System.arraycopy(pixelData, 0, canvasData, offset, pixelData.length);
        connection.set(CANVAS_KEY.getBytes(), canvasData);

        // Pub/Sub 메시지 전송
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
