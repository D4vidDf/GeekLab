package com.daviddf.geeklab.navigation;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;
import androidx.window.core.layout.WindowSizeClass;
import androidx.window.layout.WindowMetrics;
import androidx.window.layout.WindowMetricsCalculator;

import com.daviddf.geeklab.R;
import com.daviddf.geeklab.databinding.AppNavigationBinding;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.navigationrail.NavigationRailView;

public class AppNavigation extends AppCompatActivity {

    private AppNavigationBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = AppNavigationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewGroup container = binding.container;

        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);

        // navigation controller

        container.addView(new View(this){
            @Override
            protected void onConfigurationChanged(Configuration configuration){
                super.onConfigurationChanged(configuration);
                computeWindowSizeClasses();
            }
        });
        computeWindowSizeClasses();

        // ATTENTION: This was auto-generated to handle app links.
        Intent appLinkIntent = getIntent();
        String appLinkAction = appLinkIntent.getAction();
        Uri appLinkData = appLinkIntent.getData();
    }

    private void computeWindowSizeClasses() {
        WindowMetrics metrics = WindowMetricsCalculator.getOrCreate()
                .computeCurrentWindowMetrics(this);

        float widthDp = metrics.getBounds().width() /
                getResources().getDisplayMetrics().density;
        WindowSizeClass widthWindowSizeClass;

        if (widthDp < 600f) {
            BottomNavigationView navView = findViewById(R.id.bottom_nav_view);

            navView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN| View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            // Passing each menu ID as a set of Ids because each
            // menu should be considered as top level destinations.
            NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_bottom_navigation);

            NavigationUI.setupWithNavController((BottomNavigationView) binding.bottomNavView, navController);
        } else if (widthDp < 840f) {
            NavigationRailView navView = findViewById(R.id.nav_rail_view);

            navView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN| View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            // Passing each menu ID as a set of Ids because each
            // menu should be considered as top level destinations.
            NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_bottom_navigation);

            NavigationUI.setupWithNavController((NavigationRailView) binding.navRailView, navController);
        } else {
            NavigationView navView = findViewById(R.id.nav_view);

            navView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN| View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            // Passing each menu ID as a set of Ids because each
            // menu should be considered as top level destinations.
            NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_bottom_navigation);

            NavigationUI.setupWithNavController((NavigationView) binding.navView, navController);
        }
        float heightDp = metrics.getBounds().height() /
                getResources().getDisplayMetrics().density;
        WindowSizeClass heightWindowSizeClass;

        if (heightDp < 480f) {
            BottomNavigationView navView = findViewById(R.id.bottom_nav_view);

            navView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN| View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            // Passing each menu ID as a set of Ids because each
            // menu should be considered as top level destinations.
            NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_bottom_navigation);

            NavigationUI.setupWithNavController((BottomNavigationView) binding.bottomNavView, navController);
        } else if (heightDp < 900f) {
            NavigationView navView = findViewById(R.id.nav_view);

            navView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN| View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            // Passing each menu ID as a set of Ids because each
            // menu should be considered as top level destinations.
            NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_bottom_navigation);

            NavigationUI.setupWithNavController((NavigationView) binding.navView, navController);
        } else {
            NavigationView navView = findViewById(R.id.nav_view);

            navView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN| View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            // Passing each menu ID as a set of Ids because each
            // menu should be considered as top level destinations.
            NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_bottom_navigation);

            NavigationUI.setupWithNavController((NavigationView) binding.navView, navController);
        }
    }

}