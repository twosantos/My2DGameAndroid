package com.example.my2dgame.object;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

/**
 * Projectile fired by enemies (like Snipers).
 */
public class EnemyProjectile extends Circle {
    private static final double SPEED_PPS = 500.0;
    private final Paint paint;

    public EnemyProjectile(double x, double y, double dirX, double dirY) {
        super(Color.RED, x, y, 10f);
        this.velocityX = dirX * SPEED_PPS;
        this.velocityY = dirY * SPEED_PPS;
        this.paint = new Paint();
        this.paint.setColor(Color.RED);
    }

    public void init(double x, double y, double dirX, double dirY) {
        this.positionX = x;
        this.positionY = y;
        this.velocityX = dirX * SPEED_PPS;
        this.velocityY = dirY * SPEED_PPS;
    }

    @Override
    public void update(double dt) {
        positionX += velocityX * dt;
        positionY += velocityY * dt;
    }

    @Override
    public void draw(Canvas canvas) {
        canvas.drawCircle((float) positionX, (float) positionY, radius, paint);
    }

    public boolean isOffScreen(int screenWidth, int screenHeight) {
        return positionX + radius < 0 || positionX - radius > screenWidth ||
               positionY + radius < 0 || positionY - radius > screenHeight;
    }
}
