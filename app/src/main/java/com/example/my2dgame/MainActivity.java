package com.example.my2dgame;

import android.app.Activity;
import android.os.Bundle;

public class MainActivity extends Activity {

    private Game game;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        game = new Game(this);
        setContentView(game);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (game != null) {
            game.pause();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (game != null) {
            game.resume();
        }
    }
}
