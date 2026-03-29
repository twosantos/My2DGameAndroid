package com.example.my2dgame;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import androidx.annotation.NonNull;

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

    private GameLoop gameLoop;
    private final Random random = new Random();
    private final SoundManager soundManager;
    private final SharedPreferences prefs;

    private Joystick joystick;
    private Joystick aimJoystick;
    private int movePointerId = -1;
    private int aimPointerId = -1;
    private Player player;
    
    private final List<Projectile> projectiles = new ArrayList<>();
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
    private int damageFlashTimer = 0;
    private int fireTimer = 0;
    private int pickupTimer = 0;
    private int highScore;
    private boolean isNewHighScore = false;
    private long gameOverTimestamp = 0;
    private static final long GAME_OVER_COOLDOWN_MS = 1000;
    private int shakeTimer = 0;
    private float currentShakeIntensity = Constants.SHAKE_INTENSITY;
    private int currentShakeDuration = Constants.SHAKE_DURATION;

    // UI Feedback State
    private int scorePopTimer = 0;

    // UI Components
    private UIManager uiManager;
    private EnemyManager enemyManager;
    private List<ParallaxLayer> parallaxLayers;

    // Wave system state
    private int waveNumber = 0;
    private int startingWave = 1;
    private int waveBreakTimer = 0;
    private int waveAnnouncementTimer = 0;
    private boolean isBossWave = false;

    public Game(Context context) {
        super(context);

        SurfaceHolder surfaceHolder = getHolder();
        surfaceHolder.addCallback(this);

        uiManager = new UIManager(context);
        soundManager = new SoundManager(context);
        soundManager.startMusic();

        prefs = context.getSharedPreferences("my2dgame", Context.MODE_PRIVATE);
        highScore = prefs.getInt("high_score", 0);

        SpriteCache.preload(context,
            R.drawable.spaceship,
            R.drawable.asteroid,
            R.drawable.rocket,
            R.drawable.spiky_explosion
        );

        setFocusable(true);
    }

    public void onEnemyDestroyed(Enemy enemy) {
        spawnParticles(enemy.positionX(), enemy.positionY(), enemy.getType().getColor());
        int bonus = enemy.isBoss() ? Constants.BOSS_SCORE_BONUS : Constants.KILL_SCORE_BONUS;
        addScore(bonus, enemy.positionX(), enemy.positionY());
        if (enemy.isBoss()) triggerShake(Constants.BOSS_SHAKE_DURATION, Constants.BOSS_SHAKE_INTENSITY);
        else triggerShake(Constants.SHAKE_DURATION, Constants.SHAKE_INTENSITY * 0.8f);
    }

    public void onEnemyHit(Enemy enemy) {
        triggerShake(2, Constants.SHAKE_INTENSITY * 0.3f);
    }

    public void onPlayerHit(Enemy enemy) {
        player.takeDamage();
        spawnParticles(enemy.positionX(), enemy.positionY(), enemy.getType().getColor());
        soundManager.playHit();
        damageFlashTimer = Constants.DAMAGE_FLASH_DURATION;
        triggerShake(Constants.BOSS_SHAKE_DURATION, Constants.BOSS_SHAKE_INTENSITY);
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
        float btnX = screenWidth - Constants.PAUSE_BUTTON_SIZE - Constants.PAUSE_BUTTON_MARGIN;
        float btnY = Constants.PAUSE_BUTTON_MARGIN;
        return x >= btnX && x <= btnX + Constants.PAUSE_BUTTON_SIZE
                && y >= btnY && y <= btnY + Constants.PAUSE_BUTTON_SIZE;
    }

    private void startGame() {
        player.reset(screenWidth / 2.0, screenHeight + 200);
        if (enemyManager != null) enemyManager.reset();
        projectilePool.addAll(projectiles);
        particlePool.addAll(particles);
        projectiles.clear();
        particles.clear();
        pickups.clear();
        floatingTexts.clear();
        score = 0;
        scoreTimer = 0;
        damageFlashTimer = 0;
        pickupTimer = 0;
        shakeTimer = 0;
        
        // Start first wave immediately with animation
        waveNumber = startingWave;
        triggerNextWaveAnnouncement();
        
        releaseAllJoysticks();
        gameState = GameState.PLAYING;
        soundManager.playStart();
    }

    @Override
    public void surfaceCreated(@NonNull SurfaceHolder surfaceHolder) {
        screenWidth = getWidth();
        screenHeight = getHeight();

        uiManager.initVignette(screenWidth, screenHeight);

        if (parallaxLayers == null) {
            parallaxLayers = new ArrayList<>();
            parallaxLayers.add(new ParallaxLayer(screenWidth, screenHeight, 80, 1f, 2f, 0, 15f, Color.GRAY));
            parallaxLayers.add(new ParallaxLayer(screenWidth, screenHeight, 40, 2f, 4f, 0, 35f, Color.LTGRAY));
            parallaxLayers.add(new ParallaxLayer(screenWidth, screenHeight, 15, 3f, 6f, 0, 70f, Color.WHITE));
        }

        if (joystick == null) {
            joystick = new Joystick((int) (screenWidth * 0.12), (int) (screenHeight * 0.75), (int) (screenHeight * 0.08), (int) (screenHeight * 0.045), Color.BLUE, Color.GRAY);
        }
        if (aimJoystick == null) {
            aimJoystick = new Joystick((int) (screenWidth * 0.88), (int) (screenHeight * 0.75), (int) (screenHeight * 0.08), (int) (screenHeight * 0.045), Color.RED, Color.DKGRAY);
        }
        if (player == null) {
            player = new Player(getContext(), joystick, screenWidth / 2.0, screenHeight / 2.0, (float) (screenHeight * 0.035));
        }
        if (enemyManager == null) {
            enemyManager = new EnemyManager(getContext(), player);
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
            case MENU: uiManager.drawMenu(canvas, screenWidth, screenHeight, parallaxLayers, highScore, startingWave); break;
            case PLAYING: drawPlaying(canvas); break;
            case PAUSED: drawPlaying(canvas); uiManager.drawPauseOverlay(canvas, screenWidth, screenHeight); break;
            case GAME_OVER: uiManager.drawGameOver(canvas, screenWidth, screenHeight, score, highScore, isNewHighScore); break;
        }
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
        enemyManager.draw(canvas);
        for (Projectile projectile : projectiles) projectile.draw(canvas);
        for (Particle particle : particles) particle.draw(canvas);
        for (Pickup pickup : pickups) pickup.draw(canvas);
        for (FloatingText ft : floatingTexts) ft.draw(canvas);
        if (joystick != null) joystick.draw(canvas);
        if (aimJoystick != null) aimJoystick.draw(canvas);
        if (shaking) canvas.restore();

        if (damageFlashTimer > 0) canvas.drawColor(Color.argb(100, 255, 0, 0));
        
        uiManager.drawHUD(canvas, player, screenWidth, screenHeight, score, waveNumber, scorePopTimer, waveAnnouncementTimer, isBossWave);
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
        
        // Weapon Firing Logic
        if (aimJoystick != null && player != null && !player.isAnimating()) {
            double ax = aimJoystick.actuatorX();
            double ay = aimJoystick.actuatorY();
            if (ax != 0 || ay != 0) {
                fireTimer++;
                int interval = player.hasRapidFire() ? Constants.RAPID_FIRE_INTERVAL : Constants.FIRE_INTERVAL;
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
        
        enemyManager.update(dt, screenWidth, screenHeight, projectiles, projectilePool, this);

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
        if (pickupTimer >= Constants.PICKUP_SPAWN_INTERVAL) { spawnPickup(); pickupTimer = 0; }
        
        if (enemyManager.isWaveCleared() && waveBreakTimer == 0 && waveNumber > 0) { 
            waveNumber++;
            triggerNextWaveAnnouncement();
        }
        
        scoreTimer++;
        if (scoreTimer >= (int) GameLoop.MAX_UPS) { addScore(1, -1, -1); scoreTimer = 0; }
    }

    private void triggerNextWaveAnnouncement() {
        waveBreakTimer = Constants.WAVE_BREAK_DURATION;
        waveAnnouncementTimer = Constants.WAVE_ANNOUNCEMENT_DURATION;
        isBossWave = (waveNumber % 5 == 0);
        player.startFlyIn(screenWidth / 2.0, screenHeight / 2.0);
    }

    private void addScore(int amount, double x, double y) {
        score += amount;
        if (amount >= 3) {
            scorePopTimer = Constants.SCORE_POP_DURATION;
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
        enemyManager.startNextWave(waveNumber);
    }

    private void spawnPickup() {
        PickupType type = PickupType.values()[random.nextInt(PickupType.values().length)];
        float radius = 25;
        pickups.add(new Pickup(type, radius + random.nextDouble() * (screenWidth - 2 * radius), radius + random.nextDouble() * (screenHeight - 2 * radius), radius));
    }

    private Projectile obtainProjectile(double x, double y, float radius, double dirX, double dirY) {
        Projectile p;
        if (!projectilePool.isEmpty()) {
            p = projectilePool.remove(projectilePool.size() - 1);
            p.reset(x, y, radius, dirX, dirY);
        } else {
            p = new Projectile(x, y, radius, dirX, dirY, getContext());
        }
        
        if (player != null && player.hasHoming()) {
            Enemy closest = findNearestEnemy(x, y);
            if (closest != null) p.setHoming(closest);
        }
        
        return p;
    }

    private Enemy findNearestEnemy(double x, double y) {
        Enemy closest = null;
        double minDist = Double.MAX_VALUE;
        for (Enemy e : enemyManager.getEnemies()) {
            double dx = e.positionX() - x;
            double dy = e.positionY() - y;
            double dist = dx*dx + dy*dy;
            if (dist < minDist) { minDist = dist; closest = e; }
        }
        return closest;
    }

    private void spawnParticles(double x, double y, int color) {
        for (int i = 0; i < Constants.PARTICLE_COUNT; i++) {
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
