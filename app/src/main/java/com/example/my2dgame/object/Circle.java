package com.example.my2dgame.object;

import android.content.Context;
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
    public Circle(Context context, int color, double positionX, double positionY, float radius) {
        super(positionX, positionY);
        this.radius = radius;
        this.paint = new Paint();
        paint.setColor(color);
    }

    public void draw(Canvas canvas) {
        canvas.drawCircle((float) positionX, (float) positionY, radius, paint);
    }

}
