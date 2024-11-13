package com.lslt.l_place.service;

import com.lslt.l_place.dto.PixelDTO;

import java.util.List;

public interface CanvasService {

    /**
     * 전체 캔버스 데이터를 반환합니다.
     *
     * @return 전체 픽셀 데이터 목록
     */
    List<PixelDTO> getCanvas();

    /**
     * 특정 픽셀의 색상을 업데이트합니다.
     *
     * @param x     픽셀의 X 좌표
     * @param y     픽셀의 Y 좌표
     * @param color 업데이트할 색상
     * @return 업데이트된 PixelDTO
     * @throws IllegalArgumentException 잘못된 좌표 또는 색상 값이 입력된 경우
     */
    PixelDTO updatePixel(int x, int y, String color);

    /**
     * 특정 영역의 캔버스 데이터를 반환합니다.
     *
     * @param startX 시작 X 좌표
     * @param startY 시작 Y 좌표
     * @param endX   종료 X 좌표
     * @param endY   종료 Y 좌표
     * @return 해당 영역의 픽셀 데이터 목록
     * @throws IllegalArgumentException 좌표 범위가 유효하지 않은 경우
     */
    List<PixelDTO> getCanvasRegion(int startX, int startY, int endX, int endY);
}
