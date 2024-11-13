package com.lslt.l_place.entity;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "pixel")
@Getter
@NoArgsConstructor
public class Pixel {

    @EmbeddedId
    private PixelId id;

    private String color;

    public Pixel(int x, int y, String color) {
        this.id = new PixelId(x, y);
        this.color = color;
    }

    public void changeColor(String newColor) {
        this.color = newColor;
    }
}
