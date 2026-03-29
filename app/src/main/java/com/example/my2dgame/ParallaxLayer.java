package com.example.my2dgame;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ParallaxLayer {
    private final List<Star> stars = new ArrayList<>();
    private final float speedX, speedY;
    private final Paint paint = new Paint();
    private final int screenWidth, screenHeight;
    private final Path starPath = new Path();

    private static class Star {
        float x, y, radius;
        int alpha;
    }

    public ParallaxLayer(int screenWidth, int screenHeight, int count, float minRadius, float maxRadius, float speedX, float speedY, int color) {
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
        this.speedX = speedX;
        this.speedY = speedY;
        this.paint.setColor(color);
        this.paint.setStyle(Paint.Style.FILL);
        
        Random random = new Random();
        for (int i = 0; i < count; i++) {
            Star s = new Star();
            s.x = random.nextFloat() * screenWidth;
            s.y = random.nextFloat() * screenHeight;
            s.radius = minRadius + random.nextFloat() * (maxRadius - minRadius);
            s.alpha = 50 + random.nextInt(200);
            stars.add(s);
        }
    }

    public void update(double dt) {
        for (Star s : stars) {
            s.x += speedX * dt;
            s.y += speedY * dt;
            if (s.x < 0) s.x += screenWidth;
            if (s.x > screenWidth) s.x -= screenWidth;
            if (s.y < 0) s.y += screenHeight;
            if (s.y > screenHeight) s.y -= screenHeight;
        }
    }

    public void draw(Canvas canvas) {
        for (Star s : stars) {
            paint.setAlpha(s.alpha);
            drawStar(canvas, s.x, s.y, s.radius);
        }
    }

    /**
     * Draws a simple 4-pointed diamond/triangular star shape.
     */
    private void drawStar(Canvas canvas, float cx, float cy, float radius) {
        starPath.reset();
        // Top
        starPath.moveTo(cx, cy - radius * 1.5f);
        // Right
        starPath.lineTo(cx + radius * 0.8f, cy);
        // Bottom
        starPath.lineTo(cx, cy + radius * 1.5f);
        // Left
        starPath.lineTo(cx - radius * 0.8f, cy);
        starPath.close();
        
        canvas.drawPath(starPath, paint);
    }
}
