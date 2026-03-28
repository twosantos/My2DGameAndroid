package com.example.my2dgame.object;

import com.example.my2dgame.EnemyType;

public class Enemy extends Circle {

    private static final double BASE_SPEED_PPS = 400.0 * 0.7;

    private final Player player;
    private EnemyType type;
    private double maxSpeed;
    private int zigzagTimer = 0;

    public Enemy(int color, Player player, double positionX, double positionY, float radius, EnemyType type) {
        super(color, positionX, positionY, radius);
        this.player = player;
        this.type = type;
        this.maxSpeed = BASE_SPEED_PPS * type.getSpeedMultiplier();
    }

    public EnemyType getType() {
        return type;
    }

    /**
     * Reinitialize this enemy for object pooling reuse.
     */
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
    }

    @Override
    public void update(double dt) {
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
    }
}
