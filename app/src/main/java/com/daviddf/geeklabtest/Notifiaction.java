package com.daviddf.geeklabtest;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.material.textfield.TextInputLayout;

import org.w3c.dom.Text;

import java.util.concurrent.atomic.AtomicInteger;

import static com.daviddf.geeklabtest.Not.CHANNEL_1_ID;

public class Notifiaction extends AppCompatActivity {
    int NOTIFICACION_ID=1;
    String titulo,mensaje;

    TextInputLayout tt, tit, mes;
    int n=0;
    private NotificationManagerCompat notificationManager;
    private final static String CHANNEL_ID = "GeekLab";
    long no;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifiaction);

        tit = (TextInputLayout) findViewById(R.id.Titulo);
        mes = (TextInputLayout) findViewById(R.id.Mensaje);
        tt = (TextInputLayout) findViewById(R.id.tt);
        Button gen = (Button) findViewById(R.id.generar);

        gen.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View view) {
                if (tt.getEditText().getText().toString().isEmpty()){
                    tt.setErrorEnabled(true);
                    tt.setError("Añade el nº de notificaciones");
                }
                else {n++; tt.setErrorEnabled(false);};

                if (mes.getEditText().getText().toString().isEmpty()){
                    mes.setError("Añade un mensaje");
                }
                else {n++; mes.setErrorEnabled(false);};

                if (tit.getEditText().getText().toString().isEmpty()){
                    tit.setError("Añada el Título");
                }
                else {n++; tit.setErrorEnabled(false);}

                if (tit.getEditText().getText().length() >30) n--;


                if (n==3) {
                    no = Long.parseLong(tt.getEditText().getText().toString());

                    while (no>0){
                        titulo = tit.getEditText().getText().toString();
                        mensaje = mes.getEditText().getText().toString();
                        createNotificationChannel();
                        createNotification();
                        NOTIFICACION_ID++;
                        no-=1;
                    }
                }
                n=0;

            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void createNotificationChannel() {

        CharSequence name = "Notificacion";
        NotificationChannel notificationChannel = new NotificationChannel(CHANNEL_ID, name, NotificationManager.IMPORTANCE_DEFAULT);
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.createNotificationChannel(notificationChannel);
    }

    private void createNotification() {

        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID);
        builder.setBadgeIconType(NotificationCompat.BADGE_ICON_LARGE);
        builder.setSmallIcon(R.drawable.not);
        builder.setContentTitle(titulo);
        builder.setContentText(mensaje);
        builder.setColor(Color.argb(100,29,191,242));
        builder.setPriority(NotificationCompat.PRIORITY_MAX);
        builder.setVibrate(new long[]{80, 1000, 1000, 1000, 1000});
        builder.setDefaults(Notification.DEFAULT_SOUND);

        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(getApplicationContext());
        notificationManagerCompat.notify(NOTIFICACION_ID, builder.build());

    }

    public static class NotificationID{
        private final static AtomicInteger c = new AtomicInteger(0);
        public static int getID(){
            return c.incrementAndGet();
        }
    }
}