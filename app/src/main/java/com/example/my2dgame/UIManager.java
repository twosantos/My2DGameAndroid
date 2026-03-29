package com.example.my2dgame;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RadialGradient;
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
    
    private final HealthBar healthBar;
    private final Context context;

    public UIManager(Context context) {
        this.context = context;
        
        statsPaint = new Paint();
        statsPaint.setColor(ContextCompat.getColor(context, R.color.magenta));
        statsPaint.setTextSize(50);

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

        healthBar = new HealthBar(context);
    }

    public void initVignette(int screenWidth, int screenHeight) {
        RadialGradient gradient = new RadialGradient(screenWidth / 2f, screenHeight / 2f, screenWidth * 0.8f,
                new int[]{Color.TRANSPARENT, Color.argb(150, 255, 0, 0)}, null, Shader.TileMode.CLAMP);
        vignettePaint.setShader(gradient);
    }

    public void drawMenu(Canvas canvas, int screenWidth, int screenHeight, List<ParallaxLayer> parallaxLayers, int highScore, int startingWave) {
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

    public void drawPauseOverlay(Canvas canvas, int screenWidth, int screenHeight) {
        canvas.drawColor(Color.argb(180, 0, 0, 0));
        canvas.drawText("PAUSED", screenWidth / 2f, screenHeight / 2f - 50, titlePaint);
        canvas.drawText("Tap to Resume", screenWidth / 2f, screenHeight / 2f + 60, subtitlePaint);
        canvas.drawRect(screenWidth / 2f - 150, screenHeight - 180, screenWidth / 2f + 150, screenHeight - 80, buttonPaint);
        canvas.drawText("QUIT TO OS", screenWidth / 2f, screenHeight - 115, buttonPaint);
    }

    public void drawGameOver(Canvas canvas, int screenWidth, int screenHeight, int score, int highScore, boolean isNewHighScore) {
        canvas.drawColor(Color.BLACK);
        canvas.drawText("GAME OVER", screenWidth / 2f, screenHeight / 2f - 100, titlePaint);
        canvas.drawText("Score: " + score, screenWidth / 2f, screenHeight / 2f + 20, subtitlePaint);
        if (isNewHighScore) canvas.drawText("New High Score!", screenWidth / 2f, screenHeight / 2f + 100, subtitlePaint);
        else canvas.drawText("High Score: " + highScore, screenWidth / 2f, screenHeight / 2f + 100, subtitlePaint);
        canvas.drawText("Tap to Restart", screenWidth / 2f, screenHeight / 2f + 180, subtitlePaint);
    }

    public void drawHUD(Canvas canvas, Player player, int screenWidth, int screenHeight, int score, int waveNumber, int scorePopTimer, int waveAnnouncementTimer, boolean isBossWave) {
        // UX: Low Health Vignette
        if (player != null && player.getHealthPoints() <= 2) {
            vignettePaint.setAlpha((int) (Math.abs(Math.sin(System.currentTimeMillis() / 300.0)) * 150));
            canvas.drawRect(0, 0, screenWidth, screenHeight, vignettePaint);
        }

        healthBar.draw(canvas, player, screenWidth);
        
        float scoreScale = 1.0f;
        if (scorePopTimer > 0) {
            scoreScale = 1.0f + (scorePopTimer / (float) Constants.SCORE_POP_DURATION) * 0.3f;
            scorePaint.setTextSize(50 * scoreScale);
            scorePaint.setColor(Color.CYAN);
        } else {
            scorePaint.setTextSize(50);
            scorePaint.setColor(Color.WHITE);
        }
        canvas.drawText("Score: " + score, screenWidth / 2f, 130, scorePaint);
        if (waveNumber > 0) canvas.drawText("Wave: " + waveNumber, screenWidth / 2f, 180, scorePaint);

        if (waveAnnouncementTimer > 0) {
            float animProgress = (float) waveAnnouncementTimer / Constants.WAVE_ANNOUNCEMENT_DURATION;
            float waveScale = 1.0f + (animProgress * 0.5f);
            titlePaint.setTextSize(120 * waveScale);
            titlePaint.setAlpha((int) (Math.min(1.0f, animProgress * 2) * 255));
            String text = isBossWave ? "BOSS WAVE!" : "Wave " + waveNumber;
            canvas.drawText(text, screenWidth / 2f, screenHeight / 2f, titlePaint);
            titlePaint.setAlpha(255);
            titlePaint.setTextSize(120);
        }
        
        drawPauseButton(canvas, screenWidth);
    }

    private void drawPauseButton(Canvas canvas, int screenWidth) {
        float btnX = screenWidth - Constants.PAUSE_BUTTON_SIZE - Constants.PAUSE_BUTTON_MARGIN;
        float btnY = Constants.PAUSE_BUTTON_MARGIN;
        float barWidth = Constants.PAUSE_BUTTON_SIZE * 0.2f;
        float barHeight = Constants.PAUSE_BUTTON_SIZE * 0.6f;
        float barY = btnY + Constants.PAUSE_BUTTON_SIZE * 0.2f;
        float gap = Constants.PAUSE_BUTTON_SIZE * 0.15f;
        float centerX = btnX + Constants.PAUSE_BUTTON_SIZE / 2f;
        canvas.drawRect(centerX - gap - barWidth, barY, centerX - gap, barY + barHeight, pauseButtonPaint);
        canvas.drawRect(centerX + gap, barY, centerX + gap + barWidth, barY + barHeight, pauseButtonPaint);
    }
}
