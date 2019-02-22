package com.lanmon.sleeptrainer.util

import com.lanmon.sleeptrainer.internal.ONE_SECOND

fun Long.millisToMinutesAndSeconds(): String {
    if (this == 0L) return "0:00"

    val millis = if (this % ONE_SECOND != 0L) {
        this.roundUpToNearestSecond()
    } else {
        this
    }
    val normalized = millis / ONE_SECOND
    val minutes = normalized / 60
    val seconds = normalized % 60
    return "$minutes:${if (seconds < 10) "0$seconds" else "$seconds"}"
}

fun Long.roundUpToNearestSecond() = this + (ONE_SECOND - (this % ONE_SECOND))