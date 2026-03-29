package com.example.my2dgame;

import android.graphics.Canvas;
import android.graphics.Color;
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
    private final int baseInnerColor;

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
        this.baseInnerColor = innerColor;

        innerCirclePaint = new Paint();
        innerCirclePaint.setColor(innerColor);
        innerCirclePaint.setStyle(Paint.Style.FILL_AND_STROKE);

        outerCirclePaint = new Paint();
        outerCirclePaint.setColor(outerColor);
        outerCirclePaint.setStyle(Paint.Style.FILL_AND_STROKE);
    }

    public void draw(Canvas canvas) {
        if (!pressed && hasBeenInitialised) return;

        int oldOuterAlpha = outerCirclePaint.getAlpha();
        int oldInnerAlpha = innerCirclePaint.getAlpha();

        if (!pressed && !hasBeenInitialised) {
            outerCirclePaint.setAlpha(60);
            innerCirclePaint.setAlpha(60);
        }

        // UX: Interactive Glow - Inner circle changes color/brightness based on distance
        if (pressed) {
            double distancePercent = Math.sqrt(actuatorX * actuatorX + actuatorY * actuatorY);
            if (distancePercent > 0.8) {
                innerCirclePaint.setColor(Color.WHITE); // Max tilt glow
            } else {
                innerCirclePaint.setColor(baseInnerColor);
            }
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

        outerCirclePaint.setAlpha(oldOuterAlpha);
        innerCirclePaint.setAlpha(oldInnerAlpha);
        innerCirclePaint.setColor(baseInnerColor); // Reset for next frame
    }

    public void update() {
        if (isPressed()) {
            updateInnerCirclePosition();
        } else if (!hasBeenInitialised) {
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
