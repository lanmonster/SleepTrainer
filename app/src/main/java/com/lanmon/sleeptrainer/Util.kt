package com.lanmon.sleeptrainer

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.CountDownTimer
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.lanmon.sleeptrainer.Constants.ASLEEP_BUTTON_CODE
import com.lanmon.sleeptrainer.Constants.AWAKE_BUTTON_CODE
import com.lanmon.sleeptrainer.Constants.TIMER_NOTIFICATION_CODE

const val secondsPerMinute = 1
const val oneSecond = 1000L
const val fiveMinutes = oneSecond * secondsPerMinute * 5
const val tenMinutes = fiveMinutes + fiveMinutes
const val fifteenMinutes = tenMinutes + fiveMinutes

class ServiceCountDownTimer(
    time: Long,
    private val onTickCallback: (millisUntilFinished: Long) -> Unit = {},
    private val onFinishCallback: () -> Unit = {}
): CountDownTimer(time, oneSecond) {
    private val _timeLeft = MutableLiveData<Long>()
    val timeLeft: LiveData<Long>
            get() = _timeLeft

    override fun onFinish() {
        _timeLeft.postValue(0)
        onFinishCallback()
    }

    override fun onTick(millisUntilFinished: Long) {
        _timeLeft.postValue(millisUntilFinished)
        onTickCallback(millisUntilFinished)
    }

}

fun NotificationManagerCompat.createNotificationChannels(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val checkChannel = NotificationChannel(
            Constants.CHECK_CHANNEL_ID,
            context.getString(R.string.check_channel_name),
            NotificationManager.IMPORTANCE_HIGH
        ).also {
            it.description = context.getString(R.string.check_channel_desc)
        }
        val timerChannel = NotificationChannel(
            Constants.TIMER_CHANNEL_ID,
            context.getString(R.string.timer_channel_name),
            NotificationManager.IMPORTANCE_LOW
        ).also {
            it.description = context.getString(R.string.timer_channel_desc)
        }
        createNotificationChannels(
            listOf(
                checkChannel,
                timerChannel
            )
        )
    }
}

private fun Context.buildAsleepButtonIntent(num: Int) = PendingIntent.getActivity(
    this,
    ASLEEP_BUTTON_CODE,
    Intent(this, MainActivity::class.java).also {
        it.action = getString(R.string.asleep_intent_action)
        it.putExtra(getString(R.string.intent_num_checks), num)
    },
    PendingIntent.FLAG_CANCEL_CURRENT
)

private fun Context.buildAwakeButtonIntent(num: Int) = PendingIntent.getActivity(
    this,
    AWAKE_BUTTON_CODE,
    Intent(this, MainActivity::class.java).also {
        it.action = getString(R.string.awake_intent_action)
        it.putExtra(getString(R.string.intent_num_checks), num)
    },
    PendingIntent.FLAG_CANCEL_CURRENT
)

fun Context.buildCheckNotification(num: Int) = NotificationCompat.Builder(this, Constants.CHECK_CHANNEL_ID)
    .setContentTitle(getString(R.string.check_notification_title))
    .setContentText(getString(R.string.check_notification_text))
    .setSmallIcon(R.mipmap.ic_launcher)
    .addAction(R.mipmap.ic_launcher, getString(R.string.asleep_notification_button), buildAsleepButtonIntent(num))
    .addAction(R.mipmap.ic_launcher, getString(R.string.awake_notification_button), buildAwakeButtonIntent(num))
    .setAutoCancel(true)
    .build()!!

fun Context.buildTimerNotification(millis: Long, num: Int): Notification {
    val intent = PendingIntent.getActivity(
        this,
        TIMER_NOTIFICATION_CODE,
        Intent(this, MainActivity::class.java).also {
            it.putExtra(getString(R.string.intent_time_remaining), millis)
            it.putExtra(getString(R.string.intent_num_checks), num)
        },
        PendingIntent.FLAG_UPDATE_CURRENT
    )
    return NotificationCompat.Builder(this, Constants.TIMER_CHANNEL_ID)
        .setContentTitle(getString(R.string.timer_notification_title))
        .setContentText(millisToMinutesSeconds(millis))
        .setSmallIcon(R.mipmap.ic_launcher)
        .setAutoCancel(true)
        .setOngoing(true)
        .setOnlyAlertOnce(true)
        .setContentIntent(intent)
        .build()!!
}

fun Long.padFrontWithZero() = if (this < 10) "0$this" else "$this"

fun Context.millisToMinutesSeconds(millis: Long): String {
    return if (millis == 0L) {
        getString(R.string.timer_all_zeroes)
    } else {
        var minutes = (millis / 1000) / 60
        var seconds = ((millis / 1000) % 60) + 1
        if (seconds == 60L) {
            seconds = 0
            minutes++
        }
        getString(R.string.timer_time, minutes, seconds.padFrontWithZero())
    }
}

fun Context.isServiceRunning(serviceClass: Class<*>): Boolean {
    val manager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    @Suppress("DEPRECATION") // I only need to know if my service is running
    for (service in manager.getRunningServices(Int.MAX_VALUE)) {
        if (service.service.className == serviceClass.name) {
            return true
        }
    }
    return false
}