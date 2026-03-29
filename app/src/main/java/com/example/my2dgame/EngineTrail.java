package com.example.my2dgame;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Manages a trail of particles emitted from the back of the spaceship.
 */
public class EngineTrail {
    private final List<TrailParticle> particles = new ArrayList<>();
    private final List<TrailParticle> pool = new ArrayList<>();
    private final Paint paint = new Paint();
    private static final int MAX_PARTICLES = 50;

    private static class TrailParticle {
        float x, y, radius;
        int alpha;
        
        void init(float x, float y, float radius) {
            this.x = x;
            this.y = y;
            this.radius = radius;
            this.alpha = 150;
        }
        
        boolean update() {
            alpha -= 15;
            radius *= 0.95f;
            return alpha <= 0;
        }
    }

    public EngineTrail() {
        paint.setColor(Color.CYAN); // Classic engine glow color
        paint.setStyle(Paint.Style.FILL);
    }

    public void emit(double px, double py, float shipRadius, float angleDegrees) {
        // Calculate the back of the ship
        // angleDegrees is where the ship faces. The back is opposite.
        double angleRad = Math.toRadians(angleDegrees - 90); // -90 because our "Up" was +90
        float ox = (float) (px - Math.cos(angleRad) * shipRadius * 0.8f);
        float oy = (float) (py - Math.sin(angleRad) * shipRadius * 0.8f);

        TrailParticle p;
        if (!pool.isEmpty()) {
            p = pool.remove(pool.size() - 1);
        } else if (particles.size() < MAX_PARTICLES) {
            p = new TrailParticle();
        } else {
            return;
        }
        
        p.init(ox, oy, shipRadius * 0.4f);
        particles.add(p);
    }

    public void update() {
        Iterator<TrailParticle> it = particles.iterator();
        while (it.hasNext()) {
            TrailParticle p = it.next();
            if (p.update()) {
                pool.add(p);
                it.remove();
            }
        }
    }

    public void draw(Canvas canvas) {
        for (TrailParticle p : particles) {
            paint.setAlpha(p.alpha);
            canvas.drawCircle(p.x, p.y, p.radius, paint);
        }
    }
}
