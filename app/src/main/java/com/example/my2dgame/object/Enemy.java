package com.example.my2dgame.object;

import com.example.my2dgame.EnemyType;
import com.example.my2dgame.GameLoop;

public class Enemy extends Circle {

    private static final double BASE_SPEED_PPS = 400.0 * 0.7;

    private final Player player;
    private final EnemyType type;
    private final double maxSpeed;
    private int zigzagTimer = 0;

    public Enemy(int color, Player player, double positionX, double positionY, float radius, EnemyType type) {
        super(color, positionX, positionY, radius);
        this.player = player;
        this.type = type;
        this.maxSpeed = (BASE_SPEED_PPS * type.getSpeedMultiplier()) / GameLoop.MAX_UPS;
    }

    public EnemyType getType() {
        return type;
    }

    @Override
    public void update() {
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
        positionX += velocityX;
        positionY += velocityY;
    }
}
