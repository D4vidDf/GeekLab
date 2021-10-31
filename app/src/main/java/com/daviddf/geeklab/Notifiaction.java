package com.daviddf.geeklab;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageView;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputLayout;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

public class Notifiaction extends AppCompatActivity {
    int NOTIFICACION_ID=1, SELECTED_PHOTO=1;
    String titulo,mensaje;
    Bitmap imagen;
    Boolean imagen_selected=false;
    Uri uri;
    ImageView imageView;
    ActivityResultLauncher<Intent> activityResultLauncher;
    MaterialToolbar appbar;
    public static final int PICK_IMAGE = 1;

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

        appbar = (MaterialToolbar) findViewById(R.id.topAppBar);

        MaterialButton choose = findViewById(R.id.img_se);
        imageView = findViewById(R.id.img);

        activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {

                if (result.getResultCode() == RESULT_OK){
                    uri = result.getData().getData();
                    try {

                        imagen = MediaStore.Images.Media.getBitmap(getContentResolver(),uri);
                        imageView.setImageBitmap(imagen);
                        imagen_selected = true;

                    } catch (IOException e){
                        e.printStackTrace();
                    }
                }

            }
        });

        choose.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                Intent intentimg =new Intent(Intent.ACTION_GET_CONTENT, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                intentimg.setType("image/*");
                activityResultLauncher.launch(intentimg);
            }
        });



        MaterialButton gen = (MaterialButton) findViewById(R.id.generar);

        gen.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View view) {
                if (tt.getEditText().getText().toString().isEmpty()){
                    tt.setErrorEnabled(true);
                    tt.setError(getString(R.string.error_number));
                }
                else {n++; tt.setErrorEnabled(false);}

                if (mes.getEditText().getText().toString().isEmpty()){
                    mes.setErrorEnabled(true);
                    mes.setError(getString(R.string.error_body));
                }
                else {n++; mes.setErrorEnabled(false);}

                if (tit.getEditText().getText().toString().isEmpty()){
                    tit.setErrorEnabled(true);
                    tit.setError(getString(R.string.error_title));
                }
                else if (tit.getEditText().getText().length()>30){
                    tit.setErrorEnabled(true);
                    tit.setError(getString(R.string.error_title_length));
                }
                else {n++; tit.setErrorEnabled(false);}


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
        appbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
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