package com.example.my2dgame;

import android.content.Context;
import android.graphics.Canvas;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

/**
 * Manages all particles in the game, including pooling.
 */
public class ParticleManager {
    private final List<Particle> particles = new ArrayList<>();
    private final List<Particle> pool = new ArrayList<>();
    private final Context context;
    private final Random random = new Random();

    public ParticleManager(Context context) {
        this.context = context;
    }

    public void spawnExplosion(double x, double y, int color) {
        for (int i = 0; i < Constants.PARTICLE_COUNT; i++) {
            double angle = random.nextDouble() * 2 * Math.PI;
            float pRadius = 3 + random.nextFloat() * 4;
            Particle p;
            if (!pool.isEmpty()) {
                p = pool.remove(pool.size() - 1);
            } else {
                p = new Particle();
            }
            p.init(x, y, pRadius, color, Math.cos(angle), Math.sin(angle), context);
            particles.add(p);
        }
    }

    public void update(double dt) {
        Iterator<Particle> it = particles.iterator();
        while (it.hasNext()) {
            Particle p = it.next();
            p.update(dt);
            if (p.isDead()) {
                pool.add(p);
                it.remove();
            }
        }
    }

    public void draw(Canvas canvas) {
        for (Particle p : particles) {
            p.draw(canvas);
        }
    }

    public void reset() {
        pool.addAll(particles);
        particles.clear();
    }
}
