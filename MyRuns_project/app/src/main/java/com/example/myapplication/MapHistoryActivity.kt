package com.example.myapplication

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.myapplication.data.database.MyRunsDB
import com.example.myapplication.data.databaseDao.MyRunsDatabaseDao
import com.example.myapplication.data.models.History
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.example.myapplication.databinding.ActivityHistoryMapBinding
import com.example.myapplication.repository.MyRunsRepository
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.math.RoundingMode

class MapHistoryActivity: AppCompatActivity() , OnMapReadyCallback {

    private lateinit var binding: ActivityHistoryMapBinding
    private lateinit var map: GoogleMap
    private lateinit var history: History
    private val milesUnit = 0.00062
    private lateinit var database: MyRunsDB
    private lateinit var databaseDao: MyRunsDatabaseDao
    private lateinit var repository: MyRunsRepository
    private lateinit var viewModelFactory: MyRunsViewModelFactory
    private lateinit var historyViewModel: MyRunsViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHistoryMapBinding.inflate(layoutInflater)
        setContentView(binding.root)
        database = MyRunsDB.getInstance(this)
        databaseDao = database.MyRunsDatabaseDao
        repository = MyRunsRepository(databaseDao)
        viewModelFactory = MyRunsViewModelFactory(repository)
        historyViewModel = ViewModelProvider(this, viewModelFactory).get(MyRunsViewModel::class.java)
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        history = intent.getSerializableExtra("exercise") as History
        val activityTypes = resources.getStringArray(R.array.activity_type)

        binding.mapType.text = "Type: ${activityTypes[history.activityType!!]}"
        val sp = getSharedPreferences("setting", Context.MODE_PRIVATE)
        val unitType = sp.getInt("unit_check", 0)

        val distance = history.distance ?: 0f
        val duration = history.duration ?: 0f
        val climb = history.climb?:0f
        println("debug msg: ${distance}, ${duration}, ${climb}")
        if (unitType == 0) {
            binding.mapClimb.text = "Climb: ${climb / 0.62137f} Kilometers"
            binding.mapDistance.text = "Distance: ${distance / 0.62137f} Kilometers"
        } else {
            binding.mapClimb.text = "Climb: $climb Miles"
            binding.mapDistance.text = "Distance: $distance Miles"
        }

        if (distance == 0f || duration == 0f) {
            binding.mapAvgSpeed.text = "Avg Speed: 0 m/h"
        } else {
            val avgSpeedkmh = ((distance / 0.62137f)/ (duration/60)).toBigDecimal().setScale(2, RoundingMode.HALF_UP).toDouble()

            if(unitType == 0) {
                binding.mapAvgSpeed.text = if (avgSpeedkmh == 0.0) {
                    "Avg Speed: 0 km/h"
                } else {
                    String.format("Average Speed: %.2f km/h", avgSpeedkmh)
                }
            }
            else{
                binding.mapAvgSpeed.text = if (avgSpeedkmh == 0.0) {
                    "Avg Speed: 0 m/h"
                } else {
                    String.format("Average Speed: %.2f m/h", avgSpeedkmh * 0.62137f)
                }
            }
        }
        //println("calocires goes here: ${(distance*1609 *1.2 * 0.1).toInt()}")
        binding.mapCalorie.text = "Calorie: ${(distance*1609 *1.2 * 0.1).toInt()}"
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        if (history.points?.isNotEmpty() == true) {
            val gson = Gson()
            val type = object : TypeToken<ArrayList<LatLng>>() {}.type
            val list = gson.fromJson<ArrayList<LatLng>>(history.points, type)

            if (list.isNotEmpty()) {
                map.addMarker(
                    MarkerOptions().position(list[0]).title("Run start").icon(
                        BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)
                    )
                )

                val polyline = map.addPolyline(PolylineOptions().addAll(list))
                polyline.color = Color.GRAY
                polyline.endCap = RoundCap()
                polyline.jointType = JointType.ROUND
                polyline.width = 8f

                map.addMarker(
                    MarkerOptions().position(list.last()).title("Run current").icon(
                        BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)
                    )
                )

                map.animateCamera(
                    CameraUpdateFactory.newLatLngZoom(
                        list[0], 17f
                    )
                )
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_delete, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_delete -> {
                historyViewModel.deleteHistory(history.id)
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}