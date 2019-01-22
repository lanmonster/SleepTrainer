package com.lanmon.sleeptrainer

import android.app.IntentService
import android.content.Intent
import androidx.core.app.NotificationManagerCompat

class TimerService: IntentService("") {

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        NotificationManagerCompat.from(this).notify(2, buildTimerNotification(0))

        return super.onStartCommand(intent, flags, startId)
    }
    override fun onHandleIntent(p0: Intent?) {
        startForeground(2, applicationContext.buildTimerNotification(0))
    }
}