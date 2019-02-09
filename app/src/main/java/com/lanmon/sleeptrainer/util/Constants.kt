package com.lanmon.sleeptrainer.util

object Constants {
    const val CHECK_CHANNEL_ID = "check"
    const val TIMER_CHANNEL_ID = "timer"
    const val CHECK_NOTIFICATION_ID = 1
    const val TIMER_NOTIFICATION_ID = 2
    const val ASLEEP_BUTTON_CODE = 0
    const val AWAKE_BUTTON_CODE = 1
    const val TIMER_NOTIFICATION_CODE = 2
    const val ADMOB_APP_ID = "ca-app-pub-3750115849025588~6338256481"
    const val ADMOB_BANNER_ID = "ca-app-pub-3750115849025588/3228166907"
    const val ADMOB_BANNER_ID_TEST = "ca-app-pub-3940256099942544/6300978111"
    const val PREFERENCES_NAME = "Prefs"

    private const val SECONDS_PER_MINUTE = 60
    private const val ONE_SECOND = 1000L
    private const val FIVE_MINUTES = ONE_SECOND * SECONDS_PER_MINUTE * 5
    private const val TEN_MINUTES = FIVE_MINUTES + FIVE_MINUTES
    private const val FIFTEEN_MINUTES = TEN_MINUTES + FIVE_MINUTES
}