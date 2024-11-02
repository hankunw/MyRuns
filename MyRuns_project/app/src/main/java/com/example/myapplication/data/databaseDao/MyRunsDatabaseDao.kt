package com.example.myapplication.data.databaseDao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.myapplication.data.models.History
import kotlinx.coroutines.flow.Flow

@Dao
interface MyRunsDatabaseDao {
    @Insert
    suspend fun insert(history: History)

    @Query("SELECT * FROM history_table WHERE id= :key")
    suspend fun getHistoryByID(key:Long): History

    @Query("SELECT * FROM history_table")
    fun getAllHistory(): Flow<List<History>>

    @Query("DELETE FROM history_table")
    suspend fun deleteAll()

    @Query("DELETE FROM history_table WHERE id= :key")
    suspend fun deleteHistory(key: Long)

}