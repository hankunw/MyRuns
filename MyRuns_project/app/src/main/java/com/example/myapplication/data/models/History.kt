package com.example.myapplication.data.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.Polyline
import java.io.Serializable

@Entity(tableName = "history_table")
data class History(
    @PrimaryKey(autoGenerate = true)
    var id:Long = 0L,

    @ColumnInfo(name = "input_type")
    var inputType:Int=0,

    @ColumnInfo(name = "activity_type")
    var activityType:Int=0,

    @ColumnInfo(name = "datetime")
    var dateTime: Long = 0L,

    @ColumnInfo(name = "duration")
    var duration: Float = 0f,

    @ColumnInfo(name = "distance")
    var distance: Float = 0f,

    @ColumnInfo(name = "calories")
    var calories: Float = 0f,

    @ColumnInfo(name = "heart_rate")
    var heartRate: Int = 0,

    @ColumnInfo(name = "point")
    var points: String="",

    @ColumnInfo(name = "cLimb")
    var climb: Float = 0f,

) : Serializable
