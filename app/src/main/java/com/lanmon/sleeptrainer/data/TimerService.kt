package com.lanmon.sleeptrainer.data

import android.app.Service
import android.content.Intent
import android.os.Binder
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.lanmon.sleeptrainer.R
import com.lanmon.sleeptrainer.util.Constants.CHECK_NOTIFICATION_ID
import com.lanmon.sleeptrainer.util.Constants.TIMER_NOTIFICATION_ID
import com.lanmon.sleeptrainer.util.ServiceCountDownTimer
import com.lanmon.sleeptrainer.util.buildCheckNotification
import com.lanmon.sleeptrainer.util.buildTimerNotification
import com.lanmon.sleeptrainer.util.fiveMinutes

class TimerService: Service() {
    lateinit var timer: ServiceCountDownTimer
    private val _checks = MutableLiveData<Int>()
    val checks: LiveData<Int>
        get() = _checks

    inner class TimerServiceBinder: Binder() {
        fun getService() = this@TimerService
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        val time = intent.getLongExtra(getString(R.string.intent_time_remaining),
            fiveMinutes
        )
        val numChecks = intent.getIntExtra(getString(R.string.intent_num_checks), 1)
        _checks.postValue(numChecks)
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