package com.example.my2dgame;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.RectF;
import android.graphics.Shader;
import androidx.core.content.ContextCompat;
import com.example.my2dgame.object.Player;
import java.util.List;

public class UIManager {
    private final Paint titlePaint;
    private final Paint subtitlePaint;
    private final Paint scorePaint;
    private final Paint pauseButtonPaint;
    private final Paint buttonPaint;
    private final Paint selectorPaint;
    private final Paint vignettePaint;
    private final Paint statsPaint;
    private final Paint timerPaint;
    private final Paint timerBgPaint;
    private final Paint upgradeTitlePaint;
    private final Paint scrapCountPaint;
    private final Paint shipDescriptionPaint;
    private final Paint lockOverlayPaint;
    
    private final HealthBar healthBar;
    private final Context context;
    private final RectF timerRect = new RectF();
    private final RectF upgradeButtonRect = new RectF();
    private final RectF genericButtonRect = new RectF();

    public UIManager(Context context) {
        this.context = context;
        
        statsPaint = new Paint();
        statsPaint.setColor(ContextCompat.getColor(context, R.color.magenta));
        statsPaint.setTextSize(50);

        titlePaint = new Paint();
        titlePaint.setColor(Color.WHITE);
        titlePaint.setFakeBoldText(true);
        titlePaint.setTextAlign(Paint.Align.CENTER);

        subtitlePaint = new Paint();
        subtitlePaint.setColor(Color.LTGRAY);
        subtitlePaint.setTextAlign(Paint.Align.CENTER);

        scorePaint = new Paint();
        scorePaint.setColor(Color.WHITE);
        scorePaint.setTextAlign(Paint.Align.CENTER);

        pauseButtonPaint = new Paint();
        pauseButtonPaint.setColor(Color.WHITE);
        pauseButtonPaint.setAlpha(180);

        buttonPaint = new Paint();
        buttonPaint.setColor(Color.WHITE);
        buttonPaint.setStyle(Paint.Style.STROKE);
        buttonPaint.setStrokeWidth(5);
        buttonPaint.setTextAlign(Paint.Align.CENTER);

        selectorPaint = new Paint();
        selectorPaint.setColor(Color.CYAN);
        selectorPaint.setTextAlign(Paint.Align.CENTER);

        vignettePaint = new Paint();
        vignettePaint.setStyle(Paint.Style.FILL);

        timerPaint = new Paint();
        timerPaint.setStyle(Paint.Style.FILL);
        timerPaint.setAntiAlias(true);

        timerBgPaint = new Paint();
        timerBgPaint.setColor(Color.argb(100, 0, 0, 0));
        timerBgPaint.setStyle(Paint.Style.FILL);

        upgradeTitlePaint = new Paint();
        upgradeTitlePaint.setColor(Color.WHITE);
        upgradeTitlePaint.setTextAlign(Paint.Align.LEFT);

        scrapCountPaint = new Paint();
        scrapCountPaint.setColor(Color.GREEN);
        scrapCountPaint.setTextAlign(Paint.Align.RIGHT);
        scrapCountPaint.setFakeBoldText(true);

        shipDescriptionPaint = new Paint();
        shipDescriptionPaint.setColor(Color.WHITE);
        shipDescriptionPaint.setTextAlign(Paint.Align.CENTER);

        lockOverlayPaint = new Paint();
        lockOverlayPaint.setColor(Color.argb(200, 0, 0, 0));
        lockOverlayPaint.setStyle(Paint.Style.FILL);

        healthBar = new HealthBar(context);
    }

    public void initVignette(int screenWidth, int screenHeight) {
        RadialGradient gradient = new RadialGradient(screenWidth / 2f, screenHeight / 2f, screenWidth * 0.8f,
                new int[]{Color.TRANSPARENT, Color.argb(150, 255, 0, 0)}, null, Shader.TileMode.CLAMP);
        vignettePaint.setShader(gradient);
        
        float scale = screenHeight / 1080f;
        titlePaint.setTextSize(120 * scale);
        subtitlePaint.setTextSize(60 * scale);
        scorePaint.setTextSize(50 * scale);
        buttonPaint.setTextSize(50 * scale);
        selectorPaint.setTextSize(60 * scale);
        upgradeTitlePaint.setTextSize(40 * scale);
        scrapCountPaint.setTextSize(60 * scale);
        shipDescriptionPaint.setTextSize(45 * scale);
        statsPaint.setTextSize(35 * scale);
    }

