package com.daviddf.geeklab;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsControllerCompat;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle AppCombatActivity) {
        super.onCreate(AppCombatActivity);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);

        WindowInsetsControllerCompat windowInsetsController =
                ViewCompat.getWindowInsetsController(getWindow().getDecorView());
        if (windowInsetsController == null) {
            return;
        }

        windowInsetsController.setAppearanceLightNavigationBars(true);
        Intent mainIntent = new Intent(MainActivity.this, HomeActivity.class);
        MainActivity.this.startActivity(mainIntent);
        MainActivity.this.finish();

    }


}