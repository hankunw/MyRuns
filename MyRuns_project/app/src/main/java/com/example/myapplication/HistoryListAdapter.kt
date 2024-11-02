package com.example.myapplication

import android.content.Context
import android.content.SharedPreferences
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.example.myapplication.data.models.History

class HistoryListAdapter(private val context: Context, private var historyList: List<History>) : BaseAdapter(){
    var sp: SharedPreferences = context.getSharedPreferences("setting",Context.MODE_PRIVATE)
    var activityTypes:Array<String> = context.resources.getStringArray(R.array.activity_type)
    var inputTypes:Array<String> = context.resources.getStringArray(R.array.input_type)
    private var date_format = SimpleDateFormat("HH:mm:ss MM dd yyyy", Locale.ENGLISH)
    private var unit = 0
    fun updateUnit(newUnit: Int) {
        unit = newUnit
        notifyDataSetChanged() // Notify the adapter that the data has changed, causing it to refresh the views.
    }
    override fun getItem(position: Int): History {
        return historyList.get(position)
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getCount(): Int {
        return historyList.size
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view: View = View.inflate(context, R.layout.history_layout_adapter,null)
        val first_line:TextView = view.findViewById(R.id.first_line)
        val second_line: TextView = view.findViewById(R.id.second_line)

        var unit = sp.getInt("unit_check", 0)
        var history = getItem(position)
        var formatedDatetime = date_format.format(Date(history.dateTime))
        var inputType = inputTypes[history.inputType]
        var activityType = activityTypes[history.activityType]
        first_line.text = "${inputType}:${activityType},$formatedDatetime"
        var distance = history.distance
        var distanceText = ""
        if(unit == 0){

            distanceText = "${distance/ 0.62137f} Kilometers,"
        }
        else{
            distanceText = "$distance Miles,"
        }
        var minis = ""
        var secs = ""
        val durationInMinutes = history.duration?.toInt() ?: 0 // Assuming exercise.duration is an Integer representing minutes

// Calculate minutes and seconds
        val minutes = durationInMinutes
        val seconds = (history.duration?.rem(1))?.times(60)
        if(minutes>0){
            minis=" ${minutes}mins"
        }
        else minis = "0mins"
        secs=" ${seconds}secs"
        second_line.text = "${distanceText}${minis}${secs}"

        return view
    }
    fun replace(newHistoryList: List<History>){
        historyList = newHistoryList
    }
}