package com.example.my2dgame;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.Shader;
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

    private static final int INITIAL_SPAWN_INTERVAL = 90;
    private static final int MIN_SPAWN_INTERVAL = 20;
    private static final int FIRE_INTERVAL = 10;
    private static final int RAPID_FIRE_INTERVAL = 4;
    private static final int DAMAGE_FLASH_DURATION = 6;
    private static final int KILL_SCORE_BONUS = 3;
    private static final int BOSS_SCORE_BONUS = 100;
    private static final int PAUSE_BUTTON_SIZE = 80;
    private static final int PAUSE_BUTTON_MARGIN = 20;
    private static final int PARTICLE_COUNT = 8;
    private static final int SHAKE_DURATION = 4;
    private static final int BOSS_SHAKE_DURATION = 12;
    private static final float SHAKE_INTENSITY = 12f;
    private static final float BOSS_SHAKE_INTENSITY = 35f;
    private static final int PICKUP_SPAWN_INTERVAL = 450;
    
    private static final int WAVE_BREAK_DURATION = 90;
    private static final int WAVE_ANNOUNCEMENT_DURATION = 90;

    private GameLoop gameLoop;
    private final Paint titlePaint;
    private final Paint subtitlePaint;
    private final Paint scorePaint;
    private final Paint pauseButtonPaint;
    private final Paint buttonPaint;
    private final Paint selectorPaint;
    private final Paint vignettePaint;
    
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
    private final List<FloatingText> floatingTexts = new ArrayList<>();
    
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

    // UI Feedback State
    private int scorePopTimer = 0;
    private static final int SCORE_POP_DURATION = 15;
    private float vignetteAlpha = 0;

    // UI Components
    private HealthBar healthBar;
    private List<ParallaxLayer> parallaxLayers;

    // Wave system state
    private int waveNumber = 0;
    private int startingWave = 1;
    private int enemiesToSpawn = 0;
    private int waveBreakTimer = 0;
    private int waveAnnouncementTimer = 0;
    private boolean isBossWave = false;

    public Game(Context context) {
        super(context);

        SurfaceHolder surfaceHolder = getHolder();
        surfaceHolder.addCallback(this);

        titlePaint = new Paint();
        titlePaint.setColor(Color.WHITE);
        titlePaint.setTextSize(120);
        titlePaint.setTextAlign(Paint.Align.CENTER);
        titlePaint.setFakeBoldText(true);

        subtitlePaint = new Paint();
        subtitlePaint.setColor(Color.LTGRAY);
        subtitlePaint.setTextSize(60);
        subtitlePaint.setTextAlign(Paint.Align.CENTER);

        scorePaint = new Paint();
        scorePaint.setColor(Color.WHITE);
        scorePaint.setTextSize(50);
        scorePaint.setTextAlign(Paint.Align.CENTER);

        pauseButtonPaint = new Paint();
        pauseButtonPaint.setColor(Color.WHITE);
        pauseButtonPaint.setAlpha(180);

        buttonPaint = new Paint();
        buttonPaint.setColor(Color.WHITE);
        buttonPaint.setStyle(Paint.Style.STROKE);
        buttonPaint.setStrokeWidth(5);
        buttonPaint.setTextAlign(Paint.Align.CENTER);
        buttonPaint.setTextSize(50);

        selectorPaint = new Paint();
        selectorPaint.setColor(Color.CYAN);
        selectorPaint.setTextSize(60);
        selectorPaint.setTextAlign(Paint.Align.CENTER);

        vignettePaint = new Paint();
        vignettePaint.setStyle(Paint.Style.FILL);

        soundManager = new SoundManager(context);
        soundManager.startMusic();

        prefs = context.getSharedPreferences("my2dgame", Context.MODE_PRIVATE);
        highScore = prefs.getInt("high_score", 0);

        healthBar = new HealthBar(context);

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
        if (event.getActionMasked() != MotionEvent.ACTION_DOWN) {
            if (gameState == GameState.PLAYING) return handlePlayingTouch(event);
            return true;
        }

        float x = event.getX();
        float y = event.getY();

        switch (gameState) {
            case MENU:
                if (handleMenuTouch(x, y)) return true;
                break;
            case GAME_OVER:
                if (System.currentTimeMillis() - gameOverTimestamp >= GAME_OVER_COOLDOWN_MS) {
                    startGame();
                }
                return true;
            case PAUSED:
                if (handlePauseTouch(x, y)) return true;
                gameState = GameState.PLAYING;
                return true;
            case PLAYING:
                return handlePlayingTouch(event);
        }
        return super.onTouchEvent(event);
    }

    private boolean handleMenuTouch(float x, float y) {
        float centerY = screenHeight / 2f + 250;
        float centerX = screenWidth / 2f;
        if (x > centerX - 150 && x < centerX - 50 && y > centerY - 50 && y < centerY + 50) {
            startingWave = Math.max(1, startingWave - 1);
            return true;
        }
        if (x > centerX + 50 && x < centerX + 150 && y > centerY - 50 && y < centerY + 50) {
            startingWave = Math.min(20, startingWave + 1);
            return true;
        }
        if (x > screenWidth - 250 && y > screenHeight - 150) {
            exitGame();
            return true;
        }
        if (y < screenHeight - 200) {
            startGame();
            return true;
        }
        return false;
    }

    private boolean handlePauseTouch(float x, float y) {
        if (x > screenWidth / 2f - 150 && x < screenWidth / 2f + 150 && y > screenHeight - 200) {
            exitGame();
            return true;
        }
        return false;
    }

    private void exitGame() {
        if (getContext() instanceof Activity) {
            ((Activity) getContext()).finish();
        }
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
        enemyPool.addAll(enemies);
        projectilePool.addAll(projectiles);
        particlePool.addAll(particles);
        enemies.clear();
        projectiles.clear();
        particles.clear();
        pickups.clear();
        floatingTexts.clear();
        score = 0;
        scoreTimer = 0;
        spawnTimer = 0;
        fireTimer = 0;
        damageFlashTimer = 0;
        pickupTimer = 0;
        shakeTimer = 0;
        
        waveNumber = startingWave - 1;
        enemiesToSpawn = 0;
        waveBreakTimer = 1;
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

        if (parallaxLayers == null) {
            parallaxLayers = new ArrayList<>();
            parallaxLayers.add(new ParallaxLayer(screenWidth, screenHeight, 80, 1f, 2f, 0, 15f, Color.GRAY));
            parallaxLayers.add(new ParallaxLayer(screenWidth, screenHeight, 40, 2f, 4f, 0, 35f, Color.LTGRAY));
            parallaxLayers.add(new ParallaxLayer(screenWidth, screenHeight, 15, 3f, 6f, 0, 70f, Color.WHITE));
        }

        RadialGradient gradient = new RadialGradient(screenWidth / 2f, screenHeight / 2f, screenWidth * 0.8f,
                new int[]{Color.TRANSPARENT, Color.argb(150, 255, 0, 0)}, null, Shader.TileMode.CLAMP);
        vignettePaint.setShader(gradient);

        if (joystick == null) {
            joystick = new Joystick((int) (screenWidth * 0.12), (int) (screenHeight * 0.75), (int) (screenHeight * 0.08), (int) (screenHeight * 0.045), Color.BLUE, Color.GRAY);
        }
        if (aimJoystick == null) {
            aimJoystick = new Joystick((int) (screenWidth * 0.88), (int) (screenHeight * 0.75), (int) (screenHeight * 0.08), (int) (screenHeight * 0.045), Color.RED, Color.DKGRAY);
        }
        if (player == null) {
            player = new Player(getContext(), joystick, screenWidth / 2.0, screenHeight / 2.0, (float) (screenHeight * 0.035));
        }

        if (gameLoop == null || !gameLoop.isAlive()) {
            gameLoop = new GameLoop(this, surfaceHolder);
            gameLoop.startLoop();
        }
    }

    @Override
    public void surfaceChanged(@NonNull SurfaceHolder surfaceHolder, int i, int i1, int i2) {}

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
            case MENU: drawMenu(canvas); break;
            case PLAYING: drawPlaying(canvas); break;
            case PAUSED: drawPlaying(canvas); drawPauseOverlay(canvas); break;
            case GAME_OVER: drawGameOver(canvas); break;
        }
    }

    private void drawMenu(Canvas canvas) {
        canvas.drawColor(Color.rgb(5, 5, 15));
        if (parallaxLayers != null) { for (ParallaxLayer layer : parallaxLayers) layer.draw(canvas); }
        canvas.drawText("STAR DEFENDER", screenWidth / 2f, screenHeight / 2f - 150, titlePaint);
        canvas.drawText("Tap to Start", screenWidth / 2f, screenHeight / 2f, subtitlePaint);
        float centerY = screenHeight / 2f + 250;
        canvas.drawText("Starting Wave", screenWidth / 2f, centerY - 80, buttonPaint);
        canvas.drawText("<", screenWidth / 2f - 100, centerY + 15, selectorPaint);
        canvas.drawText(String.valueOf(startingWave), screenWidth / 2f, centerY + 15, selectorPaint);
        canvas.drawText(">", screenWidth / 2f + 100, centerY + 15, selectorPaint);
        if (highScore > 0) canvas.drawText("Best: " + highScore, 150, 80, scorePaint);
        canvas.drawText("EXIT", screenWidth - 120, screenHeight - 80, buttonPaint);
    }

    private void drawPlaying(Canvas canvas) {
        canvas.drawColor(Color.rgb(5, 5, 15));
        boolean shaking = shakeTimer > 0;
        if (shaking) {
            float progress = (float) shakeTimer / currentShakeDuration;
            float intensity = progress * currentShakeIntensity;
            canvas.save();
            canvas.translate((random.nextFloat() * 2 - 1) * intensity, (random.nextFloat() * 2 - 1) * intensity);
        }
        if (parallaxLayers != null) { for (ParallaxLayer layer : parallaxLayers) layer.draw(canvas); }
        if (player != null) player.draw(canvas);
        for (Enemy enemy : enemies) enemy.draw(canvas);
        for (Projectile projectile : projectiles) projectile.draw(canvas);
        for (Particle particle : particles) particle.draw(canvas);
        for (Pickup pickup : pickups) pickup.draw(canvas);
        for (FloatingText ft : floatingTexts) ft.draw(canvas);
        if (joystick != null) joystick.draw(canvas);
        if (aimJoystick != null) aimJoystick.draw(canvas);
        if (shaking) canvas.restore();

        if (player != null && player.getHealthPoints() <= 2) {
            vignettePaint.setAlpha((int) (Math.abs(Math.sin(System.currentTimeMillis() / 300.0)) * 150));
            canvas.drawRect(0, 0, screenWidth, screenHeight, vignettePaint);
        }

        if (damageFlashTimer > 0) canvas.drawColor(Color.argb(100, 255, 0, 0));
        healthBar.draw(canvas, player, screenWidth);
        
        float scoreScale = 1.0f;
        if (scorePopTimer > 0) {
            scoreScale = 1.0f + (scorePopTimer / (float) SCORE_POP_DURATION) * 0.3f;
            scorePaint.setTextSize(50 * scoreScale);
            scorePaint.setColor(Color.CYAN);
        } else {
            scorePaint.setTextSize(50);
            scorePaint.setColor(Color.WHITE);
        }
        canvas.drawText("Score: " + score, screenWidth / 2f, 130, scorePaint);
        if (waveNumber > 0) canvas.drawText("Wave: " + waveNumber, screenWidth / 2f, 180, scorePaint);

        if (waveAnnouncementTimer > 0) {
            float animProgress = (float) waveAnnouncementTimer / WAVE_ANNOUNCEMENT_DURATION;
            float waveScale = 1.0f + (animProgress * 0.5f);
            titlePaint.setTextSize(120 * waveScale);
            titlePaint.setAlpha((int) (Math.min(1.0f, animProgress * 2) * 255));
            String text = isBossWave ? "BOSS WAVE!" : "Wave " + waveNumber;
            canvas.drawText(text, screenWidth / 2f, screenHeight / 2f, titlePaint);
            titlePaint.setAlpha(255);
            titlePaint.setTextSize(120);
        }
        
        drawPauseButton(canvas);
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
        canvas.drawColor(Color.argb(180, 0, 0, 0));
        canvas.drawText("PAUSED", screenWidth / 2f, screenHeight / 2f - 50, titlePaint);
        canvas.drawText("Tap to Resume", screenWidth / 2f, screenHeight / 2f + 60, subtitlePaint);
        canvas.drawRect(screenWidth / 2f - 150, screenHeight - 180, screenWidth / 2f + 150, screenHeight - 80, buttonPaint);
        canvas.drawText("QUIT TO OS", screenWidth / 2f, screenHeight - 115, buttonPaint);
    }

    private void drawGameOver(Canvas canvas) {
        canvas.drawColor(Color.BLACK);
        canvas.drawText("GAME OVER", screenWidth / 2f, screenHeight / 2f - 100, titlePaint);
        canvas.drawText("Score: " + score, screenWidth / 2f, screenHeight / 2f + 20, subtitlePaint);
        if (isNewHighScore) canvas.drawText("New High Score!", screenWidth / 2f, screenHeight / 2f + 100, subtitlePaint);
        else canvas.drawText("High Score: " + highScore, screenWidth / 2f, screenHeight / 2f + 100, subtitlePaint);
        canvas.drawText("Tap to Restart", screenWidth / 2f, screenHeight / 2f + 180, subtitlePaint);
    }

    public void update(double dt) {
        if (parallaxLayers != null) { for (ParallaxLayer layer : parallaxLayers) layer.update(dt); }
        if (gameState != GameState.PLAYING) return;
        if (damageFlashTimer > 0) damageFlashTimer--;
        if (shakeTimer > 0) shakeTimer--;
        if (scorePopTimer > 0) scorePopTimer--;
        if (waveBreakTimer > 0) {
            waveBreakTimer--;
            if (waveBreakTimer == 0) startNextWave();
        }
        if (waveAnnouncementTimer > 0) waveAnnouncementTimer--;
        if (joystick != null) joystick.update();
        if (aimJoystick != null) aimJoystick.update();
        if (player != null) {
            player.update(dt);
            player.clampToScreen(screenWidth, screenHeight);
        }
        if (aimJoystick != null && player != null) {
            double ax = aimJoystick.actuatorX();
            double ay = aimJoystick.actuatorY();
            if (ax != 0 || ay != 0) {
                fireTimer++;
                int interval = player.hasRapidFire() ? RAPID_FIRE_INTERVAL : FIRE_INTERVAL;
                if (fireTimer >= interval) {
                    double mag = Math.sqrt(ax * ax + ay * ay);
                    projectiles.add(obtainProjectile(player.positionX(), player.positionY(), (float) (screenHeight * 0.01), ax / mag, ay / mag));
                    fireTimer = 0;
                }
            } else fireTimer = 0;
        }
        Iterator<Projectile> projIterator = projectiles.iterator();
        while (projIterator.hasNext()) {
            Projectile p = projIterator.next();
            p.update(dt);
            if (p.isOffScreen(screenWidth, screenHeight)) { projectilePool.add(p); projIterator.remove(); }
        }
        for (Enemy enemy : enemies) { enemy.update(dt); enemy.clampToScreen(screenWidth, screenHeight); }
        Iterator<Pickup> pickupIterator = pickups.iterator();
        while (pickupIterator.hasNext()) {
            Pickup p = pickupIterator.next();
            p.update(dt);
            if (Circle.isColliding(player, p)) {
                player.applyPickup(p.getType());
                floatingTexts.add(new FloatingText(p.getType().getLabel(), p.positionX(), p.positionY(), Color.YELLOW, 40));
                pickupIterator.remove();
            } else if (p.isExpired()) pickupIterator.remove();
        }
        
        List<Enemy> splitEnemies = new ArrayList<>();
        Iterator<Enemy> enemyIterator = enemies.iterator();
        while (enemyIterator.hasNext()) {
            Enemy enemy = enemyIterator.next();
            Iterator<Projectile> bulletIterator = projectiles.iterator();
            boolean enemyHit = false;
            while (bulletIterator.hasNext()) {
                Projectile p = bulletIterator.next();
                if (Circle.isColliding(p, enemy)) { projectilePool.add(p); bulletIterator.remove(); enemyHit = true; break; }
            }
            if (enemyHit) {
                if (enemy.takeDamage()) {
                    spawnParticles(enemy.positionX(), enemy.positionY(), enemy.getType().getColor());
                    int bonus = enemy.isBoss() ? BOSS_SCORE_BONUS : KILL_SCORE_BONUS;
                    addScore(bonus, enemy.positionX(), enemy.positionY());
                    
                    if (enemy.getType() == EnemyType.SPLITTER) {
                        float scrapRadius = (float)(screenHeight * 0.03 * EnemyType.SMALL.getSizeMultiplier());
                        for (int i = 0; i < 3; i++) {
                            double angle = i * (2 * Math.PI / 3);
                            double dist = enemy.getRadius(); // Separation offset
                            double sx = enemy.positionX() + Math.cos(angle) * dist;
                            double sy = enemy.positionY() + Math.sin(angle) * dist;
                            splitEnemies.add(obtainEnemy(EnemyType.SMALL.getColor(), sx, sy, scrapRadius, EnemyType.SMALL));
                        }
                    }
                    
                    enemyPool.add(enemy);
                    enemyIterator.remove();
                    if (enemy.isBoss()) triggerShake(BOSS_SHAKE_DURATION, BOSS_SHAKE_INTENSITY);
                    else triggerShake(SHAKE_DURATION, SHAKE_INTENSITY * 0.8f);
                } else triggerShake(2, SHAKE_INTENSITY * 0.3f);
            }
        }
        enemies.addAll(splitEnemies);

        if (player != null) {
            enemyIterator = enemies.iterator();
            while (enemyIterator.hasNext()) {
                Enemy enemy = enemyIterator.next();
                if (Circle.isColliding(player, enemy)) {
                    player.takeDamage();
                    spawnParticles(enemy.positionX(), enemy.positionY(), enemy.getType().getColor());
                    if (!enemy.isBoss()) { enemyPool.add(enemy); enemyIterator.remove(); }
                    soundManager.playHit();
                    damageFlashTimer = DAMAGE_FLASH_DURATION;
                    triggerShake(BOSS_SHAKE_DURATION, BOSS_SHAKE_INTENSITY);
                }
            }
        }
        Iterator<Particle> partIterator = particles.iterator();
        while (partIterator.hasNext()) {
            Particle p = partIterator.next();
            p.update(dt);
            if (p.isDead()) { particlePool.add(p); partIterator.remove(); }
        }
        Iterator<FloatingText> ftIterator = floatingTexts.iterator();
        while (ftIterator.hasNext()) { if (ftIterator.next().update(dt)) ftIterator.remove(); }

        if (player != null && !player.isAlive()) {
            gameState = GameState.GAME_OVER;
            gameOverTimestamp = System.currentTimeMillis();
            if (score > highScore) { highScore = score; prefs.edit().putInt("high_score", highScore).apply(); }
            soundManager.playGameOver();
            return;
        }
        pickupTimer++;
        if (pickupTimer >= PICKUP_SPAWN_INTERVAL) { spawnPickup(); pickupTimer = 0; }
        if (enemiesToSpawn > 0) {
            spawnTimer++;
            int spawnInterval = Math.max(MIN_SPAWN_INTERVAL, INITIAL_SPAWN_INTERVAL - (waveNumber * 5));
            if (spawnTimer >= spawnInterval) {
                if (isBossWave) spawnBoss(); else spawnEnemy();
                enemiesToSpawn--;
                spawnTimer = 0;
            }
        } else if (enemies.isEmpty() && waveBreakTimer == 0 && waveNumber > 0) { waveBreakTimer = WAVE_BREAK_DURATION; }
        scoreTimer++;
        if (scoreTimer >= (int) GameLoop.MAX_UPS) { addScore(1, -1, -1); scoreTimer = 0; }
    }

    private void addScore(int amount, double x, double y) {
        score += amount;
        if (amount >= 3) {
            scorePopTimer = SCORE_POP_DURATION;
            if (x != -1) floatingTexts.add(new FloatingText("+" + amount, x, y, Color.CYAN, 50));
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
        enemiesToSpawn = isBossWave ? 1 : 5 + (waveNumber * 2);
        waveAnnouncementTimer = WAVE_ANNOUNCEMENT_DURATION;
        spawnTimer = 0;
    }

    private EnemyType pickEnemyType() {
        int roll = random.nextInt(100);
        if (waveNumber >= 6 && roll < 15) return EnemyType.SPLITTER;
        if (waveNumber >= 4 && roll < 25) return EnemyType.ZIGZAG;
        if (waveNumber >= 3 && roll < 35) return EnemyType.TANK;
        if (waveNumber >= 2 && roll < 50) return EnemyType.FAST;
        return EnemyType.NORMAL;
    }

    private void spawnEnemy() {
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

    private void spawnBoss() {
        EnemyType type = EnemyType.TANK;
        float bossRadius = (float) ((screenHeight * 0.03) * 2.5);
        Enemy boss = obtainEnemy(Color.RED, screenWidth / 2.0, bossRadius, bossRadius, type);
        boss.setAsBoss(10 + (waveNumber / 5) * 5);
        enemies.add(boss);
    }

    private void spawnPickup() {
        PickupType type = PickupType.values()[random.nextInt(PickupType.values().length)];
        float radius = 25;
        pickups.add(new Pickup(type, radius + random.nextDouble() * (screenWidth - 2 * radius), radius + random.nextDouble() * (screenHeight - 2 * radius), radius));
    }

    private Projectile obtainProjectile(double x, double y, float radius, double dirX, double dirY) {
        if (!projectilePool.isEmpty()) { Projectile p = projectilePool.remove(projectilePool.size() - 1); p.reset(x, y, radius, dirX, dirY); return p; }
        return new Projectile(x, y, radius, dirX, dirY, getContext());
    }

    private Enemy obtainEnemy(int color, double x, double y, float radius, EnemyType type) {
        if (!enemyPool.isEmpty()) { Enemy e = enemyPool.remove(enemyPool.size() - 1); e.reset(color, x, y, radius, type); return e; }
        return new Enemy(color, player, x, y, radius, type, getContext());
    }

    private void spawnParticles(double x, double y, int color) {
        for (int i = 0; i < PARTICLE_COUNT; i++) {
            double angle = random.nextDouble() * 2 * Math.PI;
            float pRadius = 3 + random.nextFloat() * 4;
            Particle p;
            if (!particlePool.isEmpty()) p = particlePool.remove(particlePool.size() - 1);
            else p = new Particle();
            p.init(x, y, pRadius, color, Math.cos(angle), Math.sin(angle), getContext());
            particles.add(p);
        }
    }
}
