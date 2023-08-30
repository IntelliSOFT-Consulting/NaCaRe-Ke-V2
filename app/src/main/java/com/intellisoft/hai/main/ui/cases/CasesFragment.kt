package com.intellisoft.hai.main.ui.cases

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.intellisoft.hai.adapter.PatientAdapter
import com.intellisoft.hai.databinding.FragmentCasesBinding
import com.intellisoft.hai.helper_class.FormatterClass
import com.intellisoft.hai.room.MainViewModel
import com.intellisoft.hai.room.RegistrationData

class CasesFragment : Fragment() {
  private lateinit var binding: FragmentCasesBinding
  private lateinit var mRecyclerView: RecyclerView
  private lateinit var viewModel: MainViewModel
  private var dataList: List<RegistrationData>? = null
  private lateinit var formatterClass: FormatterClass
  override fun onStart() {
    super.onStart()
    loadData()
  }

  private fun loadData() {
    try {
      viewModel = ViewModelProvider(this).get(MainViewModel::class.java)
      dataList = viewModel.getPatients(requireContext())

      if (dataList!!.isNotEmpty()) {
        val dataEntryAdapter = PatientAdapter(dataList!!, requireContext(), this::onclick)
        mRecyclerView.adapter = dataEntryAdapter

      }
    } catch (e: Exception) {
      e.printStackTrace()
    }
  }
  private fun onclick(data: RegistrationData) {
//        formatterClass.saveSharedPref("patient", data.patientId, requireContext())
//        val intent = Intent(requireContext(), PatientProfileActivity::class.java)
//        intent.putExtra("data", data)
//        startActivity(intent)
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

    binding = FragmentCasesBinding.inflate(inflater, container, false)
    formatterClass = FormatterClass()
    val root: View = binding.root
    mRecyclerView = binding.recyclerView
    mRecyclerView.setHasFixedSize(true)
    mRecyclerView.layoutManager = LinearLayoutManager(requireContext())
    return root
  }


}