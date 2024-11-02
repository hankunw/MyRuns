package com.example.myapplication

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.text.InputType
import android.widget.Button
import android.widget.DatePicker
import android.widget.EditText
import android.widget.TextView
import android.widget.TimePicker
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.myapplication.data.database.MyRunsDB
import com.example.myapplication.data.databaseDao.MyRunsDatabaseDao
import com.example.myapplication.data.models.History
import com.example.myapplication.repository.MyRunsRepository
import java.util.Calendar

class Fill_in_info : AppCompatActivity(), TimePickerDialog.OnTimeSetListener, DatePickerDialog.OnDateSetListener{
    lateinit var info_cancel_button:Button
    lateinit var info_save_button:Button
    private lateinit var dateText:TextView
    private lateinit var timeText: TextView
    private lateinit var durationText: TextView
    private lateinit var distanceText: TextView
    private lateinit var calText: TextView
    private lateinit var hrText: TextView
    private lateinit var commentsText: TextView
    private var temp:String? = null


    var historyEntry: History? = null
    var insertYear: Int? = null
    var insertMonth: Int? = null
    var insertDay: Int? = null
    var insertHourOfDay: Int? = null
    var insertMinute: Int? = null

    private lateinit var database: MyRunsDB
    private lateinit var databaseDao: MyRunsDatabaseDao
    private lateinit var repository: MyRunsRepository
    private lateinit var viewModelFactory: MyRunsViewModelFactory
    private lateinit var historyViewModel: MyRunsViewModel
    //#2
    private lateinit var editText: EditText
    private val calendar = Calendar.getInstance()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        println("reaching here")
        setContentView(R.layout.fill_in_info)
        dateText=findViewById(R.id.start_date)
        timeText=findViewById(R.id.start_time)
        historyEntry = History()
        historyEntry?.inputType = intent.getIntExtra("inputType",0)
        historyEntry?.activityType = intent.getIntExtra("activityType",0)
        insertYear = calendar.get(Calendar.YEAR)
        insertMonth = calendar.get(Calendar.MONTH)
        insertDay = calendar.get(Calendar.DAY_OF_MONTH)
        insertHourOfDay = calendar.get(Calendar.HOUR_OF_DAY)
        insertMinute = calendar.get(Calendar.MINUTE)
        dateText.setOnClickListener(){
            val datePickerDialog = DatePickerDialog(
                this, this,
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            )
            datePickerDialog.show()
        }
        timeText.setOnClickListener(){
            //#3
            val timePickerDialog = TimePickerDialog(
                this, this,
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE), true
            )
            timePickerDialog.show()
        }
        durationText=findViewById(R.id.start_duration)
        distanceText=findViewById(R.id.start_distance)
        calText=findViewById(R.id.start_calories)
        hrText=findViewById(R.id.start_heart_rate)
        commentsText=findViewById(R.id.start_comment)

        database = MyRunsDB.getInstance(this)
        databaseDao = database.MyRunsDatabaseDao
        repository = MyRunsRepository(databaseDao)
        viewModelFactory = MyRunsViewModelFactory(repository)
        historyViewModel = ViewModelProvider(this, viewModelFactory).get(MyRunsViewModel::class.java)

        durationText.setOnClickListener(){
            val item:String="Duration"
            showDialogWindow(item, InputType.TYPE_CLASS_NUMBER,historyEntry?.duration?.toString() ?:"",
                historyEntry!!
            )
        }
        distanceText.setOnClickListener(){
            val item:String="Distance"
            showDialogWindow(item,InputType.TYPE_CLASS_NUMBER,historyEntry?.distance?.toString()?:"",historyEntry!!)

        }
        calText.setOnClickListener(){
            val item:String="Calories"
            showDialogWindow(item,InputType.TYPE_CLASS_NUMBER,historyEntry?.calories?.toString()?:"",historyEntry!!)

        }
        hrText.setOnClickListener(){
            val item:String="Heart Rate"
            showDialogWindow(item,InputType.TYPE_CLASS_NUMBER,historyEntry?.heartRate?.toString()?:"",historyEntry!!)

        }
        commentsText.setOnClickListener(){
            val item:String="Comments"
            showDialogWindow(item,InputType.TYPE_CLASS_TEXT,"",historyEntry!!)
        }

        info_save_button=findViewById(R.id.info_save_button)
        info_cancel_button=findViewById(R.id.info_cancel_button )
        info_cancel_button.setOnClickListener(){
            finish()
        }
        info_save_button.setOnClickListener(){
            val ca: Calendar = Calendar.getInstance()
            ca.set(insertYear!!, insertMonth!!, insertDay!!, insertHourOfDay!!, insertMinute!!)
            historyEntry?.dateTime = ca.timeInMillis

            historyViewModel.insert(historyEntry!!)
            finish()
        }

    }
    override fun onTimeSet(view: TimePicker, hourOfDay: Int, minute: Int) {
        //#4
        insertHourOfDay = hourOfDay
        insertMinute = minute

    }

    override fun onDateSet(view: DatePicker, year: Int, monthOfYear: Int, dayOfMonth: Int) {
        //6
        insertDay = dayOfMonth
        insertYear = year
        insertMonth = monthOfYear

    }
    private fun showDialogWindow(item:String, inputType:Int, text:String,history: History){

        editText = EditText(this)
        editText.inputType = inputType
        editText.setText(text)
        var dialog = AlertDialog.Builder(this)
            .setTitle(item)
            .setView(editText)
            .setPositiveButton("OK"){dialog, _->
                val typedValue = editText.text.toString()
                if(item == "Duration"){
                    history.duration = typedValue.toFloat()
                }
                else if(item == "Distance"){
                    history.distance = typedValue.toFloat()
                }
                else if(item == "Calories"){
                    history.calories = typedValue.toFloat()
                }
                else if(item == "Heart Rate"){
                    history.heartRate = typedValue.toInt()
                }
                dialog.dismiss()

            }
            .setNegativeButton("CANCEL",null)
        dialog.create()
        dialog.show()
    }
}