    public void drawMenu(Canvas canvas, int screenWidth, int screenHeight, List<ParallaxLayer> parallaxLayers, int highScore, int startingWave, ShipProfile selectedProfile, SaveManager saveManager) {
        canvas.drawColor(Color.rgb(5, 5, 15));
        if (parallaxLayers != null) { for (ParallaxLayer layer : parallaxLayers) layer.draw(canvas); }
        
        canvas.drawText("STAR DEFENDER", screenWidth / 2f, screenHeight * 0.25f, titlePaint);
        
        float centerY = screenHeight / 2f;
        canvas.drawText("<", screenWidth * 0.35f, centerY + 20, selectorPaint);
        canvas.drawText(">", screenWidth * 0.65f, centerY + 20, selectorPaint);
        
        // Task 4.3: Menu Gating Visuals
        boolean isUnlocked = selectedProfile.isUnlocked(saveManager);
        boolean isOwned = selectedProfile.isOwned(saveManager);

        if (!isUnlocked) {
            canvas.drawText("LOCKED", screenWidth / 2f, centerY + 20, subtitlePaint);
            canvas.drawText("REACH WAVE " + selectedProfile.getUnlockWave(), screenWidth / 2f, centerY + screenHeight * 0.1f, shipDescriptionPaint);
        } else {
            canvas.drawText(selectedProfile.getName(), screenWidth / 2f, centerY + 20, subtitlePaint);
            if (!isOwned) {
                canvas.drawText("UNLOCKED: BUY FOR " + selectedProfile.getScrapPrice() + " SCRAP", screenWidth / 2f, centerY + screenHeight * 0.1f, shipDescriptionPaint);
            } else {
                canvas.drawText(selectedProfile.getDescription(), screenWidth / 2f, centerY + screenHeight * 0.1f, shipDescriptionPaint);
                canvas.drawText("Tap to Start", screenWidth / 2f, screenHeight * 0.75f, subtitlePaint);
            }
        }
        
        float btnW = screenWidth * 0.25f;
        float btnH = screenHeight * 0.1f;
        genericButtonRect.set(screenWidth / 2f - btnW/2, screenHeight * 0.8f, screenWidth / 2f + btnW/2, screenHeight * 0.8f + btnH);
        canvas.drawRect(genericButtonRect, buttonPaint);
        canvas.drawText("HANGAR", screenWidth / 2f, screenHeight * 0.8f + btnH * 0.65f, buttonPaint);

        if (highScore > 0) canvas.drawText("Best: " + highScore, screenWidth * 0.1f, screenHeight * 0.1f, scorePaint);
        
        // EXIT Button responsive hitbox area
        canvas.drawText("EXIT", screenWidth * 0.9f, screenHeight * 0.9f, buttonPaint);
    }

    public void drawHangar(Canvas canvas, int screenWidth, int screenHeight, SaveManager saveManager) {
        canvas.drawColor(Color.rgb(10, 10, 30));
        
        canvas.drawText("SHIP HANGAR", screenWidth / 2f, screenHeight * 0.15f, titlePaint);
        canvas.drawText("SCRAP: " + saveManager.getTotalScrap(), screenWidth * 0.95f, screenHeight * 0.15f, scrapCountPaint);

        float startX = screenWidth * 0.1f;
        float startY = screenHeight * 0.3f;
        float spacingY = screenHeight * 0.12f;

        drawUpgradeRow(canvas, "REINFORCED HULL", SaveManager.UPGRADE_HULL, startX, startY, screenWidth, saveManager);
        drawUpgradeRow(canvas, "FUSION THRUSTERS", SaveManager.UPGRADE_SPEED, startX, startY + spacingY, screenWidth, saveManager);
        drawUpgradeRow(canvas, "WEAPON OVERCLOCK", SaveManager.UPGRADE_FIRE, startX, startY + spacingY * 2, screenWidth, saveManager);
        drawUpgradeRow(canvas, "SCRAP MAGNET", SaveManager.UPGRADE_MAGNET, startX, startY + spacingY * 3, screenWidth, saveManager);
        drawUpgradeRow(canvas, "EMERGENCY BATTERY", SaveManager.UPGRADE_BATTERY, startX, startY + spacingY * 4, screenWidth, saveManager);

        float backBtnW = screenWidth * 0.15f;
        float backBtnH = screenHeight * 0.1f;
        genericButtonRect.set(screenWidth * 0.05f, screenHeight * 0.85f, screenWidth * 0.05f + backBtnW, screenHeight * 0.85f + backBtnH);
        canvas.drawRect(genericButtonRect, buttonPaint);
        canvas.drawText("BACK", screenWidth * 0.05f + backBtnW/2, screenHeight * 0.85f + backBtnH * 0.65f, buttonPaint);
    }

