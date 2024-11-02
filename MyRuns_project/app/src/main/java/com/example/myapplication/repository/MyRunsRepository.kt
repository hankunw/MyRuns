package com.example.myapplication.repository

import com.example.myapplication.data.databaseDao.MyRunsDatabaseDao
import com.example.myapplication.data.models.History
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MyRunsRepository(private val myRunsDatabaseDao: MyRunsDatabaseDao) {

    val allHistory: Flow<List<History>> = myRunsDatabaseDao.getAllHistory()

    fun insert(history: History){
        CoroutineScope(Dispatchers.IO).launch{
            myRunsDatabaseDao.insert(history)
        }
    }

    suspend fun selectHistoryById(id: Long): History?{
        return withContext(Dispatchers.IO) {
            myRunsDatabaseDao.getHistoryByID(id)
        }
    }

    fun delete(id: Long){
        CoroutineScope(Dispatchers.IO).launch {
            myRunsDatabaseDao.deleteHistory(id)
        }
    }

    fun deleteAll(){
        CoroutineScope(Dispatchers.IO).launch {
            myRunsDatabaseDao.deleteAll()
        }
    }

}