package com.lanmon.sleeptrainer.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationManagerCompat
import com.lanmon.sleeptrainer.R
import com.lanmon.sleeptrainer.util.Constants.CHECK_CHANNEL_ID
import com.lanmon.sleeptrainer.util.Constants.TIMER_CHANNEL_ID

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
