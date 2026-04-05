package com.example.my2dgame;

import android.graphics.Canvas;
import com.example.my2dgame.object.Circle;
import com.example.my2dgame.object.Enemy;
import com.example.my2dgame.object.Player;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

/**
 * Manages Star Scrap entities, including spawning based on enemy types and player collection.
 */
public class ScrapManager {
    private final List<Scrap> scraps = new ArrayList<>();
    private final List<Scrap> pool = new ArrayList<>();
    private final Random random = new Random();

    public void onEnemyDestroyed(Enemy enemy) {
        EnemyType type = enemy.getType();
        int roll = random.nextInt(100);
        int chance = 2; // Default 2%
        int amount = 1;

        if (type == EnemyType.SPLITTER || type == EnemyType.ZIGZAG) {
            chance = 10;
        } else if (enemy.isBoss()) {
            chance = 100;
            amount = 15;
        }

        if (roll < chance) {
            for (int i = 0; i < amount; i++) {
                spawnScrap(enemy.positionX() + (random.nextDouble() - 0.5) * 20,
                           enemy.positionY() + (random.nextDouble() - 0.5) * 20);
            }
        }
    }

    private void spawnScrap(double x, double y) {
        Scrap s;
        if (!pool.isEmpty()) {
            s = pool.remove(pool.size() - 1);
            s.init(x, y, 15f);
        } else {
            s = new Scrap(x, y, 15f);
        }
        scraps.add(s);
    }

    public void update(double dt, Player player, Game game) {
        float magnetRadius = player.getMagnetRadius();
        Iterator<Scrap> it = scraps.iterator();
        while (it.hasNext()) {
            Scrap s = it.next();
            s.update(dt, player.positionX(), player.positionY(), magnetRadius);

            if (Circle.isColliding(player, s)) {
                game.onScrapCollected(1);
                pool.add(s);
                it.remove();
            } else if (s.isExpired()) {
                pool.add(s);
                it.remove();
            }
        }
    }

    public void draw(Canvas canvas) {
        for (Scrap s : scraps) {
            s.draw(canvas);
        }
    }

    public void reset() {
        pool.addAll(scraps);
        scraps.clear();
    }
}
