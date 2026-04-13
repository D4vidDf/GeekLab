package com.daviddf.geeklab.notification

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager.PERMISSION_DENIED
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.widget.ImageView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.daviddf.geeklab.R
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputLayout
import java.io.IOException
import java.util.concurrent.atomic.AtomicInteger

class Notifiaction : AppCompatActivity() {
    private var NOTIFICACION_ID = 1
    private var titulo: String? = null
    private var mensaje: String? = null
    private var imagen: Bitmap? = null
    private var imagen_selected = false
    private var uri: Uri? = null
    private lateinit var imageView: ImageView
    private lateinit var activityResultLauncher: ActivityResultLauncher<Intent>
    private lateinit var pickMedia: ActivityResultLauncher<PickVisualMediaRequest>
    private lateinit var appbar: MaterialToolbar

    private lateinit var tt: TextInputLayout
    private lateinit var tit: TextInputLayout
    private lateinit var mes: TextInputLayout
    private var n = 0
    private var no: Long = 0

    companion object {
        private const val CHANNEL_ID = "GeekLab"
        private val c = AtomicInteger(0)
        fun getID(): Int = c.incrementAndGet()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notifiaction)

        tit = findViewById(R.id.Titulo)
        mes = findViewById(R.id.Mensaje)
        tt = findViewById(R.id.tt)
        appbar = findViewById(R.id.topAppBar)

        val choose = findViewById<MaterialButton>(R.id.img_se)
        imageView = findViewById(R.id.img)

        activityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                uri = result.data?.data
                try {
                    imagen = MediaStore.Images.Media.getBitmap(contentResolver, uri)
                    imageView.setImageBitmap(imagen)
                    imagen_selected = true
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }

        pickMedia = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { selectedUri ->
            if (selectedUri != null) {
                uri = selectedUri
                try {
                    imagen = MediaStore.Images.Media.getBitmap(contentResolver, selectedUri)
                    imageView.setImageBitmap(imagen)
                    imagen_selected = true
                } catch (e: IOException) {
                    imageView.setImageBitmap(null)
                    imagen_selected = false
                    e.printStackTrace()
                }
            } else {
                imageView.setImageBitmap(null)
                imagen_selected = false
            }
        }

        choose.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
            } else {
                val intentimg = Intent(Intent.ACTION_GET_CONTENT, MediaStore.Images.Media.EXTERNAL_CONTENT_URI).apply {
                    type = "image/*"
                }
                activityResultLauncher.launch(intentimg)
            }
        }

        val gen = findViewById<MaterialButton>(R.id.generar)

        gen.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (ContextCompat.checkSelfPermission(applicationContext, "android.permission.POST_NOTIFICATIONS") == PERMISSION_DENIED) {
                    requestPermissions(arrayOf("android.permission.POST_NOTIFICATIONS"), NOTIFICACION_ID)
                }
            }

            val numStr = tt.editText?.text.toString()
            if (numStr.isEmpty()) {
                tt.isErrorEnabled = true
                tt.error = getString(R.string.error_number)
            } else {
                n++
                tt.isErrorEnabled = false
            }

            val mesStr = mes.editText?.text.toString()
            if (mesStr.isEmpty()) {
                mes.isErrorEnabled = true
                mes.error = getString(R.string.error_body)
            } else {
                n++
                mes.isErrorEnabled = false
            }

            val titStr = tit.editText?.text.toString()
            if (titStr.isEmpty()) {
                tit.isErrorEnabled = true
                tit.error = getString(R.string.error_title)
            } else if (titStr.length > 30) {
                tit.isErrorEnabled = true
                tit.error = getString(R.string.error_title_length)
            } else {
                n++
                tit.isErrorEnabled = false
            }

            if (n == 3) {
                no = numStr.toLong()
                while (no > 0) {
                    titulo = titStr
                    mensaje = mesStr
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        createNotificationChannel()
                    }
                    createNotification()
                    NOTIFICACION_ID++
                    no -= 1
                }
            }
            n = 0
        }

        appbar.setNavigationOnClickListener {
            finish()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel() {
        val name: CharSequence = "GeekLab"
        val notificationChannel = NotificationChannel(CHANNEL_ID, name, NotificationManager.IMPORTANCE_DEFAULT)
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(notificationChannel)
    }

    private fun createNotification() {
        val builder = NotificationCompat.Builder(applicationContext, CHANNEL_ID).apply {
            setBadgeIconType(NotificationCompat.BADGE_ICON_LARGE)
            setSmallIcon(R.drawable.ic_launcher_foreground)
            setContentTitle(titulo)
            setContentText(mensaje)
            color = Color.rgb(255, 255, 255)
            priority = NotificationCompat.PRIORITY_MAX
            setVibrate(longArrayOf(80, 1000, 1000, 1000, 1000))
            setDefaults(Notification.DEFAULT_SOUND)
            setStyle(NotificationCompat.BigTextStyle().bigText(mensaje))
            if (imagen_selected) {
                setStyle(NotificationCompat.BigPictureStyle().bigPicture(imagen).setBigContentTitle(titulo).setSummaryText(mensaje))
            }
        }

        val notificationManagerCompat = NotificationManagerCompat.from(applicationContext)
        try {
            notificationManagerCompat.notify(NOTIFICACION_ID, builder.build())
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }
}
