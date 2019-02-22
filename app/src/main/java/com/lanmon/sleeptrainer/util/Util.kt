package com.lanmon.sleeptrainer.util

import android.content.Context
import android.content.Intent
import com.lanmon.sleeptrainer.R
import com.lanmon.sleeptrainer.internal.ONE_SECOND
import com.lanmon.sleeptrainer.ui.BackgroundTimer

fun Context.startBackgroundTimer(time: Long) = Intent(this, BackgroundTimer::class.java).apply {
    action = getString(R.string.action_start_background_timer)
    putExtra(getString(R.string.time_extra_key), time - ONE_SECOND)
}

fun Context.stopBackgroundTimer() = Intent(this, BackgroundTimer::class.java).apply {
    action = getString(R.string.action_stop_background_timer)
}




