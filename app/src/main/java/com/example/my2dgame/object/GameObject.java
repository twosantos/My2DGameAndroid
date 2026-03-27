package com.example.my2dgame.object;

import android.graphics.Canvas;

public abstract class GameObject {
    protected double positionX;
    protected double positionY;
    protected double velocityX;
    protected double velocityY;

    public GameObject(double positionX, double positionY) {
        this.positionX = positionX;
        this.positionY = positionY;
    }

    protected static double getDistanceBetweenObjects(GameObject ob1, GameObject ob2) {
        return Math.sqrt(
                Math.pow(ob2.positionX() - ob1.positionX(), 2) +
                        Math.pow(ob2.positionY() - ob1.positionY(), 2)
        );
    }

    public double positionX() {
        return positionX;
    }

    public double positionY() {
        return positionY;
    }

    public abstract void draw(Canvas canvas);

    public abstract void update();
}
