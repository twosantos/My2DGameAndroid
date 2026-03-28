package com.example.my2dgame.object;

import android.graphics.Color;

/**
 * Projectile fired by the player. Travels in a straight line at fixed velocity.
 */
public class Projectile extends Circle {

    private static final double SPEED_PPS = 800.0;

    public Projectile(double positionX, double positionY, float radius, double directionX, double directionY) {
        super(Color.CYAN, positionX, positionY, radius);
        velocityX = directionX * SPEED_PPS;
        velocityY = directionY * SPEED_PPS;
    }

    /**
     * Reinitialize this projectile for object pooling reuse.
     */
    public void reset(double positionX, double positionY, float radius, double directionX, double directionY) {
        this.positionX = positionX;
        this.positionY = positionY;
        this.radius = radius;
        velocityX = directionX * SPEED_PPS;
        velocityY = directionY * SPEED_PPS;
    }

    @Override
    public void update(double dt) {
        positionX += velocityX * dt;
        positionY += velocityY * dt;
    }

    public boolean isOffScreen(int screenWidth, int screenHeight) {
        return positionX + radius < 0
                || positionX - radius > screenWidth
                || positionY + radius < 0
                || positionY - radius > screenHeight;
    }
}
