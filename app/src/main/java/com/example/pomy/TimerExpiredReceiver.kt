package com.example.pomy


import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.pomy.util.NotificationUtil
import com.example.pomy.util.PrefUtil

class TimerExpiredReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        NotificationUtil.showTimerExpired(context, PrefUtil.getAlarmSetTime(context))
        //PrefUtil.setTimerState(MainActivity.TimerState.Running, context)

        //NotificationUtil.showTimerExpired(context)
        //PrefUtil.setTimerState(MainActivity.TimerState.Stopped, context)
        //PrefUtil.setAlarmSetTime(0, context)

    }
}