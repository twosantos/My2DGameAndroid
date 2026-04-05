package com.example.my2dgame;

import android.app.Activity;
import android.content.Context;
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
    private final SaveManager saveManager;

    private Joystick joystick;
    private Joystick aimJoystick;
    private int movePointerId = -1;
    private int aimPointerId = -1;
    private Player player;
    
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
    private boolean isNewHighScore = false;
    private long gameOverTimestamp = 0;
    private static final long GAME_OVER_COOLDOWN_MS = 1000;
    private int shakeTimer = 0;
    private float currentShakeIntensity = Constants.SHAKE_INTENSITY;
    private int currentShakeDuration = Constants.SHAKE_DURATION;

    private int scorePopTimer = 0;

    // Modular Managers
    private UIManager uiManager;
    private EnemyManager enemyManager;
    private ProjectileManager projectileManager;
    private ParticleManager particleManager;
    private ScrapManager scrapManager;
    private List<ParallaxLayer> parallaxLayers;

    // Wave system state
    private int waveNumber = 0;
    private int startingWave = 1;
    private int waveBreakTimer = 0;
    private int waveAnnouncementTimer = 0;
    private boolean isBossWave = false;

    // Ship Class Logic
    private ShipProfile selectedProfile = ShipProfile.TECH;

    public Game(Context context) {
        super(context);

        SurfaceHolder surfaceHolder = getHolder();
        surfaceHolder.addCallback(this);

        saveManager = new SaveManager(context);
        uiManager = new UIManager(context);
        projectileManager = new ProjectileManager(context);
        particleManager = new ParticleManager(context);
        scrapManager = new ScrapManager();
        
        soundManager = new SoundManager(context);
        soundManager.startMusic();

        // Load persisted ship selection
        int shipIndex = saveManager.getSelectedShipIndex();
        selectedProfile = ShipProfile.values()[shipIndex];

        SpriteCache.preload(context,
            R.drawable.spaceship,
            R.drawable.asteroid,
            R.drawable.rocket,
            R.drawable.spiky_explosion
        );

        setFocusable(true);
    }

    public void onEnemyDestroyed(Enemy enemy) {
        particleManager.spawnExplosion(enemy.positionX(), enemy.positionY(), enemy.getType().getColor());
        scrapManager.onEnemyDestroyed(enemy);
        
        int bonus = enemy.isBoss() ? Constants.BOSS_SCORE_BONUS : Constants.KILL_SCORE_BONUS;
        addScore(bonus, enemy.positionX(), enemy.positionY());
        if (enemy.isBoss()) {
            triggerShake(Constants.BOSS_SHAKE_DURATION, Constants.BOSS_SHAKE_INTENSITY);
            saveManager.setBossDefeated(0);
        }
        else triggerShake(Constants.SHAKE_DURATION, Constants.SHAKE_INTENSITY * 0.8f);
    }

    public void onEnemyHit(Enemy enemy) {
        triggerShake(2, Constants.SHAKE_INTENSITY * 0.3f);
    }

    public void onPlayerHit(Enemy enemy) {
        player.takeDamage();
        particleManager.spawnExplosion(enemy.positionX(), enemy.positionY(), enemy.getType().getColor());
        soundManager.playHit();
        damageFlashTimer = Constants.DAMAGE_FLASH_DURATION;
        triggerShake(Constants.BOSS_SHAKE_DURATION, Constants.BOSS_SHAKE_INTENSITY);
    }

    public void onScrapCollected(int amount) {
        saveManager.addScrap(amount);
        floatingTexts.add(new FloatingText("SCRAP +"+amount, player.positionX(), player.positionY(), Color.GREEN, 35));
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
            case HANGAR:
                if (handleHangarTouch(x, y)) return true;
                break;
            case GAME_OVER:
                if (System.currentTimeMillis() - gameOverTimestamp >= GAME_OVER_COOLDOWN_MS) {
                    gameState = GameState.MENU;
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
        float centerY = screenHeight / 2f;
        float centerX = screenWidth / 2f;
        
        if (y > centerY - 50 && y < centerY + 150) {
            int shipCount = ShipProfile.values().length;
            int currentIndex = selectedProfile.ordinal();
            if (x < centerX - 100) {
                currentIndex = (currentIndex - 1 + shipCount) % shipCount;
                selectedProfile = ShipProfile.values()[currentIndex];
                saveManager.setSelectedShipIndex(currentIndex);
                return true;
            } else if (x > centerX + 100) {
                currentIndex = (currentIndex + 1) % shipCount;
                selectedProfile = ShipProfile.values()[currentIndex];
                saveManager.setSelectedShipIndex(currentIndex);
                return true;
            }
            
            if (!selectedProfile.isOwned(saveManager) && selectedProfile.isUnlocked(saveManager)) {
                if (saveManager.spendScrap(selectedProfile.getScrapPrice())) {
                    saveManager.setShipOwned(selectedProfile.ordinal());
                    soundManager.playStart();
                    return true;
                }
            }
        }

        if (x > centerX - 150 && x < centerX + 150 && y > screenHeight - 250 && y < screenHeight - 150) {
            gameState = GameState.HANGAR;
            return true;
        }

        if (x > screenWidth - 250 && y > screenHeight - 150) {
            exitGame();
            return true;
        }
        
        if (y < screenHeight - 300 && selectedProfile.isOwned(saveManager)) {
            startGame();
            return true;
        }
        return false;
    }

    private boolean handleHangarTouch(float x, float y) {
        float startX = screenWidth * 0.1f;
        float startY = screenHeight * 0.3f;
        float spacingY = screenHeight * 0.12f;
        float buyButtonX = screenWidth * 0.7f;

        if (x > buyButtonX && x < buyButtonX + screenWidth * 0.2f) {
            if (y > startY - 40 && y < startY + 40) tryUpgrade(SaveManager.UPGRADE_HULL);
            else if (y > startY + spacingY - 40 && y < startY + spacingY + 40) tryUpgrade(SaveManager.UPGRADE_SPEED);
            else if (y > startY + spacingY * 2 - 40 && y < startY + spacingY * 2 + 40) tryUpgrade(SaveManager.UPGRADE_FIRE);
            else if (y > startY + spacingY * 3 - 40 && y < startY + spacingY * 3 + 40) tryUpgrade(SaveManager.UPGRADE_MAGNET);
            else if (y > startY + spacingY * 4 - 40 && y < startY + spacingY * 4 + 40) tryUpgrade(SaveManager.UPGRADE_BATTERY);
            return true;
        }

        if (x > screenWidth * 0.05f && x < screenWidth * 0.2f && y > screenHeight - 150 && y < screenHeight - 50) {
            gameState = GameState.MENU;
            return true;
        }

        return false;
    }

    private void tryUpgrade(String key) {
        int level = saveManager.getUpgradeLevel(key);
        if (level < 5) {
            int[] costs = {50, 150, 450, 1200, 3000};
            int cost = costs[level];
            if (saveManager.spendScrap(cost)) {
                saveManager.incrementUpgrade(key);
                soundManager.playStart();
            }
        }
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
        float btnSize = Constants.PAUSE_BUTTON_SIZE;
        float margin = Constants.PAUSE_BUTTON_MARGIN;
        float btnX = screenWidth - btnSize - margin;
        float btnY = margin;
        return x >= btnX && x <= btnX + btnSize
                && y >= btnY && y <= btnY + btnSize;
    }

    private void startGame() {
        player.initFromProfile(selectedProfile, saveManager);
        player.reset(screenWidth / 2.0, screenHeight + 200);
        player.startFlyIn(screenWidth / 2.0, screenHeight / 2.0);
        if (enemyManager != null) enemyManager.reset();
        projectileManager.reset();
        particleManager.reset();
        scrapManager.reset();
        pickups.clear();
        floatingTexts.clear();
        score = 0;
        scoreTimer = 0;
        damageFlashTimer = 0;
        pickupTimer = 0;
        shakeTimer = 0;
        
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
            player = new Player(getContext(), saveManager, selectedProfile, joystick, screenWidth / 2.0, screenHeight / 2.0, (float) (screenHeight * 0.035));
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
            case MENU: uiManager.drawMenu(canvas, screenWidth, screenHeight, parallaxLayers, saveManager.getHighScore(), startingWave, selectedProfile, saveManager); break;
            case HANGAR: uiManager.drawHangar(canvas, screenWidth, screenHeight, saveManager); break;
            case PLAYING: drawPlaying(canvas); break;
            case PAUSED: drawPlaying(canvas); uiManager.drawPauseOverlay(canvas, screenWidth, screenHeight); break;
            case GAME_OVER: uiManager.drawGameOver(canvas, screenWidth, screenHeight, score, saveManager.getHighScore(), isNewHighScore); break;
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
        projectileManager.draw(canvas);
        particleManager.draw(canvas);
        scrapManager.draw(canvas);
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
        
        if (aimJoystick != null && player != null && !player.isAnimating()) {
            double ax = aimJoystick.actuatorX();
            double ay = aimJoystick.actuatorY();
            if (ax != 0 || ay != 0) {
                fireTimer++;
                int interval = player.getFireInterval();
                if (fireTimer >= interval) {
                    double mag = Math.sqrt(ax * ax + ay * ay);
                    projectileManager.fire(player.positionX(), player.positionY(), (float) (screenHeight * 0.01), ax / mag, ay / mag, player, enemyManager);
                    fireTimer = 0;
                }
            } else fireTimer = 0;
        }
        
        projectileManager.update(dt, screenWidth, screenHeight);
        enemyManager.update(dt, screenWidth, screenHeight, projectileManager.getProjectiles(), projectileManager.getPool(), this);

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
        
        particleManager.update(dt);
        scrapManager.update(dt, player, this);
        Iterator<FloatingText> ftIterator = floatingTexts.iterator();
        while (ftIterator.hasNext()) { if (ftIterator.next().update(dt)) ftIterator.remove(); }

        if (player != null && !player.isAlive()) {
            gameState = GameState.GAME_OVER;
            gameOverTimestamp = System.currentTimeMillis();
            isNewHighScore = saveManager.trySaveHighScore(score);
            saveManager.updateMaxWave(waveNumber);
            saveManager.addScrap(waveNumber * 5);
            soundManager.playGameOver();
            return;
        }
        pickupTimer++;
        if (pickupTimer >= Constants.PICKUP_SPAWN_INTERVAL) { spawnPickup(); pickupTimer = 0; }
        
        if (enemyManager.isWaveCleared() && waveBreakTimer == 0 && waveNumber > 0) { 
            waveNumber++;
            if (saveManager.updateMaxWave(waveNumber)) {
                if (waveNumber == 10 || waveNumber == 20 || waveNumber == 30) {
                    floatingTexts.add(new FloatingText("MILESTONE REACHED: WAVE " + waveNumber, screenWidth/2.0, screenHeight/2.0, Color.CYAN, 60));
                }
            }
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
        PickupType[] types = PickupType.values();
        PickupType type;
        do {
            type = types[random.nextInt(types.length)];
        } while (type == PickupType.HOMING && !saveManager.isBossDefeated(0));

        float radius = 25;
        pickups.add(new Pickup(type, radius + random.nextDouble() * (screenWidth - 2 * radius), radius + random.nextDouble() * (screenHeight - 2 * radius), radius));
    }
}
