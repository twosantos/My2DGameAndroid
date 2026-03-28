package com.example.my2dgame.object;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import androidx.core.content.ContextCompat;

import com.example.my2dgame.Joystick;
import com.example.my2dgame.PickupType;
import com.example.my2dgame.R;

public class Player extends Circle {
    private static final int MAX_HEALTH = 5;
    private static final int EFFECT_DURATION = 300; // 10 seconds at 30 UPS

    private final Joystick joystick;
    private int healthPoints = MAX_HEALTH;

    private int speedBoostTimer = 0;
    private int rapidFireTimer = 0;
    private int shieldTimer = 0;
    
    private final Paint shieldPaint;

    public Player(Context context, Joystick joystick, double positionX, double positionY, float radius) {
        super(ContextCompat.getColor(context, R.color.player), positionX, positionY, radius);
        this.joystick = joystick;
        
        shieldPaint = new Paint();
        shieldPaint.setColor(Color.MAGENTA);
        shieldPaint.setAlpha(80);
        shieldPaint.setStyle(Paint.Style.STROKE);
        shieldPaint.setStrokeWidth(10);
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

        if (speedBoostTimer > 0) speedBoostTimer--;
        if (rapidFireTimer > 0) rapidFireTimer--;
        if (shieldTimer > 0) shieldTimer--;
    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
        if (shieldTimer > 0) {
            canvas.drawCircle((float)positionX, (float)positionY, radius + 10, shieldPaint);
        }
    }
}
