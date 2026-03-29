package com.example.my2dgame.object;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;

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

    public abstract void update(double dt);

    /**
     * Utility to remove white background from a bitmap and make it transparent.
     */
    protected static Bitmap createTransparentBitmap(Bitmap bitmap) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        Bitmap result = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        int[] pixels = new int[width * height];
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height);
        
        for (int i = 0; i < pixels.length; i++) {
            int p = pixels[i];
            int r = (p >> 16) & 0xff;
            int g = (p >> 8) & 0xff;
            int b = p & 0xff;
            // If the pixel is white or very close to white, make it transparent
            if (r > 245 && g > 245 && b > 245) {
                pixels[i] = Color.TRANSPARENT;
            }
        }
        result.setPixels(pixels, 0, width, 0, 0, width, height);
        return result;
    }
}
