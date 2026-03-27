package com.example.my2dgame;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.media.SoundPool;

/**
 * Manages all game audio: SoundPool for short effects, MediaPlayer for background music.
 */
public class SoundManager {

    private final SoundPool soundPool;
    private final int hitSoundId;
    private final int gameOverSoundId;
    private final int startSoundId;

    private MediaPlayer musicPlayer;
    private final Context context;

    public SoundManager(Context context) {
        this.context = context;

        AudioAttributes attrs = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();

        soundPool = new SoundPool.Builder()
                .setMaxStreams(4)
                .setAudioAttributes(attrs)
                .build();

        hitSoundId = soundPool.load(context, R.raw.hit, 1);
        gameOverSoundId = soundPool.load(context, R.raw.game_over, 1);
        startSoundId = soundPool.load(context, R.raw.start, 1);
    }

    public void playHit() {
        soundPool.play(hitSoundId, 1f, 1f, 1, 0, 1f);
    }

    public void playGameOver() {
        soundPool.play(gameOverSoundId, 1f, 1f, 1, 0, 1f);
    }

    public void playStart() {
        soundPool.play(startSoundId, 1f, 1f, 1, 0, 1f);
    }

    public void startMusic() {
        if (musicPlayer != null) {
            musicPlayer.release();
        }
        musicPlayer = MediaPlayer.create(context, R.raw.main_menu_loop);
        musicPlayer.setLooping(true);
        musicPlayer.setVolume(0.5f, 0.5f);
        musicPlayer.start();
    }

    public void stopMusic() {
        if (musicPlayer != null) {
            musicPlayer.stop();
            musicPlayer.release();
            musicPlayer = null;
        }
    }

    public void release() {
        soundPool.release();
        stopMusic();
    }
}
