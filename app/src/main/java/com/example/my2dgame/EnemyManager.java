package com.example.my2dgame;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import com.example.my2dgame.object.Circle;
import com.example.my2dgame.object.Enemy;
import com.example.my2dgame.object.Player;
import com.example.my2dgame.object.Projectile;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class EnemyManager {
    private final List<Enemy> enemies = new ArrayList<>();
    private final List<Enemy> enemyPool = new ArrayList<>();
    private final List<Enemy> splitEnemies = new ArrayList<>();
    private final Random random = new Random();
    private final Context context;
    private final Player player;

    private int spawnTimer = 0;
    private int waveNumber = 0;
    private int enemiesToSpawn = 0;
    private boolean isBossWave = false;

    public EnemyManager(Context context, Player player) {
        this.context = context;
        this.player = player;
    }

    public void update(double dt, int screenWidth, int screenHeight, List<Projectile> projectiles, List<Projectile> projectilePool, Game game) {
        if (enemiesToSpawn > 0) {
            spawnTimer++;
            int spawnInterval = Math.max(Constants.MIN_SPAWN_INTERVAL, Constants.INITIAL_SPAWN_INTERVAL - (waveNumber * 5));
            if (spawnTimer >= spawnInterval) {
                if (isBossWave) spawnBoss(screenWidth, screenHeight);
                else spawnEnemy(screenWidth, screenHeight);
                enemiesToSpawn--;
                spawnTimer = 0;
            }
        }

        for (Enemy enemy : enemies) {
            enemy.update(dt);
            enemy.clampToScreen(screenWidth, screenHeight);
        }

        splitEnemies.clear();
        Iterator<Enemy> enemyIterator = enemies.iterator();
        while (enemyIterator.hasNext()) {
            Enemy enemy = enemyIterator.next();
            
            Iterator<Projectile> bulletIterator = projectiles.iterator();
            boolean enemyHit = false;
            while (bulletIterator.hasNext()) {
                Projectile p = bulletIterator.next();
                if (Circle.isColliding(p, enemy)) {
                    if (p.isPiercing()) {
                        if (!p.hasHitEnemy(enemy)) {
                            p.registerHit(enemy);
                            enemyHit = true;
                        }
                    } else {
                        projectilePool.add(p);
                        bulletIterator.remove();
                        enemyHit = true;
                    }
                    break;
                }
            }

            if (enemyHit) {
                if (enemy.takeDamage()) {
                    game.onEnemyDestroyed(enemy);
                    
                    if (enemy.getType() == EnemyType.SPLITTER) {
                        float scrapRadius = (float)(screenHeight * 0.03 * EnemyType.SMALL.getSizeMultiplier());
                        for (int i = 0; i < 3; i++) {
                            double angle = i * (2 * Math.PI / 3);
                            double dist = enemy.getRadius();
                            double sx = enemy.positionX() + Math.cos(angle) * dist;
                            double sy = enemy.positionY() + Math.sin(angle) * dist;
                            splitEnemies.add(obtainEnemy(EnemyType.SMALL.getColor(), sx, sy, scrapRadius, EnemyType.SMALL));
                        }
                    }
                    
                    enemyPool.add(enemy);
                    enemyIterator.remove();
                    continue;
                } else {
                    game.onEnemyHit(enemy);
                }
            }

            if (player != null && Circle.isColliding(player, enemy)) {
                game.onPlayerHit(enemy);
                if (!enemy.isBoss()) {
                    enemyPool.add(enemy);
                    enemyIterator.remove();
                }
            }
        }
        enemies.addAll(splitEnemies);
    }

    public void draw(Canvas canvas) {
        for (Enemy enemy : enemies) {
            enemy.draw(canvas);
        }
    }

    public void startNextWave(int waveNumber) {
        this.waveNumber = waveNumber;
        this.isBossWave = (waveNumber % 5 == 0);
        this.enemiesToSpawn = isBossWave ? 1 : 5 + (waveNumber * 2);
        this.spawnTimer = 0;
    }

    public boolean isWaveCleared() {
        return enemiesToSpawn == 0 && enemies.isEmpty();
    }

    public void reset() {
        enemyPool.addAll(enemies);
        enemies.clear();
        enemiesToSpawn = 0;
        spawnTimer = 0;
    }

    private void spawnEnemy(int screenWidth, int screenHeight) {
        EnemyType type = pickEnemyType();
        float baseRadius = (float) (screenHeight * 0.03);
        float enemyRadius = (float) (baseRadius * type.getSizeMultiplier());
        double x, y;
        int edge = random.nextInt(4);
        switch (edge) {
            case 0: x = random.nextDouble() * screenWidth; y = enemyRadius; break;
            case 1: x = screenWidth - enemyRadius; y = random.nextDouble() * screenHeight; break;
            case 2: x = random.nextDouble() * screenWidth; y = screenHeight - enemyRadius; break;
            default: x = enemyRadius; y = random.nextDouble() * screenHeight; break;
        }
        enemies.add(obtainEnemy(type.getColor(), x, y, enemyRadius, type));
    }

    private void spawnBoss(int screenWidth, int screenHeight) {
        EnemyType type = EnemyType.TANK;
        float bossRadius = (float) ((screenHeight * 0.03) * 2.5);
        Enemy boss = obtainEnemy(Color.RED, screenWidth / 2.0, bossRadius, bossRadius, type);
        boss.setAsBoss(10 + (waveNumber / 5) * 5);
        enemies.add(boss);
    }

    private Enemy obtainEnemy(int color, double x, double y, float radius, EnemyType type) {
        if (!enemyPool.isEmpty()) {
            Enemy e = enemyPool.remove(enemyPool.size() - 1);
            e.reset(color, x, y, radius, type);
            return e;
        }
        return new Enemy(color, player, x, y, radius, type, context);
    }

    private EnemyType pickEnemyType() {
        int roll = random.nextInt(100);
        // SPLITTER available from Wave 1 as requested
        if (roll < 10) return EnemyType.SPLITTER;
        if (waveNumber >= 4 && roll < 25) return EnemyType.ZIGZAG;
        if (waveNumber >= 3 && roll < 35) return EnemyType.TANK;
        if (waveNumber >= 2 && roll < 50) return EnemyType.FAST;
        return EnemyType.NORMAL;
    }

    public List<Enemy> getEnemies() {
        return enemies;
    }
}
