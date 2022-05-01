package com.example.pomy

import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.example.pomy.databinding.FragmentSecondBinding
import java.io.IOException
import java.sql.SQLException

/**
 * A simple [Fragment] subclass as the second destination in the navigation.
 */
class SecondFragment : Fragment() {

    private var _binding: FragmentSecondBinding? = null
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private lateinit var taskArrayList: ArrayList<Tasks>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSecondBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //binding = FragmentSecondBinding.inflate(layoutInflater)
        //binding!!.root

        taskArrayList = ArrayList()
        //añadir de la base de datos
        val db = TaskDB (context!!)

        try {
            db.createDB()
        } catch (ioe : IOException) {
            throw Error ("Database not created ....")
        }
        try {
            db.openDB()
        } catch (sqlE : SQLException) {
            throw Error ("Database not opened ....")
        }

            val db1 = context!!.openOrCreateDatabase(TaskDB.DB_NAME + ".db", Context.MODE_PRIVATE, null)

            val c: Cursor = db1.rawQuery(
                "SELECT * FROM task_table ORDER BY taskId DESC",
                null
            )

            c.moveToFirst()

            while (!c.isAfterLast()) {
                taskArrayList.add(Tasks(c.getString(1), c.getString(2), c.getString(3), c.getString(4)))
                c.moveToNext()
            }
            db1.close()
            binding.lvTask.adapter = activity?.let { MyAdapter(it, taskArrayList) }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}