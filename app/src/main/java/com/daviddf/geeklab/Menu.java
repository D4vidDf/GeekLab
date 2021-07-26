package com.daviddf.geeklab;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

public class Menu extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        LinearLayout not = (LinearLayout)findViewById(R.id.notification);

        not.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent( Menu.this, Notifiaction.class));
            }
        });

        LinearLayout lp = (LinearLayout)findViewById(R.id.loop);

        lp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(Menu.this, Countdown.class));
            }
        });

        LinearLayout inf = (LinearLayout)findViewById(R.id.info);

        inf.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String urlInfo = "https://d4viddf.github.io/GeekLab/";

                Intent info = new Intent(Intent.ACTION_VIEW);
                info.setData(Uri.parse(urlInfo));
                startActivity(info);
            }
        });

        LinearLayout git = (LinearLayout)findViewById(R.id.github);

        git.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String urlGit = "https://github.com/D4vidDf/GeekLab";

                Intent Git = new Intent(Intent.ACTION_VIEW);
                Git.setData(Uri.parse(urlGit));
                startActivity(Git);
            }
        });

    }
}