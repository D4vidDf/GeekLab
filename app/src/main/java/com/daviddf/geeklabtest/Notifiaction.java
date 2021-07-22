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

import java.util.concurrent.atomic.AtomicInteger;

import static com.daviddf.geeklabtest.Not.CHANNEL_1_ID;

public class Notifiaction extends AppCompatActivity {
    int NOTIFICACION_ID=1;
    String titulo,mensaje;
    EditText num,tit,mes;
    private NotificationManagerCompat notificationManager;
    private final static String CHANNEL_ID = "GeekLab";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifiaction);

        num = (EditText) findViewById(R.id.numero);
        tit = (EditText) findViewById(R.id.Titulo);
        mes = (EditText) findViewById(R.id.mensaje);
        Button gen = (Button) findViewById(R.id.generar);

        gen.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View view) {
                int no = Integer.parseInt(num.getText().toString());
                titulo = tit.getText().toString();
                mensaje = mes.getText().toString();
                createNotificationChannel();
                createNotification();
                NOTIFICACION_ID++;


            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void createNotificationChannel() {

        CharSequence name = "Notificacion";
        @SuppressLint("WrongConstant") NotificationChannel notificationChannel = new NotificationChannel(CHANNEL_ID, name, NotificationManager.IMPORTANCE_HIGH);
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