    private void drawUpgradeRow(Canvas canvas, String label, String key, float x, float y, int screenWidth, SaveManager saveManager) {
        int level = saveManager.getUpgradeLevel(key);
        canvas.drawText(label + " (LVL " + level + "/5)", x, y, upgradeTitlePaint);

        float pipX = x;
        float pipY = y + 20;
        float pipW = screenWidth * 0.03f;
        float pipH = 15;
        float pipGap = screenWidth * 0.01f;
        for (int i = 0; i < 5; i++) {
            timerRect.set(pipX + i * (pipW + pipGap), pipY, pipX + i * (pipW + pipGap) + pipW, pipY + pipH);
            canvas.drawRect(timerRect, i < level ? timerPaint : timerBgPaint);
        }

        if (level < 5) {
            int cost = getUpgradeCost(level);
            float btnW = screenWidth * 0.2f;
            upgradeButtonRect.set(screenWidth * 0.7f, y - 40, screenWidth * 0.7f + btnW, y + 40);
            canvas.drawRoundRect(upgradeButtonRect, 10, 10, buttonPaint);
            canvas.drawText("BUY: " + cost, screenWidth * 0.7f + btnW/2, y + 15, buttonPaint);
        } else {
            canvas.drawText("MAXED", screenWidth * 0.7f + screenWidth * 0.1f, y + 15, statsPaint);
        }
    }

    private int getUpgradeCost(int currentLevel) {
        int[] costs = {50, 150, 450, 1200, 3000};
        return costs[currentLevel];
    }

    public void drawPauseOverlay(Canvas canvas, int screenWidth, int screenHeight) {
        canvas.drawColor(Color.argb(180, 0, 0, 0));
        canvas.drawText("PAUSED", screenWidth / 2f, screenHeight / 2f - 50, titlePaint);
        canvas.drawText("Tap to Resume", screenWidth / 2f, screenHeight / 2f + 60, subtitlePaint);
        
        float btnW = screenWidth * 0.25f;
        float btnH = screenHeight * 0.1f;
        genericButtonRect.set(screenWidth / 2f - btnW/2, screenHeight * 0.85f, screenWidth / 2f + btnW/2, screenHeight * 0.85f + btnH);
        canvas.drawRect(genericButtonRect, buttonPaint);
        canvas.drawText("QUIT TO OS", screenWidth / 2f, screenHeight * 0.85f + btnH * 0.65f, buttonPaint);
    }

    public void drawGameOver(Canvas canvas, int screenWidth, int screenHeight, int score, int highScore, boolean isNewHighScore) {
        canvas.drawColor(Color.BLACK);
        canvas.drawText("GAME OVER", screenWidth / 2f, screenHeight * 0.4f, titlePaint);
        canvas.drawText("Score: " + score, screenWidth / 2f, screenHeight * 0.5f, subtitlePaint);
        if (isNewHighScore) canvas.drawText("New High Score!", screenWidth / 2f, screenHeight * 0.6f, subtitlePaint);
        else canvas.drawText("High Score: " + highScore, screenWidth / 2f, screenHeight * 0.6f, subtitlePaint);
        canvas.drawText("Tap to Restart", screenWidth / 2f, screenHeight * 0.75f, subtitlePaint);
    }

