package com.nacare.capture.ui.home

import android.app.Application
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.nacare.capture.adapters.DashAdapter
import com.nacare.capture.data.FormatterClass
import com.nacare.capture.databinding.FragmentHomeBinding
import com.nacare.capture.model.HomeData
import com.nacare.capture.room.MainViewModel


class HomeFragment : Fragment() {
    private lateinit var viewModel: MainViewModel
    private lateinit var binding: FragmentHomeBinding

    val formatter = FormatterClass()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root
        viewModel = MainViewModel(requireContext().applicationContext as Application)
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadDashBoard()
    }

    private fun loadDashBoard() {

        val data: List<HomeData> = formatter.generateHomeData(viewModel)
        binding.apply {
            recyclerView.layoutManager = LinearLayoutManager(requireContext())
            val adapter = DashAdapter(requireContext(), data, this@HomeFragment::handleClick)
            recyclerView.adapter = adapter
            recyclerView.layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        }
    }

    private fun handleClick(homeData: HomeData) {

    }
}