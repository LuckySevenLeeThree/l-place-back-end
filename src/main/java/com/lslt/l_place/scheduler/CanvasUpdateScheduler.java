package com.lslt.l_place.scheduler;

import com.lslt.l_place.entity.Pixel;
import com.lslt.l_place.repository.PixelRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class CanvasUpdateScheduler {

    private static final Logger logger = LoggerFactory.getLogger(CanvasUpdateScheduler.class);
    private final StringRedisTemplate redisTemplate;
    private final PixelRepository pixelRepository;

    private static final int BATCH_SIZE = 500;

    /**
     * 1분마다 Redis의 변경된 데이터를 MySQL로 동기화.
     */
    @Scheduled(fixedRate = 60000)
    public void syncToDatabase() {
        processBatch("changed_pixels");
    }

    /**
     * 5분마다 Redis의 실패 데이터를 재처리.
     */
    @Scheduled(fixedRate = 300000)
    public void retryFailedData() {
        processBatch("failed_pixels");
    }

    /**
     * Redis의 특정 Set 키를 기준으로 배치를 처리.
     *
     * @param redisSetKey Redis의 Set 키 (changed_pixels 또는 failed_pixels)
     */
    private void processBatch(String redisSetKey) {
        Set<String> pixelKeys = redisTemplate.opsForSet().members(redisSetKey);
        if (pixelKeys == null || pixelKeys.isEmpty()) {
            logger.info("{}에 동기화할 데이터가 없습니다.", redisSetKey);
            return;
        }

        List<String> keysList = new ArrayList<>(pixelKeys);

        for (int i = 0; i < keysList.size(); i += BATCH_SIZE) {
            List<String> batch = keysList.subList(i, Math.min(i + BATCH_SIZE, keysList.size()));

            List<Pixel> pixels = fetchPixelsFromRedis(batch);

            try {
                // MySQL에 배치 저장
                pixelRepository.saveAll(pixels);

                // Redis에서 처리된 키 제거
                redisTemplate.opsForSet().remove(redisSetKey, batch.toArray(new String[0]));
                logger.info("{}: MySQL 저장 완료 - {}건", redisSetKey, pixels.size());
            } catch (Exception e) {
                logger.error("MySQL 저장 실패: {}", e.getMessage());
                // 실패한 키를 실패 리스트에 저장
                if (!redisSetKey.equals("failed_pixels")) {
                    redisTemplate.opsForSet().add("failed_pixels", batch.toArray(new String[0]));
                }
            }
        }
    }

    /**
     * Redis에서 픽셀 데이터를 가져와 Pixel 객체로 변환.
     *
     * @param keys Redis Hash 키 리스트
     * @return Pixel 객체 리스트
     */
    private List<Pixel> fetchPixelsFromRedis(List<String> keys) {
        // List<String>을 Collection<Object>로 변환
        List<Object> keysAsObjects = keys.stream().map(Object.class::cast).collect(Collectors.toList());

        // Redis에서 데이터 가져오기
        List<Object> rawColors = redisTemplate.opsForHash().multiGet("canvas", keysAsObjects);

        if (rawColors == null || rawColors.isEmpty()) {
            logger.warn("Redis에서 데이터를 가져오지 못했습니다.");
            return Collections.emptyList();
        }

        List<Pixel> pixels = new ArrayList<>();
        for (int i = 0; i < keys.size(); i++) {
            String key = keys.get(i);
            String[] coords = key.split("_");
            try {
                int x = Integer.parseInt(coords[1]);
                int y = Integer.parseInt(coords[2]);
                String color = rawColors.get(i) == null ? "#FFFFFF" : rawColors.get(i).toString(); // 기본값 처리
                pixels.add(new Pixel(x, y, color));
            } catch (Exception e) {
                logger.error("잘못된 키 형식: {}", key, e);
            }
        }
        return pixels;
    }

}
