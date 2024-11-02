package com.example.myapplication

import android.Manifest
import android.app.*
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Binder
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.example.myapplication.R
import java.util.Timer
import java.util.TimerTask

class LocationUpdateService : Service(), LocationListener {
    private lateinit var  binder: IBinder
    private lateinit var locationManager: LocationManager
    private var mapCentered = false
    private var CHANNEL_ID = "notification channel"
    private var locationHandler: Handler? = null
    private lateinit var notificationManager:NotificationManager
    private val NOTIFICATION_ID = 777
    private lateinit var timer: Timer
    private var elapsedTimeInSeconds: Long = 0
    private var totalSpeed: Double = 0.0
    private var totalLocations: Int = 0
    private var lastLocation: Location? = null
    private var currentSpeed:Double = 0.0
    private var averageSpeed: Double = 0.0
    companion object{
        val MSG_LOCATION_UPDATE = 1
        val MSG_INT_VALUE = 0

    }

    override fun onCreate() {
        super.onCreate()
        println("debug msg: onCreate() is called")

        initializeLocationManager()
        timer = Timer()
        timer.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                // Increment elapsed time every second
                //println("debug msg: time")
                elapsedTimeInSeconds++
                calculateAndSendSpeeds()
            }
        }, 0, 1000)
        binder = MyBinder()

    }
    override fun onBind(intent: Intent?): IBinder? {
        println("Service onBind()")
        return binder
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        showNotification()
        return START_NOT_STICKY
    }
    override fun onLocationChanged(location: Location) {
        println("onLocationCHanged is called")
        println("${location.longitude} and ${location.latitude}")
        // Create a Message and set its data and type
        if(locationHandler != null) {
            val bundle = Bundle().apply {
                putParcelable("location", location) // Add location data to the bundle
            }
            val message = locationHandler!!.obtainMessage()

                message.data = bundle
                message.what = MSG_INT_VALUE
            // Send the message to the handler
            locationHandler!!.sendMessage(message)
        }

    }
    private fun calculateAndSendSpeeds() {
        totalLocations++

        // Broadcast speeds and time to MapActivity
        val speedIntent = Intent("SPEED_UPDATE_ACTION")
        speedIntent.putExtra("elapsedTime", elapsedTimeInSeconds)
        sendBroadcast(speedIntent)
    }


    private fun initializeLocationManager() {
        try {
            locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager

            if (ContextCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    500,
                    0.5f,
                    this
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    inner class MyBinder : Binder() {
        fun setLocationHandler(handler:Handler){
            this@LocationUpdateService.locationHandler = handler
        }
        fun getService(): LocationUpdateService = this@LocationUpdateService
    }
    private fun showNotification() {
        println("debug msg: showNotification() is called")
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_IMMUTABLE
        )
        val notificationBuilder: NotificationCompat.Builder = NotificationCompat.Builder(
            this,
            CHANNEL_ID
        )
        notificationBuilder.setSmallIcon(R.drawable.ic_launcher_foreground)
        notificationBuilder.setContentText("Recording your path now")
        notificationBuilder.setContentTitle("MyRuns")
        notificationBuilder.setContentIntent(pendingIntent)
        val notification = notificationBuilder.build()
        notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= 26) {
            val notificationChannel = NotificationChannel(
                CHANNEL_ID,
                "channel name",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(notificationChannel)
        }
        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    override fun onUnbind(intent: Intent?): Boolean {
        println("debug msg: onUnbind() is called")
        locationHandler = null
        return true
    }

    override fun onDestroy() {
        super.onDestroy()
        timer.cancel()
        cleanupTasks()
    }
    override fun onTaskRemoved(rootTintent: Intent?){
        super.onTaskRemoved(rootTintent)
        cleanupTasks()
        stopSelf()
    }

    private fun cleanupTasks(){
        notificationManager.cancel(NOTIFICATION_ID)
    }

}