package com.example.myapplication

import android.content.Context
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.myapplication.data.database.MyRunsDB
import com.example.myapplication.data.databaseDao.MyRunsDatabaseDao
import com.example.myapplication.data.models.History
import com.example.myapplication.repository.MyRunsRepository
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HistoryListInfo: AppCompatActivity()  {

    private lateinit var history: History
    lateinit var inputTypeTextView:TextView
    lateinit var activityTypeTextView:TextView
    lateinit var dateTimeTextView:TextView
    lateinit var durationTextView:TextView
    lateinit var distanceTextView:TextView
    lateinit var caloriesTextView:TextView
    lateinit var heartRateTextView:TextView
    lateinit var activityTypes:Array<String>
    lateinit var inputTypes:Array<String>
    private var date_format = SimpleDateFormat("HH:mm:ss MM dd yyyy", Locale.ENGLISH)
    private var unit = 0

    private lateinit var database: MyRunsDB
    private lateinit var databaseDao: MyRunsDatabaseDao
    private lateinit var repository: MyRunsRepository
    private lateinit var viewModelFactory: MyRunsViewModelFactory
    private lateinit var historyViewModel: MyRunsViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history_info)
        var sp = getSharedPreferences("setting", Context.MODE_PRIVATE)
        history = intent.getSerializableExtra("history") as History
        unit = sp.getInt("unit_check",-1)
        println(unit)
        activityTypes = this.resources.getStringArray(R.array.activity_type)
        inputTypes = this.resources.getStringArray(R.array.input_type)

        inputTypeTextView = findViewById(R.id.info_inputType)
        activityTypeTextView = findViewById(R.id.info_activityType)
        dateTimeTextView = findViewById(R.id.info_dateTime)
        durationTextView = findViewById(R.id.info_duration)
        distanceTextView = findViewById(R.id.info_distance)
        caloriesTextView = findViewById(R.id.info_calories)
        heartRateTextView = findViewById(R.id.info_heartRate)
        database = MyRunsDB.getInstance(this)
        databaseDao = database.MyRunsDatabaseDao
        repository = MyRunsRepository(databaseDao)
        viewModelFactory = MyRunsViewModelFactory(repository)
        historyViewModel = ViewModelProvider(this, viewModelFactory).get(MyRunsViewModel::class.java)


        inputTypeTextView.text = inputTypes[history.inputType].toString()
        activityTypeTextView.text = activityTypes[history.activityType].toString()
        dateTimeTextView.text = date_format.format(Date(history.dateTime)).toString()
        var minis = ""
        var secs = ""
        val durationInMinutes = history.duration?.toInt() ?: 0
        val minutes = durationInMinutes
        val seconds = (history.duration?.rem(1))?.times(60)
        if(minutes>0){
            minis=" ${minutes}mins"
        }
        else minis = "0mins"
        secs=" ${seconds}secs"
        durationTextView.text = "${minis}${secs}"

        caloriesTextView.text = history.calories.toString()+" "+"cals"
        heartRateTextView.text = history.heartRate.toString()+" "+"bpm"
        if(unit == 0){
            distanceTextView.text = (history.distance/ 0.62137f).toString() +" "+ "Kilometers"
        }
        else{
            distanceTextView.text = history.distance.toString() +" "+ "Miles"
        }


    }
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_delete, menu)
        return true
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_delete -> {
                historyViewModel.deleteHistory(id = history.id)
                finish()
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

}