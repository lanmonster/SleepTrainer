package com.lanmon.sleeptrainer.internal

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationManagerCompat
import com.lanmon.sleeptrainer.R
import com.lanmon.sleeptrainer.util.startBackgroundTimer

class NotificationActionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        NotificationManagerCompat.from(context).cancel(CHECK_NOTIFICATION_ID)
        val timer = FerberTimer.getFerberTimer()
        when (intent.action) {
            context.getString(R.string.asleep_intent_action) -> {
                timer.finish()
            }
            context.getString(R.string.awake_intent_action) -> {
                timer.next()
                with(context) {
                    startService(startBackgroundTimer(when (timer.state) {
                        is FerberTimer.TimerState.FiveMinutes -> TEN_MINUTES
                        else -> FIFTEEN_MINUTES
                    }))
                }
            }
        }
    }
}
