package com.example.my2dgame.object;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Rect;
import com.example.my2dgame.EngineTrail;
import com.example.my2dgame.R;
import com.example.my2dgame.SaveManager;
import com.example.my2dgame.SpriteCache;
import java.util.HashSet;
import java.util.Set;

/**
 * Projectile fired by the player. Travels in a straight line at fixed velocity.
 */
public class Projectile extends Circle {

    private static final double SPEED_PPS = 800.0;
    private final Bitmap sprite;
    private final Rect dstRect = new Rect();
    private final Paint spritePaint = new Paint();
    private float rotationAngle = 0f;
    private Enemy target = null;
    private boolean isHoming = false;
    private boolean isPiercing = false;
    private final Set<Integer> hitEnemies = new HashSet<>();
    private final EngineTrail engineTrail;
    private SaveManager saveManager;

    public Projectile(double positionX, double positionY, float radius, double directionX, double directionY, Context context, SaveManager saveManager) {
        super(Color.WHITE, positionX, positionY, radius);
        this.saveManager = saveManager;
        velocityX = directionX * SPEED_PPS;
        velocityY = directionY * SPEED_PPS;
        
        sprite = SpriteCache.getSprite(context, R.drawable.rocket);
        spritePaint.setColorFilter(new PorterDuffColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP));
        engineTrail = new EngineTrail(15, 0.4f);
        updateRotation();
    }

    public void reset(double positionX, double positionY, float radius, double directionX, double directionY) {
        this.positionX = positionX;
        this.positionY = positionY;
        this.radius = radius;
        this.velocityX = directionX * SPEED_PPS;
        this.velocityY = directionY * SPEED_PPS;
        this.target = null;
        this.isHoming = false;
        this.isPiercing = false;
        this.hitEnemies.clear();
        spritePaint.setColorFilter(new PorterDuffColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP));
        engineTrail.reset();
        updateRotation();
    }

    public void setHoming(Enemy target) {
        this.isHoming = true;
        this.target = target;
        spritePaint.setColorFilter(new PorterDuffColorFilter(Color.rgb(255, 100, 0), PorterDuff.Mode.SRC_ATOP));
    }

    public void setPiercing(boolean piercing) {
        this.isPiercing = piercing;
        if (piercing) {
            spritePaint.setColorFilter(new PorterDuffColorFilter(Color.MAGENTA, PorterDuff.Mode.SRC_ATOP));
        }
    }

    public boolean isPiercing() {
        return isPiercing;
    }

    public boolean hasHitEnemy(Enemy enemy) {
        return hitEnemies.contains(enemy.hashCode());
    }

    public void registerHit(Enemy enemy) {
        hitEnemies.add(enemy.hashCode());
    }

    private void updateRotation() {
        if (velocityX != 0 || velocityY != 0) {
            rotationAngle = (float) Math.toDegrees(Math.atan2(velocityY, velocityX)) - 45;
        }
    }

    @Override
    public void update(double dt) {
        if (isHoming && target != null) {
            if (target.isDead()) {
                target = null;
            }
            
            if (target != null) {
                double dx = target.positionX() - positionX;
                double dy = target.positionY() - positionY;
                double dist = Math.sqrt(dx*dx + dy*dy);
                if (dist > 0) {
                    velocityX = (dx / dist) * SPEED_PPS;
                    velocityY = (dy / dist) * SPEED_PPS;
                    updateRotation();
                }
            }
        }

        positionX += velocityX * dt;
        positionY += velocityY * dt;

        engineTrail.emit(positionX, positionY, radius, rotationAngle);
        engineTrail.update();
    }

    @Override
    public void draw(Canvas canvas) {
        engineTrail.draw(canvas);

        if (saveManager != null && !saveManager.isRetroMode() && sprite != null) {
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
        } else {
            canvas.drawCircle((float) positionX, (float) positionY, radius, paint);
        }
    }

    public boolean isOffScreen(int screenWidth, int screenHeight) {
        return positionX + radius < 0
                || positionX - radius > screenWidth
                || positionY + radius < 0
                || positionY - radius > screenHeight;
    }
}
