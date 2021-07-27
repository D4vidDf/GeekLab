package com.daviddf.geeklab;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;

public class Menu extends AppCompatActivity {

    MaterialButton noti, dev, band, ia_hdr, speed, account, data, performance, qcolor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        noti = (MaterialButton) findViewById(R.id.not);

        noti.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent( Menu.this, Notifiaction.class));
            }
        });

        dev = (MaterialButton) findViewById(R.id.developer);

        dev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent Dev = new Intent(Intent.ACTION_VIEW);
                Dev.setClassName("com.android.settings","com.android.settings.Settings$DevelopmentSettingsDashboardActivity");
                startActivity(Dev);
            }
        });

        band = (MaterialButton) findViewById(R.id.band);

        band.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Intent Band = new Intent(Intent.ACTION_VIEW);
                    Band.setClassName("com.android.settings", "com.android.settings.MiuiBandMode");
                    startActivity(Band);
                } catch (RuntimeException e) {
                    Toast errorToast = Toast.makeText(Menu.this, "Esta función no está disponible en su terminal", Toast.LENGTH_LONG);
                    errorToast.show();
                }
            }
        });

        ia_hdr = (MaterialButton) findViewById(R.id.hdr);

        ia_hdr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Intent Hdr = new Intent(Intent.ACTION_VIEW);
                    Hdr.setClassName("com.android.settings", "com.android.settings.display.ScreenEnhanceEngineS2hActivity");
                    startActivity(Hdr);
                } catch (RuntimeException e) {
                    Toast errorToast = Toast.makeText(Menu.this, "Esta función no está disponible en su terminal", Toast.LENGTH_LONG);
                    errorToast.show();
                }
            }
        });

        speed = (MaterialButton) findViewById(R.id.velo);

        speed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Intent Speed = new Intent(Intent.ACTION_VIEW);
                    Speed.setClassName("com.android.settings", "com.android.settings.wifi.linkturbo.WifiLinkTurboSettings");
                    startActivity(Speed);
                }catch (RuntimeException e){
                    Toast errorToast = Toast.makeText(Menu.this,"Esta función no está disponible en su terminal", Toast.LENGTH_LONG);
                    errorToast.show();
                }
            }
        });

        account = (MaterialButton) findViewById(R.id.accounts);

        account.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Intent Account = new Intent(Intent.ACTION_VIEW);
                    Account.setClassName("com.android.settings", "com.android.settings.Settings$UserSettingsActivity");
                    startActivity(Account);
                }catch (RuntimeException e){
                    Toast errorToast = Toast.makeText(Menu.this,"Esta función no está disponible en su terminal", Toast.LENGTH_LONG);
                    errorToast.show();
                }
            }
        });

        data = (MaterialButton) findViewById(R.id.usage);

        data.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                Intent Data = new Intent(Intent.ACTION_VIEW);
                Data.setClassName("com.xiaomi.misettings","com.xiaomi.misettings.usagestats.UsageStatsMainActivity");
                startActivity(Data);
                }catch (RuntimeException e){
                    Toast errorToast = Toast.makeText(Menu.this,"Esta función no está disponible en su terminal", Toast.LENGTH_LONG);
                    errorToast.show();
                }
            }
        });

        performance = (MaterialButton) findViewById(R.id.rendimiento);

        performance.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Intent Performance = new Intent(Intent.ACTION_VIEW);
                    Performance.setClassName("com.qualcomm.qti.performancemode","com.qualcomm.qti.performancemode.PerformanceModeActivity");
                    startActivity(Performance);
                } catch (RuntimeException e){
                    Toast errorToast = Toast.makeText(Menu.this,"Esta función no está disponible en su terminal", Toast.LENGTH_LONG);
                    errorToast.show();
                }

            }
        });

        qcolor = (MaterialButton) findViewById(R.id.qcolor);

        qcolor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Intent Performance = new Intent(Intent.ACTION_VIEW);
                    Performance.setClassName("com.qualcomm.qti.qcolor", "com.qualcomm.qti.qcolor.QColorActivity");
                    startActivity(Performance);
                } catch (RuntimeException e) {
                    Toast errorToast = Toast.makeText(Menu.this, "Esta función no está disponible en su terminal", Toast.LENGTH_LONG);
                    errorToast.show();
                }
            }
        });

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
                startActivity(new Intent(Menu.this, Consentimiento.class));
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