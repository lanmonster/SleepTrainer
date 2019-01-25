package com.lanmon.sleeptrainer.data

import android.app.Service
import android.content.Intent
import android.os.Binder
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.lanmon.sleeptrainer.R
import com.lanmon.sleeptrainer.util.*

class TimerService : Service() {
    lateinit var timer: ServiceCountDownTimer
    private val _checks = MutableLiveData<Int>()
    val checks: LiveData<Int>
        get() = _checks

    inner class TimerServiceBinder : Binder() {
        fun getService() = this@TimerService
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        val time = intent.getLongExtra(
            getString(R.string.intent_time_remaining),
            fiveMinutes
        )
        val numChecks = intent.getIntExtra(getString(R.string.intent_num_checks), 1)
        _checks.postValue(numChecks)
        val notificationManager = NotificationManagerCompat.from(this)
        timer = ServiceCountDownTimer(
            time,
            onTickCallback = {
                notificationManager.notify(Constants.TIMER_NOTIFICATION_ID, buildTimerNotification(it, numChecks))
            },
            onFinishCallback = {
                stopSelf()
                notificationManager.notify(Constants.CHECK_NOTIFICATION_ID, buildCheckNotification(isAppActive, numChecks))
            }
        )
        timer.start()

        startForeground(Constants.TIMER_NOTIFICATION_ID, buildTimerNotification(time, numChecks))

        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?) = TimerServiceBinder()
}