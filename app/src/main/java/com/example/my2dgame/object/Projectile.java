package com.example.my2dgame.object;

import android.graphics.Color;

import com.example.my2dgame.GameLoop;

/**
 * Projectile fired by the player. Travels in a straight line at fixed velocity.
 */
public class Projectile extends Circle {

    private static final double SPEED_PIXELS_PER_SECOND = 800.0;
    private static final double MAX_SPEED = SPEED_PIXELS_PER_SECOND / GameLoop.MAX_UPS;

    public Projectile(double positionX, double positionY, float radius, double directionX, double directionY) {
        super(Color.CYAN, positionX, positionY, radius);
        velocityX = directionX * MAX_SPEED;
        velocityY = directionY * MAX_SPEED;
    }

    @Override
    public void update() {
        positionX += velocityX;
        positionY += velocityY;
    }

    public boolean isOffScreen(int screenWidth, int screenHeight) {
        return positionX + radius < 0
                || positionX - radius > screenWidth
                || positionY + radius < 0
                || positionY - radius > screenHeight;
    }
}
