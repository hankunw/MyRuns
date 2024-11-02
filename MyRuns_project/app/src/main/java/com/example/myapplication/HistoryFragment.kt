package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import androidx.lifecycle.ViewModelProvider
import com.example.myapplication.data.database.MyRunsDB
import com.example.myapplication.data.databaseDao.MyRunsDatabaseDao
import com.example.myapplication.data.models.History
import com.example.myapplication.repository.MyRunsRepository

class HistoryFragment : Fragment() {

    private lateinit var myListView: ListView

    // arraylist
    private lateinit var arrayList: ArrayList<History>
    private lateinit var arrayAdapter: HistoryListAdapter

    private lateinit var database: MyRunsDB
    private lateinit var databaseDao: MyRunsDatabaseDao
    private lateinit var repository: MyRunsRepository
    private lateinit var viewModelFactory: MyRunsViewModelFactory
    private lateinit var historyViewModel: MyRunsViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.history_list, container, false)
        myListView = view.findViewById(R.id.list)

        arrayList = ArrayList()
        arrayAdapter = HistoryListAdapter(requireActivity(), arrayList)
        myListView.adapter = arrayAdapter

        // Database:

        database = MyRunsDB.getInstance(requireActivity())
        databaseDao = database.MyRunsDatabaseDao
        repository = MyRunsRepository(databaseDao)
        viewModelFactory = MyRunsViewModelFactory(repository)
        historyViewModel = ViewModelProvider(this, viewModelFactory).get(MyRunsViewModel::class.java)

        myListView.setOnItemClickListener { _, itemView, position, _ ->
            val history = arrayAdapter.getItem(position)
            if(history.inputType==0){
                startActivity(Intent(requireContext(),HistoryListInfo::class.java).apply {
                    putExtra("exercise",history)
                })
            }else{
                startActivity(Intent(requireContext(),MapHistoryActivity::class.java).apply {
                    putExtra("exercise",history)
                })
            }
        }

        historyViewModel.allHistoryLiveData.observe(viewLifecycleOwner) { data ->
            data?.let {
                arrayList.clear() // Clear the list before updating
                arrayList.addAll(it)
                arrayAdapter.notifyDataSetChanged()
            }
        }

        return view
    }

    override fun onResume() {
        super.onResume()
        arrayAdapter?.notifyDataSetChanged()
    }
    override fun onDestroy() {
        super.onDestroy()
    }

}