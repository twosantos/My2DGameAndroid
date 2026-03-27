package com.example.my2dgame.object;

import android.content.Context;

import androidx.core.content.ContextCompat;

import com.example.my2dgame.Joystick;
import com.example.my2dgame.R;

/**
 * Player is the main PJ of the game and extends from Circle which is a GameObject
 */
public class Player extends Circle {
    private static final int MAX_HEALTH = 5;

    private final Joystick joystick;
    private int healthPoints = MAX_HEALTH;

    public Player(Context context, Joystick joystick, double positionX, double positionY, float radius) {
        super(ContextCompat.getColor(context, R.color.player), positionX, positionY, radius);
        this.joystick = joystick;
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
        healthPoints--;
    }

    public void reset(double x, double y) {
        positionX = x;
        positionY = y;
        velocityX = 0;
        velocityY = 0;
        healthPoints = MAX_HEALTH;
    }

    public void update() {
        velocityX = joystick.actuatorX() * MAX_SPEED;
        velocityY = joystick.actuatorY() * MAX_SPEED;
        positionX += velocityX;
        positionY += velocityY;
    }
}
