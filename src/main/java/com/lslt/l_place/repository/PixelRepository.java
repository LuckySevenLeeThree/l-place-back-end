package com.lslt.l_place.repository;
import com.lslt.l_place.entity.Pixel;
import com.lslt.l_place.entity.PixelId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface PixelRepository extends JpaRepository<Pixel, PixelId> {

    /**
     * 특정 영역에 속한 픽셀 데이터를 조회합니다.
     */
    @Query("SELECT p FROM Pixel p WHERE p.id.x BETWEEN :startX AND :endX AND p.id.y BETWEEN :startY AND :endY")
    List<Pixel> findPixelsInRegion(@Param("startX") int startX, @Param("endX") int endX,
                                   @Param("startY") int startY, @Param("endY") int endY);

    /**
     * 픽셀 데이터를 삽입하거나 업데이트합니다.
     * MySQL의 ON DUPLICATE KEY UPDATE 기능을 활용.
     */
    @Modifying
    @Transactional
    @Query(value = "INSERT INTO pixel (x, y, color) VALUES (:x, :y, :color) " +
            "ON DUPLICATE KEY UPDATE color = :color", nativeQuery = true)
    void upsertPixel(@Param("x") int x, @Param("y") int y, @Param("color") String color);
}
