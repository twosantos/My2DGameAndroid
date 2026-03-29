package com.example.my2dgame;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import androidx.core.content.ContextCompat;
import com.example.my2dgame.object.Player;

/**
 * Modern, styled health bar for the player.
 */
public class HealthBar {
    private final Paint borderPaint;
    private final Paint bgPaint;
    private final Paint fillPaint;
    private final RectF outerRect = new RectF();
    private final RectF innerRect = new RectF();
    
    private final float width = 400;
    private final float height = 30;
    private final float margin = 40;
    private final float cornerRadius = 15;
    
    public HealthBar(Context context) {
        borderPaint = new Paint();
        borderPaint.setColor(Color.WHITE);
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeWidth(4);
        borderPaint.setAntiAlias(true);

        bgPaint = new Paint();
        bgPaint.setColor(ContextCompat.getColor(context, R.color.health_bar_bg));
        bgPaint.setStyle(Paint.Style.FILL);
        bgPaint.setAntiAlias(true);

        fillPaint = new Paint();
        fillPaint.setStyle(Paint.Style.FILL);
        fillPaint.setAntiAlias(true);
    }

    public void draw(Canvas canvas, Player player, int screenWidth) {
        if (player == null) return;

        // Position: Bottom Left (Traditional for horizontal bars)
        // or Top Center. Let's go with Top Center for a "Star Defender" feel.
        float x = (screenWidth - width) / 2f;
        float y = margin;

        outerRect.set(x, y, x + width, y + height);
        
        // Draw background
        canvas.drawRoundRect(outerRect, cornerRadius, cornerRadius, bgPaint);

        // Calculate fill
        float healthRatio = (float) player.getHealthPoints() / player.getMaxHealth();
        if (healthRatio > 0.6f) {
            fillPaint.setColor(Color.GREEN);
        } else if (healthRatio > 0.3f) {
            fillPaint.setColor(Color.YELLOW);
        } else {
            fillPaint.setColor(Color.RED);
        }

        float fillWidth = width * healthRatio;
        if (fillWidth < cornerRadius * 2 && fillWidth > 0) fillWidth = cornerRadius * 2; // Keep it rounded
        
        innerRect.set(x, y, x + fillWidth, y + height);
        canvas.drawRoundRect(innerRect, cornerRadius, cornerRadius, fillPaint);

        // Draw border
        canvas.drawRoundRect(outerRect, cornerRadius, cornerRadius, borderPaint);
    }
}
