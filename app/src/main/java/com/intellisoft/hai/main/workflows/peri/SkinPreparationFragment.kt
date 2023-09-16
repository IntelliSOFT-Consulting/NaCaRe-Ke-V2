package com.intellisoft.hai.main.workflows.peri

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import com.intellisoft.hai.R
import com.intellisoft.hai.databinding.FragmentSkinPreparationBinding
import com.intellisoft.hai.helper_class.FormatterClass
import com.intellisoft.hai.room.MainViewModel
import com.intellisoft.hai.room.SkinPreparationData

/**
 * A simple [Fragment] subclass.
 * Use the [SkinPreparationFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class SkinPreparationFragment : Fragment() {
    private lateinit var formatterClass: FormatterClass
    private lateinit var mainViewModel: MainViewModel
    private lateinit var binding: FragmentSkinPreparationBinding
    private val risk_factors = HashSet<String>()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentSkinPreparationBinding.inflate(layoutInflater)
        mainViewModel = ViewModelProvider(this).get(MainViewModel::class.java)
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
               saveData()
            }
        }

        val data = formatterClass.getSharedPref("patient", requireContext())
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

                val chlorhexidine_alcohol =
                    if (binding.radioButtonChlorhexidineAlcoholNo.isChecked) "No" else "Yes"
                val iodine_alcohol =
                    if (binding.radioButtonIodineAlcoholNo.isChecked) "No" else "Yes"

                val chlorhexidine_aq =
                    if (binding.radioButtonChlorhexidineAqNo.isChecked) "No" else "Yes"
                val iodine_aq =
                    if (binding.radioButtonIodineAqNo.isChecked) "No" else "Yes"
                val skin_fully_dry =
                    if (binding.radioButtonSkinFullyDryNo.isChecked) "No" else "Yes"
                val enc = formatterClass.getSharedPref("caseId", requireContext())
                val data =
                    SkinPreparationData(
                        userId = user,
                        patientId = patient.toString(),
                        encounterId = enc.toString(),
                        chlorhexidine_alcohol = chlorhexidine_alcohol,
                        iodine_alcohol = iodine_alcohol,
                        chlorhexidine_aq = chlorhexidine_aq,
                        iodine_aq = iodine_aq,
                        skin_fully_dry = skin_fully_dry
                    )
                val added = mainViewModel.addSkinPreparationData(data)
                if (added) {

                    val hostNavController =
                        requireActivity().findNavController(R.id.nav_host_fragment_content_dashboard)
                    hostNavController.navigate(R.id.handPreparationFragment)

                }
                else {
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

    private fun loadInitialData(patient: String) {
        val caseId = formatterClass.getSharedPref("caseId", requireContext())
        val data = mainViewModel.loadSkinPreparationData(requireContext(), patient, caseId)

        binding.apply {
            if (data != null) {
                if (data.chlorhexidine_alcohol == "No") {
                    radioButtonChlorhexidineAlcoholNo.isChecked = true
                }
                if (data.chlorhexidine_alcohol == "Yes") {
                    radioButtonChlorhexidineAlcoholYes.isChecked = true
                }

                if (data.iodine_alcohol == "No") {
                    radioButtonIodineAlcoholNo.isChecked = true
                }
                if (data.iodine_alcohol == "Yes") {
                    radioButtonIodineAlcoholYes.isChecked = true
                }

                if (data.chlorhexidine_aq == "No") {
                    radioButtonChlorhexidineAqNo.isChecked = true
                }
                if (data.chlorhexidine_aq == "Yes") {
                    radioButtonChlorhexidineAqYes.isChecked = true
                }

                if (data.iodine_aq == "No") {
                    radioButtonIodineAqNo.isChecked = true
                }
                if (data.iodine_aq == "Yes") {
                    radioButtonIodineAqYes.isChecked = true
                }

                if (data.skin_fully_dry == "No") {
                    radioButtonSkinFullyDryNo.isChecked = true
                }
                if (data.skin_fully_dry == "Yes") {
                    radioButtonSkinFullyDryYes.isChecked = true
                }
            }
        }
    }

    private fun validate(): Boolean {

        if (!binding.radioButtonChlorhexidineAlcoholNo.isChecked && !binding.radioButtonChlorhexidineAlcoholYes.isChecked) {
            Toast.makeText(
                requireContext(),
                "Please select Chlorhexidine+Alcohol",
                Toast.LENGTH_SHORT
            ).show()
            return false
        }
        if (!binding.radioButtonIodineAlcoholNo.isChecked && !binding.radioButtonIodineAlcoholYes.isChecked) {
            Toast.makeText(requireContext(), "Please select Iodine+Alcohol", Toast.LENGTH_SHORT)
                .show()
            return false
        }
        if (!binding.radioButtonChlorhexidineAqNo.isChecked && !binding.radioButtonChlorhexidineAqYes.isChecked) {
            Toast.makeText(requireContext(), "Please select Chlorhexidine-aq", Toast.LENGTH_SHORT)
                .show()
            return false
        }
        if (!binding.radioButtonIodineAqNo.isChecked && !binding.radioButtonIodineAqYes.isChecked) {
            Toast.makeText(requireContext(), "Please select Iodine-aq", Toast.LENGTH_SHORT).show()
            return false
        }
        if (!binding.radioButtonSkinFullyDryNo.isChecked && !binding.radioButtonSkinFullyDryYes.isChecked) {
            Toast.makeText(
                requireContext(),
                "Please select if skin allowed to fully dry",
                Toast.LENGTH_SHORT
            ).show()
            return false
        }

        return true
    }
}