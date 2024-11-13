package com.lslt.l_place.service;

import com.lslt.l_place.dto.PixelDTO;

import java.util.List;

public interface CanvasService {
    List<PixelDTO> getCanvas();
    PixelDTO updatePixel(int x, int y, String color);
}
