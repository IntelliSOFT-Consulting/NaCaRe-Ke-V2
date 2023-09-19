package com.intellisoft.hai.main.workflows.peri

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.RadioButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import com.google.android.material.button.MaterialButton
import com.intellisoft.hai.R
import com.intellisoft.hai.databinding.FragmentHandPreparationBinding
import com.intellisoft.hai.helper_class.FormatterClass
import com.intellisoft.hai.room.HandPreparationData
import com.intellisoft.hai.room.MainViewModel
import com.intellisoft.hai.util.AppUtils
import org.w3c.dom.Text

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
        loadPractitioners()
        val data = formatterClass.getSharedPref("patient", requireContext())
        if (data != null) {
            loadInitialData(data)
        }
        return binding.root
    }

    private fun loadPractitioners() {
        val stringList = mutableListOf<String>()
        mainViewModel.loadPractitioners(requireContext())?.forEach {
            stringList.add(it.name)
        }
        val adapter =
            ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, stringList)
        binding.aucPractitioner.setAdapter(adapter)
        if (stringList.isNotEmpty()) {
            binding.aucPractitioner.setText(stringList[0], false)
        }
    }

    private fun loadInitialData(patient: String) {
        val caseId = formatterClass.getSharedPref("caseId", requireContext())
        val data = mainViewModel.loadHandPreparationData(requireContext(), patient, caseId)
        binding.apply {
            if (data != null) {
                val conditions = formatterClass.generateTimings(requireContext())
                val adapter =
                    ArrayAdapter(
                        requireContext(),
                        android.R.layout.simple_dropdown_item_1line,
                        conditions
                    )
                binding.aucTime.setAdapter(adapter)
                aucTime.setText(data.time_spent, false)
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
                val prac = binding.aucPractitioner.text.toString()
                val data =
                    HandPreparationData(
                        userId = user,
                        patientId = patient.toString(),
                        encounterId = enc.toString(),
                        practitioner = prac,
                        time_spent = time_spent,
                        plain_soap_water = plain_soap_water,
                        antimicrobial_soap_water = antimicrobial_soap_water,
                        hand_rub = hand_rub
                    )
                val added = mainViewModel.addHandPreparationData(data)
                if (added) {

                    showDialog()

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

    private fun showDialog() {
        val dialogView: View =
            LayoutInflater.from(requireContext()).inflate(R.layout.custom_dialog_layout, null)

        val builder = AlertDialog.Builder(requireContext())
            .setCancelable(false)
            .setView(dialogView)
        val alertDialog = builder.create()
        // Customize the dialog view
        dialogView.findViewById<TextView>(R.id.tv_title).apply {
            text = "Hand preparation details added"
        }
        dialogView.findViewById<TextView>(R.id.tv_message).apply {
            text = "Do you wish to add another practitioner?"
        }
        dialogView.findViewById<ImageView>(R.id.dialog_cancel_image).apply {
            setOnClickListener {
                alertDialog.dismiss()
                proceed()
            }
        }
        dialogView.findViewById<MaterialButton>(R.id.cancelButton).apply {
            setOnClickListener {
                alertDialog.dismiss()
                proceed()
            }
        }
        dialogView.findViewById<MaterialButton>(R.id.saveButton).apply {
            setOnClickListener {
                alertDialog.dismiss()
                onStart()
                clearInputs()
            }
        }

        alertDialog.show()
    }

    private fun clearInputs() {
        binding.apply {
            val conditions = formatterClass.generateTimings(requireContext())
            val adapter =
                ArrayAdapter(
                    requireContext(),
                    android.R.layout.simple_dropdown_item_1line,
                    conditions
                )

            binding.aucTime.setAdapter(adapter)
            radioButtonPlainSoapWaterNo.isChecked = false
            radioButtonPlainSoapWaterYes.isChecked = false
            radioButtonAntimicrobialSoapWaterNo.isChecked = false
            radioButtonAntimicrobialSoapWaterYes.isChecked = false
            radioButtonAlcoholBasedHandRubNo.isChecked = false
            radioButtonAlcoholBasedHandRubYes.isChecked = false
            loadPractitioners()
        }
    }

    private fun proceed() {

        val hostNavController =
            requireActivity().findNavController(R.id.nav_host_fragment_content_dashboard)
        hostNavController.navigate(R.id.preFragment)
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

        val prac = binding.aucPractitioner.text.toString()
        if (prac.isNullOrEmpty()) {
            binding.aucPractitioner.requestFocus()
            binding.practitionerHolder.error = "Please select practitioner"
            return false
        }
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