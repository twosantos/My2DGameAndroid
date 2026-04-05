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

import com.example.my2dgame.Constants;
import com.example.my2dgame.EngineTrail;
import com.example.my2dgame.Joystick;
import com.example.my2dgame.PickupType;
import com.example.my2dgame.R;
import com.example.my2dgame.SaveManager;
import com.example.my2dgame.ShipProfile;
import com.example.my2dgame.SpriteCache;

public class Player extends Circle {
    private final Joystick joystick;
    private final Context context;
    private ShipProfile profile;
    private SaveManager saveManager;

    private int healthPoints;
    private int maxHealth;
    
    private int speedBoostTimer = 0;
    private int rapidFireTimer = 0;
    private int shieldTimer = 0;
    private int homingTimer = 0;
    
    private final Paint shieldPaint;
    private Bitmap sprite;
    private final Rect dstRect = new Rect();
    private final Paint spritePaint = new Paint();
    private float rotationAngle = 0f;
    private final EngineTrail engineTrail;

    // Animation State
    private boolean isAnimating = false;
    private double targetX, targetY;
    private static final double FLY_IN_SPEED = 600.0;

    public Player(Context context, SaveManager saveManager, ShipProfile profile, Joystick joystick, double positionX, double positionY, float radius) {
        super(ContextCompat.getColor(context, R.color.player), positionX, positionY, radius);
        this.context = context;
        this.joystick = joystick;
        this.saveManager = saveManager;
        this.profile = profile;

        initFromProfile(profile, saveManager);

        shieldPaint = new Paint();
        shieldPaint.setColor(Color.MAGENTA);
        shieldPaint.setAlpha(80);
        shieldPaint.setStyle(Paint.Style.STROKE);
        shieldPaint.setStrokeWidth(10);

        engineTrail = new EngineTrail(30, 0.4f);
    }

    public void initFromProfile(ShipProfile profile, SaveManager saveManager) {
        this.profile = profile;
        this.saveManager = saveManager;
        
        int hullUpgrade = saveManager.getUpgradeLevel(SaveManager.UPGRADE_HULL);
        this.maxHealth = profile.getBaseHealth() + hullUpgrade;
        this.healthPoints = this.maxHealth;
        
        this.sprite = SpriteCache.getSprite(context, profile.getSpriteResId());
    }

    public int getHealthPoints() { return healthPoints; }
    public int getMaxHealth() { return maxHealth; }
    public boolean isAlive() { return healthPoints > 0; }
    public ShipProfile getProfile() { return profile; }

    public void takeDamage() {
        if (isAnimating) return; // Invulnerable while flying in
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
        
        // Recalculate stats in case upgrades changed
        int hullUpgrade = saveManager.getUpgradeLevel(SaveManager.UPGRADE_HULL);
        this.maxHealth = profile.getBaseHealth() + hullUpgrade;
        this.healthPoints = this.maxHealth;
        
        speedBoostTimer = 0;
        rapidFireTimer = 0;
        shieldTimer = 0;
        homingTimer = 0;
        rotationAngle = 0f;
        isAnimating = false;
    }

    public void startFlyIn(double tx, double ty) {
        this.targetX = tx;
        this.targetY = ty;
        this.isAnimating = true;
    }

    public boolean isAnimating() { return isAnimating; }

    public void applyPickup(PickupType type) {
        int duration = 300;
        if (profile == ShipProfile.TECH) {
            duration *= 2; // Tech trait: double power-up duration
        }

        switch (type) {
            case HEALTH:
                healthPoints = Math.min(maxHealth, healthPoints + 2);
                break;
            case SPEED:
                speedBoostTimer = duration;
                break;
            case RAPID_FIRE:
                rapidFireTimer = duration;
                break;
            case SHIELD:
                shieldTimer = duration;
                break;
            case HOMING:
                homingTimer = duration;
                break;
        }
    }

    public boolean hasRapidFire() { return rapidFireTimer > 0; }
    public boolean hasHoming() { return homingTimer > 0; }
    public int getSpeedBoostTimer() { return speedBoostTimer; }
    public int getRapidFireTimer() { return rapidFireTimer; }
    public int getShieldTimer() { return shieldTimer; }
    public int getHomingTimer() { return homingTimer; }

    /**
     * Calculates the current firing interval based on permanent upgrades and temp boosts.
     */
    public int getFireInterval() {
        int fireUpgrade = saveManager.getUpgradeLevel(SaveManager.UPGRADE_FIRE);
        // Each level reduces interval by 1 tick (approx 10%)
        int baseInterval = Constants.FIRE_INTERVAL - fireUpgrade;
        int rapidInterval = Constants.RAPID_FIRE_INTERVAL;
        
        return hasRapidFire() ? rapidInterval : Math.max(4, baseInterval);
    }

    /**
     * Task 1.4: Collection radius logic
     */
    public float getMagnetRadius() {
        int magnetUpgrade = saveManager.getUpgradeLevel(SaveManager.UPGRADE_MAGNET);
        float baseRadius = 250f;
        return baseRadius * (1 + 0.25f * magnetUpgrade);
    }

    @Override
    public void update(double dt) {
        if (isAnimating) {
            double dx = targetX - positionX;
            double dy = targetY - positionY;
            double dist = Math.sqrt(dx*dx + dy*dy);
            
            if (dist < 10) {
                positionX = targetX;
                positionY = targetY;
                isAnimating = false;
            } else {
                velocityX = (dx / dist) * FLY_IN_SPEED;
                velocityY = (dy / dist) * FLY_IN_SPEED;
                positionX += velocityX * dt;
                positionY += velocityY * dt;
                rotationAngle = (float) Math.toDegrees(Math.atan2(velocityY, velocityX)) + 90;
                engineTrail.emit(positionX, positionY, radius, rotationAngle);
            }
        } else {
            int speedUpgrade = saveManager.getUpgradeLevel(SaveManager.UPGRADE_SPEED);
            double speedMod = profile.getSpeedMultiplier() * (1 + 0.05 * speedUpgrade);
            double currentMaxSpeed = SPEED_PPS * speedMod * (speedBoostTimer > 0 ? 1.6 : 1.0);
            
            velocityX = joystick.actuatorX() * currentMaxSpeed;
            velocityY = joystick.actuatorY() * currentMaxSpeed;
            
            positionX += velocityX * dt;
            positionY += velocityY * dt;

            if (velocityX != 0 || velocityY != 0) {
                rotationAngle = (float) Math.toDegrees(Math.atan2(velocityY, velocityX)) + 90;
                engineTrail.emit(positionX, positionY, radius, rotationAngle);
            }
        }
        
        engineTrail.update();

        if (speedBoostTimer > 0) speedBoostTimer--;
        if (rapidFireTimer > 0) rapidFireTimer--;
        if (shieldTimer > 0) shieldTimer--;
        if (homingTimer > 0) homingTimer--;
    }

    @Override
    public void draw(Canvas canvas) {
        engineTrail.draw(canvas);

        if (!saveManager.isRetroMode() && sprite != null) {
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
            // Retro Mode: Draw primitive shape
            canvas.drawCircle((float) positionX, (float) positionY, radius, paint);
        }

        if (shieldTimer > 0) {
            canvas.drawCircle((float)positionX, (float)positionY, radius + 10, shieldPaint);
        }
    }
}
