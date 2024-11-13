package com.lslt.l_place.controller;

import com.lslt.l_place.dto.PixelDTO;
import com.lslt.l_place.service.CanvasService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class CanvasController {

    private final CanvasService canvasService;

    public CanvasController(CanvasService canvasService) {
        this.canvasService = canvasService;
    }

    @GetMapping("/canvas")
    public ResponseEntity<List<PixelDTO>> getCanvas() {
        return ResponseEntity.ok(canvasService.getCanvas());
    }

    @PostMapping("/pixel")
    public ResponseEntity<PixelDTO> updatePixel(@RequestBody PixelDTO pixelDTO) {
        PixelDTO response = canvasService.updatePixel(pixelDTO.getX(), pixelDTO.getY(), pixelDTO.getColor());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
