package com.example.my2dgame.object;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Rect;
import com.example.my2dgame.R;
import com.example.my2dgame.SpriteCache;

/**
 * Projectile fired by the player. Travels in a straight line at fixed velocity.
 */
public class Projectile extends Circle {

    private static final double SPEED_PPS = 800.0;
    private final Bitmap sprite;
    private final Rect dstRect = new Rect();
    private final Paint spritePaint = new Paint();
    private float rotationAngle = 0f;

    public Projectile(double positionX, double positionY, float radius, double directionX, double directionY, Context context) {
        super(Color.CYAN, positionX, positionY, radius);
        velocityX = directionX * SPEED_PPS;
        velocityY = directionY * SPEED_PPS;
        
        // Optimization: Use SpriteCache
        sprite = SpriteCache.getSprite(context, R.drawable.rocket);
        
        // Apply tint
        spritePaint.setColorFilter(new PorterDuffColorFilter(Color.CYAN, PorterDuff.Mode.SRC_ATOP));
        
        // Initial rotation
        updateRotation();
    }

    /**
     * Reinitialize this projectile for object pooling reuse.
     */
    public void reset(double positionX, double positionY, float radius, double directionX, double directionY) {
        this.positionX = positionX;
        this.positionY = positionY;
        this.radius = radius;
        velocityX = directionX * SPEED_PPS;
        velocityY = directionY * SPEED_PPS;
        updateRotation();
    }

    private void updateRotation() {
        if (velocityX != 0 || velocityY != 0) {
            // Assuming the sprite faces BOTTOM-RIGHT (45 deg) by default.
            // atan2(y, x) returns angle from positive X axis.
            // For a sprite facing 45deg, we subtract 45deg to align it to 0.
            rotationAngle = (float) Math.toDegrees(Math.atan2(velocityY, velocityX)) - 45;
        }
    }

    @Override
    public void update(double dt) {
        positionX += velocityX * dt;
        positionY += velocityY * dt;
    }

    @Override
    public void draw(Canvas canvas) {
        dstRect.set(
            (int) (positionX - radius),
            (int) (positionY - radius),
            (int) (positionX + radius),
            (int) (positionY + radius)
        );
        
        canvas.save();
        canvas.rotate(rotationAngle, (float) positionX, (float) positionY);
        canvas.drawBitmap(sprite, null, dstRect, spritePaint);
        canvas.restore();
    }

    public boolean isOffScreen(int screenWidth, int screenHeight) {
        return positionX + radius < 0
                || positionX - radius > screenWidth
                || positionY + radius < 0
                || positionY - radius > screenHeight;
    }
}
