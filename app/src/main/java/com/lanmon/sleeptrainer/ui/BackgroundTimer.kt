package com.lanmon.sleeptrainer.ui

import android.app.Service
import android.content.Intent
import android.os.CountDownTimer
import androidx.core.app.NotificationManagerCompat
import com.lanmon.sleeptrainer.R
import com.lanmon.sleeptrainer.internal.CHECK_NOTIFICATION_ID
import com.lanmon.sleeptrainer.internal.FIVE_MINUTES
import com.lanmon.sleeptrainer.internal.ONE_SECOND
import com.lanmon.sleeptrainer.internal.TIMER_NOTIFICATION_ID
import com.lanmon.sleeptrainer.util.buildCheckNotification
import com.lanmon.sleeptrainer.util.buildTimerNotification

class BackgroundTimer : Service() {
    private var timer: ServiceCountDownTimer? = null
    private lateinit var notificationManager: NotificationManagerCompat

    override fun onBind(intent: Intent) = null // no binding allowed

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        notificationManager = NotificationManagerCompat.from(this)
        when (intent.action) {
            getString(R.string.action_start_background_timer) -> {
                val time = intent.getLongExtra(getString(R.string.time_extra_key),
                    FIVE_MINUTES
                )
                timer = ServiceCountDownTimer(time).apply {
                    start()
                }
                startForeground(TIMER_NOTIFICATION_ID, buildTimerNotification(time))
            }
            getString(R.string.action_stop_background_timer) -> {
                timer?.cancel()
                stopForeground(true)
                stopSelf()
            }
        }
        return START_NOT_STICKY
    }

    inner class ServiceCountDownTimer(time: Long) : CountDownTimer(time, ONE_SECOND) {
        override fun onFinish() {
            stopSelf()
            notificationManager.notify(CHECK_NOTIFICATION_ID, buildCheckNotification())
        }
        override fun onTick(millisUntilFinished: Long) {
            notificationManager.notify(TIMER_NOTIFICATION_ID, buildTimerNotification(millisUntilFinished))
        }
    }
}
