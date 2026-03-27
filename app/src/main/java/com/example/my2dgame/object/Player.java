package com.example.my2dgame.object;

import android.content.Context;

import androidx.core.content.ContextCompat;

import com.example.my2dgame.Joystick;
import com.example.my2dgame.R;

/**
 * Player is the main PJ of the game and extends from Circle which is a GameObject
 */
public class Player extends Circle {
    private final Joystick joystick;

    public Player(Context context, Joystick joystick, double positionX, double positionY, float radius) {
        super(context, ContextCompat.getColor(context, R.color.player), positionX, positionY, radius);
        this.joystick = joystick;
    }

    public void update() {
        // update velocity based of joystick
        velocityX = joystick.actuatorX() * MAX_SPEED;
        velocityY = joystick.actuatorY() * MAX_SPEED;

        // update position based on velocity
        positionX += velocityX;
        positionY += velocityY;
    }
}
