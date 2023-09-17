package com.intellisoft.hai.main.workflows.peri

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import com.google.gson.Gson
import com.intellisoft.hai.R
import com.intellisoft.hai.databinding.FragmentPeriBinding
import com.intellisoft.hai.helper_class.FormatterClass
import com.intellisoft.hai.room.Converters
import com.intellisoft.hai.room.MainViewModel
import com.intellisoft.hai.room.PeriData
import com.intellisoft.hai.util.AppUtils.controlData
import com.intellisoft.hai.util.AppUtils.generateUuid


/**
 * A simple [Fragment] subclass.
 * Use the [PeriFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class PeriFragment : Fragment() {
    private lateinit var formatterClass: FormatterClass
    private lateinit var mainViewModel: MainViewModel
    private lateinit var binding: FragmentPeriBinding


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentPeriBinding.inflate(layoutInflater)
        mainViewModel = ViewModelProvider(this).get(MainViewModel::class.java)
        formatterClass = FormatterClass()

        val conditions = formatterClass.generateRiskFactors(requireContext())
        val adapter =
            ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, conditions)
        binding.aucFactors.setAdapter(adapter)
        handleListeners()
        binding.prevButton.apply {
            setOnClickListener {
                val caseId = formatterClass.getSharedPref("caseId", requireContext())
                val bundle = Bundle()
                bundle.putString("caseId", caseId)
                val hostNavController =
                    requireActivity().findNavController(R.id.nav_host_fragment_content_dashboard)
                hostNavController.navigate(R.id.nav_slideshow, bundle)
            }
        }
        binding.nextButton.apply {
            setOnClickListener {
               saveData()
            }
        }
        binding.radioButtonBloodGlucoseYes.apply {
            setOnCheckedChangeListener { _, isChecked ->
                binding.glucoseHolder.isVisible = isChecked
                binding.interventionHolder.isVisible = isChecked
            }
        }
        val data = formatterClass.getSharedPref("peri", requireContext())
        if (data != null) {
            loadInitialData(data)
        }
        return binding.root
    }

    private fun saveData() {
        if (validate()) {
            val user = formatterClass.getSharedPref("username", requireContext())
            if (user != null) {
                val patient = formatterClass.getSharedPref("patient", requireContext())
                val caseId = formatterClass.getSharedPref("caseId", requireContext())

                val measured =
                    if (binding.radioButtonBloodGlucoseNo.isChecked) "No" else "Yes"
                val level = binding.edtGlucose.text.toString()
                val intervention = binding.edtIntervention.text.toString()
                val risks = binding.aucFactors.text.toString()
                val peri =
                    PeriData(
                        userId = user,
                        patientId = patient.toString(),
                        encounterId = caseId.toString(),
                        risk_factors = risks,
                        glucose_measured = measured,
                        glucose_level = level,
                        intervention = intervention,
                    )
                val added = mainViewModel.addPeriData(peri)
                if (added) {

                    val hostNavController =
                        requireActivity().findNavController(R.id.nav_host_fragment_content_dashboard)
                    hostNavController.navigate(R.id.patientPreparationFragment)

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
                    "Please check user account",
                    Toast.LENGTH_SHORT
                )
                    .show()
            }
        }
    }

    private fun loadInitialData(data: String) {
        val converters = Converters()
        val periData: PeriData = converters.periFromJson(data)
        val conditions = formatterClass.generateRiskFactors(requireContext())
        val adapter =
            ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, conditions)
        binding.aucFactors.setAdapter(adapter)
        binding.aucFactors.setText(periData.risk_factors,false)
        binding.edtGlucose.setText(periData.glucose_level)
        binding.edtIntervention.setText(periData.intervention)
        if (periData.glucose_measured == "Yes") {
            binding.radioButtonBloodGlucoseYes.isChecked = true
        }
    }


    private fun handleListeners() {
        if (binding.radioButtonBloodGlucoseYes.isChecked) {
            controlData(
                binding.edtIntervention,
                binding.interventionHolder,
                "Please provide intervention",
                hasMin = false,
                hasMax = false,
                min = 0,
                max = 0
            )
            controlData(
                binding.edtGlucose,
                binding.glucoseHolder,
                "Please provide glucose level",
                hasMin = false,
                hasMax = false,
                min = 0,
                max = 0
            )
        }
    }

    fun validate(): Boolean {
        val glucose = binding.edtGlucose.text?.toString()
        val intervention = binding.edtIntervention.text?.toString()
        val risks = binding.aucFactors.text?.toString()
        if (risks.isNullOrEmpty()) {
            binding.riskHolder.error = "Please select risk factors"
            binding.aucFactors.requestFocus()
            return false
        }
        if (!binding.radioButtonBloodGlucoseYes.isChecked && !binding.radioButtonBloodGlucoseNo.isChecked) {
            Toast.makeText(
                requireContext(),
                "Please check if glucose was measured",
                Toast.LENGTH_SHORT
            ).show()
            return false
        }
        if (binding.radioButtonBloodGlucoseYes.isChecked) {
            if (glucose.isNullOrEmpty()) {
                binding.glucoseHolder.error = "Please provide glucose level"
                binding.edtGlucose.requestFocus()
                return false
            }
            if (intervention.isNullOrEmpty()) {
                binding.interventionHolder.error = "Please provide intervention"
                binding.edtIntervention.requestFocus()
                return false
            }
        }

        return true
    }

}