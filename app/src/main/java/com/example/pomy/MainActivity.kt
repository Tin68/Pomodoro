package com.example.pomy

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.graphics.drawable.Drawable
import android.media.MediaPlayer
import android.os.Bundle
import android.os.CountDownTimer
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.text.Editable
import android.text.TextWatcher
import android.text.format.DateFormat
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import com.example.pomy.databinding.ActivityMainBinding
import com.example.pomy.util.NotificationUtil
import com.example.pomy.util.PrefUtil
import kotlinx.android.synthetic.main.fragment_first.*
import java.io.IOException
import java.sql.SQLException
import java.util.*


class MainActivity : AppCompatActivity() {

    companion object {
        fun setAlarm(context: Context, nowSeconds: Long, secondsRemaining: Long): Long {
            val wakeUpTime = (nowSeconds + secondsRemaining) * 1000
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = Intent(context, TimerExpiredReceiver::class.java)
            val dependingIntent = PendingIntent.getBroadcast(context, 0, intent, 0)
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, wakeUpTime, dependingIntent)
            PrefUtil.setAlarmSetTime(nowSeconds, context)
            return wakeUpTime
        }
        fun removeAlarm(context: Context) {
            val intent = Intent(context, TimerExpiredReceiver::class.java)
            val dependingIntent = PendingIntent.getBroadcast(context, 0, intent, 0)
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarmManager.cancel(dependingIntent)
            PrefUtil.setAlarmSetTime(0, context)
        }

