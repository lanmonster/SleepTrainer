package com.lanmon.sleeptrainer

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.CountDownTimer
import android.widget.TextView
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

const val oneSecond = 1000L
const val fiveMinutes = oneSecond * 60 * 5
const val tenMinutes = fiveMinutes * 2
const val fifteenMinutes = tenMinutes + fiveMinutes

sealed class GenericCountDownTimer(time: Long, private val view: TextView, private val notificationManager: NotificationManagerCompat, private val count: Int): CountDownTimer(time, oneSecond) {
    var timeLeft = 0L
    override fun onFinish() {
        notificationManager.notify(1, view.context.buildCheckNotification(count))
        view.text = view.resources.getString(R.string.timer_all_zeroes)
    }

    override fun onTick(millis: Long) {
        timeLeft = millis
        view.text = millis.millisToMinutesSeconds()
    }
}

class FiveMinuteCountDownTimer(
    view: TextView,
    notificationManager: NotificationManagerCompat
) : GenericCountDownTimer(5000, view, notificationManager, 1)

class TenMinuteCountDownTimer(
    view: TextView,
    notificationManager: NotificationManagerCompat
) : GenericCountDownTimer(10000, view, notificationManager, 2)

class FifteenMinuteCountDownTimer(
    view: TextView,
    notificationManager: NotificationManagerCompat,
    count: Int
) : GenericCountDownTimer(15000, view, notificationManager, count)

private fun Context.buildAsleepButtonIntent(num: Int) = PendingIntent.getActivity(
    this,
    0,
    Intent(this, MainActivity::class.java).also {
        it.action = "asleep"
        it.putExtra("num", num)
    },
    PendingIntent.FLAG_CANCEL_CURRENT
)

private fun Context.buildAwakeButtonIntent(num: Int) = PendingIntent.getActivity(
    this,
    1,
    Intent(this, MainActivity::class.java).also {
        it.action = "awake"
        it.putExtra("num", num)
    },
    PendingIntent.FLAG_CANCEL_CURRENT
)

private fun Context.buildCheckNotification(num: Int) = NotificationCompat.Builder(this, Constants.CHECK_CHANNEL_ID)
    .setContentTitle(getString(R.string.check_notification_title))
    .setContentText(getString(R.string.check_notification_text))
    .setSmallIcon(R.mipmap.ic_launcher)
    .addAction(R.mipmap.ic_launcher, "Asleep", buildAsleepButtonIntent(num))
    .addAction(R.mipmap.ic_launcher, "Awake", buildAwakeButtonIntent(num))
    .setAutoCancel(true)
    .build()

fun Context.buildTimerNotification(millis: Long) = NotificationCompat.Builder(this, Constants.TIMER_CHANNEL_ID)
    .setContentTitle(getString(R.string.timer_notification_title))
    .setSmallIcon(R.mipmap.ic_launcher)
    .setAutoCancel(true)
    .setUsesChronometer(true)
    .setShowWhen(true)
    .setOngoing(true)
    .setOnlyAlertOnce(true)
    .setWhen(millis)
    .build()!!



fun Long.millisToMinutesSeconds(): String {
    val minutes = (this / 1000) / 60
    val seconds = ((this / 1000) % 60) + 1
    return "$minutes:${if (seconds < 10) "0" else ""}$seconds"
}