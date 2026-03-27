package com.example.my2dgame;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.example.my2dgame.object.Enemy;
import com.example.my2dgame.object.Player;

public class Game extends SurfaceView implements SurfaceHolder.Callback {

    private final GameLoop gameLoop;
    private final Paint statsPaint;
    private Joystick joystick;
    private Enemy enemy;
    private Player player;
    private int screenWidth;
    private int screenHeight;

    public Game(Context context) {
        super(context);

        SurfaceHolder surfaceHolder = getHolder();
        surfaceHolder.addCallback(this);

        gameLoop = new GameLoop(this, surfaceHolder);

        statsPaint = new Paint();
        statsPaint.setColor(ContextCompat.getColor(context, R.color.magenta));
        statsPaint.setTextSize(50);

        setFocusable(true);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                joystick.pressed((double)event.getX(), (double)event.getY());
                return true;
            case MotionEvent.ACTION_MOVE:
                if (joystick.isPressed()) {
                    joystick.setActuator((double)event.getX(), (double)event.getY());
                }
                return true;
            case MotionEvent.ACTION_UP:
                joystick.notPressed();
                return true;
        }
        return super.onTouchEvent(event);
    }

    @Override
    public void surfaceCreated(@NonNull SurfaceHolder surfaceHolder) {
        screenWidth = getWidth();
        screenHeight = getHeight();

        joystick = new Joystick(
                (int) (screenWidth * 0.12),
                (int) (screenHeight * 0.75),
                (int) (screenHeight * 0.08),
                (int) (screenHeight * 0.045)
        );
        player = new Player(
                getContext(),
                joystick,
                screenWidth / 2.0,
                screenHeight / 2.0,
                (float) (screenHeight * 0.035)
        );
        enemy = new Enemy(
                getContext(),
                player,
                screenWidth * 0.25,
                screenHeight * 0.15,
                (float) (screenHeight * 0.03)
        );

        gameLoop.startLoop();
    }

    @Override
    public void surfaceChanged(@NonNull SurfaceHolder surfaceHolder, int i, int i1, int i2) {

    }

    @Override
    public void surfaceDestroyed(@NonNull SurfaceHolder surfaceHolder) {
        gameLoop.stopLoop();
    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
        drawUPS(canvas);
        drawFPS(canvas);
        player.draw(canvas);
        joystick.draw(canvas);
        enemy.draw(canvas);
    }

    public void drawUPS(Canvas canvas) {
        String averageUPS = Double.toString(gameLoop.getAverageUPS());
        canvas.drawText("UPS " + averageUPS, 100, 100, statsPaint);
    }

    public void drawFPS(Canvas canvas) {
        String averageFPS = Double.toString(gameLoop.getAverageFPS());
        canvas.drawText("FPS " + averageFPS, 100, 200, statsPaint);
    }

    public void update() {
        joystick.update();
        player.update();
        enemy.update();

        // Clamp objects to screen bounds
        player.clampToScreen(screenWidth, screenHeight);
        enemy.clampToScreen(screenWidth, screenHeight);
    }
}
