package com.lslt.l_place.scheduler;

import com.lslt.l_place.entity.Pixel;
import com.lslt.l_place.repository.PixelRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class CanvasUpdateScheduler {

    private static final Logger logger = LoggerFactory.getLogger(CanvasUpdateScheduler.class);
    private final StringRedisTemplate redisTemplate;
    private final PixelRepository pixelRepository;

    private static final int BATCH_SIZE = 500;

    @Scheduled(fixedRate = 60000) // 1분마다 실행
    public void syncToDatabase() {
        List<String> changedPixelKeys = redisTemplate.opsForSet().members("changed_pixels").stream().collect(Collectors.toList());

        for (int i = 0; i < changedPixelKeys.size(); i += BATCH_SIZE) {
            List<String> batch = changedPixelKeys.subList(i, Math.min(i + BATCH_SIZE, changedPixelKeys.size()));

            List<Pixel> pixels = batch.stream()
                    .map(key -> {
                        String[] coords = key.split("_");
                        int x = Integer.parseInt(coords[1]);
                        int y = Integer.parseInt(coords[2]);
                        String color = redisTemplate.opsForHash().get("canvas", key).toString();
                        return new Pixel(x, y, color); // Pixel 객체 생성 시 EmbeddedId 사용
                    })
                    .collect(Collectors.toList());

            try {
                pixelRepository.saveAll(pixels); // EmbeddedId로 동작
                redisTemplate.opsForSet().remove("changed_pixels", batch.toArray());
            } catch (Exception e) {
                logger.error("MySQL 저장 실패: {}", e.getMessage());
                batch.forEach(key -> redisTemplate.opsForSet().add("failed_pixels", key));
            }
        }
        logger.info("Redis -> MySQL 동기화 완료: {}건", changedPixelKeys.size());
    }
}
