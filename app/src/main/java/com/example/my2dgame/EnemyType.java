package com.example.my2dgame;

import android.graphics.Color;

public enum EnemyType {
    NORMAL(1.0, 1.0, Color.RED, 100),
    FAST(1.6, 0.65, Color.rgb(255, 136, 0), 40),
    TANK(0.5, 1.5, Color.rgb(136, 0, 0), 30),
    ZIGZAG(0.9, 0.85, Color.rgb(170, 0, 255), 25),
    SPLITTER(0.4, 2.0, Color.rgb(0, 255, 0), 15),
    SMALL(2.2, 0.4, Color.rgb(0, 255, 150), 0),
    SNIPER(0.6, 0.8, Color.rgb(0, 150, 255), 20),
    KAMIKAZE(1.2, 0.7, Color.rgb(255, 50, 50), 20);

    private final double speedMultiplier;
    private final double sizeMultiplier;
    private final int color;
    private final int baseWeight;

    EnemyType(double speedMultiplier, double sizeMultiplier, int color, int baseWeight) {
        this.speedMultiplier = speedMultiplier;
        this.sizeMultiplier = sizeMultiplier;
        this.color = color;
        this.baseWeight = baseWeight;
    }

    public double getSpeedMultiplier() { return speedMultiplier; }
    public double getSizeMultiplier() { return sizeMultiplier; }
    public int getColor() { return color; }
    public int getBaseWeight() { return baseWeight; }
}
