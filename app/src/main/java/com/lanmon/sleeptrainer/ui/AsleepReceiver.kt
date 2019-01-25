package com.lanmon.sleeptrainer.ui

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.core.app.NotificationManagerCompat
import com.lanmon.sleeptrainer.R

class AsleepReceiver: BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        NotificationManagerCompat.from(context).cancelAll()
        val numChecks = intent.getIntExtra(context.getString(R.string.intent_num_checks), 1)
        Toast.makeText(
            context,
            context.resources.getQuantityString(
                R.plurals.numberOfChecks,
                numChecks,
                numChecks
            ),
            Toast.LENGTH_LONG
        ).show()
//        context.startActivity(Intent(context, MainActivity::class.java).apply {
//            action = context.getString(R.string.asleep_intent_action)
//            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
//        })
    }
}