package com.daviddf.geeklabtest;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

public class Loop extends AppCompatActivity {
    private final int SPLASH_DISPLAY_LENGTH = 10;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loop);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent mainIntent = new Intent(Loop.this, Loop.class);
                Loop.this.startActivity(mainIntent);
                Loop.this.finish();
            }
        }, SPLASH_DISPLAY_LENGTH);
    }
}