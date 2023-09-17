package com.intellisoft.hai.main.workflows.peri

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.RadioButton
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import com.intellisoft.hai.R
import com.intellisoft.hai.databinding.FragmentHandPreparationBinding
import com.intellisoft.hai.helper_class.FormatterClass
import com.intellisoft.hai.room.HandPreparationData
import com.intellisoft.hai.room.MainViewModel
import com.intellisoft.hai.util.AppUtils

/**
 * A simple [Fragment] subclass.
 * Use the [HandPreparationFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class HandPreparationFragment : Fragment() {
    private lateinit var formatterClass: FormatterClass
    private lateinit var mainViewModel: MainViewModel
    private lateinit var binding: FragmentHandPreparationBinding
    private lateinit var time_spent: String
    private lateinit var plain_soap_water: String
    private lateinit var antimicrobial_soap_water: String
    private lateinit var hand_rub: String


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentHandPreparationBinding.inflate(layoutInflater)
        mainViewModel = ViewModelProvider(this).get(MainViewModel::class.java)
        formatterClass = FormatterClass()

        val conditions = formatterClass.generateTimings(requireContext())
        val adapter =
            ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, conditions)
        binding.aucTime.setAdapter(adapter)

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
                saveData()
            }
        }
        handleClicks()
        AppUtils.controlSelectionData(
            binding.aucTime,
            binding.timekHolder,
            "Please provide timing",
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
        val data = mainViewModel.loadHandPreparationData(requireContext(), patient, caseId)
        binding.apply {
            if (data != null) {
                val conditions = formatterClass.generateTimings(requireContext())
                val adapter =
                    ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, conditions)
                binding.aucTime.setAdapter(adapter)
                aucTime.setText(data.time_spent,false)
                if (data.plain_soap_water == "No") {
                    radioButtonPlainSoapWaterNo.isChecked = true
                }
                if (data.plain_soap_water == "Yes") {
                    radioButtonPlainSoapWaterYes.isChecked = true
                }

                if (data.antimicrobial_soap_water == "No") {
                    radioButtonAntimicrobialSoapWaterNo.isChecked = true
                }
                if (data.antimicrobial_soap_water == "Yes") {
                    radioButtonAntimicrobialSoapWaterYes.isChecked = true
                }

                if (data.hand_rub == "No") {
                    radioButtonAlcoholBasedHandRubNo.isChecked = true
                }
                if (data.hand_rub == "Yes") {
                    radioButtonAlcoholBasedHandRubYes.isChecked = true
                }
            }
        }
    }

    private fun saveData() {
        if (validate()) {
            val user = formatterClass.getSharedPref("username", requireContext())
            if (user != null) {
                val patient = formatterClass.getSharedPref("patient", requireContext())
                val enc = formatterClass.getSharedPref("caseId", requireContext())
                time_spent = binding.aucTime.text.toString()
                val data =
                    HandPreparationData(
                        userId = user,
                        patientId = patient.toString(),
                        encounterId = enc.toString(),
                        practitioner = AppUtils.generateUuid(),
                        time_spent = time_spent,
                        plain_soap_water = plain_soap_water,
                        antimicrobial_soap_water = antimicrobial_soap_water,
                        hand_rub = hand_rub
                    )
                val added = mainViewModel.addHandPreparationData(data)
                if (added) {
                    val hostNavController =
                        requireActivity().findNavController(R.id.nav_host_fragment_content_dashboard)
                    hostNavController.navigate(R.id.preFragment)

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

    private fun handleClicks() {

        binding.plainSoapWaterRadioGroup.setOnCheckedChangeListener { _, checkedId ->
            val selectedRadioButton = binding.root.findViewById<RadioButton>(checkedId)
            plain_soap_water = selectedRadioButton.text.toString()
        }
        binding.antimicrobialSoapWaterRadioGroup.setOnCheckedChangeListener { _, checkedId ->
            val selectedRadioButton = binding.root.findViewById<RadioButton>(checkedId)
            antimicrobial_soap_water = selectedRadioButton.text.toString()
        }
        binding.alcoholBasedHandRubRadioGroup.setOnCheckedChangeListener { _, checkedId ->
            val selectedRadioButton = binding.root.findViewById<RadioButton>(checkedId)
            hand_rub = selectedRadioButton.text.toString()
        }
    }

    private fun validate(): Boolean {

        time_spent = binding.aucTime.text.toString()
        if (time_spent.isNullOrEmpty()) {
            binding.aucTime.requestFocus()
            binding.timekHolder.error = "Please select time"
            return false
        }
        if (binding.plainSoapWaterRadioGroup.checkedRadioButtonId == -1) {
            Toast.makeText(
                requireContext(),
                "Please select Plain Soap + Water",
                Toast.LENGTH_SHORT
            ).show()
            return false
        }
        if (binding.antimicrobialSoapWaterRadioGroup.checkedRadioButtonId == -1) {
            Toast.makeText(
                requireContext(),
                "Please select Antimicrobial Soap + Water",
                Toast.LENGTH_SHORT
            ).show()
            return false
        }
        if (binding.alcoholBasedHandRubRadioGroup.checkedRadioButtonId == -1) {
            Toast.makeText(
                requireContext(),
                "Please select Alcohol-based Hand Rub",
                Toast.LENGTH_SHORT
            ).show()
            return false
        }


        return true
    }

}