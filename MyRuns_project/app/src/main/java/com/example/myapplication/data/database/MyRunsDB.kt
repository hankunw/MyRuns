package com.example.myapplication.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.myapplication.data.databaseDao.MyRunsDatabaseDao
import com.example.myapplication.data.models.History

@Database(entities = [History::class], version = 2)
abstract class MyRunsDB : RoomDatabase() { //XD: Room automatically generates implementations of your abstract CommentDatabase class.
    abstract val MyRunsDatabaseDao: MyRunsDatabaseDao

    companion object{
        //The Volatile keyword guarantees visibility of changes to the INSTANCE variable across threads
        @Volatile
        private var INSTANCE: MyRunsDB? = null

        fun getInstance(context: Context) : MyRunsDB {
            synchronized(this){
                var instance = INSTANCE
                if(instance == null){
                    instance = Room.databaseBuilder(context.applicationContext,
                        MyRunsDB::class.java, "history_table").build()
                    INSTANCE = instance
                }
                return instance
            }
        }
    }
}
