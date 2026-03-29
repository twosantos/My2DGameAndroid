package com.example.my2dgame;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.example.my2dgame.object.Circle;
import com.example.my2dgame.object.Enemy;
import com.example.my2dgame.object.Pickup;
import com.example.my2dgame.object.Player;
import com.example.my2dgame.object.Projectile;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class Game extends SurfaceView implements SurfaceHolder.Callback {

    private static final int INITIAL_SPAWN_INTERVAL = 90; // 3 seconds at 30 UPS
    private static final int MIN_SPAWN_INTERVAL = 20;
    private static final int FIRE_INTERVAL = 10; // auto-fire every 0.33s at 30 UPS
    private static final int RAPID_FIRE_INTERVAL = 4; // fast-fire every 0.13s
    private static final int DAMAGE_FLASH_DURATION = 6; // frames (~0.2s at 30 UPS)
    private static final int KILL_SCORE_BONUS = 3;
    private static final int BOSS_SCORE_BONUS = 100;
    private static final int PAUSE_BUTTON_SIZE = 80;
    private static final int PAUSE_BUTTON_MARGIN = 20;
    private static final int PARTICLE_COUNT = 8;
    private static final int SHAKE_DURATION = 4;
    private static final int BOSS_SHAKE_DURATION = 12;
    private static final float SHAKE_INTENSITY = 12f;
    private static final float BOSS_SHAKE_INTENSITY = 35f;
    private static final int PICKUP_SPAWN_INTERVAL = 450; // every 15 seconds
    
    // Wave system constants
    private static final int WAVE_BREAK_DURATION = 90; // 3 seconds
    private static final int WAVE_ANNOUNCEMENT_DURATION = 60; // 2 seconds

    private GameLoop gameLoop;
    private final Paint statsPaint;
    private final Paint titlePaint;
    private final Paint subtitlePaint;
    private final Paint scorePaint;
    private final Paint healthBarPaint;
    private final Paint healthBarBgPaint;
    private final Paint damageFlashPaint;
    private final Paint pauseButtonPaint;
    private final Random random = new Random();
    private final SoundManager soundManager;
    private final SharedPreferences prefs;

    private Joystick joystick;
    private Joystick aimJoystick;
    private int movePointerId = -1;
    private int aimPointerId = -1;
    private Player player;
    private final List<Enemy> enemies = new ArrayList<>();
    private final List<Projectile> projectiles = new ArrayList<>();
    private final List<Enemy> enemyPool = new ArrayList<>();
    private final List<Projectile> projectilePool = new ArrayList<>();
    private final List<Particle> particles = new ArrayList<>();
    private final List<Particle> particlePool = new ArrayList<>();
    private final List<Pickup> pickups = new ArrayList<>();
    private int screenWidth;
    private int screenHeight;

    private GameState gameState = GameState.MENU;
    private int score = 0;
    private int scoreTimer = 0;
    private int spawnTimer = 0;
    private int fireTimer = 0;
    private int damageFlashTimer = 0;
    private int pickupTimer = 0;
    private int highScore;
    private boolean isNewHighScore = false;
    private long gameOverTimestamp = 0;
    private static final long GAME_OVER_COOLDOWN_MS = 1000;
    private int shakeTimer = 0;
    private float currentShakeIntensity = SHAKE_INTENSITY;
    private int currentShakeDuration = SHAKE_DURATION;

    // Wave system state
    private int waveNumber = 0;
    private int enemiesToSpawn = 0;
    private int waveBreakTimer = 0;
    private int waveAnnouncementTimer = 0;
    private boolean isBossWave = false;

    public Game(Context context) {
        super(context);

        SurfaceHolder surfaceHolder = getHolder();
        surfaceHolder.addCallback(this);

        statsPaint = new Paint();
        statsPaint.setColor(ContextCompat.getColor(context, R.color.magenta));
        statsPaint.setTextSize(50);

        titlePaint = new Paint();
        titlePaint.setColor(Color.WHITE);
        titlePaint.setTextSize(120);
        titlePaint.setTextAlign(Paint.Align.CENTER);

        subtitlePaint = new Paint();
        subtitlePaint.setColor(Color.LTGRAY);
        subtitlePaint.setTextSize(60);
        subtitlePaint.setTextAlign(Paint.Align.CENTER);

        scorePaint = new Paint();
        scorePaint.setColor(Color.WHITE);
        scorePaint.setTextSize(50);
        scorePaint.setTextAlign(Paint.Align.CENTER);

        healthBarPaint = new Paint();
        healthBarBgPaint = new Paint();
        healthBarBgPaint.setColor(ContextCompat.getColor(context, R.color.health_bar_bg));

        damageFlashPaint = new Paint();
        damageFlashPaint.setColor(Color.RED);
        damageFlashPaint.setAlpha(100);

        pauseButtonPaint = new Paint();
        pauseButtonPaint.setColor(Color.WHITE);
        pauseButtonPaint.setAlpha(180);

        soundManager = new SoundManager(context);
        soundManager.startMusic();

        prefs = context.getSharedPreferences("my2dgame", Context.MODE_PRIVATE);
        highScore = prefs.getInt("high_score", 0);

        // Preload all sprites to avoid lag during gameplay
        SpriteCache.preload(context,
            R.drawable.spaceship,
            R.drawable.asteroid,
            R.drawable.rocket,
            R.drawable.spiky_explosion
        );

        setFocusable(true);
    }

    public void pause() {
        if (gameState == GameState.PLAYING) {
            gameState = GameState.PAUSED;
            releaseAllJoysticks();
        }
        soundManager.pauseMusic();
    }

    public void resume() {
        soundManager.resumeMusic();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (gameState) {
            case MENU:
            case GAME_OVER:
                if (event.getActionMasked() == MotionEvent.ACTION_DOWN
                        && System.currentTimeMillis() - gameOverTimestamp >= GAME_OVER_COOLDOWN_MS) {
                    startGame();
                }
                return true;
            case PAUSED:
                if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
                    gameState = GameState.PLAYING;
                }
                return true;
            case PLAYING:
                return handlePlayingTouch(event);
        }
        return super.onTouchEvent(event);
    }

    private boolean handlePlayingTouch(MotionEvent event) {
        int actionIndex = event.getActionIndex();
        int pointerId = event.getPointerId(actionIndex);
        float x = event.getX(actionIndex);
        float y = event.getY(actionIndex);

        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_POINTER_DOWN:
                if (isTouchOnPauseButton(x, y)) {
                    gameState = GameState.PAUSED;
                    releaseAllJoysticks();
                    return true;
                }
                // Left half → movement, right half → aim
                if (x < screenWidth / 2.0) {
                    movePointerId = pointerId;
                    joystick.setCenter((int) x, (int) y);
                } else {
                    aimPointerId = pointerId;
                    aimJoystick.setCenter((int) x, (int) y);
                }
                return true;

            case MotionEvent.ACTION_MOVE:
                for (int i = 0; i < event.getPointerCount(); i++) {
                    int id = event.getPointerId(i);
                    if (id == movePointerId) {
                        joystick.setActuator(event.getX(i), event.getY(i));
                    } else if (id == aimPointerId) {
                        aimJoystick.setActuator(event.getX(i), event.getY(i));
                    }
                }
                return true;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_POINTER_UP:
                if (pointerId == movePointerId) {
                    joystick.notPressed();
                    movePointerId = -1;
                } else if (pointerId == aimPointerId) {
                    aimJoystick.notPressed();
                    aimPointerId = -1;
                }
                return true;
        }
        return true;
    }

    private void releaseAllJoysticks() {
        if (joystick != null) joystick.notPressed();
        if (aimJoystick != null) aimJoystick.notPressed();
        movePointerId = -1;
        aimPointerId = -1;
    }

    private boolean isTouchOnPauseButton(float x, float y) {
        float btnX = screenWidth - PAUSE_BUTTON_SIZE - PAUSE_BUTTON_MARGIN;
        float btnY = PAUSE_BUTTON_MARGIN;
        return x >= btnX && x <= btnX + PAUSE_BUTTON_SIZE
                && y >= btnY && y <= btnY + PAUSE_BUTTON_SIZE;
    }

    private void startGame() {
        player.reset(screenWidth / 2.0, screenHeight / 2.0);
        // Pool active objects before clearing
        enemyPool.addAll(enemies);
        projectilePool.addAll(projectiles);
        particlePool.addAll(particles);
        enemies.clear();
        projectiles.clear();
        particles.clear();
        pickups.clear();
        score = 0;
        scoreTimer = 0;
        spawnTimer = 0;
        fireTimer = 0;
        damageFlashTimer = 0;
        pickupTimer = 0;
        shakeTimer = 0;
        
        // Reset wave system
        waveNumber = 0;
        enemiesToSpawn = 0;
        waveBreakTimer = 30; // Short delay before first wave
        waveAnnouncementTimer = 0;
        isBossWave = false;
        
        releaseAllJoysticks();
        gameState = GameState.PLAYING;
        soundManager.playStart();
    }

    @Override
    public void surfaceCreated(@NonNull SurfaceHolder surfaceHolder) {
        screenWidth = getWidth();
        screenHeight = getHeight();

        if (joystick == null) {
            joystick = new Joystick(
                    (int) (screenWidth * 0.12),
                    (int) (screenHeight * 0.75),
                    (int) (screenHeight * 0.08),
                    (int) (screenHeight * 0.045),
                    Color.BLUE,
                    Color.GRAY
            );
        }
        if (aimJoystick == null) {
            aimJoystick = new Joystick(
                    (int) (screenWidth * 0.88),
                    (int) (screenHeight * 0.75),
                    (int) (screenHeight * 0.08),
                    (int) (screenHeight * 0.045),
                    Color.RED,
                    Color.DKGRAY
            );
        }
        if (player == null) {
            player = new Player(
                    getContext(),
                    joystick,
                    screenWidth / 2.0,
                    screenHeight / 2.0,
                    (float) (screenHeight * 0.035)
            );
        }

        if (gameLoop == null || !gameLoop.isAlive()) {
            gameLoop = new GameLoop(this, surfaceHolder);
            gameLoop.startLoop();
        }
    }

    @Override
    public void surfaceChanged(@NonNull SurfaceHolder surfaceHolder, int i, int i1, int i2) {
    }

    @Override
    public void surfaceDestroyed(@NonNull SurfaceHolder surfaceHolder) {
        if (gameLoop != null) {
            gameLoop.stopLoop();
            gameLoop = null;
        }
    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
        if (canvas == null) return;
        
        switch (gameState) {
            case MENU:
                drawMenu(canvas);
                break;
            case PLAYING:
                drawPlaying(canvas);
                break;
            case PAUSED:
                drawPlaying(canvas);
                drawPauseOverlay(canvas);
                break;
            case GAME_OVER:
                drawGameOver(canvas);
                break;
        }
    }

    private void drawMenu(Canvas canvas) {
        canvas.drawColor(Color.BLACK);
        canvas.drawText("My2DGame", screenWidth / 2f, screenHeight / 2f - 80, titlePaint);
        canvas.drawText("Tap to Start", screenWidth / 2f, screenHeight / 2f + 30, subtitlePaint);
        if (highScore > 0) {
            canvas.drawText("High Score: " + highScore, screenWidth / 2f, screenHeight / 2f + 120, subtitlePaint);
        }
    }

    private void drawPlaying(Canvas canvas) {
        // Screen shake offset
        boolean shaking = shakeTimer > 0;
        if (shaking) {
            float progress = (float) shakeTimer / currentShakeDuration;
            float intensity = progress * currentShakeIntensity;
            float offsetX = (random.nextFloat() * 2 - 1) * intensity;
            float offsetY = (random.nextFloat() * 2 - 1) * intensity;
            canvas.save();
            canvas.translate(offsetX, offsetY);
        }

        // Game objects
        if (player != null) player.draw(canvas);
        for (Enemy enemy : enemies) {
            enemy.draw(canvas);
        }
        for (Projectile projectile : projectiles) {
            projectile.draw(canvas);
        }
        for (Particle particle : particles) {
            particle.draw(canvas);
        }
        for (Pickup pickup : pickups) {
            pickup.draw(canvas);
        }
        if (joystick != null) joystick.draw(canvas);
        if (aimJoystick != null) aimJoystick.draw(canvas);

        if (shaking) {
            canvas.restore();
        }

        // Damage flash overlay (drawn after restore so it doesn't shake)
        if (damageFlashTimer > 0) {
            canvas.drawColor(Color.argb(100, 255, 0, 0));
        }

        // HUD (stable, no shake)
        drawHealthBar(canvas);
        drawScore(canvas);
        
        // Wave Announcement
        if (waveAnnouncementTimer > 0) {
            titlePaint.setAlpha(Math.min(255, waveAnnouncementTimer * 10));
            String text = isBossWave ? "BOSS WAVE!" : "Wave " + waveNumber;
            canvas.drawText(text, screenWidth / 2f, screenHeight / 2f, titlePaint);
            titlePaint.setAlpha(255);
        }
        
        drawPauseButton(canvas);
        drawUPS(canvas);
        drawFPS(canvas);
    }

    private void drawPauseButton(Canvas canvas) {
        float btnX = screenWidth - PAUSE_BUTTON_SIZE - PAUSE_BUTTON_MARGIN;
        float btnY = PAUSE_BUTTON_MARGIN;
        float barWidth = PAUSE_BUTTON_SIZE * 0.2f;
        float barHeight = PAUSE_BUTTON_SIZE * 0.6f;
        float barY = btnY + PAUSE_BUTTON_SIZE * 0.2f;
        float gap = PAUSE_BUTTON_SIZE * 0.15f;
        float centerX = btnX + PAUSE_BUTTON_SIZE / 2f;

        canvas.drawRect(centerX - gap - barWidth, barY, centerX - gap, barY + barHeight, pauseButtonPaint);
        canvas.drawRect(centerX + gap, barY, centerX + gap + barWidth, barY + barHeight, pauseButtonPaint);
    }

    private void drawPauseOverlay(Canvas canvas) {
        canvas.drawColor(Color.argb(150, 0, 0, 0));
        canvas.drawText("Paused", screenWidth / 2f, screenHeight / 2f - 50, titlePaint);
        canvas.drawText("Tap to Resume", screenWidth / 2f, screenHeight / 2f + 60, subtitlePaint);
    }

    private void drawGameOver(Canvas canvas) {
        canvas.drawColor(Color.BLACK);
        canvas.drawText("Game Over", screenWidth / 2f, screenHeight / 2f - 100, titlePaint);
        canvas.drawText("Score: " + score, screenWidth / 2f, screenHeight / 2f + 20, subtitlePaint);
        if (isNewHighScore) {
            canvas.drawText("New High Score!", screenWidth / 2f, screenHeight / 2f + 100, subtitlePaint);
        } else {
            canvas.drawText("High Score: " + highScore, screenWidth / 2f, screenHeight / 2f + 100, subtitlePaint);
        }
        canvas.drawText("Tap to Restart", screenWidth / 2f, screenHeight / 2f + 180, subtitlePaint);
    }

    private void drawHealthBar(Canvas canvas) {
        if (player == null) return;
        float barX = 30;
        float barY = 20;
        float barWidth = 300;
        float barHeight = 25;
        float healthRatio = (float) player.getHealthPoints() / player.getMaxHealth();

        if (healthRatio > 0.6f) {
            healthBarPaint.setColor(Color.GREEN);
        } else if (healthRatio > 0.3f) {
            healthBarPaint.setColor(Color.YELLOW);
        } else {
            healthBarPaint.setColor(Color.RED);
        }

        canvas.drawRect(barX, barY, barX + barWidth, barY + barHeight, healthBarBgPaint);
        canvas.drawRect(barX, barY, barX + barWidth * healthRatio, barY + barHeight, healthBarPaint);
    }

    private void drawScore(Canvas canvas) {
        canvas.drawText("Score: " + score, screenWidth / 2f, 50, scorePaint);
        if (waveNumber > 0) {
            canvas.drawText("Wave: " + waveNumber, screenWidth / 2f, 110, scorePaint);
        }
    }

    private void drawUPS(Canvas canvas) {
        if (gameLoop == null) return;
        String averageUPS = Double.toString(gameLoop.getAverageUPS());
        canvas.drawText("UPS " + averageUPS, 30, 80, statsPaint);
    }

    private void drawFPS(Canvas canvas) {
        if (gameLoop == null) return;
        String averageFPS = Double.toString(gameLoop.getAverageFPS());
        canvas.drawText("FPS " + averageFPS, 30, 130, statsPaint);
    }

    public void update(double dt) {
        if (gameState != GameState.PLAYING) {
            return;
        }

        // Decrement timers
        if (damageFlashTimer > 0) damageFlashTimer--;
        if (shakeTimer > 0) shakeTimer--;
        
        // Wave system timers
        if (waveBreakTimer > 0) {
            waveBreakTimer--;
            if (waveBreakTimer == 0) {
                startNextWave();
            }
        }
        if (waveAnnouncementTimer > 0) waveAnnouncementTimer--;

        if (joystick != null) joystick.update();
        if (aimJoystick != null) aimJoystick.update();
        if (player != null) {
            player.update(dt);
            player.clampToScreen(screenWidth, screenHeight);
        }

        // Fire projectiles in aim joystick direction
        if (aimJoystick != null && player != null) {
            double ax = aimJoystick.actuatorX();
            double ay = aimJoystick.actuatorY();
            if (ax != 0 || ay != 0) {
                fireTimer++;
                int interval = player.hasRapidFire() ? RAPID_FIRE_INTERVAL : FIRE_INTERVAL;
                if (fireTimer >= interval) {
                    double mag = Math.sqrt(ax * ax + ay * ay);
                    float bulletRadius = (float) (screenHeight * 0.01);
                    projectiles.add(obtainProjectile(
                            player.positionX(), player.positionY(),
                            bulletRadius, ax / mag, ay / mag
                    ));
                    fireTimer = 0;
                }
            } else {
                fireTimer = 0;
            }
        }

        // Update projectiles, remove off-screen
        Iterator<Projectile> projIterator = projectiles.iterator();
        while (projIterator.hasNext()) {
            Projectile p = projIterator.next();
            p.update(dt);
            if (p.isOffScreen(screenWidth, screenHeight)) {
                projectilePool.add(p);
                projIterator.remove();
            }
        }

        // Update enemies
        for (Enemy enemy : enemies) {
            enemy.update(dt);
            enemy.clampToScreen(screenWidth, screenHeight);
        }

        // Update Pickups
        Iterator<Pickup> pickupIterator = pickups.iterator();
        while (pickupIterator.hasNext()) {
            Pickup p = pickupIterator.next();
            p.update(dt);
            if (Circle.isColliding(player, p)) {
                player.applyPickup(p.getType());
                pickupIterator.remove();
            } else if (p.isExpired()) {
                pickupIterator.remove();
            }
        }

        // Projectile-enemy collisions
        Iterator<Enemy> enemyIterator = enemies.iterator();
        while (enemyIterator.hasNext()) {
            Enemy enemy = enemyIterator.next();
            Iterator<Projectile> bulletIterator = projectiles.iterator();
            boolean enemyHit = false;
            while (bulletIterator.hasNext()) {
                Projectile p = bulletIterator.next();
                if (Circle.isColliding(p, enemy)) {
                    projectilePool.add(p);
                    bulletIterator.remove();
                    enemyHit = true;
                    break;
                }
            }
            if (enemyHit) {
                if (enemy.takeDamage()) {
                    spawnParticles(enemy.positionX(), enemy.positionY(), enemy.getType().getColor());
                    enemyPool.add(enemy);
                    enemyIterator.remove();
                    score += enemy.isBoss() ? BOSS_SCORE_BONUS : KILL_SCORE_BONUS;
                    
                    // Kill shake
                    if (enemy.isBoss()) {
                        triggerShake(BOSS_SHAKE_DURATION, BOSS_SHAKE_INTENSITY);
                    } else {
                        triggerShake(SHAKE_DURATION, SHAKE_INTENSITY * 0.8f);
                    }
                } else {
                    // Impact shake (lighter)
                    triggerShake(2, SHAKE_INTENSITY * 0.3f);
                }
            }
        }

        // Player-enemy collisions
        if (player != null) {
            enemyIterator = enemies.iterator();
            while (enemyIterator.hasNext()) {
                Enemy enemy = enemyIterator.next();
                if (Circle.isColliding(player, enemy)) {
                    player.takeDamage();
                    spawnParticles(enemy.positionX(), enemy.positionY(), enemy.getType().getColor());
                    
                    if (!enemy.isBoss()) {
                        enemyPool.add(enemy);
                        enemyIterator.remove();
                    }
                    
                    soundManager.playHit();
                    damageFlashTimer = DAMAGE_FLASH_DURATION;
                    
                    // Heavy shake when player is hit
                    triggerShake(BOSS_SHAKE_DURATION, BOSS_SHAKE_INTENSITY);
                }
            }
        }

        // Update particles
        Iterator<Particle> partIterator = particles.iterator();
        while (partIterator.hasNext()) {
            Particle p = partIterator.next();
            p.update(dt);
            if (p.isDead()) {
                particlePool.add(p);
                partIterator.remove();
            }
        }

        if (player != null && !player.isAlive()) {
            gameState = GameState.GAME_OVER;
            gameOverTimestamp = System.currentTimeMillis();
            isNewHighScore = score > highScore;
            if (isNewHighScore) {
                highScore = score;
                prefs.edit().putInt("high_score", highScore).apply();
            }
            soundManager.playGameOver();
            return;
        }

        // Pickup spawning
        pickupTimer++;
        if (pickupTimer >= PICKUP_SPAWN_INTERVAL) {
            spawnPickup();
            pickupTimer = 0;
        }

        // Structured Enemy spawning
        if (enemiesToSpawn > 0) {
            spawnTimer++;
            int spawnInterval = Math.max(MIN_SPAWN_INTERVAL, INITIAL_SPAWN_INTERVAL - (waveNumber * 5));
            if (spawnTimer >= spawnInterval) {
                if (isBossWave) {
                    spawnBoss();
                } else {
                    spawnEnemy();
                }
                enemiesToSpawn--;
                spawnTimer = 0;
            }
        } else if (enemies.isEmpty() && waveBreakTimer == 0 && waveNumber > 0) {
            // Wave cleared
            waveBreakTimer = WAVE_BREAK_DURATION;
        }

        // Survival score: +1 per second
        scoreTimer++;
        if (scoreTimer >= (int) GameLoop.MAX_UPS) {
            score++;
            scoreTimer = 0;
        }
    }

    private void triggerShake(int duration, float intensity) {
        if (intensity > currentShakeIntensity || shakeTimer <= 0) {
            shakeTimer = duration;
            currentShakeDuration = duration;
            currentShakeIntensity = intensity;
        }
    }

    private void startNextWave() {
        waveNumber++;
        isBossWave = (waveNumber % 5 == 0);
        
        if (isBossWave) {
            enemiesToSpawn = 1;
        } else {
            enemiesToSpawn = 5 + (waveNumber * 2);
        }
        
        waveAnnouncementTimer = WAVE_ANNOUNCEMENT_DURATION;
        spawnTimer = 0;
    }

    private EnemyType pickEnemyType() {
        int roll = random.nextInt(100);
        if (waveNumber >= 4 && roll < 15) return EnemyType.ZIGZAG;
        if (waveNumber >= 3 && roll < 25) return EnemyType.TANK;
        if (waveNumber >= 2 && roll < 40) return EnemyType.FAST;
        return EnemyType.NORMAL;
    }

    private void spawnEnemy() {
        EnemyType type = pickEnemyType();
        float baseRadius = (float) (screenHeight * 0.03);
        float enemyRadius = (float) (baseRadius * type.getSizeMultiplier());
        double x, y;
        int edge = random.nextInt(4);
        switch (edge) {
            case 0: // top
                x = random.nextDouble() * screenWidth;
                y = enemyRadius;
                break;
            case 1: // right
                x = screenWidth - enemyRadius;
                y = random.nextDouble() * screenHeight;
                break;
            case 2: // bottom
                x = random.nextDouble() * screenWidth;
                y = screenHeight - enemyRadius;
                break;
            default: // left
                x = enemyRadius;
                y = random.nextDouble() * screenHeight;
                break;
        }
        enemies.add(obtainEnemy(type.getColor(), x, y, enemyRadius, type));
    }

    private void spawnBoss() {
        EnemyType type = EnemyType.TANK; // Use tank stats as base
        float baseRadius = (float) (screenHeight * 0.03);
        float bossRadius = (float) (baseRadius * 2.5);
        double x = screenWidth / 2.0;
        double y = bossRadius;
        
        Enemy boss = obtainEnemy(Color.RED, x, y, bossRadius, type);
        boss.setAsBoss(10 + (waveNumber / 5) * 5); // Scaling boss health
        enemies.add(boss);
    }

    private void spawnPickup() {
        PickupType[] types = PickupType.values();
        PickupType type = types[random.nextInt(types.length)];
        float radius = 25;
        double x = radius + random.nextDouble() * (screenWidth - 2 * radius);
        double y = radius + random.nextDouble() * (screenHeight - 2 * radius);
        pickups.add(new Pickup(type, x, y, radius));
    }

    private Projectile obtainProjectile(double x, double y, float radius, double dirX, double dirY) {
        if (!projectilePool.isEmpty()) {
            Projectile p = projectilePool.remove(projectilePool.size() - 1);
            p.reset(x, y, radius, dirX, dirY);
            return p;
        }
        return new Projectile(x, y, radius, dirX, dirY, getContext());
    }

    private Enemy obtainEnemy(int color, double x, double y, float radius, EnemyType type) {
        if (!enemyPool.isEmpty()) {
            Enemy e = enemyPool.remove(enemyPool.size() - 1);
            e.reset(color, x, y, radius, type);
            return e;
        }
        return new Enemy(color, player, x, y, radius, type, getContext());
    }

    private void spawnParticles(double x, double y, int color) {
        for (int i = 0; i < PARTICLE_COUNT; i++) {
            double angle = random.nextDouble() * 2 * Math.PI;
            float pRadius = 3 + random.nextFloat() * 4;
            Particle p;
            if (!particlePool.isEmpty()) {
                p = particlePool.remove(particlePool.size() - 1);
            } else {
                p = new Particle();
            }
            p.init(x, y, pRadius, color, Math.cos(angle), Math.sin(angle), getContext());
            particles.add(p);
        }
    }
}
