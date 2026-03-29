package com.example.my2dgame.object;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Rect;

import androidx.core.content.ContextCompat;

import com.example.my2dgame.Joystick;
import com.example.my2dgame.PickupType;
import com.example.my2dgame.R;
import com.example.my2dgame.SpriteCache;

public class Player extends Circle {
    private static final int MAX_HEALTH = 5;
    private static final int EFFECT_DURATION = 300; // 10 seconds at 30 UPS

    private final Joystick joystick;
    private int healthPoints = MAX_HEALTH;

    private int speedBoostTimer = 0;
    private int rapidFireTimer = 0;
    private int shieldTimer = 0;
    
    private final Paint shieldPaint;
    private final Bitmap sprite;
    private final Rect dstRect = new Rect();
    private final Paint spritePaint = new Paint();
    private float rotationAngle = 0f;

    public Player(Context context, Joystick joystick, double positionX, double positionY, float radius) {
        super(ContextCompat.getColor(context, R.color.player), positionX, positionY, radius);
        this.joystick = joystick;
        
        shieldPaint = new Paint();
        shieldPaint.setColor(Color.MAGENTA);
        shieldPaint.setAlpha(80);
        shieldPaint.setStyle(Paint.Style.STROKE);
        shieldPaint.setStrokeWidth(10);

        // Use cache instead of decoding and processing every time
        sprite = SpriteCache.getSprite(context, R.drawable.spaceship);
        
        // Apply tint
        spritePaint.setColorFilter(new PorterDuffColorFilter(paint.getColor(), PorterDuff.Mode.SRC_ATOP));
    }

    public int getHealthPoints() {
        return healthPoints;
    }

    public int getMaxHealth() {
        return MAX_HEALTH;
    }

    public boolean isAlive() {
        return healthPoints > 0;
    }

    public void takeDamage() {
        if (shieldTimer > 0) {
            shieldTimer = 0; // Shield breaks on hit
            return;
        }
        healthPoints--;
    }

    public void reset(double x, double y) {
        positionX = x;
        positionY = y;
        velocityX = 0;
        velocityY = 0;
        healthPoints = MAX_HEALTH;
        speedBoostTimer = 0;
        rapidFireTimer = 0;
        shieldTimer = 0;
        rotationAngle = 0f;
    }

    public void applyPickup(PickupType type) {
        switch (type) {
            case HEALTH:
                healthPoints = Math.min(MAX_HEALTH, healthPoints + 2);
                break;
            case SPEED:
                speedBoostTimer = EFFECT_DURATION;
                break;
            case RAPID_FIRE:
                rapidFireTimer = EFFECT_DURATION;
                break;
            case SHIELD:
                shieldTimer = EFFECT_DURATION;
                break;
        }
    }

    public boolean hasRapidFire() {
        return rapidFireTimer > 0;
    }

    @Override
    public void update(double dt) {
        double currentSpeed = SPEED_PPS * (speedBoostTimer > 0 ? 1.6 : 1.0);
        velocityX = joystick.actuatorX() * currentSpeed;
        velocityY = joystick.actuatorY() * currentSpeed;
        
        positionX += velocityX * dt;
        positionY += velocityY * dt;

        // Update rotation based on movement direction
        if (velocityX != 0 || velocityY != 0) {
            // Reverting to +90 as it was correct before (assuming sprite faces UP)
            rotationAngle = (float) Math.toDegrees(Math.atan2(velocityY, velocityX)) + 90;
        }

        if (speedBoostTimer > 0) speedBoostTimer--;
        if (rapidFireTimer > 0) rapidFireTimer--;
        if (shieldTimer > 0) shieldTimer--;
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

        if (shieldTimer > 0) {
            canvas.drawCircle((float)positionX, (float)positionY, radius + 10, shieldPaint);
        }
    }
}
