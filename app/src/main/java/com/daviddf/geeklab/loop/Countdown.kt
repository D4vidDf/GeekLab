package com.daviddf.geeklab.loop;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.widget.TextView;

import com.daviddf.geeklab.Loop;
import com.daviddf.geeklab.R;

public class Countdown extends AppCompatActivity {
    private final int SPLASH_DISPLAY_LENGTH = 1000;
    int num= 5;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_countdown);
        TextView count = (TextView)findViewById(R.id.countdown);

            new CountDownTimer(11000,1000){


                @Override
                public void onTick(long millisUntilFinished) {
                    count.setText(""+millisUntilFinished/1000);
                }

                @Override
                public void onFinish() {
                    Intent mainIntent = new Intent(Countdown.this, Loop.class);
                    Countdown.this.startActivity(mainIntent);
                    Countdown.this.finish();

                }
            }.start();


    }
}