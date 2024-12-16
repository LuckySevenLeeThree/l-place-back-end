package com.lslt.l_place.controller;

import com.lslt.l_place.dto.PixelDTO;
import com.lslt.l_place.service.CanvasService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class CanvasController {

    private final CanvasService canvasService;

    /**
     * 전체 캔버스 데이터를 반환합니다.
     * @return 전체 픽셀 데이터
     */
    @GetMapping("/canvas")
    public List<PixelDTO> getCanvas() {
        return canvasService.getCanvas();
    }

    /**
     * 특정 픽셀의 색상을 업데이트합니다.
     * @param pixelDTO 업데이트할 픽셀 데이터
     * @return 업데이트된 픽셀 데이터
     */
    @PostMapping("/pixel")
    public PixelDTO updatePixel(@RequestBody PixelDTO pixelDTO) {
        if (pixelDTO.getX() < 0 || pixelDTO.getY() < 0 || pixelDTO.getColor() == null) {
            throw new IllegalArgumentException("Invalid pixel data");
        }
        return canvasService.updatePixel(pixelDTO.getX(), pixelDTO.getY(), pixelDTO.getColor());
    }

    /**
     * 특정 x, y 좌표의 픽셀 색상을 반환합니다.
     * @param x x 좌표
     * @param y y 좌표
     * @return 해당 픽셀의 색상 정보
     */
    @GetMapping("/pixel")
    public PixelDTO getPixel(@RequestParam int x, @RequestParam int y) {
        if (x < 0 || x >= 256 || y < 0 || y >= 256) {
            throw new IllegalArgumentException("Invalid pixel coordinates.");
        }
        return canvasService.getPixel(x, y);
    }


}
