package com.lslt.l_place.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lslt.l_place.dto.PixelDTO;
import java.nio.ByteBuffer;
import io.micrometer.core.annotation.Timed;
import org.springframework.data.redis.connection.RedisConnection;
import java.util.ArrayList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.connection.BitFieldSubCommands;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CanvasServiceImpl implements CanvasService {

    private static final int CANVAS_HEIGHT = 256;
    private static final int CANVAS_WIDTH = 256;
    private static final String CANVAS_KEY = "canvas";
    private static final Logger log = LoggerFactory.getLogger(CanvasServiceImpl.class);

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
        //byteBuffer로 속도 향상
        if (data != null) {
            ByteBuffer buffer = ByteBuffer.wrap(data);
            for (int y = 0; y < CANVAS_HEIGHT; y++) {
                for (int x = 0; x < CANVAS_WIDTH; x++) {
                    // 3바이트씩 읽어서 한 번에 처리
                    int r = buffer.get() & 0xFF;
                    int g = buffer.get() & 0xFF;
                    int b = buffer.get() & 0xFF;
                    String hexColor = String.format("#%02X%02X%02X", r, g, b);
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

        // 기존 색상 값을 확인
        byte[] currentPixelData = connection.getRange(CANVAS_KEY.getBytes(), offset, offset + 2);
        if (currentPixelData != null && currentPixelData.length == 3) {
            int currentColorValue = ((currentPixelData[0] & 0xFF) << 16) |
                    ((currentPixelData[1] & 0xFF) << 8) |
                    (currentPixelData[2] & 0xFF);
            if (currentColorValue == colorValue) {
                log.info("같은 위치 같은 색상, 저장하지 않음");
                return null;
            }
        }

        // 새로운 색상 데이터 저장
        connection.setRange(CANVAS_KEY.getBytes(), pixelData, offset);
        log.info("Redis에 저장 완료: {} - offset: {}", color, offset);

        PixelDTO pixelDTO = new PixelDTO(x, y, color);
        redisTemplate.convertAndSend("canvas-update", serialize(pixelDTO));

        return pixelDTO;
    }



    @Override
    public PixelDTO getPixel(int x, int y) {
        int offset = (y * CANVAS_WIDTH + x) * 3; // 픽셀의 바이트 오프셋 계산
        RedisConnection connection = redisTemplate.getConnectionFactory().getConnection();

        byte[] data = connection.getRange(CANVAS_KEY.getBytes(), offset, offset + 2);
        if (data == null || data.length != 3) {
            throw new IllegalStateException("픽셀 데이터가 없거나 유효하지 않습니다.");
        }

        int r = data[0] & 0xFF;
        int g = data[1] & 0xFF;
        int b = data[2] & 0xFF;
        String hexColor = String.format("#%02X%02X%02X", r, g, b);

        return new PixelDTO(x, y, hexColor);
    }



    private String serialize(PixelDTO pixelDTO) {
        try {
            return objectMapper.writeValueAsString(pixelDTO);
        } catch (Exception e) {
            throw new IllegalStateException("PixelDTO 직렬화 실패", e);
        }
    }
}
