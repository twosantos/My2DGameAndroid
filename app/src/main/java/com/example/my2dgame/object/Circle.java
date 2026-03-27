package com.example.my2dgame.object;

import android.graphics.Canvas;
import android.graphics.Paint;

import com.example.my2dgame.GameLoop;

/**
 * Circle is an abstract class which implements the draw method for a GameObject
 */
public abstract class Circle extends GameObject {

    protected static final double SPEED_PIXELS_PER_SECOND = 400.0;
    protected static final double MAX_SPEED = SPEED_PIXELS_PER_SECOND / GameLoop.MAX_UPS;
    protected float radius;
    protected Paint paint;
    public Circle(int color, double positionX, double positionY, float radius) {
        super(positionX, positionY);
        this.radius = radius;
        this.paint = new Paint();
        paint.setColor(color);
    }

    public float getRadius() {
        return radius;
    }

    /**
     * Check if two circles are overlapping.
     */
    public static boolean isColliding(Circle c1, Circle c2) {
        double distance = getDistanceBetweenObjects(c1, c2);
        return distance < c1.radius + c2.radius;
    }

    public void draw(Canvas canvas) {
        canvas.drawCircle((float) positionX, (float) positionY, radius, paint);
    }

    /**
     * Clamp position so the circle stays within screen bounds.
     */
    public void clampToScreen(int screenWidth, int screenHeight) {
        if (positionX - radius < 0) positionX = radius;
        if (positionX + radius > screenWidth) positionX = screenWidth - radius;
        if (positionY - radius < 0) positionY = radius;
        if (positionY + radius > screenHeight) positionY = screenHeight - radius;
    }

}
