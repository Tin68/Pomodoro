package com.example.pomy.util

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.TaskStackBuilder
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.support.v4.app.NotificationCompat
import com.example.pomy.AppConstants
import com.example.pomy.MainActivity
import com.example.pomy.MainActivity.Companion.setAlarm
import com.example.pomy.R
import com.example.pomy.TimerNotificationActionReceiver
import java.text.SimpleDateFormat
import java.util.*


class NotificationUtil {
    companion object {
        private const val CHANNEL_ID_TIMER = "menu_timer"
        private const val CHANNEL_NAME_TIMER = "Pomy Timer"
        private const val TIMER_ID = 0


        fun showTimerExpired (context: Context, wakeUpTime: Long) {

            val iteration = PrefUtil.getIteration(context) + 1
            val asc: String
            val now = System.currentTimeMillis()
            val sound : Int

            val secondsRemaining : Long
            when {
                iteration % 8 == 0 -> {
                    asc = "ASC"
                    secondsRemaining = AppConstants.LONG_BREAK_TIME.toLong() * 60L
                    sound=R.raw.campanillas
                }
                iteration % 2 == 0 -> {
                    asc = "ASC"
                    secondsRemaining =  AppConstants.SHORT_BREAK_TIME.toLong() * 60L
                    sound=R.raw.campanillas
                }
                else -> {
                    asc = "DESC"
                    secondsRemaining = AppConstants.POMODORO_TIME.toLong() * 60L
                    sound=R.raw.sirena
                }
            }
            val mt: MediaPlayer = MediaPlayer.create(context,sound)
            mt.start()
            PrefUtil.setIteration(iteration,context)
            val wakeUpTime1 = setAlarm(context, now / 1000 , secondsRemaining)
            showTimerRunning(context, wakeUpTime1, asc)
        }

        @SuppressLint("RestrictedApi", "UnspecifiedImmutableFlag")
        fun showTimerRunning (context: Context, wakeUpTime: Long, asc: String) {
            val stopIntent = Intent(context, TimerNotificationActionReceiver::class.java)
            stopIntent.action = AppConstants.ACTION_STOP
            val stopPendingIntent = PendingIntent.getBroadcast(context,
                0, stopIntent, PendingIntent.FLAG_UPDATE_CURRENT)

            val pauseIntent = Intent(context, TimerNotificationActionReceiver::class.java)
            pauseIntent.action = AppConstants.ACTION_PAUSE
            val pausePendingIntent = PendingIntent.getBroadcast(context, 0,
                pauseIntent, PendingIntent.FLAG_UPDATE_CURRENT)

            val nextIntent = Intent(context, TimerNotificationActionReceiver::class.java)
            nextIntent.action = AppConstants.ACTION_NEXT
            val nextPendingIntent = PendingIntent.getBroadcast(context, 0,
                nextIntent, PendingIntent.FLAG_UPDATE_CURRENT)

            val df= SimpleDateFormat.getTimeInstance(SimpleDateFormat.SHORT)
            val resource: Int = if (asc =="DESC"){
                R.string.pomodoro_running_task
            } else {
                R.string.pomodoro_running_pause
            }
            val nBuilder = getBasicNotificationBuilder(context, CHANNEL_ID_TIMER, true)
            nBuilder.setContentTitle(context.getString(resource))
                .setContentText(context.getString(R.string.end) + " ${df.format(Date(wakeUpTime))}")
                .setContentIntent(getPendingIntentWithStack(context, MainActivity::class.java))
                .setOngoing(true)
                //.addAction(R.drawable.ic_stop, "Stop", stopPendingIntent)
                .addAction(R.drawable.ic_pause, "Pause", pausePendingIntent)
                .addAction(R.drawable.ic_next, "Next", nextPendingIntent)
            val nManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            nManager.createNotificationChannel(CHANNEL_ID_TIMER, CHANNEL_NAME_TIMER, true)
            nManager.notify(TIMER_ID, nBuilder.build())
        }

        @SuppressLint("UnspecifiedImmutableFlag")
        fun showTimerPaused (context: Context){
            val stopIntent = Intent(context, TimerNotificationActionReceiver::class.java)
            stopIntent.action = AppConstants.ACTION_STOP
            val stopPendingIntent = PendingIntent.getBroadcast(context,
                0, stopIntent, PendingIntent.FLAG_UPDATE_CURRENT)
            val resumeIntent = Intent(context, TimerNotificationActionReceiver::class.java)
            resumeIntent.action = AppConstants.ACTION_RESUME
            val resumePendingIntent = PendingIntent.getBroadcast(context,
                0, resumeIntent, PendingIntent.FLAG_UPDATE_CURRENT)

            val nextIntent = Intent(context, TimerNotificationActionReceiver::class.java)
            nextIntent.action = AppConstants.ACTION_NEXT
            val nextPendingIntent = PendingIntent.getBroadcast(context, 0,
                nextIntent, PendingIntent.FLAG_UPDATE_CURRENT)

            val nBuilder = getBasicNotificationBuilder(context, CHANNEL_ID_TIMER, true)
            nBuilder.setContentTitle(context.getString(R.string.pomodoro_paused))
                .setContentText(context.getString(R.string.resume))
                .setContentIntent(getPendingIntentWithStack(context, MainActivity::class.java))
                .setOngoing(true)
                //.addAction(R.drawable.ic_stop, "Stop", stopPendingIntent)
                .addAction(R.drawable.ic_play_arrow, "Resume", resumePendingIntent)
                .addAction(R.drawable.ic_next, "Next", nextPendingIntent)

            val nManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            nManager.createNotificationChannel(CHANNEL_ID_TIMER, CHANNEL_NAME_TIMER, true)
            nManager.notify(TIMER_ID, nBuilder.build())
        }

        fun hideTimerNotification(context: Context){
            val nManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            nManager.cancel(TIMER_ID)
        }

        private fun getBasicNotificationBuilder(
            context: Context,
            channelID: String,
            playSound: Boolean
        ) : NotificationCompat.Builder {
            val notificationSound: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val nBuilder = NotificationCompat.Builder(context, channelID)
                .setSmallIcon(R.drawable.ic_timer)
                .setAutoCancel(true)
                .setDefaults(0)
            if (playSound) nBuilder.setSound(notificationSound)
            return nBuilder
        }

        private fun <T> getPendingIntentWithStack (context: Context, javaClass : Class<T>): PendingIntent {
            val resultIntent = Intent(context, javaClass)
            resultIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP

            val stackBuilder = TaskStackBuilder.create(context)
            stackBuilder.addParentStack(javaClass)
            stackBuilder.addNextIntent(resultIntent)

            return stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT)
        }

        @SuppressLint("ObsoleteSdkInt")
        @TargetApi(26 )
        private fun NotificationManager.createNotificationChannel(
            channelID: String,
            channelName: String,
            playSound: Boolean
        ){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
                val channelImportance = if (playSound) NotificationManager.IMPORTANCE_DEFAULT
                else NotificationManager.IMPORTANCE_LOW
                val nChannel = NotificationChannel(channelID, channelName, channelImportance)
                nChannel.enableLights(true)
                nChannel.lightColor = Color.BLUE
                this.createNotificationChannel(nChannel)
            }
        }
    }
}

