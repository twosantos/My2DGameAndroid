package com.example.my2dgame.object;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import com.example.my2dgame.PickupType;

public class Pickup extends Circle {
    private final PickupType type;
    private int lifeTimer;
    private static final int MAX_LIFE = 300; // 10 seconds at 30 UPS
    private final Paint labelPaint;

    public Pickup(PickupType type, double x, double y, float radius) {
        super(type.getColor(), x, y, radius);
        this.type = type;
        this.lifeTimer = MAX_LIFE;
        
        labelPaint = new Paint();
        labelPaint.setColor(Color.WHITE);
        labelPaint.setTextSize(30);
        labelPaint.setTextAlign(Paint.Align.CENTER);
    }

    public void reset(PickupType type, double x, double y) {
        this.positionX = x;
        this.positionY = y;
        this.paint.setColor(type.getColor());
        this.lifeTimer = MAX_LIFE;
        // The type field is final, so pooling might need care if types change.
        // For simplicity, let's assume we create new ones or handle type change.
    }

    public PickupType getType() {
        return type;
    }

    public boolean isExpired() {
        return lifeTimer <= 0;
    }

    public void update(double dt) {
        if (lifeTimer > 0) lifeTimer--;
        
        // Pulsing effect
        float pulse = (float) Math.sin(System.currentTimeMillis() / 200.0) * 5;
        this.radius = 25 + pulse;
    }

    @Override
    public void draw(Canvas canvas) {
        // Draw glow/fading effect based on lifeTimer
        int alpha = (lifeTimer < 60) ? (lifeTimer * 255 / 60) : 255;
        paint.setAlpha(alpha);
        super.draw(canvas);
        
        labelPaint.setAlpha(alpha);
        canvas.drawText(type.getLabel(), (float)positionX, (float)positionY - radius - 10, labelPaint);
        
        paint.setAlpha(255); // Restore
        labelPaint.setAlpha(255);
    }
}
