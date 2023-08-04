package com.intellisoft.hai.main.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.intellisoft.hai.adapter.PatientAdapter
import com.intellisoft.hai.databinding.FragmentHomeBinding
import com.intellisoft.hai.room.MainViewModel
import com.intellisoft.hai.room.RegistrationData

class HomeFragment : Fragment() {

  private lateinit var binding: FragmentHomeBinding
  private lateinit var mRecyclerView: RecyclerView
  private lateinit var viewModel: MainViewModel
  private var patientList: List<RegistrationData>? = null

  override fun onStart() {
    super.onStart()
    loadData()
  }

  private fun loadData() {
    try {
      viewModel = ViewModelProvider(this).get(MainViewModel::class.java)
      patientList = viewModel.getPatients(requireContext())
      if (patientList != null) {
        if (patientList!!.isNotEmpty()) {
          val dataEntryAdapter = PatientAdapter(patientList!!, requireContext())
          mRecyclerView.adapter = dataEntryAdapter
        }
      }
    } catch (e: Exception) {
      e.printStackTrace()
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
    val root: View = binding.root
    mRecyclerView = binding.recyclerView
    mRecyclerView.setHasFixedSize(true)
    mRecyclerView.layoutManager = LinearLayoutManager(requireContext())

    return root
  }
}
