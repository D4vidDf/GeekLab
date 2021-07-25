package com.daviddf.geeklabtest;

import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
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
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputLayout;

import org.w3c.dom.Text;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

import static com.daviddf.geeklabtest.Not.CHANNEL_1_ID;

public class Notifiaction extends AppCompatActivity {
    int NOTIFICACION_ID=1, SELECTED_PHOTO=1;
    String titulo,mensaje;
    Bitmap imagen;
    Boolean imagen_selected=false;
    Uri uri;
    ImageView imageView;

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

        MaterialButton choose = findViewById(R.id.img_se);
        imageView = findViewById(R.id.img);

        choose.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                Intent intentimg =new Intent(Intent.ACTION_PICK);
                intentimg.setType("image/*");
                startActivityForResult(intentimg,SELECTED_PHOTO);
            }
        });



        MaterialButton gen = (MaterialButton) findViewById(R.id.generar);

        gen.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View view) {
                if (tt.getEditText().getText().toString().isEmpty()){
                    tt.setErrorEnabled(true);
                    tt.setError("Error: Introduzca el nº de Notificaciones");
                }
                else {n++; tt.setErrorEnabled(false);};

                if (mes.getEditText().getText().toString().isEmpty()){
                    mes.setError("Error: Añada el cuerpo de la Notificación");
                }
                else {n++; mes.setErrorEnabled(false);};

                if (tit.getEditText().getText().toString().isEmpty()){
                    tit.setError("Error: Añada el título de la Notificación");
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
    @Override

    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data){

        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && requestCode == SELECTED_PHOTO && data !=null && data.getData() !=null){

            uri = data.getData();
            try {

                imagen = MediaStore.Images.Media.getBitmap(getContentResolver(),uri);
                imageView.setImageBitmap(imagen);
                imagen_selected = true;

            } catch (FileNotFoundException e){
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }


        }

    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    private void createNotificationChannel() {

        CharSequence name = "GeekLab";
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
        builder.setColor(Color.rgb(29,191,242));
        builder.setPriority(NotificationCompat.PRIORITY_MAX);
        builder.setVibrate(new long[]{80, 1000, 1000, 1000, 1000});
        builder.setDefaults(Notification.DEFAULT_SOUND);
        builder.setStyle(new NotificationCompat.BigTextStyle().bigText(mensaje));
        if (imagen_selected){
            builder.setStyle(new NotificationCompat.BigPictureStyle().bigPicture(imagen).setBigContentTitle(titulo).setSummaryText(mensaje));
        }


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