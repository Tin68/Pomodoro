package com.example.pomy

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.pomy.util.NotificationUtil
import com.example.pomy.util.PrefUtil

class TimerNotificationActionReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            AppConstants.ACTION_STOP -> {
                MainActivity.removeAlarm(context)
                PrefUtil.setTimerState(MainActivity.TimerState.Stopped, context)
                NotificationUtil.hideTimerNotification(context)
                //actualizar bd
                MainActivity().insertDataToDatabase()

            }

            AppConstants.ACTION_PAUSE -> {
                var secondsRemaining = PrefUtil.getSecondsRemaining(context)
                val alarmSetTime = PrefUtil.getAlarmSetTime(context)
                val nowSeconds = MainActivity.nowSeconds

                secondsRemaining -= nowSeconds - alarmSetTime
                PrefUtil.setSecondsRemaining(secondsRemaining, context)

                MainActivity.removeAlarm(context)
                PrefUtil.setTimerState(MainActivity.TimerState.Paused, context)
                NotificationUtil.showTimerPaused(context)
            }

            AppConstants.ACTION_RESUME -> {
                val secondsRemaining = PrefUtil.getSecondsRemaining(context)
                val wakeUpTime = MainActivity.setAlarm(context, MainActivity.nowSeconds, secondsRemaining)
                PrefUtil.setTimerState(MainActivity.TimerState.Running, context)
                val asc = if (PrefUtil.getIteration(context) % 2 == 0) "ASC" else "DESC"
                NotificationUtil.showTimerRunning(context, wakeUpTime,asc)
            }
            AppConstants.ACTION_START -> {
                val minutesRemaining = PrefUtil.getTimerLength(context)
                val secondsRemaining = minutesRemaining * 60L
                val wakeUpTime = MainActivity.setAlarm(context, MainActivity.nowSeconds, secondsRemaining)
                PrefUtil.setTimerState(MainActivity.TimerState.Running, context)
                PrefUtil.setSecondsRemaining(secondsRemaining, context)
                val asc = if (PrefUtil.getIteration(context) % 2 == 0) "ASC" else "DESC"
                NotificationUtil.showTimerRunning(context, wakeUpTime,asc)
            }
            AppConstants.ACTION_NEXT -> {
                val secondsRemaining : Long
                val iteratorTemp = PrefUtil.getIteration(context)
                secondsRemaining = when {
                    iteratorTemp % 2 == 0-> {
                        AppConstants.POMODORO_TIME.toLong() * 60L
                    }
                    else -> {
                        if ((iteratorTemp +1) % 8 == 0) {
                            AppConstants.LONG_BREAK_TIME.toLong() * 60L
                        } else {
                            AppConstants.SHORT_BREAK_TIME.toLong() * 60L
                        }
                    }
                }
                val wakeUpTime = MainActivity.setAlarm(context, MainActivity.nowSeconds, secondsRemaining)
                PrefUtil.setTimerState(MainActivity.TimerState.Running, context)
                PrefUtil.setSecondsRemaining(secondsRemaining, context)
                val asc = if (PrefUtil.getIteration(context) % 2 == 0) "ASC" else "DESC"
                NotificationUtil.showTimerExpired(context, wakeUpTime)
                //NotificationUtil.showTimerRunning(context, wakeUpTime,asc)
            }
        }
    }
}