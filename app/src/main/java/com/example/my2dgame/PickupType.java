package com.example.my2dgame;

import android.graphics.Color;

public enum PickupType {
    HEALTH(Color.GREEN, "Health Restore"),
    SPEED(Color.CYAN, "Speed Boost"),
    RAPID_FIRE(Color.YELLOW, "Rapid Fire"),
    SHIELD(Color.MAGENTA, "Shield"),
    HOMING(Color.rgb(255, 100, 0), "Homing Missiles");

    private final int color;
    private final String label;

    PickupType(int color, String label) {
        this.color = color;
        this.label = label;
    }

    public int getColor() {
        return color;
    }

    public String getLabel() {
        return label;
    }
}
