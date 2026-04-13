package com.daviddf.geeklab

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.daviddf.geeklab.loop.Consentimiento
import com.daviddf.geeklab.notification.Notifiaction
import com.google.android.material.button.MaterialButton

class Menu : AppCompatActivity() {

    private lateinit var noti: MaterialButton
    private lateinit var dev: MaterialButton
    private lateinit var band: MaterialButton
    private lateinit var iaHdr: MaterialButton
    private lateinit var speed: MaterialButton
    private lateinit var account: MaterialButton
    private lateinit var data: MaterialButton
    private lateinit var performance: MaterialButton
    private lateinit var qcolor: MaterialButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu)

        noti = findViewById(R.id.not)
        noti.setOnClickListener {
            startActivity(Intent(this, Notifiaction::class.java))
        }

        dev = findViewById(R.id.developer)
        dev.setOnClickListener {
            val intentDev = Intent(Intent.ACTION_VIEW).apply {
                setClassName("com.android.settings", "com.android.settings.Settings\$DevelopmentSettingsDashboardActivity")
            }
            startActivity(intentDev)
        }

        band = findViewById(R.id.band)
        band.setOnClickListener {
            try {
                val intentBand = Intent(Intent.ACTION_VIEW).apply {
                    setClassName("com.android.settings", "com.android.settings.MiuiBandMode")
                }
                startActivity(intentBand)
            } catch (e: RuntimeException) {
                Toast.makeText(this, R.string.error_function_not_available, Toast.LENGTH_LONG).show()
            }
        }

        iaHdr = findViewById(R.id.hdr)
        iaHdr.setOnClickListener {
            try {
                val intentHdr = Intent(Intent.ACTION_VIEW).apply {
                    setClassName("com.android.settings", "com.android.settings.display.ScreenEnhanceEngineS2hActivity")
                }
                startActivity(intentHdr)
            } catch (e: RuntimeException) {
                Toast.makeText(this, R.string.error_function_not_available, Toast.LENGTH_LONG).show()
            }
        }

        speed = findViewById(R.id.velo)
        speed.setOnClickListener {
            try {
                val intentSpeed = Intent(Intent.ACTION_VIEW).apply {
                    setClassName("com.android.settings", "com.android.settings.wifi.linkturbo.WifiLinkTurboSettings")
                }
                startActivity(intentSpeed)
            } catch (e: RuntimeException) {
                Toast.makeText(this, R.string.error_function_not_available, Toast.LENGTH_LONG).show()
            }
        }

        account = findViewById(R.id.accounts)
        account.setOnClickListener {
            try {
                val intentAccount = Intent(Intent.ACTION_VIEW).apply {
                    setClassName("com.android.settings", "com.android.settings.Settings\$UserSettingsActivity")
                }
                startActivity(intentAccount)
            } catch (e: RuntimeException) {
                Toast.makeText(this, R.string.error_function_not_available, Toast.LENGTH_LONG).show()
            }
        }

        data = findViewById(R.id.usage)
        data.setOnClickListener {
            try {
                val intentData = Intent(Intent.ACTION_VIEW).apply {
                    setClassName("com.xiaomi.misettings", "com.xiaomi.misettings.usagestats.UsageStatsMainActivity")
                }
                startActivity(intentData)
            } catch (e: RuntimeException) {
                Toast.makeText(this, R.string.error_function_not_available, Toast.LENGTH_LONG).show()
            }
        }

        performance = findViewById(R.id.rendimiento)
        performance.setOnClickListener {
            try {
                val intentPerformance = Intent(Intent.ACTION_VIEW).apply {
                    setClassName("com.qualcomm.qti.performancemode", "com.qualcomm.qti.performancemode.PerformanceModeActivity")
                }
                startActivity(intentPerformance)
            } catch (e: RuntimeException) {
                Toast.makeText(this, R.string.error_function_not_available, Toast.LENGTH_LONG).show()
            }
        }

        qcolor = findViewById(R.id.qcolor)
        qcolor.setOnClickListener {
            try {
                val intentQcolor = Intent(Intent.ACTION_VIEW).apply {
                    setClassName("com.qualcomm.qti.qcolor", "com.qualcomm.qti.qcolor.QColorActivity")
                }
                startActivity(intentQcolor)
            } catch (e: RuntimeException) {
                Toast.makeText(this, R.string.error_function_not_available, Toast.LENGTH_LONG).show()
            }
        }

        val lp = findViewById<MaterialButton>(R.id.loop)
        lp.setOnClickListener {
            startActivity(Intent(this, Consentimiento::class.java))
        }

        // Handle app links
        val appLinkIntent = intent
        val appLinkAction = appLinkIntent.action
        val appLinkData = appLinkIntent.data
    }
}
