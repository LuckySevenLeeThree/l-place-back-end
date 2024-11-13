package com.lslt.l_place.controller;

import com.lslt.l_place.dto.PixelDTO;
import com.lslt.l_place.service.CanvasService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class CanvasController {

    private final CanvasService canvasService;

    /**
     * 전체 캔버스 데이터를 반환합니다.
     * @return 캔버스 데이터
     */
    @GetMapping("/canvas")
    public ResponseEntity<List<PixelDTO>> getCanvas() {
        List<PixelDTO> canvasData = canvasService.getCanvas();
        return ResponseEntity.ok(canvasData);
    }

    /**
     * 특정 픽셀을 업데이트합니다. 색상 변경이 있을 경우에만 처리됩니다.
     * @param pixelDTO 업데이트할 픽셀 데이터
     * @return 업데이트된 픽셀 데이터
     */
    @PostMapping("/pixel")
    public ResponseEntity<PixelDTO> updatePixel(@RequestBody @Valid PixelDTO pixelDTO) {
        PixelDTO updatedPixel = canvasService.updatePixel(pixelDTO.getX(), pixelDTO.getY(), pixelDTO.getColor());

        // 픽셀이 수정되지 않았다면 204 No Content 반환
        if (updatedPixel == null) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        }

        // 수정이 있으면 201 Created 반환
        return ResponseEntity.status(HttpStatus.CREATED).body(updatedPixel);
    }

    /**
     * 특정 영역의 캔버스를 반환합니다.
     * @param startX 시작 X 좌표
     * @param startY 시작 Y 좌표
     * @param endX 종료 X 좌표
     * @param endY 종료 Y 좌표
     * @return 특정 영역의 픽셀 데이터 목록
     */
    @GetMapping("/canvas/region")
    public ResponseEntity<List<PixelDTO>> getCanvasRegion(
            @RequestParam int startX,
            @RequestParam int startY,
            @RequestParam int endX,
            @RequestParam int endY) {
        List<PixelDTO> regionData = canvasService.getCanvasRegion(startX, startY, endX, endY);
        return ResponseEntity.ok(regionData);
    }
}
