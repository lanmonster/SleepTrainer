package com.lanmon.sleeptrainer.internal

import android.os.CountDownTimer
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

class FerberTimer private constructor() {
    companion object {
        @Volatile
        private var instance: FerberTimer? = null

        fun getFerberTimer() = instance ?: synchronized(this) {
            FerberTimer()
                .also { instance = it }
        }
    }

    private val fiveMinuteTimer = GenericCountDownTimer(FIVE_MINUTES)
    private val tenMinuteTimer = GenericCountDownTimer(TEN_MINUTES)
    private val fifteenMinuteTimer = GenericCountDownTimer(FIFTEEN_MINUTES)
    private var timer = fiveMinuteTimer

    private val _state = MutableLiveData<TimerState>(TimerState.NotStarted)
    val stateLive: LiveData<TimerState>
        get() = _state
    val state: TimerState
        get() = _state.value ?: TimerState.NotStarted
    private val _timeLeft = MutableLiveData(FIVE_MINUTES)
    val timeLeftLive: LiveData<Long>
        get() = _timeLeft
    val timeLeft: Long
        get() = _timeLeft.value ?: 0
    private var _status: TimerStatus =
        TimerStatus.Stopped
    val status: TimerStatus
        get() = _status
    private val _numChecks = MutableLiveData(0)
    val numChecks: LiveData<Int>
        get() = _numChecks

    sealed class TimerState {
        abstract fun getLength(): Long

        object NotStarted : TimerState() {
            override fun getLength() = FIVE_MINUTES
        }

        object FiveMinutes : TimerState() {
            override fun getLength() = FIVE_MINUTES
        }

        object TenMinutes : TimerState() {
            override fun getLength() = TEN_MINUTES
        }

        object FifteenMinutes : TimerState() {
            override fun getLength() = FIFTEEN_MINUTES
        }
    }

    sealed class TimerStatus {
        object Running : TimerStatus()
        object Stopped : TimerStatus()
    }

    private inner class GenericCountDownTimer(length: Long) : CountDownTimer(length, 1) {
        override fun onTick(millisUntilFinished: Long) {
            _timeLeft.postValue(millisUntilFinished)
        }

        override fun onFinish() {
            _timeLeft.postValue(0)
            _status = TimerStatus.Stopped
        }
    }

    fun start() {
        timer.start()
        _state.postValue(TimerState.FiveMinutes)
        _status = TimerStatus.Running
    }

    fun next() {
        timer = when (_state.value!!) {
            TimerState.NotStarted -> {
                throw IllegalStateException("cannot call `next` on a timer that has not been started!")
            }
            TimerState.FiveMinutes -> {
                _state.postValue(TimerState.TenMinutes)
                tenMinuteTimer
            }
            TimerState.TenMinutes -> {
                _state.postValue(TimerState.FifteenMinutes)
                 fifteenMinuteTimer
            }
            TimerState.FifteenMinutes -> {
                _state.postValue(TimerState.FifteenMinutes)
                fifteenMinuteTimer
            }
        }
        _numChecks.postValue((_numChecks.value ?: 0) + 1)
        _status = TimerStatus.Running
        timer.start()
    }

    fun cancel() {
        timer.cancel()
        _timeLeft.postValue(FIVE_MINUTES)
        timer = fiveMinuteTimer
        _state.postValue(TimerState.NotStarted)
        _status = TimerStatus.Stopped
        _numChecks.postValue(0)
    }

    fun finish() {
        cancel()
    }
}

