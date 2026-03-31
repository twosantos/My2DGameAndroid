package com.example.my2dgame;

import android.content.Context;
import android.graphics.Canvas;
import com.example.my2dgame.object.Enemy;
import com.example.my2dgame.object.Player;
import com.example.my2dgame.object.Projectile;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Manages all projectiles fired by the player, including pooling and homing logic.
 */
public class ProjectileManager {
    private final List<Projectile> projectiles = new ArrayList<>();
    private final List<Projectile> pool = new ArrayList<>();
    private final Context context;

    public ProjectileManager(Context context) {
        this.context = context;
    }

    public void fire(double x, double y, float radius, double dirX, double dirY, Player player, EnemyManager enemyManager) {
        Projectile p;
        if (!pool.isEmpty()) {
            p = pool.remove(pool.size() - 1);
            p.reset(x, y, radius, dirX, dirY);
        } else {
            p = new Projectile(x, y, radius, dirX, dirY, context);
        }

        if (player.hasHoming()) {
            Enemy closest = findNearestEnemy(x, y, enemyManager.getEnemies());
            if (closest != null) {
                p.setHoming(closest);
            }
        }
        projectiles.add(p);
    }

    private Enemy findNearestEnemy(double x, double y, List<Enemy> enemies) {
        Enemy closest = null;
        double minDist = Double.MAX_VALUE;
        for (Enemy e : enemies) {
            double dx = e.positionX() - x;
            double dy = e.positionY() - y;
            double dist = dx * dx + dy * dy;
            if (dist < minDist) {
                minDist = dist;
                closest = e;
            }
        }
        return closest;
    }

    public void update(double dt, int screenWidth, int screenHeight) {
        Iterator<Projectile> it = projectiles.iterator();
        while (it.hasNext()) {
            Projectile p = it.next();
            p.update(dt);
            if (p.isOffScreen(screenWidth, screenHeight)) {
                pool.add(p);
                it.remove();
            }
        }
    }

    public void draw(Canvas canvas) {
        for (Projectile p : projectiles) {
            p.draw(canvas);
        }
    }

    public List<Projectile> getProjectiles() {
        return projectiles;
    }

    public List<Projectile> getPool() {
        return pool;
    }

    public void reset() {
        pool.addAll(projectiles);
        projectiles.clear();
    }
}
