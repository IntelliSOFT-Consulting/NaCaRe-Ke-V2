package com.intellisoft.hai.main.workflows.peri

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import com.intellisoft.hai.R
import com.intellisoft.hai.databinding.FragmentPatientPreparationBinding
import com.intellisoft.hai.helper_class.FormatterClass
import com.intellisoft.hai.room.MainViewModel
import com.intellisoft.hai.room.PreparationData
import com.intellisoft.hai.util.AppUtils

/**
 * A simple [Fragment] subclass.
 * Use the [PatientPreparationFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class PatientPreparationFragment : Fragment() {
    private lateinit var binding: FragmentPatientPreparationBinding
    private lateinit var formatterClass: FormatterClass
    private lateinit var mainViewModel: MainViewModel


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentPatientPreparationBinding.inflate(layoutInflater)
        AppUtils.disableTextInputEditText(binding.edtDate)
        mainViewModel = ViewModelProvider(this).get(MainViewModel::class.java)
        formatterClass = FormatterClass()
        binding.edtDate.apply {
            setOnClickListener {
                AppUtils.showDatePickerDialog(
                    requireContext(), binding.edtDate, setMaxNow = false, setMinNow = true
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
                    val user = formatterClass.getSharedPref("username", requireContext())
                    val patient = formatterClass.getSharedPref("patient", requireContext())

                    if (user != null) {
                        val bath = if (binding.radioNo.isChecked) "No" else "Yes"
                        val soap = if (binding.radioAntibacterialNo.isChecked) "No" else "Yes"
                        val hair =
                            if (binding.radioHairNo.isChecked) {
                                "No"
                            } else if (binding.radioHairRazor.isChecked) {
                                "Razor"
                            } else {
                                "Clippers"
                            }
                        val date = binding.edtDate.text?.toString()
                        if (binding.radioHairNo.isChecked) {
                            binding.edtDate.setText("")
                        }
                        val enc = formatterClass.getSharedPref("caseId", requireContext())
                        val peri =
                            PreparationData(
                                userId = user,
                                patientId = patient.toString(),
                                encounterId = enc.toString(),
                                pre_bath = bath,
                                soap_used = soap,
                                hair_removal = hair,
                                date_of_removal = date
                            )
                        val added = mainViewModel.addPreparationData(peri)
                        if (added) {
                            val hostNavController =
                                requireActivity().findNavController(R.id.nav_host_fragment_content_dashboard)
                            hostNavController.navigate(R.id.skinPreparationFragment)
                        } else {
                            Toast.makeText(
                                requireContext(),
                                "Encountered problems registering patient",
                                Toast.LENGTH_SHORT
                            )
                                .show()
                        }
                    } else {
                        Toast.makeText(
                            requireContext(),
                            "Please check user account",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }
        binding.radioGroupHairRemoval.setOnCheckedChangeListener { _, checkedId ->
            val selectedRadioButton = binding.root.findViewById<RadioButton>(checkedId)
            val micro = selectedRadioButton.text.toString()
            if (micro == "No") {
                binding.dateHolder.visibility = View.GONE
            } else {
                binding.dateHolder.visibility = View.VISIBLE
            }
        }
        AppUtils.controlData(
            binding.edtDate,
            binding.dateHolder,
            "Please provide date",
            hasMin = false,
            hasMax = false,
            min = 0,
            max = 0
        )
        val data = formatterClass.getSharedPref("patient", requireContext())
        if (data != null) {
            loadInitialData(data)
        }
        return binding.root
    }

    private fun loadInitialData(patient: String) {
        val caseId = formatterClass.getSharedPref("caseId", requireContext())
        val data = mainViewModel.loadPreparationData(requireContext(), patient, caseId)

        binding.apply {
            if (data != null) {
                edtDate.setText(data.date_of_removal)
                if (data.pre_bath == "No") {
                    radioNo.isChecked = true
                }
                if (data.pre_bath == "Yes") {
                    radioYes.isChecked = true
                }
                if (data.soap_used == "No") {
                    radioAntibacterialNo.isChecked = true
                }
                if (data.soap_used == "Yes") {
                    radioAntibacterialYes.isChecked = true
                }
                if (data.hair_removal == "No") {
                    radioHairNo.isChecked = true
                }
                if (data.hair_removal == "Clippers") {
                    radioHairClippers.isChecked = true
                }
                if (data.hair_removal == "Razor") {
                    radioHairRazor.isChecked = true
                }
            }
        }
    }


    private fun validate(): Boolean {
        val date = binding.edtDate.text?.toString()
        if (!binding.radioNo.isChecked && !binding.radioYes.isChecked) {
            Toast.makeText(requireContext(), "please specify bath", Toast.LENGTH_SHORT).show()
            return false
        }
        if (!binding.radioAntibacterialNo.isChecked && !binding.radioAntibacterialYes.isChecked) {
            Toast.makeText(requireContext(), "please specify soap used", Toast.LENGTH_SHORT).show()
            return false
        }
        if (!binding.radioHairNo.isChecked &&
            !binding.radioHairClippers.isChecked &&
            !binding.radioHairRazor.isChecked
        ) {
            Toast.makeText(requireContext(), "please specify hair removal", Toast.LENGTH_SHORT)
                .show()
            return false
        }
        if (binding.radioHairClippers.isChecked || binding.radioHairRazor.isChecked) {
            if (date.isNullOrEmpty()) {
                binding.dateHolder.error = "Please provide hair date"
                binding.edtDate.requestFocus()
                return false
            }
        }
        return true
    }
}