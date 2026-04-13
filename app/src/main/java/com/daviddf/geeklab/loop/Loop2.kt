package com.daviddf.geeklab.loop

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.daviddf.geeklab.R

class Loop2 : AppCompatActivity() {
    private val SPLASH_DISPLAY_LENGTH = 10

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_loop2)
        Handler(Looper.getMainLooper()).postDelayed({
            val mainIntent = Intent(this@Loop2, Consentimiento::class.java)
            startActivity(mainIntent)
            finish()
        }, SPLASH_DISPLAY_LENGTH.toLong())
    }
}
