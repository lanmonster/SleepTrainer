package com.lanmon.sleeptrainer.internal

import android.content.Context
import android.preference.PreferenceManager
import com.lanmon.sleeptrainer.R

private fun Context.getPreferences() = getSharedPreferences(getString(R.string.preferences_name), Context.MODE_PRIVATE)

fun Context.setNumChecks(numChecks: Int) =
    getPreferences()
        .edit()
        .putInt(getString(R.string.num_checks_extra_key), numChecks)
        .apply()


fun Context.getTimerStartTime() = getPreferences()
    .getLong(getString(R.string.timer_start_time_preference), 0)

fun Context.setTimerStartTime(startTime: Long) = getPreferences()
    .edit()
    .putLong(getString(R.string.timer_start_time_preference), startTime)
    .apply()

fun Context.getChildName() = PreferenceManager
    .getDefaultSharedPreferences(this)
    .getString(getString(R.string.child_name_preference), "your child")!!