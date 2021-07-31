package com.daviddf.geeklab;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

import com.google.android.material.button.MaterialButton;

public class Consentimiento extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_consentimiento);

        MaterialButton btn = (MaterialButton) findViewById(R.id.aceptar);
        MaterialButton close = (MaterialButton) findViewById(R.id.close);

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(Consentimiento.this, Countdown.class));
            }
        });

        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        // ATTENTION: This was auto-generated to handle app links.
        Intent appLinkIntent = getIntent();
        String appLinkAction = appLinkIntent.getAction();
        Uri appLinkData = appLinkIntent.getData();
    }
}