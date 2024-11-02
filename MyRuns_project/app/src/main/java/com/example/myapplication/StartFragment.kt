package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.Button
import android.widget.Spinner
import com.example.myapplication.databinding.StartFragmentBinding


class StartFragment: Fragment() {
    lateinit var start_button: Button
    lateinit var binding : StartFragmentBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = StartFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val spinner = view.findViewById<Spinner>(R.id.input_type)
        start_button=view.findViewById(R.id.start_button)
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                val selectedItem = parent?.getItemAtPosition(position).toString()
                start_button.setOnClickListener(){
                    when (selectedItem) {
                        "Manual Entry" -> {
                            startActivity(Intent(requireContext(), Fill_in_info::class.java))
                        }

                        else -> {
                            val intent = Intent(requireContext(), MapDisplayActivity::class.java)
                            intent.putExtra("activityType",binding.activityType.selectedItemPosition)
                            intent.putExtra("inputType", binding.inputType.selectedItemPosition)
                            startActivity(intent)
                        }
                    }
                }

            }
            override fun onNothingSelected(parent: AdapterView<*>?) {
                println("no spinners are selected")
            }
        }

    }
}