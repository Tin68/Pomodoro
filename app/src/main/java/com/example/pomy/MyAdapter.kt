package com.example.pomy

import android.annotation.SuppressLint
import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import kotlinx.android.synthetic.main.custom_row.view.*

class MyAdapter (private val context: Activity,
                 private val arrayList: ArrayList<Tasks>)  : ArrayAdapter<Tasks> (context, R.layout.custom_row, arrayList) {


    @SuppressLint("ViewHolder", "InflateParams")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {

        val inflater : LayoutInflater = LayoutInflater.from(context)
        val view = inflater.inflate(R.layout.custom_row, null)

        view.pomstar.text = arrayList[position].timeStar
        view.pomend.text = arrayList[position].timeEnd
        view.pomtask.text = arrayList[position].task
        view.pomtotal.text = arrayList[position].total

        return view
    }
}