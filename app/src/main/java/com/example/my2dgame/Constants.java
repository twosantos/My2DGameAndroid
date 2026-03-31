package com.example.my2dgame;

public class Constants {
    // Increased difficulty: Spawn faster and reach a lower floor
    public static final int INITIAL_SPAWN_INTERVAL = 60; // 2 seconds (down from 80 ticks)
    public static final int MIN_SPAWN_INTERVAL = 10;    // 0.33 seconds (down from 20 ticks)

    public static final int FIRE_INTERVAL = 10;
    public static final int RAPID_FIRE_INTERVAL = 4;
    public static final int DAMAGE_FLASH_DURATION = 6;
    public static final int KILL_SCORE_BONUS = 3;
    public static final int BOSS_SCORE_BONUS = 100;
    public static final int PAUSE_BUTTON_SIZE = 80;
    public static final int PAUSE_BUTTON_MARGIN = 20;
    public static final int PARTICLE_COUNT = 8;
    public static final int SHAKE_DURATION = 4;
    public static final int BOSS_SHAKE_DURATION = 12;
    public static final float SHAKE_INTENSITY = 12f;
    public static final float BOSS_SHAKE_INTENSITY = 35f;
    public static final int PICKUP_SPAWN_INTERVAL = 350;
    
    public static final int WAVE_BREAK_DURATION = 90;
    public static final int WAVE_ANNOUNCEMENT_DURATION = 90;
    
    public static final int SCORE_POP_DURATION = 15;
}
