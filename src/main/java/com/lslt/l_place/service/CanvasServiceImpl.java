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
        // 현재 색상값 읽어오기
        List<Long> result = connection.bitField(
                CANVAS_KEY.getBytes(),
                BitFieldSubCommands.create()
                        .get(BitFieldSubCommands.BitFieldType.unsigned(24))
                        .valueAt(offset)
        );

        // 현재 색상값이 같으면 null 반환
        if (result != null && !result.isEmpty() && result.get(0) == colorValue) {
            log.info("같은 위치 같은 색");
            return null;
        }

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
