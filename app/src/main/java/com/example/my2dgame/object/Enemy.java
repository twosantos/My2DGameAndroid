package com.example.my2dgame.object;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Rect;
import com.example.my2dgame.EnemyType;
import com.example.my2dgame.R;
import com.example.my2dgame.SpriteCache;

public class Enemy extends Circle {

    private static final double BASE_SPEED_PPS = 400.0 * 0.7;
    private static final int FLASH_DURATION = 4;

    private final Player player;
    private EnemyType type;
    private double maxSpeed;
    private int zigzagTimer = 0;
    
    private int health;
    private int maxHealth;
    private boolean isBoss;
    private final Paint healthBarPaint;
    private final Paint healthBarBgPaint;
    private final Bitmap sprite;
    private final Rect dstRect = new Rect();
    private final Paint spritePaint = new Paint();
    private float rotationAngle = 0f;
    private float rotationSpeed = 0f;
    private int flashTimer = 0;
    private final PorterDuffColorFilter tintFilter;
    private final PorterDuffColorFilter flashFilter = new PorterDuffColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);

    public Enemy(int color, Player player, double positionX, double positionY, float radius, EnemyType type, Context context) {
        super(color, positionX, positionY, radius);
        this.player = player;
        this.type = type;
        this.maxSpeed = BASE_SPEED_PPS * type.getSpeedMultiplier();
        this.isBoss = false;
        this.health = 1;
        this.maxHealth = 1;

        healthBarPaint = new Paint();
        healthBarPaint.setColor(Color.RED);
        healthBarBgPaint = new Paint();
        healthBarBgPaint.setColor(Color.DKGRAY);

        this.sprite = SpriteCache.getSprite(context, R.drawable.asteroid);
        
        tintFilter = new PorterDuffColorFilter(color, PorterDuff.Mode.SRC_ATOP);
        spritePaint.setColorFilter(tintFilter);
        
        rotationSpeed = (float) (Math.random() * 180 - 90);
    }

    public EnemyType getType() {
        return type;
    }

    public boolean isBoss() {
        return isBoss;
    }

    public void setAsBoss(int health) {
        this.isBoss = true;
        this.health = health;
        this.maxHealth = health;
    }

    public boolean takeDamage() {
        health--;
        flashTimer = FLASH_DURATION;
        return health <= 0;
    }

    public float getHealthRatio() {
        return (float) health / maxHealth;
    }

    public void reset(int color, double positionX, double positionY, float radius, EnemyType type) {
        this.positionX = positionX;
        this.positionY = positionY;
        this.radius = radius;
        this.type = type;
        this.maxSpeed = BASE_SPEED_PPS * type.getSpeedMultiplier();
        this.zigzagTimer = 0;
        this.velocityX = 0;
        this.velocityY = 0;
        this.paint.setColor(color);
        this.isBoss = false;
        this.health = 1;
        this.maxHealth = 1;
        this.rotationAngle = 0f;
        this.rotationSpeed = (float) (Math.random() * 180 - 90);
        this.flashTimer = 0;
        
        spritePaint.setColorFilter(new PorterDuffColorFilter(color, PorterDuff.Mode.SRC_ATOP));
    }

    @Override
    public void update(double dt) {
        if (flashTimer > 0) flashTimer--;

        double distanceToTargetX = player.positionX() - positionX;
        double distanceToTargetY = player.positionY() - positionY;
        double distanceToTarget = GameObject.getDistanceBetweenObjects(this, player);

        if (distanceToTarget > 1) {
            double directionX = distanceToTargetX / distanceToTarget;
            double directionY = distanceToTargetY / distanceToTarget;

            if (type == EnemyType.ZIGZAG) {
                zigzagTimer++;
                double perpX = -directionY;
                double perpY = directionX;
                double zigzag = Math.sin(zigzagTimer * 0.15) * 0.8;
                velocityX = (directionX + perpX * zigzag) * maxSpeed;
                velocityY = (directionY + perpY * zigzag) * maxSpeed;
            } else {
                velocityX = directionX * maxSpeed;
                velocityY = directionY * maxSpeed;
            }
        } else {
            velocityX = 0;
            velocityY = 0;
        }
        positionX += velocityX * dt;
        positionY += velocityY * dt;
        
        rotationAngle += rotationSpeed * dt;
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
        
        if (flashTimer > 0) {
            spritePaint.setColorFilter(flashFilter);
        } else {
            spritePaint.setColorFilter(tintFilter);
        }

        canvas.drawBitmap(sprite, null, dstRect, spritePaint);
        canvas.restore();

        if (isBoss) {
            float barWidth = radius * 2;
            float barHeight = 15;
            float x = (float) positionX - radius;
            float y = (float) positionY - radius - 25;

            canvas.drawRect(x, y, x + barWidth, y + barHeight, healthBarBgPaint);
            canvas.drawRect(x, y, x + barWidth * getHealthRatio(), y + barHeight, healthBarPaint);
        }
    }
}
