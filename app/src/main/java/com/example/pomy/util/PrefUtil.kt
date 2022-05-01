package com.example.pomy.util

import android.content.Context
import com.example.pomy.AppConstants
import com.example.pomy.MainActivity

class PrefUtil {
    companion object {
        private const val PREFS_FILE_NAME = "com.example.pomy.PREFERENCE_FILE_KEY"

        //private const val TIMER_LENGTH_ID = "com.example.pomy.timer_length"

        fun getTimerLength(context: Context): Int {
            //placehorder
            return AppConstants.POMODORO_TIME
        }
        /*
        fun setTimerLength(context: Context, timerLength: Int) {
            val preferences = context.getSharedPreferences(PREFS_FILE_NAME, Context.MODE_PRIVATE)
            val editor = preferences.edit()
            editor.putInt(TIMER_LENGTH_ID, timerLength)
            editor.apply()
        }
        */
        private const val TIMER_BREAK_LENGHT_ID = "com.example.pomy.timer_break_length"

        fun getTimerBreakLength(context: Context): Int {
            val preferences = context.getSharedPreferences(PREFS_FILE_NAME, Context.MODE_PRIVATE)
            return preferences.getInt(TIMER_BREAK_LENGHT_ID, AppConstants.LONG_BREAK_TIME)
        }

        fun setTimerBreakLength(context: Context, timerBreakLength: Int) {
            val preferences = context.getSharedPreferences(PREFS_FILE_NAME, Context.MODE_PRIVATE)
            val editor = preferences.edit()
            editor.putInt(TIMER_BREAK_LENGHT_ID, timerBreakLength)
            editor.apply()
        }



        private const val PREVIOUS_TIMER_LENGTH_SECONDS_ID =
            "com.example.pomy.previous_timer_length"


        fun getPreviousTimerLengthSeconds(context: Context): Long {
            val preferences = context.getSharedPreferences(PREFS_FILE_NAME, Context.MODE_PRIVATE)
            return preferences.getLong(PREVIOUS_TIMER_LENGTH_SECONDS_ID, 0)
        }

        fun setPreviousTimerLengthSeconds(seconds: Long, context: Context) {
            val editor = context.getSharedPreferences(PREFS_FILE_NAME, Context.MODE_PRIVATE).edit()
            editor.putLong(PREVIOUS_TIMER_LENGTH_SECONDS_ID, seconds)
            editor.apply()
        }

        private const val TIMER_STATE_ID = "com.example.pomy.timer_state"

        fun getTimerState(context: Context): MainActivity.TimerState {
            val preferences = context.getSharedPreferences(PREFS_FILE_NAME, Context.MODE_PRIVATE)
            val ordinal = preferences.getInt(TIMER_STATE_ID, 0)
            return MainActivity.TimerState.values()[ordinal]
        }

        fun setTimerState(state: MainActivity.TimerState, context: Context) {
            val editor = context.getSharedPreferences(PREFS_FILE_NAME, Context.MODE_PRIVATE).edit()
            val ordinal = state.ordinal
            editor.putInt(TIMER_STATE_ID, ordinal)
            editor.apply()
        }

        private const val SECONDS_REMAINING_ID =
            "com.example.pomy.seconds_remaining"

        fun getSecondsRemaining(context: Context): Long {
            val preferences = context.getSharedPreferences(PREFS_FILE_NAME, Context.MODE_PRIVATE)
            return preferences.getLong(SECONDS_REMAINING_ID, 0)
        }

        fun setSecondsRemaining(seconds: Long, context: Context) {
            val editor = context.getSharedPreferences(PREFS_FILE_NAME, Context.MODE_PRIVATE).edit()
            editor.putLong(SECONDS_REMAINING_ID, seconds)
            editor.apply()
        }

        private const val ALARM_SET_TIME_ID = "com.example.pomy.backgrounded_time"

        fun getAlarmSetTime(context: Context): Long {
            val preferences = context.getSharedPreferences(PREFS_FILE_NAME, Context.MODE_PRIVATE)
            return preferences.getLong(ALARM_SET_TIME_ID, 0)
        }

        fun setAlarmSetTime(time: Long, context: Context) {
            val editor = context.getSharedPreferences(PREFS_FILE_NAME, Context.MODE_PRIVATE).edit()
            editor.putLong(ALARM_SET_TIME_ID, time)
            editor.apply()
        }

        private const val ITERATION = "com.example.pomy.iterator_number"

        fun getIteration(context: Context): Int {
            val preferences = context.getSharedPreferences(PREFS_FILE_NAME, Context.MODE_PRIVATE)
            return preferences.getInt(ITERATION, 0)
        }

        fun setIteration(iteration: Int, context: Context) {
            val editor = context.getSharedPreferences(PREFS_FILE_NAME, Context.MODE_PRIVATE).edit()
            editor.putInt(ITERATION, iteration)
            editor.apply()
        }

        private const val PROGRESS_BAR_MAX = "com.example.pomy.progress_bar_max"

        fun getProgressBarMax(context: Context): Int {
            val preferences = context.getSharedPreferences(PREFS_FILE_NAME, Context.MODE_PRIVATE)
            return preferences.getInt(PROGRESS_BAR_MAX, 0)
        }

        fun setProgressBarMax(max: Int, context: Context) {
            val editor = context.getSharedPreferences(PREFS_FILE_NAME, Context.MODE_PRIVATE).edit()
            editor.putInt(PROGRESS_BAR_MAX, max)
            editor.apply()
        }

        private const val INI_TIME_TASK_ID = "com.example.pomy.ini_time_task"

        fun getIniTimeTask(context: Context): Long {
            val preferences = context.getSharedPreferences(PREFS_FILE_NAME, Context.MODE_PRIVATE)
            return preferences.getLong(INI_TIME_TASK_ID, 0)
        }

        fun setIniTimeTask(time: Long, context: Context) {
            val editor = context.getSharedPreferences(PREFS_FILE_NAME, Context.MODE_PRIVATE).edit()
            editor.putLong(INI_TIME_TASK_ID, time)
            editor.apply()
        }

        private const val TEXT_TASK_ID = "com.example.pomy.text_task"

        fun getTextTask(context: Context): String {
            val preferences = context.getSharedPreferences(PREFS_FILE_NAME, Context.MODE_PRIVATE)
            return preferences.getString(TEXT_TASK_ID, "" )!!
        }

        fun setTextTask(text: String, context: Context) {
            val editor = context.getSharedPreferences(PREFS_FILE_NAME, Context.MODE_PRIVATE).edit()
            editor.putString(TEXT_TASK_ID, text)
            editor.apply()
        }

    }
}