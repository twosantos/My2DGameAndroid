package com.example.my2dgame;

import android.graphics.Color;

public enum EnemyType {
    NORMAL(1.0, 1.0, Color.RED),
    FAST(1.6, 0.65, Color.rgb(255, 136, 0)),
    TANK(0.5, 1.5, Color.rgb(136, 0, 0)),
    ZIGZAG(0.9, 0.85, Color.rgb(170, 0, 255));

    private final double speedMultiplier;
    private final double sizeMultiplier;
    private final int color;

    EnemyType(double speedMultiplier, double sizeMultiplier, int color) {
        this.speedMultiplier = speedMultiplier;
        this.sizeMultiplier = sizeMultiplier;
        this.color = color;
    }

    public double getSpeedMultiplier() {
        return speedMultiplier;
    }

    public double getSizeMultiplier() {
        return sizeMultiplier;
    }

    public int getColor() {
        return color;
    }
}
