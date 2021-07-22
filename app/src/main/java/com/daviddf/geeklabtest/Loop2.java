package com.daviddf.geeklabtest;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

public class Loop2 extends AppCompatActivity {
    private final int SPLASH_DISPLAY_LENGTH = 10;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loop2);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent mainIntent = new Intent(Loop2.this, Consentimiento.class);
                Loop2.this.startActivity(mainIntent);
                Loop2.this.finish();
            }
        }, SPLASH_DISPLAY_LENGTH);
    }
}