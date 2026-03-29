package com.example.my2dgame;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import java.util.HashMap;
import java.util.Map;

/**
 * Caches processed bitmaps to avoid expensive re-decoding and transparency processing.
 */
public class SpriteCache {
    private static final Map<Integer, Bitmap> cache = new HashMap<>();

    public static Bitmap getSprite(Context context, int resourceId) {
        Bitmap sprite = cache.get(resourceId);
        if (sprite == null) {
            Bitmap rawBitmap = BitmapFactory.decodeResource(context.getResources(), resourceId);
            sprite = createTransparentBitmap(rawBitmap);
            cache.put(resourceId, sprite);
            // Note: rawBitmap.recycle() is only safe if createTransparentBitmap returns a NEW bitmap.
            // Our implementation does create a new one.
            rawBitmap.recycle();
        }
        return sprite;
    }

    /**
     * Pre-processes and caches multiple sprites at once.
     */
    public static void preload(Context context, int... resourceIds) {
        for (int id : resourceIds) {
            getSprite(context, id);
        }
    }

    private static Bitmap createTransparentBitmap(Bitmap bitmap) {
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
            // Remove white/near-white backgrounds
            if (r > 240 && g > 240 && b > 240) {
                pixels[i] = Color.TRANSPARENT;
            }
        }
        result.setPixels(pixels, 0, width, 0, 0, width, height);
        return result;
    }
    
    public static void clear() {
        for (Bitmap bmp : cache.values()) {
            if (bmp != null) bmp.recycle();
        }
        cache.clear();
    }
}
