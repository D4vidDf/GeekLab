package com.daviddf.geeklab.loop

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.daviddf.geeklab.R
import com.google.android.material.button.MaterialButton

class Consentimiento : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_consentimiento)

        val btn = findViewById<MaterialButton>(R.id.aceptar)
        val close = findViewById<MaterialButton>(R.id.close)

        btn.setOnClickListener {
            startActivity(Intent(this, Countdown::class.java))
        }

        close.setOnClickListener {
            finish()
        }

        // Handle app links
        val appLinkIntent = intent
        val appLinkAction = appLinkIntent.action
        val appLinkData = appLinkIntent.data
    }
}
