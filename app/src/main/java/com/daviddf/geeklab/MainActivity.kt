package com.daviddf.geeklab;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.view.WindowCompat;

import com.daviddf.geeklab.navigation.AppNavigation;

public class MainActivity extends AppCompatActivity {
    private NotificationManagerCompat notificationManagerCompat;

    @Override
    protected void onCreate(Bundle AppCombatActivity) {
        super.onCreate(AppCombatActivity);

        View view = new View(this);
        view.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);

        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);

        Intent mainIntent = new Intent(MainActivity.this, AppNavigation.class);
        MainActivity.this.startActivity(mainIntent);
        MainActivity.this.finish();

    }
}