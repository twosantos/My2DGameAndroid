package com.example.my2dgame;

import android.graphics.Canvas;
import android.util.Log;
import android.view.SurfaceHolder;

public class GameLoop extends Thread {
    public static final double MAX_UPS = 30.0;
    private static final double UPS_PERIOD = 1E+3 / MAX_UPS;
    Game game;
    SurfaceHolder surfaceHolder;
    private boolean isRunning;

    private double averageUPS;
    private double averageFPS;

    public GameLoop(Game game, SurfaceHolder surfaceHolder) {
        this.game = game;
        this.surfaceHolder = surfaceHolder;
        isRunning = false;
    }

    public double getAverageUPS() {
        return averageUPS;
    }

    public double getAverageFPS() {
        return averageFPS;
    }

    public void startLoop() {
        isRunning = true;
        start();
    }

    public void stopLoop() {
        isRunning = false;
        try {
            join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        super.run();

        int updateCount = 0;
        int frameCount = 0;

        long startTime = System.currentTimeMillis();
        long previousTime = startTime;
        double accumulatedTime = 0;

        Canvas canvas = null;
        while (isRunning) {
            long currentTime = System.currentTimeMillis();
            long time = currentTime - previousTime;
            previousTime = currentTime;
            accumulatedTime += time;

            // Fixed-step game logic updates
            double dt = UPS_PERIOD / 1000.0;
            while (accumulatedTime >= UPS_PERIOD) {
                game.update(dt);
                updateCount++;
                accumulatedTime -= UPS_PERIOD;
            }

            // Render a frame every iteration (FPS decoupled from UPS)
            try {
                canvas = surfaceHolder.lockCanvas();
                synchronized (surfaceHolder) {
                    game.draw(canvas);
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (canvas != null) {
                    try {
                        surfaceHolder.unlockCanvasAndPost(canvas);
                        frameCount++;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            // Calculate and log averages every second
            long elapsed = currentTime - startTime;
            if (elapsed >= 1000) {
                averageUPS = updateCount / (elapsed / 1000.0);
                averageFPS = frameCount / (elapsed / 1000.0);
                Log.d("GameLoop", "FPS: " + averageFPS + " UPS: " + averageUPS);
                updateCount = 0;
                frameCount = 0;
                startTime = currentTime;
            }
        }
    }

}
