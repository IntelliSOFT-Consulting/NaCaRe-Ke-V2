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
import com.intellisoft.hai.databinding.FragmentPostDateBinding
import com.intellisoft.hai.helper_class.FormatterClass
import com.intellisoft.hai.room.MainViewModel
import com.intellisoft.hai.room.PostOperativeData
import com.intellisoft.hai.util.AppUtils

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [PostDateFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class PostDateFragment : Fragment() {
    private lateinit var binding: FragmentPostDateBinding
    private lateinit var viewModel: MainViewModel
    private lateinit var formatterClass: FormatterClass
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewModel = ViewModelProvider(this).get(MainViewModel::class.java)
        binding = FragmentPostDateBinding.inflate(inflater, container, false)
        formatterClass = FormatterClass()
        AppUtils.disableTextInputEditText(binding.edtWound)
        binding.edtWound.apply {
            setOnClickListener {
                AppUtils.showDatePickerDialog(
                    requireContext(), binding.edtWound, setMaxNow = false, setMinNow = false
                )
            }
        }
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
        dataControl()
        return binding.root
    }

    private fun saveData() {
        val user = formatterClass.getSharedPref("username", requireContext())
        if (user != null) {
            val patient = formatterClass.getSharedPref("patient", requireContext())
            val caseId = formatterClass.getSharedPref("caseId", requireContext())
            val post = AppUtils.generateUuid()
            formatterClass.saveSharedPref("post-data", post, requireContext())
            val data =
                PostOperativeData(
                    userId = user,
                    patientId = patient.toString(),
                    encounterId = post,
                    caseId = caseId.toString(),
                    check_up_date = binding.edtWound.text.toString(),
                    infection_signs = if (binding.radioButtonNo.isChecked) "No" else "Yes",
                    event_date = "",
                    ssi = "",
                    infection_surgery_time = "",
                    drainage = "",
                    pain = "",
                    erythema = "",
                    heat = "",
                    fever = "",
                    incision_opened = "",
                    wound_dehisces = "",
                    abscess = "",
                    sinus = "",
                    hypothermia = "",
                    apnea = "",
                    bradycardia = "",
                    lethargy = "",
                    cough = "",
                    nausea = "",
                    vomiting = "",
                    symptom_other = "",
                    samples_sent = "",
                )
            val added = viewModel.addPostOperativeData(data)
            if (added) {
                val hostNavController =
                    requireActivity().findNavController(R.id.nav_host_fragment_content_dashboard)
                hostNavController.navigate(R.id.infectionFragment)

            } else {
                Toast.makeText(
                    requireContext(),
                    "Encountered problems saving data",
                    Toast.LENGTH_SHORT
                )
                    .show()
            }
        }

    }

    private fun validate(): Boolean {

        if (binding.edtWound.text?.toString().isNullOrEmpty()) {
            binding.woundHolder.error = "Please provide date"
            binding.edtWound.requestFocus()
            return false
        }
        if (binding.woundStatusRadioGroup.checkedRadioButtonId == -1) {
            Toast.makeText(
                requireContext(),
                "Please select if wound has signs",
                Toast.LENGTH_SHORT
            ).show()
            return false
        }
        return true
    }

    private fun dataControl() {
        AppUtils.controlData(
            binding.edtWound,
            binding.woundHolder,
            "Please provide date",
            hasMin = false,
            hasMax = false,
            min = 0,
            max = 0
        )
    }

}