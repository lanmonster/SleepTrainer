package com.lanmon.sleeptrainer.ui

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.SystemClock
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import com.lanmon.sleeptrainer.R
import com.lanmon.sleeptrainer.data.TimerService
import com.lanmon.sleeptrainer.util.*
import com.lanmon.sleeptrainer.util.Constants.ADMOB_APP_ID
import com.lanmon.sleeptrainer.util.Constants.CHECK_NOTIFICATION_ID
import com.lanmon.sleeptrainer.util.Constants.PREFERENCES_NAME
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.toolbar.*

class MainActivity : AppCompatActivity() {
    private lateinit var notificationManager: NotificationManagerCompat
    private lateinit var timerService: TimerService
    private val serviceConnection = TimerServiceConnection(onConnectCallback = {
        timerService = it
        subscribeUi()
    }, onDisconnectCallback = {
        unsubscribeUi()
    })
    private var isTimerStarted = false
    private lateinit var preferences: SharedPreferences
    private lateinit var asleepReceiver: ComponentName

    override fun onStart() {
        super.onStart()
        asleepReceiver = ComponentName(this, AsleepReceiver::class.java)
        packageManager.setComponentEnabledSetting(
            asleepReceiver,
            PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
            PackageManager.DONT_KILL_APP
        )
        isAppActive = true
    }

    override fun onStop() {
        super.onStop()
        packageManager.setComponentEnabledSetting(
            asleepReceiver,
            PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
            PackageManager.DONT_KILL_APP
        )
        isAppActive = false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        MobileAds.initialize(this, ADMOB_APP_ID)
        preferences = getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)

        getNewAd()

        notificationManager = NotificationManagerCompat.from(this)
        notificationManager.createNotificationChannels(this)

        if (!intent.action.isNullOrBlank()) {
            onNewIntent(intent)
        }

        val timerServiceIntent = Intent(this, TimerService::class.java)

        if (isServiceRunning(TimerService::class.java)) {
            application.bindService(timerServiceIntent, serviceConnection, Context.BIND_IMPORTANT)
            isTimerStarted = true
            elapsed_time.base = preferences.getLong(getString(R.string.time_started_key), SystemClock.elapsedRealtime())
            elapsed_time.start()
            onTimerRunning()
        }

        if (!isTimerStarted) {
            onTimerStopped()
        }

        start_button.setOnClickListener {
            if (!isTimerStarted) {
                progressBar.max = fiveMinutes.toInt()
                progressBar.progress = fiveMinutes.toInt()
                startTimer(timerServiceIntent)
                val startTime = SystemClock.elapsedRealtime()
                elapsed_time.base = startTime
                elapsed_time.start()
                preferences.edit().putLong(getString(R.string.time_started_key), startTime).apply()
            }
        }

        cancel_button.setOnClickListener {
            if (isTimerStarted) {
                elapsed_time.stop()
                progressBar.progress = 0
                stopTimer()
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        notificationManager.cancel(CHECK_NOTIFICATION_ID)
        val numChecks = intent.getIntExtra(getString(R.string.intent_num_checks), 1)
        when (intent.action) {
            getString(R.string.asleep_intent_action) -> {
                num_checks.text = numChecks.toString()
                stopTimer()
            }
            getString(R.string.awake_intent_action) -> {
                getNewAd()
                startTimer(
                    Intent(this, TimerService::class.java).also {
                        it.putExtra(getString(R.string.intent_time_remaining), if (numChecks == 1) {
                            progressBar.max = tenMinutes.toInt()
                            tenMinutes
                        } else {
                            progressBar.max = fifteenMinutes.toInt()
                            fifteenMinutes
                        })
                        it.putExtra(getString(R.string.intent_num_checks), numChecks + 1)
                    }
                )
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.action_settings -> {
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    private fun subscribeUi() {
        timerService.timer.timeLeft.observe(this, Observer {
            progressBar.progress = it.toInt()
            chronometer.text = applicationContext.millisToMinutesSeconds(it)
        })
        timerService.checks.observe(this, Observer {
            num_checks.text = (it - 1).toString()
        })
    }

    private fun unsubscribeUi() {
        timerService.timer.timeLeft.removeObservers(this)
    }

    private fun startTimer(intent: Intent) {
        ContextCompat.startForegroundService(this, intent)
        application.bindService(intent, serviceConnection, Context.BIND_IMPORTANT)
        isTimerStarted = true
        onTimerRunning()
    }

    private fun stopTimer() {
        elapsed_time.stop()
        timerService.timer.cancel()
        application.unbindService(serviceConnection)
        stopService(Intent(this, TimerService::class.java))
        notificationManager.cancelAll()
        isTimerStarted = false
        onTimerStopped()
    }

    private fun onTimerStopped() {
        chronometer.visibility = View.GONE
        cancel_button.visibility = View.GONE
        start_button.visibility = View.VISIBLE
    }

    private fun onTimerRunning() {
        chronometer.visibility = View.VISIBLE
        cancel_button.visibility = View.VISIBLE
        start_button.visibility = View.GONE
    }

    private fun getNewAd() = adView.loadAd(AdRequest.Builder()
        .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
        .build())
}