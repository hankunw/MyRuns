package com.example.myapplication

import android.os.Build
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import android.Manifest
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.os.Handler
import android.os.IBinder
import android.os.Message
import android.util.Log
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModelProvider
import com.example.myapplication.data.database.MyRunsDB
import com.example.myapplication.data.databaseDao.MyRunsDatabaseDao
import com.example.myapplication.data.models.History
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.model.PolylineOptions

import com.example.myapplication.databinding.MapFragmentBinding
import com.example.myapplication.repository.MyRunsRepository
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.math.RoundingMode
import kotlin.properties.Delegates

class MapDisplayActivity : AppCompatActivity(),OnMapReadyCallback,ServiceConnection {
    lateinit var binding: MapFragmentBinding
    lateinit var map_save_button: Button
    lateinit var map_cancel_button: Button
    private lateinit var mMap: GoogleMap
    private var mapCentered = false
    private var locationUpdateHandler: LocationUpdateHandler
    private lateinit var markerOptions: MarkerOptions
    private lateinit var polylineOptions: PolylineOptions
    private lateinit var polylines:ArrayList<Polyline>
    private lateinit var markers: ArrayList<Marker>
    private lateinit var typeText: TextView
    private lateinit var avgSpeed: TextView
    private lateinit var currentSpeed: TextView
    private lateinit var climbText: TextView
    private lateinit var calText:TextView
    private lateinit var distanceText:TextView
    private var inputType: Int = 0
    private var distance:Float = 0f
    private var startTime: Long = 0
    private var endTime: Long = 0
    private var distanceCovered: Float = 0f
    private var caloriesBurned: Double = 0.0
    private var totalLocations: Int = 0
    private var activityType: Int = 0
    private var elapsedTime: Long = 0
    private var climbNum: Float = 0f
    private var currSpeed: Double = 0.0
    private var averageSpeed: Double = 0.0
    private var mRunPointList = arrayListOf<LatLng>()
    private var instantTime: Long = 1
    private lateinit var initialLocation: Location
    private var isFirst: Boolean =true

    private var isBind = false

    lateinit var history: History

    private lateinit var database: MyRunsDB
    private lateinit var databaseDao: MyRunsDatabaseDao
    private lateinit var repository: MyRunsRepository
    private lateinit var viewModelFactory: MyRunsViewModelFactory
    private lateinit var historyViewModel: MyRunsViewModel

