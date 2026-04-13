package com.daviddf.geeklab.loop

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.daviddf.geeklab.Loop
import com.daviddf.geeklab.R

class Countdown : AppCompatActivity() {
    private val SPLASH_DISPLAY_LENGTH = 1000
    var num = 5

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_countdown)
        val count = findViewById<TextView>(R.id.countdown)

        object : CountDownTimer(11000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                count.text = (millisUntilFinished / 1000).toString()
            }

            override fun onFinish() {
                val mainIntent = Intent(this@Countdown, Loop::class.java)
                startActivity(mainIntent)
                finish()
            }
        }.start()
    }
}
