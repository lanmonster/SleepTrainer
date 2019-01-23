package com.lanmon.sleeptrainer

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import com.lanmon.sleeptrainer.Constants.CHECK_NOTIFICATION_ID
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    private lateinit var notificationManager: NotificationManagerCompat
    private lateinit var timerService: TimerService
    private val serviceConnection = object : ServiceConnection {
        override fun onServiceDisconnected(name: ComponentName?) {
            unsubscribeUi()
        }

        override fun onServiceConnected(name: ComponentName?, service: IBinder) {
            timerService = (service as TimerService.TimerServiceBinder).getService()
            subscribeUi()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        notificationManager = NotificationManagerCompat.from(this)
        notificationManager.createNotificationChannels(this)

        if (!intent.action.isNullOrBlank()) {
            onNewIntent(intent)
        }

        val timerServiceIntent = Intent(this, TimerService::class.java)

        if (isServiceRunning(TimerService::class.java)) {
            chronometer.visibility = View.VISIBLE
            button.visibility = View.GONE
            application.bindService(timerServiceIntent, serviceConnection, Context.BIND_IMPORTANT)
        }

        button.setOnClickListener {
            it.visibility = View.GONE
            chronometer.visibility = View.VISIBLE
            startTimer(timerServiceIntent)
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        notificationManager.cancel(CHECK_NOTIFICATION_ID)

        val numChecks = intent.getIntExtra(getString(R.string.intent_num_checks), 1)
        when (intent.action) {
            getString(R.string.asleep_intent_action) -> {
                Toast.makeText(
                    this,
                    resources.getQuantityString(
                        R.plurals.numberOfChecks,
                        numChecks,
                        numChecks
                    ),
                    Toast.LENGTH_LONG
                ).show()
                button.visibility = View.VISIBLE
                chronometer.visibility = View.GONE
            }
            getString(R.string.awake_intent_action) -> {
                startTimer(Intent(this, TimerService::class.java).also {
                    it.putExtra(getString(R.string.intent_time_remaining), if (numChecks == 1) {
                        tenMinutes
                    } else {
                        fifteenMinutes
                    })
                    it.putExtra(getString(R.string.intent_num_checks), numChecks + 1)
                })
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.action_settings -> {
            Toast.makeText(this, "Settings clicked!", Toast.LENGTH_LONG).show()
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    private fun subscribeUi() {
        timerService.timer.timeLeft.observe(this, Observer {
            chronometer.text = applicationContext.millisToMinutesSeconds(it)
        })
    }

    private fun unsubscribeUi() {
        timerService.timer.timeLeft.removeObservers(this)
    }

    private fun startTimer(intent: Intent) {
        ContextCompat.startForegroundService(this, intent)
        application.bindService(intent, serviceConnection, Context.BIND_IMPORTANT)
    }

}