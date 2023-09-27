package com.intellisoft.nacare.main.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import com.intellisoft.nacare.helper_class.FormatterClass
import com.intellisoft.nacare.room.EventData
import com.intellisoft.nacare.room.MainViewModel
import com.intellisoft.nacare.util.AppUtils.disableTextInputEditText
import com.intellisoft.nacare.util.AppUtils.showDatePickerDialog
import com.nacare.ke.capture.R
import com.nacare.ke.capture.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {

    private lateinit var binding: FragmentHomeBinding
    private lateinit var viewModel: MainViewModel
    private lateinit var formatterClass: FormatterClass
    val hashMap1 = mutableMapOf<String, String>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewModel = ViewModelProvider(this).get(MainViewModel::class.java)

        binding = FragmentHomeBinding.inflate(inflater, container, false)
        formatterClass = FormatterClass()
        binding.apply {
            edtDate.setText(formatterClass.getFormattedDate())
            disableTextInputEditText(edtDate)
        }
        initOrganizations()
        binding.edtDate.apply {
            setOnClickListener {
                showDatePickerDialog(
                    requireContext(), binding.edtDate, setMaxNow = true, setMinNow = false
                )
            }
        }
        binding.btnNext.apply {
            setOnClickListener {
                val data = EventData(
                    date = binding.edtDate.text.toString(),
                    orgUnitCode = generateCode(binding.actOrganization.text.toString()),
                    orgUnitName = binding.actOrganization.text.toString()
                )
                viewModel.addEvent(requireContext(),data)
                val hostNavController =
                    requireActivity().findNavController(R.id.nav_host_fragment_content_dashboard)
                hostNavController.navigate(R.id.nav_gallery)
            }
        }
        return binding.root
    }

    private fun generateCode(value: String): String {
        return hashMap1[value].toString()
    }

    private fun initOrganizations() {
        val organizationList = mutableListOf<String>()
        val org = viewModel.loadOrganizations(requireActivity())
        if (!org.isNullOrEmpty()) {
            org.forEach {
                hashMap1[it.name] = it.code
                organizationList.add(it.name)
            }
            val adapter = ArrayAdapter(
                requireContext(),
                android.R.layout.simple_list_item_1,
                organizationList
            )
            binding.apply {
                actOrganization.setAdapter(adapter)
                actOrganization.setText(organizationList[0], false)
            }

        }
    }
}
