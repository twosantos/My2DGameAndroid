package com.example.my2dgame;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Rect;

/**
 * Lightweight particle for visual effects. Scatters outward and fades.
 */
public class Particle {

    private static final double SPEED_PPS = 200.0;
    private static final double FADE_PPS = 510.0; // alpha per second (dies in ~0.5s)

    private double positionX;
    private double positionY;
    private double velocityX;
    private double velocityY;
    private float radius;
    private int alpha;
    private final Paint paint = new Paint();
    private Bitmap sprite;
    private final Rect dstRect = new Rect();

    public void init(double x, double y, float radius, int color, double dirX, double dirY, Context context) {
        this.positionX = x;
        this.positionY = y;
        this.radius = radius;
        this.alpha = 255;
        this.velocityX = dirX * SPEED_PPS;
        this.velocityY = dirY * SPEED_PPS;
        paint.setAlpha(255);
        paint.setColorFilter(new PorterDuffColorFilter(color, PorterDuff.Mode.SRC_ATOP));
        
        // Optimization: Use SpriteCache to avoid expensive bitmap operations during gameplay
        this.sprite = SpriteCache.getSprite(context, R.drawable.spiky_explosion);
    }

    public void update(double dt) {
        positionX += velocityX * dt;
        positionY += velocityY * dt;
        alpha -= (int) (FADE_PPS * dt);
        if (alpha < 0) alpha = 0;
        paint.setAlpha(alpha);
    }

    public void draw(Canvas canvas) {
        if (alpha > 0) {
            dstRect.set(
                (int) (positionX - radius),
                (int) (positionY - radius),
                (int) (positionX + radius),
                (int) (positionY + radius)
            );
            canvas.drawBitmap(sprite, null, dstRect, paint);
        }
    }

    public boolean isDead() {
        return alpha <= 0;
    }
}
