package com.lanmon.sleeptrainer

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationManagerCompat
import com.lanmon.sleeptrainer.Constants.CHECK_CHANNEL_ID
import com.lanmon.sleeptrainer.Constants.TIMER_CHANNEL_ID
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    private lateinit var timer: GenericCountDownTimer
    private lateinit var notificationManager: NotificationManagerCompat

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        notificationManager = NotificationManagerCompat.from(this)
        notificationManager.createNotificationChannels()

        setContentView(R.layout.activity_main)

        timer = FiveMinuteCountDownTimer(chronometer, notificationManager)

        button.setOnClickListener {
            val intent = Intent(this, TimerService::class.java).also {

            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(intent)
            } else {
                startService(intent)
            }
            it.visibility = View.GONE
            timer.start()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        timer.cancel()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)

        notificationManager.cancel(1)

        if (intent.action == "asleep") {
            Toast.makeText(this, "yay! only took ${intent.extras!!["num"]} checks...", Toast.LENGTH_LONG).show()
            button.visibility = View.VISIBLE
        } else if (intent.action == "awake") {
            val count = intent.extras!!["num"] as Int
            timer = if (count < 2)
                TenMinuteCountDownTimer(chronometer, notificationManager)
            else
                FifteenMinuteCountDownTimer(chronometer, notificationManager, count + 1)
            timer.start()
        }
    }

    private fun NotificationManagerCompat.createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val checkChannel = NotificationChannel(
                CHECK_CHANNEL_ID,
                getString(R.string.check_channel_name),
                NotificationManager.IMPORTANCE_HIGH
            ).also {
                it.description = getString(R.string.check_channel_desc)
            }
            val timerChannel = NotificationChannel(
                TIMER_CHANNEL_ID,
                getString(R.string.timer_channel_name),
                NotificationManager.IMPORTANCE_HIGH
            ).also {
                it.description = getString(R.string.timer_channel_desc)
            }
            createNotificationChannels(
                listOf(
                    checkChannel,
                    timerChannel
                )
            )
        }
    }
}