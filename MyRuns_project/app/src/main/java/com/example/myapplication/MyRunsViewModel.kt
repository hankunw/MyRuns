package com.example.myapplication

import androidx.lifecycle.*
import com.example.myapplication.data.models.History
import com.example.myapplication.repository.MyRunsRepository
import java.lang.IllegalArgumentException


class MyRunsViewModel(private val repository: MyRunsRepository) : ViewModel() {
    val allHistoryLiveData: LiveData<List<History>> = repository.allHistory.asLiveData()

    fun insert(history: History) {
        repository.insert(history)
    }

    fun deleteHistory(id:Long){
        repository.delete(id)
    }

    fun deleteAll(){
        val historyList = allHistoryLiveData.value
        if (historyList != null && historyList.size > 0)
            repository.deleteAll()
    }


}

class MyRunsViewModelFactory (private val repository: MyRunsRepository) : ViewModelProvider.Factory {
    override fun<T: ViewModel> create(modelClass: Class<T>) : T{ //create() creates a new instance of the modelClass, which is CommentViewModel in this case.
        if(modelClass.isAssignableFrom(MyRunsViewModel::class.java))
            return MyRunsViewModel(repository) as T
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}