package com.lslt.l_place.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class PixelDTO {

    @Min(value = 0, message = "X 좌표는 0 이상이어야 합니다.")
    private int x;

    @Min(value = 0, message = "Y 좌표는 0 이상이어야 합니다.")
    private int y;

    @NotBlank(message = "색상 값은 비어 있을 수 없습니다.")
    private String color;
}
