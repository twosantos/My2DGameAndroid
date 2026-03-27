package com.example.my2dgame;

import android.content.Context;
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
    private static final int DAMAGE_FLASH_DURATION = 6; // frames (~0.2s at 30 UPS)
    private static final int KILL_SCORE_BONUS = 3;
    private static final int PAUSE_BUTTON_SIZE = 80;
    private static final int PAUSE_BUTTON_MARGIN = 20;

    private final GameLoop gameLoop;
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

    private Joystick joystick;
    private Player player;
    private final List<Enemy> enemies = new ArrayList<>();
    private final List<Projectile> projectiles = new ArrayList<>();
    private int screenWidth;
    private int screenHeight;

    private GameState gameState = GameState.MENU;
    private int score = 0;
    private int scoreTimer = 0;
    private int spawnTimer = 0;
    private int fireTimer = 0;
    private int damageFlashTimer = 0;

    public Game(Context context) {
        super(context);

        SurfaceHolder surfaceHolder = getHolder();
        surfaceHolder.addCallback(this);

        gameLoop = new GameLoop(this, surfaceHolder);

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

        setFocusable(true);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (gameState) {
            case MENU:
            case GAME_OVER:
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    startGame();
                }
                return true;
            case PLAYING:
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        if (isTouchOnPauseButton(event.getX(), event.getY())) {
                            gameState = GameState.PAUSED;
                            joystick.notPressed();
                            return true;
                        }
                        joystick.pressed((double) event.getX(), (double) event.getY());
                        return true;
                    case MotionEvent.ACTION_MOVE:
                        if (joystick.isPressed()) {
                            joystick.setActuator((double) event.getX(), (double) event.getY());
                        }
                        return true;
                    case MotionEvent.ACTION_UP:
                        joystick.notPressed();
                        return true;
                }
                break;
            case PAUSED:
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    gameState = GameState.PLAYING;
                }
                return true;
        }
        return super.onTouchEvent(event);
    }

    private boolean isTouchOnPauseButton(float x, float y) {
        float btnX = screenWidth - PAUSE_BUTTON_SIZE - PAUSE_BUTTON_MARGIN;
        float btnY = PAUSE_BUTTON_MARGIN;
        return x >= btnX && x <= btnX + PAUSE_BUTTON_SIZE
                && y >= btnY && y <= btnY + PAUSE_BUTTON_SIZE;
    }

    private void startGame() {
        player.reset(screenWidth / 2.0, screenHeight / 2.0);
        enemies.clear();
        projectiles.clear();
        score = 0;
        scoreTimer = 0;
        spawnTimer = 0;
        fireTimer = 0;
        damageFlashTimer = 0;
        joystick.notPressed();
        gameState = GameState.PLAYING;
        soundManager.playStart();
    }

    @Override
    public void surfaceCreated(@NonNull SurfaceHolder surfaceHolder) {
        screenWidth = getWidth();
        screenHeight = getHeight();

        joystick = new Joystick(
                (int) (screenWidth * 0.12),
                (int) (screenHeight * 0.75),
                (int) (screenHeight * 0.08),
                (int) (screenHeight * 0.045)
        );
        player = new Player(
                getContext(),
                joystick,
                screenWidth / 2.0,
                screenHeight / 2.0,
                (float) (screenHeight * 0.035)
        );

        gameLoop.startLoop();
    }

    @Override
    public void surfaceChanged(@NonNull SurfaceHolder surfaceHolder, int i, int i1, int i2) {
    }

    @Override
    public void surfaceDestroyed(@NonNull SurfaceHolder surfaceHolder) {
        gameLoop.stopLoop();
        soundManager.release();
    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
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
        canvas.drawText("My2DGame", screenWidth / 2f, screenHeight / 2f - 50, titlePaint);
        canvas.drawText("Tap to Start", screenWidth / 2f, screenHeight / 2f + 60, subtitlePaint);
    }

    private void drawPlaying(Canvas canvas) {
        // Game objects
        player.draw(canvas);
        for (Enemy enemy : enemies) {
            enemy.draw(canvas);
        }
        for (Projectile projectile : projectiles) {
            projectile.draw(canvas);
        }
        joystick.draw(canvas);

        // Damage flash overlay
        if (damageFlashTimer > 0) {
            canvas.drawColor(Color.argb(100, 255, 0, 0));
        }

        // HUD
        drawHealthBar(canvas);
        drawScore(canvas);
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
        canvas.drawText("Game Over", screenWidth / 2f, screenHeight / 2f - 80, titlePaint);
        canvas.drawText("Score: " + score, screenWidth / 2f, screenHeight / 2f + 40, subtitlePaint);
        canvas.drawText("Tap to Restart", screenWidth / 2f, screenHeight / 2f + 140, subtitlePaint);
    }

    private void drawHealthBar(Canvas canvas) {
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
    }

    private void drawUPS(Canvas canvas) {
        String averageUPS = Double.toString(gameLoop.getAverageUPS());
        canvas.drawText("UPS " + averageUPS, 30, 80, statsPaint);
    }

    private void drawFPS(Canvas canvas) {
        String averageFPS = Double.toString(gameLoop.getAverageFPS());
        canvas.drawText("FPS " + averageFPS, 30, 130, statsPaint);
    }

    public void update() {
        if (gameState != GameState.PLAYING) {
            return;
        }

        // Decrement damage flash
        if (damageFlashTimer > 0) {
            damageFlashTimer--;
        }

        joystick.update();
        player.update();
        player.clampToScreen(screenWidth, screenHeight);

        // Auto-fire projectiles in joystick direction
        double ax = joystick.actuatorX();
        double ay = joystick.actuatorY();
        if (ax != 0 || ay != 0) {
            fireTimer++;
            if (fireTimer >= FIRE_INTERVAL) {
                double mag = Math.sqrt(ax * ax + ay * ay);
                float bulletRadius = (float) (screenHeight * 0.01);
                projectiles.add(new Projectile(
                        player.positionX(), player.positionY(),
                        bulletRadius, ax / mag, ay / mag
                ));
                fireTimer = 0;
            }
        } else {
            fireTimer = 0;
        }

        // Update projectiles, remove off-screen
        Iterator<Projectile> projIterator = projectiles.iterator();
        while (projIterator.hasNext()) {
            Projectile p = projIterator.next();
            p.update();
            if (p.isOffScreen(screenWidth, screenHeight)) {
                projIterator.remove();
            }
        }

        // Update enemies
        for (Enemy enemy : enemies) {
            enemy.update();
            enemy.clampToScreen(screenWidth, screenHeight);
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
                    bulletIterator.remove();
                    enemyHit = true;
                    break;
                }
            }
            if (enemyHit) {
                enemyIterator.remove();
                score += KILL_SCORE_BONUS;
            }
        }

        // Player-enemy collisions
        enemyIterator = enemies.iterator();
        while (enemyIterator.hasNext()) {
            Enemy enemy = enemyIterator.next();
            if (Circle.isColliding(player, enemy)) {
                player.takeDamage();
                enemyIterator.remove();
                soundManager.playHit();
                damageFlashTimer = DAMAGE_FLASH_DURATION;
            }
        }

        if (!player.isAlive()) {
            gameState = GameState.GAME_OVER;
            soundManager.playGameOver();
            return;
        }

        // Enemy spawning
        spawnTimer++;
        int spawnInterval = Math.max(MIN_SPAWN_INTERVAL, INITIAL_SPAWN_INTERVAL - score * 2);
        if (spawnTimer >= spawnInterval) {
            spawnEnemy();
            spawnTimer = 0;
        }

        // Survival score: +1 per second
        scoreTimer++;
        if (scoreTimer >= (int) GameLoop.MAX_UPS) {
            score++;
            scoreTimer = 0;
        }
    }

    private void spawnEnemy() {
        float enemyRadius = (float) (screenHeight * 0.03);
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
        enemies.add(new Enemy(getContext(), player, x, y, enemyRadius));
    }
}
