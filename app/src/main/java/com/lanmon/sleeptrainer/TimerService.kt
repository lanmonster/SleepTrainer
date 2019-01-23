package com.lanmon.sleeptrainer

import android.app.Service
import android.content.Intent
import android.os.Binder
import androidx.core.app.NotificationManagerCompat
import com.lanmon.sleeptrainer.Constants.CHECK_NOTIFICATION_ID
import com.lanmon.sleeptrainer.Constants.TIMER_NOTIFICATION_ID

class TimerService: Service() {
    lateinit var timer: ServiceCountDownTimer

    inner class TimerServiceBinder: Binder() {
        fun getService() = this@TimerService
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        val time = intent.getLongExtra(getString(R.string.intent_time_remaining), fiveMinutes)
        val numChecks = intent.getIntExtra(getString(R.string.intent_num_checks), 1)
        val notificationManager = NotificationManagerCompat.from(this)
        timer = ServiceCountDownTimer(
            time,
            onTickCallback = {
                notificationManager.notify(TIMER_NOTIFICATION_ID, buildTimerNotification(it, numChecks))
            },
            onFinishCallback = {
                stopSelf()
                notificationManager.notify(CHECK_NOTIFICATION_ID, buildCheckNotification(numChecks))
            }
        )
        timer.start()

        startForeground(TIMER_NOTIFICATION_ID, buildTimerNotification(time, numChecks))

        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?) = TimerServiceBinder()
}