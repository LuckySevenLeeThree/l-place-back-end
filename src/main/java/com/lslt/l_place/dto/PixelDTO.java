package com.lslt.l_place.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PixelDTO{

    @Min(value = 0, message = "X 좌표는 0 이상이어야 합니다.")
    private final int x;

    @Min(value = 0, message = "Y 좌표는 0 이상이어야 합니다.")
    private final int y;

    @Pattern(
            regexp = "^#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{3})$",
            message = "색상 코드는 #RRGGBB 또는 #RGB 형식이어야 합니다."
    )
    private final String color;

    @Override
    public String toString() {
        return "PixelDTO{" +
                "x=" + x +
                ", y=" + y +
                ", color='" + color + '\'' +
                '}';
    }
}
