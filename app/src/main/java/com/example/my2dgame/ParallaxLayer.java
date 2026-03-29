package com.example.my2dgame;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RadialGradient;
import android.graphics.Shader;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ParallaxLayer {
    private final List<Star> stars = new ArrayList<>();
    private final List<Nebula> nebulas = new ArrayList<>();
    private final float speedX, speedY;
    private final Paint paint = new Paint();
    private final Paint nebulaPaint = new Paint();
    private final int screenWidth, screenHeight;
    private final Path starPath = new Path();

    private static class Star {
        float x, y, radius;
        int alpha;
    }

    private static class Nebula {
        float x, y, radius;
        int color;
    }

    public ParallaxLayer(int screenWidth, int screenHeight, int count, float minRadius, float maxRadius, float speedX, float speedY, int color) {
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
        this.speedX = speedX;
        this.speedY = speedY;
        this.paint.setColor(color);
        this.paint.setStyle(Paint.Style.FILL);
        this.paint.setAntiAlias(true);
        this.nebulaPaint.setStyle(Paint.Style.FILL);
        
        Random random = new Random();
        for (int i = 0; i < count; i++) {
            Star s = new Star();
            s.x = random.nextFloat() * screenWidth;
            s.y = random.nextFloat() * screenHeight;
            s.radius = minRadius + random.nextFloat() * (maxRadius - minRadius);
            s.alpha = 50 + random.nextInt(200);
            stars.add(s);
        }

        // Variety: Add procedural nebulas to the furthest/slowest layer
        if (speedY < 20 && count > 50) {
            for (int i = 0; i < 3; i++) {
                Nebula n = new Nebula();
                n.x = random.nextFloat() * screenWidth;
                n.y = random.nextFloat() * screenHeight;
                n.radius = 300 + random.nextFloat() * 400;
                
                int[] nebulaPool = {Color.parseColor("#330033"), Color.parseColor("#000033"), Color.parseColor("#003333")};
                n.color = nebulaPool[random.nextInt(nebulaPool.length)];
                nebulas.add(n);
            }
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
        
        for (Nebula n : nebulas) {
            n.x += speedX * dt;
            n.y += speedY * dt;
            // Wider wrapping for large nebulas
            if (n.y - n.radius > screenHeight) n.y = -n.radius;
            if (n.y + n.radius < 0) n.y = screenHeight + n.radius;
        }
    }

    public void draw(Canvas canvas) {
        // Draw Nebulas (Atmospheric variety)
        for (Nebula n : nebulas) {
            RadialGradient gradient = new RadialGradient(n.x, n.y, n.radius,
                    n.color, Color.TRANSPARENT, Shader.TileMode.CLAMP);
            nebulaPaint.setShader(gradient);
            canvas.drawCircle(n.x, n.y, n.radius, nebulaPaint);
        }

        // Draw Diamond Stars
        for (Star s : stars) {
            paint.setAlpha(s.alpha);
            drawStar(canvas, s.x, s.y, s.radius);
        }
    }

    private void drawStar(Canvas canvas, float cx, float cy, float radius) {
        starPath.reset();
        starPath.moveTo(cx, cy - radius * 1.5f);
        starPath.lineTo(cx + radius * 0.8f, cy);
        starPath.lineTo(cx, cy + radius * 1.5f);
        starPath.lineTo(cx - radius * 0.8f, cy);
        starPath.close();
        canvas.drawPath(starPath, paint);
    }
}
