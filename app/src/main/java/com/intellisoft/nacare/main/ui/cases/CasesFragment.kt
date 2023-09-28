package com.intellisoft.nacare.main.ui.cases

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.intellisoft.nacare.adapter.EventAdapter
import com.intellisoft.nacare.helper_class.FormatterClass
import com.intellisoft.nacare.main.DashboardActivity
import com.intellisoft.nacare.main.registry.RegistryActivity
import com.intellisoft.nacare.room.EventData
import com.intellisoft.nacare.room.MainViewModel
import com.intellisoft.nacare.util.AppUtils
import com.nacare.ke.capture.R
import com.nacare.ke.capture.databinding.FragmentCasesBinding

class CasesFragment : Fragment() {
    private lateinit var binding: FragmentCasesBinding
    private lateinit var mRecyclerView: RecyclerView
    private lateinit var viewModel: MainViewModel
    private lateinit var formatterClass: FormatterClass
    private lateinit var dataList: List<EventData>

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

        binding.addFab.apply {
            setOnClickListener {
                val hostNavController =
                    requireActivity().findNavController(R.id.nav_host_fragment_content_dashboard)
//                hostNavController.navigate(R.id.patientRegistrationFragment)

                val intent = Intent(requireContext(), RegistryActivity::class.java)
                startActivity(intent)
            }
        }
        loadEventData()



        return root
    }

    private fun loadEventData() {
        val data = viewModel.loadEvents(requireContext())
        if (!data.isNullOrEmpty()) {
            dataList = data
            val adapter = EventAdapter(dataList, requireContext(), this::handleClick)
            mRecyclerView.adapter = adapter
            mRecyclerView.layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        }

    }

    private fun handleClick(data: EventData) {

    }


}