package com.intellisoft.hai.main.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.intellisoft.hai.R
import com.intellisoft.hai.adapter.HomeAdapter
import com.intellisoft.hai.databinding.FragmentHomeBinding
import com.intellisoft.hai.helper_class.FormatterClass
import com.intellisoft.hai.helper_class.HomeItem
import com.intellisoft.hai.room.MainViewModel

class HomeFragment : Fragment() {

    private lateinit var binding: FragmentHomeBinding
    private lateinit var mRecyclerView: RecyclerView
    private lateinit var viewModel: MainViewModel
    private var dataList: ArrayList<HomeItem>? = null
    private lateinit var formatterClass: FormatterClass


    override fun onStart() {
        super.onStart()
        loadData()
    }

    private fun loadData() {
        try {
            viewModel = ViewModelProvider(this).get(MainViewModel::class.java)
            dataList = formatterClass.generateHomeData(requireContext())

            if (dataList!!.isNotEmpty()) {
                val dataEntryAdapter = HomeAdapter(dataList!!, requireContext(), this::onclick)
                mRecyclerView.adapter = dataEntryAdapter

            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun onclick(data: HomeItem) {
        when (data.text) {
            "Cases" -> {
                val hostNavController = requireActivity().findNavController(R.id.nav_host_fragment_content_dashboard)
                hostNavController.navigate(R.id.nav_gallery)
            }

            "Patients" -> {

            }

            "Reports" -> {

            }
            // Add more cases for other conditions if needed
            else -> {
                // Handle default case or any other conditions
                Toast.makeText(requireContext(), "Please try again!!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        loadData()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewModel = ViewModelProvider(this).get(MainViewModel::class.java)

        binding = FragmentHomeBinding.inflate(inflater, container, false)
        formatterClass = FormatterClass()
        val root: View = binding.root
        mRecyclerView = binding.recyclerView
        mRecyclerView.setHasFixedSize(true)
        mRecyclerView.layoutManager =
            GridLayoutManager(requireContext(), 2, GridLayoutManager.VERTICAL, false)

        return root
    }
}
