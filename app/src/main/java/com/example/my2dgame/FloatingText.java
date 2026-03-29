package com.example.my2dgame;

import android.graphics.Canvas;
import android.graphics.Paint;

public class FloatingText {
    private double x, y;
    private final String text;
    private final Paint paint;
    private int alpha = 255;
    private static final double SPEED_Y = -100.0; // pixels per second up
    private static final int FADE_SPEED = 400; // alpha per second

    public FloatingText(String text, double x, double y, int color, int textSize) {
        this.text = text;
        this.x = x;
        this.y = y;
        this.paint = new Paint();
        this.paint.setColor(color);
        this.paint.setTextSize(textSize);
        this.paint.setTextAlign(Paint.Align.CENTER);
        this.paint.setFakeBoldText(true);
    }

    public boolean update(double dt) {
        y += SPEED_Y * dt;
        alpha -= (int) (FADE_SPEED * dt);
        if (alpha < 0) alpha = 0;
        paint.setAlpha(alpha);
        return alpha <= 0;
    }

    public void draw(Canvas canvas) {
        canvas.drawText(text, (float) x, (float) y, paint);
    }
}
