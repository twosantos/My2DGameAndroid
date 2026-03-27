package com.example.my2dgame.object;

import android.content.Context;
import android.util.Log;

import androidx.core.content.ContextCompat;

import com.example.my2dgame.GameLoop;
import com.example.my2dgame.R;

public class Enemy extends Circle {
    protected static final double SPEED_PIXELS_PER_SECOND = 400.0*0.7;
    protected static final double MAX_SPEED = SPEED_PIXELS_PER_SECOND / GameLoop.MAX_UPS;

    private final Player player;

    public Enemy(Context context, Player player, double positionX, double positionY, float radius) {
        super(context, ContextCompat.getColor(context, R.color.enemy), positionX, positionY, radius);
        this.player = player;
    }

    @Override
    public void update() {
        double distanceToTargetX = player.positionX() - positionX;
        double distanceToTargetY = player.positionY() - positionY;

        double distanceToTarget = GameObject.getDistanceBetweenObjects(this, player);

        if (distanceToTarget > 1) {
            double directionX = distanceToTargetX / distanceToTarget;
            double directionY = distanceToTargetY / distanceToTarget;
            velocityX = directionX * MAX_SPEED;
            velocityY = directionY * MAX_SPEED;
        } else {
            velocityX = 0;
            velocityY = 0;
        }
        positionX += velocityX;
        positionY += velocityY;
    }
}
