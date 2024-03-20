package com.capture.app.ui.home

import android.app.Application
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.NavHostFragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.capture.app.R
import com.capture.app.adapters.DashAdapter
import com.capture.app.data.FormatterClass
import com.capture.app.databinding.FragmentHomeBinding
import com.capture.app.model.HomeData
import com.capture.app.room.MainViewModel


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

        binding.apply {
            btnProceed.apply {
                setOnClickListener {
                    NavHostFragment.findNavController(this@HomeFragment).navigate(R.id.nav_gallery)
                }
            }
        }
    }

    private fun loadDashBoard() {
        val level = formatter.getSharedPref("orgLevel", requireContext())
        val code = formatter.getSharedPref("orgCode", requireContext())
        val data: List<HomeData> = formatter.generateHomeData(viewModel, level, code)
        binding.apply {
            recyclerView.layoutManager = GridLayoutManager(requireContext(), 2)
            val adapter = DashAdapter(requireContext(), data, this@HomeFragment::handleClick)
            recyclerView.adapter = adapter
//            recyclerView.layoutManager =                LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        }
    }

    private fun handleClick(homeData: HomeData) {

    }
}