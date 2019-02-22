package com.lanmon.sleeptrainer.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.lanmon.sleeptrainer.R
import com.lanmon.sleeptrainer.internal.CHECK_CHANNEL_ID
import com.lanmon.sleeptrainer.internal.NotificationActionReceiver
import com.lanmon.sleeptrainer.internal.TIMER_CHANNEL_ID
import com.lanmon.sleeptrainer.internal.getChildName
import com.lanmon.sleeptrainer.ui.MainActivity

fun NotificationManagerCompat.createNotificationChannels(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val checkChannel = NotificationChannel(
            CHECK_CHANNEL_ID,
            context.getString(R.string.check_channel_name),
            NotificationManager.IMPORTANCE_HIGH
        ).also {
            it.description = context.getString(R.string.check_channel_desc)
        }
        val timerChannel = NotificationChannel(
            TIMER_CHANNEL_ID,
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

private fun Context.getMainActivityIntent() = PendingIntent.getActivity(
    this,
    0,
    Intent(this, MainActivity::class.java),
    0
)

fun Context.buildCheckNotification() =
    NotificationCompat.Builder(this, CHECK_CHANNEL_ID)
        .setSmallIcon(R.drawable.ic_baby)
        .setContentTitle(getString(R.string.check_notification_title))
        .setContentText(getString(R.string.check_notification_text, getChildName()))
        .addAction(R.drawable.ic_awake, getString(R.string.awake_notification_button), buildAwakeAction())
        .addAction(R.drawable.ic_asleep, getString(R.string.asleep_notification_button), buildAsleepAction())
        .setAutoCancel(true)
        .setContentIntent(getMainActivityIntent())
        .build()!!

private fun Context.buildAsleepAction() = PendingIntent.getBroadcast(
    this,
    0,
    Intent(this, NotificationActionReceiver::class.java).apply {
        action = getString(R.string.asleep_intent_action)
    },
    0
)

private fun Context.buildAwakeAction() = PendingIntent.getBroadcast(
    this,
    0,
    Intent(this, NotificationActionReceiver::class.java).apply {
        action = getString(R.string.awake_intent_action)
    },
    0
)

fun Context.buildTimerNotification(millisUntilFinished: Long) = NotificationCompat
    .Builder(this, TIMER_CHANNEL_ID)
    .setSmallIcon(R.drawable.ic_baby)
    .setContentTitle(getString(R.string.timer_notification_title))
    .setContentText(millisUntilFinished.millisToMinutesAndSeconds())
    .setContentIntent(getMainActivityIntent())
    .setAutoCancel(true)
    .build()!!