    init {
        locationUpdateHandler = LocationUpdateHandler()

    }
    override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
        val binder = service as LocationUpdateService.MyBinder
        binder.setLocationHandler(locationUpdateHandler)
        println("onServiceConnected() is called")
        isBind = true
    }

    override fun onServiceDisconnected(name: ComponentName?) {
        isBind = false
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = MapFragmentBinding.inflate(layoutInflater)
        setContentView(binding.root)
        history = History()
        val intent = intent
        if (intent.hasExtra("activityType")) {
            activityType = intent.getIntExtra("activityType",0)
            inputType = intent.getIntExtra("inputType",0)
        }

        typeText = findViewById(R.id.map_type)
        var  activityTypes=  resources.getStringArray(R.array.activity_type)
        typeText.text = "Type: ${activityTypes[activityType]}"
        avgSpeed = findViewById(R.id.map_avg_speed)
        currentSpeed = findViewById(R.id.map_cur_speed)
        climbText = findViewById(R.id.map_climb)
        calText = findViewById(R.id.map_calorie)
        distanceText = findViewById(R.id.map_distance)
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        history?.activityType =activityType
        history?.inputType = inputType
        history?.dateTime =System.currentTimeMillis()
        history?.heartRate= 0
        history?.calories=0f
        history?.distance=0f
        history?.duration=0f
        history?.climb = 0f

        database = MyRunsDB.getInstance(this)
        databaseDao = database.MyRunsDatabaseDao
        repository = MyRunsRepository(databaseDao)
        viewModelFactory = MyRunsViewModelFactory(repository)
        historyViewModel = ViewModelProvider(this, viewModelFactory).get(MyRunsViewModel::class.java)

        map_save_button=findViewById(R.id.map_save_button)
        map_cancel_button=findViewById(R.id.map_cancel_button)

        map_save_button.setOnClickListener(){
            if(mRunPointList!=null){
                var gson= Gson();
                var listJson = gson.toJson(mRunPointList)
                history!!.points=listJson;
            }
            history.duration = elapsedTime.toFloat()/60
            history.calories = caloriesBurned.toFloat()
            history.distance = (distanceCovered * 0.00062137).toFloat()
            history.climb = climbNum
            historyViewModel.insert(history!!)
            finish()
        }
        map_cancel_button.setOnClickListener(){
            finish()

        }
        println("onCreate is called")
        val serviceIntent = Intent(this, LocationUpdateService::class.java)
        bindService(serviceIntent, this, Context.BIND_AUTO_CREATE)

        val filter = IntentFilter("TIME_UPDATE_ACTION")
        filter.addAction("SPEED_UPDATE_ACTION")
        registerReceiver(timeUpdateReceiver, filter)


    }
    private val timeUpdateReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == "SPEED_UPDATE_ACTION") {

                elapsedTime = intent.getLongExtra("elapsedTime", 0)
                //println("debug msg: receive broadcast, $currSpeed")
                instantTime ++
                // Update UI with the speeds in your MapActivity
                updateUISpeeds()
            }
        }
    }
    private fun updateUISpeeds() {
        // Update your UI elements (TextViews, etc.) with the calculated speeds
        // For example:
        if(distance != 0f){
            currSpeed = distance / instantTime.toDouble() * 3.6
        }
        if(distanceCovered != 0f){
            averageSpeed = distanceCovered / elapsedTime.toDouble() *3.6
        }
        val sp = getSharedPreferences("setting", Context.MODE_PRIVATE)
        val unitType = sp.getInt("unit_check", 0)
        if(unitType == 0) {
            binding.mapCurSpeed.text = String.format("Current Speed: %.2f km/h", currSpeed)
            binding.mapAvgSpeed.text = String.format("Average Speed: %.2f km/h", averageSpeed)
        }
        else{
            binding.mapCurSpeed.text = String.format("Current Speed: %.2f m/h", currSpeed * 0.621371f)
            binding.mapAvgSpeed.text = String.format("Average Speed: %.2f m/h", averageSpeed * 0.621371f)
        }
    }
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        println("oMapReady is called")
        mMap.mapType = GoogleMap.MAP_TYPE_NORMAL
        markerOptions = MarkerOptions()
        polylineOptions = PolylineOptions()
        polylineOptions.color(Color.RED)
        polylines= ArrayList()
        markers = ArrayList()

        checkPermission()
    }

    private fun startLocationService(){
        val intent = Intent(this,LocationUpdateService::class.java)
        startService(intent)
    }
    private fun handleLocationUpdate(location: Location?) {
        // Handle the received location update (e.g., update map markers, draw polylines, etc.)
        // You can use the 'location' parameter to update your map UI based on the received location

        if(isFirst){
            if (location != null) {
                initialLocation = location
                isFirst = false
            }
        }
        if (location != null) {
            val latLng = LatLng(location.latitude, location.longitude)
            mRunPointList.add(latLng)

            if (!mapCentered) {
                val cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 18f)
                mMap.animateCamera(cameraUpdate)
                mapCentered = true
                startTime = System.currentTimeMillis()
            }

            for (i in markers.indices) {
                if (i != 0)
                    markers[i].remove()
            }

            markerOptions.position(latLng)
            val marker = mMap.addMarker(markerOptions)
            if (marker != null) {
                markers.add(marker)
            }

            polylineOptions.add(latLng)
            val polyline = mMap.addPolyline(polylineOptions)
            polylines.add(polyline)



//            val sp = getSharedPreferences("setting", Context.MODE_PRIVATE)
//            val unitType = sp.getInt("unit_check", 0)
            totalLocations++

            if (totalLocations > 1) {
                val previousLocation = Location("previous")
                previousLocation.latitude = polylines[totalLocations - 2].points.last().latitude
                previousLocation.longitude = polylines[totalLocations - 2].points.last().longitude
                distance = location.distanceTo(previousLocation)
                distanceCovered += distance
            }
            history.distance = distanceCovered
            history.duration = (elapsedTime/60).toFloat()

            climbNum =((location!!.altitude-initialLocation.altitude) * 0.00062137).toFloat()

            instantTime = 1
//            val currSpeed = (distance / elapsedTime).toDouble()
//            // Calculate average speed
//            val avgSpeed = if (elapsedTime > 0) {
//                (distanceCovered / elapsedTime).toDouble()
//            } else {
//                0.0
//            }
//
            // Update UI with calculated values
            updateUI(distanceCovered, climbNum)
        }
    }

    private fun updateUI(
        distanceCovered: Float,
        climb: Float
    ) {
        val sp = getSharedPreferences("setting", Context.MODE_PRIVATE)
        val unitType = sp.getInt("unit_check", 0)
        // Update your UI elements (TextViews, etc.) with the calculated values
        if(unitType == 0) {
            binding.mapDistance.setText(String.format("Distance: %.2f Kilometers", distanceCovered * 0.001))
            binding.mapClimb.setText(String.format("Climb: %.3f Kilometers", climb * 0.001))
        }
        else{
            binding.mapDistance.setText(String.format("Distance: %.3f Miles", distanceCovered * 0.001 * 0.621371f))
            binding.mapClimb.setText(String.format("Climb: %.3f Miles", climb * 0.001 * 0.621371f))
        }

        // Calculate calories burned (you may use a more accurate formula based on your needs)
        caloriesBurned = (distanceCovered *1.2 * 0.1)
        binding.mapCalorie.setText("Calories Burned: ${caloriesBurned.toInt()}")
    }
    private inner class LocationUpdateHandler : Handler() {
        override fun handleMessage(msg: Message) {
            when (msg.what) {
                LocationUpdateService.MSG_INT_VALUE -> {
                    val bundle = msg.data
                    //println("it's about to go to handleLocationUpdate()")
                    val location = bundle.getParcelable<Location>("location")
                    if (location != null) {
                        handleLocationUpdate(location)
                    } else {
                        Log.w(TAG, "Received message without location data")
                    }
                }
                // Handle other message types...
                else -> Log.w(TAG,"Unknown message type: ${msg.what}")
            }
        }
    }

    fun checkPermission(){
        if(Build.VERSION.SDK_INT < 23) return
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),0)
        }
        else{
            startLocationService()
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        // Stop the LocationUpdateService when the activity is destroyed
        if(isBind){
            unbindService(this)
            isBind=false
        }
        val intent = Intent(this, LocationUpdateService::class.java)
        unregisterReceiver(timeUpdateReceiver)
        stopService(intent)
    }
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 0) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) startLocationService()
        }
    }
}