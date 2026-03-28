package com.example.my2dgame;

import android.graphics.Canvas;
import android.graphics.Paint;

public class Joystick {

    private Paint outerCirclePaint;
    private Paint innerCirclePaint;
    private int outerCircleRadius;
    private int innerCircleRadius;
    private int outerCircleCenterPositionX;
    private int outerCircleCenterPositionY;
    private int innerCircleCenterPositionX;
    private int innerCircleCenterPositionY;
    private boolean pressed = false;
    private boolean hasBeenInitialised = false;
    private double actuatorX;
    private double actuatorY;

    public Joystick(
            int centerPositionX,
            int centerPositionY,
            int outerCircleRadius,
            int innerCircleRadius,
            int innerColor,
            int outerColor
    ) {
        this.outerCircleCenterPositionX = centerPositionX;
        this.outerCircleCenterPositionY = centerPositionY;
        this.innerCircleCenterPositionX = centerPositionX;
        this.innerCircleCenterPositionY = centerPositionY;
        this.outerCircleRadius = outerCircleRadius;
        this.innerCircleRadius = innerCircleRadius;

        innerCirclePaint = new Paint();
        innerCirclePaint.setColor(innerColor);
        innerCirclePaint.setStyle(Paint.Style.FILL_AND_STROKE);

        outerCirclePaint = new Paint();
        outerCirclePaint.setColor(outerColor);
        outerCirclePaint.setStyle(Paint.Style.FILL_AND_STROKE);
    }

    public void draw(Canvas canvas) {
        // If not pressed and already initialised (used once), don't draw
        if (!pressed && hasBeenInitialised) return;

        // Save original alpha
        int oldOuterAlpha = outerCirclePaint.getAlpha();
        int oldInnerAlpha = innerCirclePaint.getAlpha();

        // If it's just a placeholder (not pressed and not initialised yet), draw faded
        if (!pressed && !hasBeenInitialised) {
            outerCirclePaint.setAlpha(60);
            innerCirclePaint.setAlpha(60);
        }

        canvas.drawCircle(
                outerCircleCenterPositionX,
                outerCircleCenterPositionY,
                outerCircleRadius,
                outerCirclePaint
        );

        canvas.drawCircle(
                innerCircleCenterPositionX,
                innerCircleCenterPositionY,
                innerCircleRadius,
                innerCirclePaint
        );

        // Restore alpha
        outerCirclePaint.setAlpha(oldOuterAlpha);
        innerCirclePaint.setAlpha(oldInnerAlpha);
    }

    public void update() {
        if (isPressed()) {
            updateInnerCirclePosition();
        } else if (!hasBeenInitialised) {
            // Keep inner circle at center for placeholder
            innerCircleCenterPositionX = outerCircleCenterPositionX;
            innerCircleCenterPositionY = outerCircleCenterPositionY;
        }
    }

    private void updateInnerCirclePosition() {
        innerCircleCenterPositionX = (int) (outerCircleCenterPositionX + actuatorX * outerCircleRadius);
        innerCircleCenterPositionY = (int) (outerCircleCenterPositionY + actuatorY * outerCircleRadius);
    }

    public void setCenter(int x, int y) {
        outerCircleCenterPositionX = x;
        outerCircleCenterPositionY = y;
        innerCircleCenterPositionX = x;
        innerCircleCenterPositionY = y;
        pressed = true;
        hasBeenInitialised = true;
    }

    public void notPressed() {
        pressed = false;
        resetActuator();
    }

    private void resetActuator() {
        actuatorX = 0;
        actuatorY = 0;
    }

    public boolean isPressed() {
        return pressed;
    }

    public void setActuator(double x, double y) {
        double deltaX = x - outerCircleCenterPositionX;
        double deltaY = y - outerCircleCenterPositionY;
        double deltaDistance = Math.sqrt(Math.pow(deltaX, 2) + Math.pow(deltaY, 2));

        if (deltaDistance < outerCircleRadius) {
            actuatorX = deltaX / outerCircleRadius;
            actuatorY = deltaY / outerCircleRadius;
        } else {
            actuatorX = deltaX / deltaDistance;
            actuatorY = deltaY / deltaDistance;
        }
    }

    public double actuatorY() {
        return actuatorY;
    }

    public double actuatorX() {
        return actuatorX;
    }
}
