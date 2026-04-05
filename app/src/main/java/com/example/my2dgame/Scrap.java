package com.example.my2dgame;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import com.example.my2dgame.object.Circle;

public class Scrap extends Circle {
    private int lifeTimer;
    private static final int MAX_LIFE = 240; // 8 seconds at 30 UPS
    private final Paint scrapPaint = new Paint();
    private final Rect dstRect = new Rect();
    private double driftSpeedY = 40.0;

    public Scrap(double x, double y, float radius) {
        super(Color.GREEN, x, y, radius);
        this.lifeTimer = MAX_LIFE;
        scrapPaint.setColor(Color.GREEN);
        scrapPaint.setFakeBoldText(true);
    }

    public void init(double x, double y, float radius) {
        this.positionX = x;
        this.positionY = y;
        this.radius = radius;
        this.lifeTimer = MAX_LIFE;
    }

    public void update(double dt, double playerX, double playerY, float magnetRadius) {
        lifeTimer--;

        // Magnet Logic
        double dx = playerX - positionX;
        double dy = playerY - positionY;
        double distSq = dx * dx + dy * dy;

        if (distSq < magnetRadius * magnetRadius) {
            double dist = Math.sqrt(distSq);
            double pullSpeed = 400.0;
            positionX += (dx / dist) * pullSpeed * dt;
            positionY += (dy / dist) * pullSpeed * dt;
        } else {
            // Normal drift
            positionY += driftSpeedY * dt;
        }
    }

    public void draw(Canvas canvas) {
        // Blinking logic before vanishing
        if (lifeTimer < 60 && (lifeTimer / 5) % 2 == 0) return;

        canvas.drawCircle((float) positionX, (float) positionY, radius, scrapPaint);
    }

    @Override
    public void update(double dt) {

    }

    public boolean isExpired() {
        return lifeTimer <= 0;
    }
}