        val nowSeconds: Long
            get() = Calendar.getInstance().timeInMillis / 1000
    }

    enum class TimerState{
        Stopped, Paused, Running, Finalized
    }

    private lateinit var timer: CountDownTimer
    private var timerLengthSeconds: Long = 0
    private var timerState = TimerState.Stopped
    private var optionState = AppConstants.DIREC_POMODORO_IMPAR
    private var colorPomodoro  = AppConstants.COLOR_POMODORO_IMPAR
    private var secondsRemaining = 0L
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    private var iteration = 1
    private var iniTimeTask : Long? = null
    private var editTextTask: EditText? = null
    private var longPause = 15
    private var taskList = ArrayList<String>()



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        //setContentView(R.layout.activity_main)
        //val toolbar: Toolbar = findViewById(R.id.toolbar)
        val toolbar = binding.toolbar
        setSupportActionBar(binding.toolbar)
        //setSupportActionBar(toolbar)
        supportActionBar?.setIcon(R.drawable.ic_timer)
        supportActionBar?.title = "   Pomy"
        val drawable : Drawable? = ContextCompat.getDrawable(getApplicationContext(),R.drawable.ic_settings)
        toolbar.overflowIcon = drawable

        //val navHostFragment = supportFragmentManager.findFragmentById(
        //    R.id.nav_host_fragment_content_main) as NavHostFragment
        //val navController = navHostFragment.navController
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        appBarConfiguration = AppBarConfiguration(navController.graph)
        //setupActionBarWithNavController(navController, appBarConfiguration)
        setupActionBarWithNavController(navController, appBarConfiguration)

        activateBD()
    }


    private fun textWatcher () : TextWatcher {
        return object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // This space intentionally left blank
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val taskInput : String = editTextTask!!.text.toString().trim()
                btn_play.isEnabled = taskInput.isNotEmpty()
                btn_hist.isEnabled = taskInput.isEmpty()
            }

            override fun afterTextChanged(s: Editable?) {
                // This space intentionally left blank
            }
        }
    }


    override fun onStart() {
        super.onStart()
        /*
        timerState = PrefUtil.getTimerState(this)
        if (timerState == TimerState.Stopped) {
            editTextTask = findViewById(R.id.taskEditText)
            editTextTask!!.addTextChangedListener(textWatcher())
            updateListMain ()
        } else {
            secondsRemaining = PrefUtil.getSecondsRemaining(this)
            updateCountdownUI()
        }
*/
        editTextTask = findViewById(R.id.taskEditText)
        editTextTask!!.addTextChangedListener(textWatcher())
        updateListMain ()
    }

    override fun onResume() {
        super.onResume()
        initTimer()
        removeAlarm(this)
        NotificationUtil.hideTimerNotification(this)
    }

    override fun onPause() {
        super.onPause()
        if (timerState == TimerState.Running) {
            timer.cancel()
            val wakeUpTime = setAlarm(this, nowSeconds, secondsRemaining)
            val asc = if (iteration % 2 == 0) "ASC" else "DESC"
            NotificationUtil.showTimerRunning(this, wakeUpTime, asc)
        } else if (timerState == TimerState.Paused) {
            NotificationUtil.showTimerPaused(this)
        }

        //PrefUtil.setIniTimeTask(iniTimeTask!!, this)
        //PrefUtil.setTextTask(taskEditText.text.toString(), this)
        PrefUtil.setPreviousTimerLengthSeconds(timerLengthSeconds, this)
        PrefUtil.setSecondsRemaining(secondsRemaining, this)
        PrefUtil.setTimerState(timerState, this)
    }

    private fun activateBD() {
        val db = TaskDB (this)

        //pruebo mejor asi
        var contin : Boolean = false
        if (db.openDB()){
            contin = true
        } else if (db.createDB()){
            contin = true
        } else {
            Toast.makeText(this, "Error al abrir la base de datos", Toast.LENGTH_SHORT).show()
        }

        if (contin) {
            val db1: SQLiteDatabase =
                openOrCreateDatabase("Task.db", MODE_ENABLE_WRITE_AHEAD_LOGGING, null)
            val c: Cursor = db1.rawQuery(
                "SELECT * FROM task_table ORDER BY taskId DESC",
                null
            )

            c.moveToFirst()

            var temp = ""
            while (!c.isAfterLast()) {
                val s1: String = c.getString(1)
                val s2: String = c.getString(2)
                val s3: String = c.getString(3)
                val s4: String = c.getString(4)
                temp = "$temp\n Id:$s1\tType:$s2\tBal:$s3\tTal:$s4"
                c.moveToNext()
            }
            c.close()
            db1.close() //ultimo en poner
        }
    }

    private fun updateListMain () {
        val db1: SQLiteDatabase = openOrCreateDatabase("Task.db", MODE_ENABLE_WRITE_AHEAD_LOGGING, null)
        val c: Cursor = db1.rawQuery("SELECT * FROM task_table ORDER BY taskId DESC", null)

        c.moveToFirst()

        var cont = 0
        while (!c.isAfterLast() && cont < 2) {
            if (cont==0){
                pom1star.text = c.getString(1)
                pom1end.text = c.getString(2)
                pom1task.text = c.getString(3)
                pom1total.text = c.getString(4)
                cont++
                c.moveToNext()
            }else{
                pom2star.text = c.getString(1)
                pom2end.text = c.getString(2)
                pom2task.text = c.getString(3)
                pom2total.text = c.getString(4)
                cont++
            }
        }
        //c.close()
        db1.close() //ultimo en poner

    }

    private fun insertTimeToDatabase (dat : Int){
        //val db1 = SQLiteDatabase.openDatabase(TaskDB.DB_NAME + ".db", null, SQLiteDatabase.OPEN_READWRITE)
        val db1: SQLiteDatabase = openOrCreateDatabase(TaskDB.DB_NAME + ".db", MODE_ENABLE_WRITE_AHEAD_LOGGING, null)
        val sql = "SELECT * FROM long_break"

        val c: Cursor = db1.rawQuery(sql, null)

        try{
            if (c.moveToNext()){
                val contentValues = ContentValues()
                contentValues.put("timeId", 0)
                contentValues.put("timeBreak", dat)
                db1.update("long_break", contentValues, "timeId = 0", null)
            }else{
                val contentValues = ContentValues()
                contentValues.put("timeId", 0)
                contentValues.put("timeBreak", dat)
                db1.insert("long_break", null, contentValues)
            }
        }catch (e: Exception){
            Log.d("Error", e.toString())
        }
        c.close()
        db1.close()
    }

    fun insertDataToDatabase() {
        /*
        var nameTask : String = ""
        if (iniTimeTask == null || iniTimeTask == 0L  ){
            iniTimeTask = PrefUtil.getIniTimeTask(this)
        }
        nameTask = if (taskEditText?.text.toString() == "") {
            PrefUtil.getTextTask(this)
        } else {
            taskEditText?.text.toString()
        }
        */

        val ini= DateFormat.format("dd/MM/yyyy HH:mm", iniTimeTask?.let { Date(it) }).toString()
        val now = System.currentTimeMillis()
        val end= DateFormat.format("dd/MM/yyyy HH:mm", Date(now)).toString()
        val task= taskEditText?.text.toString()

        val totalSeconds= (now - iniTimeTask!!)/1000
        //val db1: SQLiteDatabase =
        //    openOrCreateDatabase("Task.db", MODE_ENABLE_WRITE_AHEAD_LOGGING, null)
        val db1: SQLiteDatabase = openOrCreateDatabase(TaskDB.DB_NAME + ".db", MODE_ENABLE_WRITE_AHEAD_LOGGING, null)
        val sql = "SELECT max(taskId) as taskId FROM task_table"

        val c: Cursor = db1.rawQuery(sql, null)
        var max : Int = 0
        try{
            if (c.moveToNext()){
                max= c.getInt(0)
            }
        }catch (e: Exception){
            Log.d("Error", e.toString())
        }

        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        val totalText = String.format("%02d:%02d", hours, minutes)

        try {
            val contentValues = ContentValues()
            contentValues.put("taskId", max + 1)
            contentValues.put("timeStart", ini)
            contentValues.put("timeEnd", end)
            contentValues.put("task", task)
            contentValues.put("totalTime", totalText)
            db1.insert("task_table", null, contentValues)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        c.close()
        db1.close()
    }

    fun btnPlayClick(view: View) {
        taskEditText.setEnabled(false) //desactiva el texto
        iniTimeTask= System.currentTimeMillis()
        startTimer()
        timerState =  TimerState.Running
        updateButtons()
    }

    fun btnPauseClick(view: View) {
        timer.cancel()
        timerState = TimerState.Paused
        updateButtons()
    }

    fun btnStopClick(view: View) {
        insertDataToDatabase()
        updateListMain ()
        taskEditText.setEnabled(true)
        taskEditText.setText("")
        timer.cancel()
        PrefUtil.setTimerState(TimerState.Stopped, view.context)
        onTimerStopped()
    }

    fun btnNextClick(view: View) {
        timer.cancel()
        onTimerFinished()
        updateButtons()
    }

    fun btnHistClick(view: View) {
        val miIntent = Intent(this@MainActivity, ListActivity::class.java)
        startActivity(miIntent)
        //updateButtons()
    }


    private fun initTimer() {
        timerState = PrefUtil.getTimerState(this)
        if (timerState == TimerState.Stopped) {
            setNewTimerLength()
        } else {
            setPreviousTimerLength()
        }
        secondsRemaining =
            if (timerState == TimerState.Running || timerState == TimerState.Paused) {
                PrefUtil.getSecondsRemaining(this)
            } else {
                timerLengthSeconds
            }
        val alarmSetTime = PrefUtil.getAlarmSetTime(this)
        if (alarmSetTime > 0) {
            secondsRemaining -= nowSeconds - alarmSetTime
        }
        if (secondsRemaining <= 0) {
            onTimerFinished()
        } else if (timerState == TimerState.Running) {
            startTimer()
        }

        //resume where we left off
        if (timerState == TimerState.Running) {
            startTimer()
        }
        updateButtons()
        updateCountdownUI()
    }

    private fun onTimerStopped() {
        timerState = TimerState.Stopped
        updateListMain()
        setNewTimerLength()
        iteration = 1
        PrefUtil.setIteration(iteration,this)
        updateCountdownUI()
        //progressbar_countdown.progress = 0

        PrefUtil.setSecondsRemaining(timerLengthSeconds, this)
        secondsRemaining = timerLengthSeconds

        updateButtons()
        updateCountdownUI()
    }

    private fun onTimerFinished() {
        //Suena sonido y Avisa con un Toast del cambio de actividad
        if (iteration % 2 == 0) {
            val mt: MediaPlayer = MediaPlayer.create(this, R.raw.sirena)
            mt.start()
            Toast.makeText(this, R.string.dialog_message_work, Toast.LENGTH_LONG).show()
        } else {
            val mp: MediaPlayer = MediaPlayer.create(this, R.raw.campanillas)
            mp.start()
            Toast.makeText(this, R.string.dialog_message_pause, Toast.LENGTH_LONG).show()
        }
        timerState = TimerState.Finalized
        //solo para ahora despues quitarlo
        //setNewTimerLength()
        //pb_circular_determinative.progress = 0

        //PrefUtil.setSecondsRemaining(timerLengthSeconds, this)
        //secondsRemaining = timerLengthSeconds

        iteration ++
        PrefUtil.setIteration(iteration,this)
        when {
            iteration % 8 == 0 -> {
                secondsRemaining = AppConstants.LONG_BREAK_TIME.toLong() * 60L
                colorPomodoro = AppConstants.COLOR_POMODORO_PAR
                optionState = AppConstants.DIREC_POMODORO_PAR
            }
            iteration % 2 == 0 -> {
                secondsRemaining =  AppConstants.SHORT_BREAK_TIME.toLong() * 60L
                colorPomodoro = AppConstants.COLOR_POMODORO_PAR
                optionState = AppConstants.DIREC_POMODORO_PAR
            }
            else -> {
                secondsRemaining = AppConstants.POMODORO_TIME.toLong() * 60L
                colorPomodoro = AppConstants.COLOR_POMODORO_IMPAR
                optionState = AppConstants.DIREC_POMODORO_IMPAR
            }
        }
        timerLengthSeconds = secondsRemaining
        //PrefUtil.setTimerLength( this, timerLengthSeconds.toInt())
        textview_countdown.setTextColor(colorPomodoro.toInt())
        pb_circular_determinative.max = secondsRemaining.toInt()
        PrefUtil.setProgressBarMax(secondsRemaining.toInt(),this)
        startTimer()

        //updateButtons()
        //updateCountdownUI()
    }
    private fun startTimer() {
        timerState = TimerState.Running
        timer = object : CountDownTimer(secondsRemaining * 1000, 1000) {
            override fun onFinish() = onTimerFinished()
            override fun onTick(millisUntilFinished: Long) {
                secondsRemaining = millisUntilFinished / 1000
                updateCountdownUI()
            }
        }.start()
    }

    private fun setNewTimerLength() {
        val lengthInMinutes = PrefUtil.getTimerLength(this)
        timerLengthSeconds = (lengthInMinutes * 60L)
        pb_circular_determinative.max = timerLengthSeconds.toInt()
        //PrefUtil.setProgressBarMax(secondsRemaining.toInt(),this)
    }

    private fun setPreviousTimerLength() {
        timerLengthSeconds = PrefUtil.getPreviousTimerLengthSeconds(this)
        pb_circular_determinative.max = timerLengthSeconds.toInt()
        //PrefUtil.setProgressBarMax(secondsRemaining.toInt(),this)
    }

    private fun updateCountdownUI() {
        val minutesUntilFinished = secondsRemaining / 60
        val secondsInMinuteUntilFinished = secondsRemaining - minutesUntilFinished * 60
        //val secondsStr = secondsInMinuteUntilFinished.toString()
        val format = String.format("%02d:%02d", minutesUntilFinished, secondsInMinuteUntilFinished)

        if (iteration % 2 == 0){
            pb_circular_determinative.secondaryProgress = (timerLengthSeconds - secondsRemaining).toInt()
            pb_circular_determinative.progress = 0
            colorPomodoro = AppConstants.COLOR_POMODORO_PAR
        } else {
            pb_circular_determinative.secondaryProgress = 0
            pb_circular_determinative.progress = secondsRemaining.toInt()
            colorPomodoro = AppConstants.COLOR_POMODORO_IMPAR
        }
        textview_countdown.setTextColor(colorPomodoro.toInt())
        textview_countdown.setText(format)
    }

    private fun updateButtons() {
        when (timerState) {
            TimerState.Running -> {
                btn_play.isEnabled = false
                btn_pause.isEnabled = true
                btn_stop.isEnabled = true
                btn_next.isEnabled = true
                btn_hist.isEnabled= false
            }
            TimerState.Stopped -> {
                btn_play.isEnabled = false
                btn_pause.isEnabled = false
                btn_stop.isEnabled = false
                btn_next.isEnabled = false
                btn_hist.isEnabled= true
            }
            TimerState.Paused -> {
                btn_play.isEnabled = true
                btn_pause.isEnabled = false
                btn_stop.isEnabled = true
                btn_next.isEnabled = true
                btn_hist.isEnabled= false
            }
            else -> { // Finalizado
                //btn_play.isEnabled = true
                //btn_pause.isEnabled = false
                //btn_stop.isEnabled = false
                //btn_next.isEnabled = false
                //btn_hist.isEnabled= true
            }
        }
    }

    fun ViewDataBase() {
        val db1: SQLiteDatabase = openOrCreateDatabase("Task.db", MODE_ENABLE_WRITE_AHEAD_LOGGING, null)
        //val db: SQLiteDatabase = this.getReadableDatabase()
        val res = db1.rawQuery("select * from task_table", null)
        res.moveToFirst()
        while (res.isAfterLast == false) {
            taskList.add(res.getString(1))
            Log.i(
                "DB values.........",
                res.getString(0) + "\t" + res.getString(1) + "\t" + res.getString(2)
            )
            //var adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, taskList)

            res.moveToNext()
        }
        res.close()
        db1.close()
    }

    // maneja el menu superior
    override fun onPrepareOptionsMenu (menu: Menu): Boolean {
        super.onPrepareOptionsMenu(menu)
        longPause = if (PrefUtil.getTimerBreakLength(this) != 0) {
            PrefUtil.getTimerBreakLength(this)
        } else {
            AppConstants.LONG_BREAK_TIME
        }
        if (longPause == 15) {
            menu.findItem(R.id.pause_short).isChecked = true
        } else {
            menu.findItem(R.id.pause_long).isChecked = true
        }
        return true
    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            //R.id.list_item -> true
            R.id.pause_short -> {
                AppConstants.LONG_BREAK_TIME = 15
                PrefUtil.setTimerBreakLength(this, AppConstants.LONG_BREAK_TIME)
                insertTimeToDatabase (AppConstants.LONG_BREAK_TIME)
                true
            }
            R.id.pause_long-> {
                AppConstants.LONG_BREAK_TIME = 30
                PrefUtil.setTimerBreakLength(this, AppConstants.LONG_BREAK_TIME)
                insertTimeToDatabase (AppConstants.LONG_BREAK_TIME)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onSupportNavigateUp(): Boolean {

        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration)
                || super.onSupportNavigateUp()
    }


    @SuppressLint("ResourceType")
    fun onListItemClick(item: MenuItem) {

        //Navigation.findNavController(navController).navigate(R.id.SecondFragment)
        // One of the group items (using the onClick attribute) was clicked
        // The item parameter passed here indicates which item it is
        // All other menu item clicks are handled by <code><a href="/reference/android/app/Activity.html#onOptionsItemSelected(android.view.MenuItem)">onOptionsItemSelected()</a></code>
    }


}