    public void drawHUD(Canvas canvas, Player player, int screenWidth, int screenHeight, int score, int waveNumber, int scorePopTimer, int waveAnnouncementTimer, boolean isBossWave) {
        if (player != null && player.getHealthPoints() <= 2) {
            vignettePaint.setAlpha((int) (Math.abs(Math.sin(System.currentTimeMillis() / 300.0)) * 150));
            canvas.drawRect(0, 0, screenWidth, screenHeight, vignettePaint);
        }

        healthBar.draw(canvas, player, screenWidth);
        
        float baseScoreSize = 50 * (screenHeight / 1080f);
        if (scorePopTimer > 0) {
            float scoreScale = 1.0f + (scorePopTimer / (float) Constants.SCORE_POP_DURATION) * 0.3f;
            scorePaint.setTextSize(baseScoreSize * scoreScale);
            scorePaint.setColor(Color.CYAN);
        } else {
            scorePaint.setTextSize(baseScoreSize);
            scorePaint.setColor(Color.WHITE);
        }
        canvas.drawText("Score: " + score, screenWidth / 2f, screenHeight * 0.12f, scorePaint);
        if (waveNumber > 0) canvas.drawText("Wave: " + waveNumber, screenWidth / 2f, screenHeight * 0.17f, scorePaint);

        if (waveAnnouncementTimer > 0) {
            float animProgress = (float) waveAnnouncementTimer / Constants.WAVE_ANNOUNCEMENT_DURATION;
            float waveScale = 1.0f + (animProgress * 0.5f);
            titlePaint.setTextSize(120 * (screenHeight / 1080f) * waveScale);
            titlePaint.setAlpha((int) (Math.min(1.0f, animProgress * 2) * 255));
            String text = isBossWave ? "BOSS WAVE!" : "Wave " + waveNumber;
            canvas.drawText(text, screenWidth / 2f, screenHeight / 2f, titlePaint);
            titlePaint.setAlpha(255);
            titlePaint.setTextSize(120 * (screenHeight / 1080f));
        }
        
        drawPowerupTimers(canvas, player, screenWidth, screenHeight);
        drawPauseButton(canvas, screenWidth);
    }

    private void drawPowerupTimers(Canvas canvas, Player player, int screenWidth, int screenHeight) {
        if (player == null) return;
        float x = screenWidth * 0.05f;
        float y = screenHeight * 0.85f;
        float barWidth = screenWidth * 0.15f;
        float barHeight = 15;
        float spacing = screenHeight * 0.04f;

        y = drawTimer(canvas, "SPEED", player.getSpeedBoostTimer(), Color.CYAN, x, y, barWidth, barHeight, spacing);
        y = drawTimer(canvas, "FIRE", player.getRapidFireTimer(), Color.YELLOW, x, y, barWidth, barHeight, spacing);
        y = drawTimer(canvas, "SHIELD", player.getShieldTimer(), Color.MAGENTA, x, y, barWidth, barHeight, spacing);
        y = drawTimer(canvas, "HOMING", player.getHomingTimer(), Color.rgb(255, 100, 0), x, y, barWidth, barHeight, spacing);
    }

    private float drawTimer(Canvas canvas, String label, int ticks, int color, float x, float y, float width, float height, float spacing) {
        if (ticks <= 0) return y;
        float ratio = (float) ticks / 300;
        statsPaint.setColor(Color.WHITE);
        canvas.drawText(label, x + width/2, y - 5, statsPaint);
        timerRect.set(x, y, x + width, y + height);
        canvas.drawRoundRect(timerRect, 5, 5, timerBgPaint);
        timerPaint.setColor(color);
        timerRect.set(x, y, x + width * ratio, y + height);
        canvas.drawRoundRect(timerRect, 5, 5, timerPaint);
        return y - spacing;
    }

    private void drawPauseButton(Canvas canvas, int screenWidth) {
        float btnSize = Constants.PAUSE_BUTTON_SIZE;
        float margin = Constants.PAUSE_BUTTON_MARGIN;
        float btnX = screenWidth - btnSize - margin;
        float btnY = margin;
        float barWidth = btnSize * 0.2f;
        float barHeight = btnSize * 0.6f;
        float barY = btnY + btnSize * 0.2f;
        float gap = btnSize * 0.15f;
        float centerX = btnX + btnSize / 2f;
        canvas.drawRect(centerX - gap - barWidth, barY, centerX - gap, barY + barHeight, pauseButtonPaint);
        canvas.drawRect(centerX + gap, barY, centerX + gap + barWidth, barY + barHeight, pauseButtonPaint);
    }
}
