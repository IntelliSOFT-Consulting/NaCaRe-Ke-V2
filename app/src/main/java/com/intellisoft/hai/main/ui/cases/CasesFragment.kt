package com.intellisoft.hai.main.ui.cases

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.intellisoft.hai.R
import com.intellisoft.hai.adapter.PatientAdapter
import com.intellisoft.hai.databinding.FragmentCasesBinding
import com.intellisoft.hai.helper_class.FormatterClass
import com.intellisoft.hai.room.MainViewModel
import com.intellisoft.hai.room.PatientData
import com.intellisoft.hai.room.RegistrationData
import com.intellisoft.hai.util.AppUtils
import com.intellisoft.hai.util.AppUtils.showDatePickerDialog

class CasesFragment : Fragment() {
    private lateinit var binding: FragmentCasesBinding
    private lateinit var mRecyclerView: RecyclerView
    private lateinit var viewModel: MainViewModel
    private var dataList: List<PatientData>? = null
    private lateinit var formatterClass: FormatterClass
    override fun onStart() {
        super.onStart()
        loadData()
    }

    private fun loadData() {
        try {
            viewModel = ViewModelProvider(this).get(MainViewModel::class.java)
            dataList = viewModel.getPatientsData(requireContext())

            if (dataList!!.isNotEmpty()) {
                val dataEntryAdapter = PatientAdapter(dataList!!, requireContext(), this::onclick)
                mRecyclerView.adapter = dataEntryAdapter

            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun onclick(data: PatientData) {
        formatterClass.saveSharedPref("patient", data.patientId, requireContext())
        val bundle = Bundle()
        bundle.putString("caseId", data.id.toString())
        val hostNavController =
            requireActivity().findNavController(R.id.nav_host_fragment_content_dashboard)
        hostNavController.navigate(R.id.nav_slideshow, bundle)

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

        binding.btnAction.apply {
            setOnClickListener {
                val hostNavController =
                    requireActivity().findNavController(R.id.nav_host_fragment_content_dashboard)
                hostNavController.navigate(R.id.patientRegistrationFragment)
            }
        }
        return root
    }


}