package com.lslt.l_place.repository;
import com.lslt.l_place.entity.Pixel;
import com.lslt.l_place.entity.PixelId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PixelRepository extends JpaRepository<Pixel, PixelId> {

}
