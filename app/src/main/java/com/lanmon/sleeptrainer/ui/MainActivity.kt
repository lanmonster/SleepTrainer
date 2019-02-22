package com.lanmon.sleeptrainer.ui

import android.content.Intent
import android.os.Bundle
import android.os.SystemClock
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.Observer
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import com.lanmon.sleeptrainer.R
import com.lanmon.sleeptrainer.internal.*
import com.lanmon.sleeptrainer.util.createNotificationChannels
import com.lanmon.sleeptrainer.util.millisToMinutesAndSeconds
import com.lanmon.sleeptrainer.util.startBackgroundTimer
import com.lanmon.sleeptrainer.util.stopBackgroundTimer
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*

class MainActivity : AppCompatActivity() {
    private val timer = FerberTimer.getFerberTimer()
    private lateinit var notificationManager: NotificationManagerCompat

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        notificationManager = NotificationManagerCompat.from(this)
        notificationManager.createNotificationChannels(this)
    }

    override fun onResume() {
        super.onResume()

        MobileAds.initialize(this, ADMOB_APP_ID)

        getAd()

        startService(stopBackgroundTimer())

        notificationManager.cancelAll()

        if (timer.state is FerberTimer.TimerState.NotStarted) {
            stopElapsedTimeCounter()
        } else {
            startElapsedTimeCounter()
        }

        start_button.setOnClickListener {
            timer.start()
            setTimerStartTime(SystemClock.elapsedRealtime())
            startElapsedTimeCounter()
        }

        cancel_button.setOnClickListener {
            timer.cancel()
            stopElapsedTimeCounter()
        }

        awake_button.setOnClickListener {
            timer.next()
        }

        asleep_button.setOnClickListener {
            timer.finish()
            stopElapsedTimeCounter()
        }

        subscribeUi()
    }

    override fun onPause() {
        super.onPause()

        if (timer.status is FerberTimer.TimerStatus.Running) {
            startService(startBackgroundTimer(timer.timeLeft))
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_settings -> {
                startActivity(Intent(applicationContext, SettingsActivity::class.java))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun showStartButton() {
        cancel_button.visibility = View.GONE
        asleep_button.visibility = View.GONE
        awake_button.visibility = View.GONE

        start_button.visibility = View.VISIBLE
    }

    private fun showCancelButton() {
        start_button.visibility = View.GONE
        asleep_button.visibility = View.GONE
        awake_button.visibility = View.GONE

        cancel_button.visibility = View.VISIBLE
    }

    private fun showAwakeAsleepButtons() {
        start_button.visibility = View.GONE
        cancel_button.visibility = View.GONE

        asleep_button.visibility = View.VISIBLE
        awake_button.visibility = View.VISIBLE
    }

    private fun subscribeUi() {
        timer.stateLive.observe(this, Observer {
            progressBar.max = it.getLength().toInt()
            when (it) {
                is FerberTimer.TimerState.NotStarted -> showStartButton()
                else -> {
                    if (timer.status is FerberTimer.TimerStatus.Running) {
                        showCancelButton()
                    } else {
                        showAwakeAsleepButtons()
                    }
                }
            }
        })
        timer.timeLeftLive.observe(this, Observer {
            time_left.text = it.millisToMinutesAndSeconds()
            progressBar.progress = it.toInt()

            if (it == 0L) {
                onTimerExpired()
            }
        })
        timer.numChecks.observe(this, Observer {
            num_checks.text = it.toString()
            setNumChecks(it)
        })
    }

    private fun onTimerExpired() {
        showAwakeAsleepButtons()
    }

    private fun getAd() {
        ad_view.loadAd(AdRequest.Builder().build())
    }

    private fun startElapsedTimeCounter() {
        elapsed_time.base = getTimerStartTime()
        elapsed_time.start()
    }

    private fun stopElapsedTimeCounter() {
        elapsed_time.stop()
        elapsed_time.base = SystemClock.elapsedRealtime()
    }
}