package com.intellisoft.hai.main.workflows.post

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import com.intellisoft.hai.R
import com.intellisoft.hai.databinding.FragmentInfectionBinding
import com.intellisoft.hai.databinding.FragmentPostDateBinding
import com.intellisoft.hai.helper_class.FormatterClass
import com.intellisoft.hai.room.Converters
import com.intellisoft.hai.room.MainViewModel
import com.intellisoft.hai.room.PostOperativeData
import com.intellisoft.hai.util.AppUtils

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [InfectionFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class InfectionFragment : Fragment() {
    private lateinit var binding: FragmentInfectionBinding
    private lateinit var viewModel: MainViewModel
    private lateinit var formatterClass: FormatterClass
    private lateinit var encounterId: String
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewModel = ViewModelProvider(this).get(MainViewModel::class.java)

        binding = FragmentInfectionBinding.inflate(inflater, container, false)
        formatterClass = FormatterClass()
        binding.prevButton.apply {
            setOnClickListener {
                val caseId = formatterClass.getSharedPref("caseId", requireContext())
                val bundle = Bundle()
                bundle.putString("caseId", caseId)
                val hostNavController =
                    requireActivity().findNavController(R.id.nav_host_fragment_content_dashboard)
                hostNavController.navigateUp()
            }
        }
        binding.nextButton.apply {
            setOnClickListener {
                if (validate()) {
                    saveData()
                }
            }
        }
        AppUtils.disableTextInputEditText(binding.edtEventDate)

        binding.edtEventDate.apply {
            setOnClickListener {
                AppUtils.showDatePickerDialog(
                    requireContext(), binding.edtEventDate, setMaxNow = false, setMinNow = false
                )
            }
        }
        dataControl()
        val data = formatterClass.getSharedPref("post", requireContext())
        if (data != null) {
            loadInitialData(data)
        } else {
            encounterId = AppUtils.generateUuid()
        }
        return binding.root
    }

    private fun saveData() {

        val date = binding.edtEventDate.text.toString()
        val initial = if (binding.radioButtonNoInfection.isChecked) "No" else "Yes"
        val ssi =
            if (binding.radioButtonSIP.isChecked) "Superficial Incisional Primary (SIP)" else (if (binding.radioButtonDIP.isChecked) "Deep Incisional Primary (DIP)" else "Organ/Space")
        val added = viewModel.updateInfectionData(requireContext(), date, initial, ssi, encounterId)
        if (added) {
            val dt = viewModel.getLatestPostData(requireContext(), encounterId)
            if (dt != null) {
                val converters = Converters()
                val jeff = converters.toPostJson(dt)
                formatterClass.saveSharedPref("post", jeff, requireContext())
                val hostNavController =
                    requireActivity().findNavController(R.id.nav_host_fragment_content_dashboard)
                hostNavController.navigate(R.id.postFragment)
            } else {
                Toast.makeText(
                    requireContext(),
                    "Encountered problems saving data",
                    Toast.LENGTH_SHORT
                )
                    .show()
            }

        } else {
            Toast.makeText(
                requireContext(),
                "Encountered problems saving data",
                Toast.LENGTH_SHORT
            )
                .show()
        }
    }

    private fun loadInitialData(data: String) {
        binding.apply {
            val converters = Converters()
            val data: PostOperativeData = converters.postFromJson(data)
            encounterId = data.encounterId
            edtEventDate.setText(data.event_date)
            if (data.infection_surgery_time == "No") {
                radioButtonNoInfection.isChecked = true
            }
            if (data.infection_surgery_time == "Yes") {
                radioButtonYesInfection.isChecked = true
            }
            if (data.ssi == "Superficial Incisional Primary (SIP)") {
                radioButtonSIP.isChecked = true
            }
            if (data.ssi == "Deep Incisional Primary (DIP)") {
                radioButtonDIP.isChecked = true
            }
            if (data.ssi == "Organ/Space") {
                radioButtonOrganSpace.isChecked = true
            }
        }
    }

    private fun validate(): Boolean {

        if (binding.edtEventDate.text?.toString().isNullOrEmpty()) {
            binding.eventHolder.error = "Please provide date"
            binding.edtEventDate.requestFocus()
            return false
        }

        if (binding.infectionPresenceRadioGroup.checkedRadioButtonId == -1) {
            Toast.makeText(
                requireContext(),
                "Please select Infection Presence",
                Toast.LENGTH_SHORT
            ).show()
            return false
        }
        if (binding.ssiTypeRadioGroup.checkedRadioButtonId == -1) {
            Toast.makeText(
                requireContext(),
                "Please select Type of SSI",
                Toast.LENGTH_SHORT
            ).show()
            return false
        }


        return true
    }

    private fun dataControl() {
        AppUtils.controlData(
            binding.edtEventDate,
            binding.eventHolder,
            "Please provide date",
            hasMin = false,
            hasMax = false,
            min = 0,
            max = 0
        )
    }

}