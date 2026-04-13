package com.daviddf.geeklab

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity

class Loop : AppCompatActivity() {
    private val SPLASH_DISPLAY_LENGTH = 10

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_loop)
        Handler(Looper.getMainLooper()).postDelayed({
            val mainIntent = Intent(this@Loop, Loop::class.java)
            startActivity(mainIntent)
            finish()
        }, SPLASH_DISPLAY_LENGTH.toLong())
    }
}
