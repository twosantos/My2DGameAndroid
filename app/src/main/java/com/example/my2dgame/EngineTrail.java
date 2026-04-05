package com.example.my2dgame;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Manages a trail of particles emitted from the back of an entity.
 */
public class EngineTrail {
    private final List<TrailParticle> particles = new ArrayList<>();
    private final List<TrailParticle> pool = new ArrayList<>();
    private final Paint paint = new Paint();
    private final int maxParticles;
    private final int color;
    private final float sizeMultiplier;

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

    public EngineTrail(int maxParticles, float sizeMultiplier) {
        this.color = Color.RED;
        this.maxParticles = maxParticles;
        this.sizeMultiplier = sizeMultiplier;
        paint.setColor(Color.RED);
        paint.setStyle(Paint.Style.FILL);
    }

    public void emit(double px, double py, float entityRadius, float angleDegrees) {
        // angleDegrees is where the entity faces. The back is opposite.
        // For Player (+90 offset): back is angleDegrees - 90
        // For Projectile (-45 offset): we need to calculate based on the specific asset orientation
        // However, generic logic: back is angleDegrees + 180 (relative to facing)
        // Adjusting back based on the ship orientation (+90):
        double angleRad = Math.toRadians(angleDegrees - 90);
        float ox = (float) (px - Math.cos(angleRad) * entityRadius * 0.8f);
        float oy = (float) (py - Math.sin(angleRad) * entityRadius * 0.8f);

        TrailParticle p;
        if (!pool.isEmpty()) {
            p = pool.remove(pool.size() - 1);
        } else if (particles.size() < maxParticles) {
            p = new TrailParticle();
        } else {
            return;
        }
        
        p.init(ox, oy, entityRadius * sizeMultiplier);
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

    public void reset() {
        pool.addAll(particles);
        particles.clear();
    }
}
