package com.example.pomy

import android.content.Context
import android.database.Cursor
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.pomy.databinding.ActivityListBinding
import java.io.IOException
import java.sql.SQLException

class ListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityListBinding
    private lateinit var taskArrayList: ArrayList<Tasks>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityListBinding.inflate(layoutInflater)
        setContentView(binding.root)
        //setContentView(R.layout.activity_list)

        taskArrayList = ArrayList()
        //añadir de la base de datos
        val db = TaskDB (this)
        /*try {
            db.createDB()
        } catch (ioe : IOException) {
            throw Error ("Database not created ....")
        }
        try {
            db.openDB()
        } catch (sqlE : SQLException) {
            throw Error ("Database not opened ....")
        }*/
        var contin : Boolean = false
        if (db.openDB()){
            contin = true
        } else if (db.createDB()){
            contin = true
        } else {
            Toast.makeText(this, "Error al abrir la base de datos", Toast.LENGTH_SHORT).show()
        }

        if (contin) {

            val db1 = this.openOrCreateDatabase(TaskDB.DB_NAME + ".db", Context.MODE_PRIVATE, null)

            val c: Cursor = db1.rawQuery(
                "SELECT * FROM task_table ORDER BY taskId DESC",
                null
            )

            c.moveToFirst()

            while (!c.isAfterLast()) {
                taskArrayList.add(Tasks(c.getString(1), c.getString(2), c.getString(3), c.getString(4)))
                c.moveToNext()
            }
            c.close()
            db1.close()

            binding.lvTask.adapter = MyAdapter(this, taskArrayList)
        }
    }
}