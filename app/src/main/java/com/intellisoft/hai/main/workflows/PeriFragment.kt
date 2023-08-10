package com.intellisoft.hai.main.workflows

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.intellisoft.hai.R
import com.intellisoft.hai.databinding.FragmentPeriBinding
import com.intellisoft.hai.helper_class.FormatterClass
import com.intellisoft.hai.listeners.OnFragmentInteractionListener
import com.intellisoft.hai.room.MainViewModel
import com.intellisoft.hai.room.PeriData
import com.intellisoft.hai.room.RegistrationData
import com.intellisoft.hai.util.AppUtils
import com.intellisoft.hai.util.AppUtils.controlData
import com.intellisoft.hai.util.AppUtils.generateUuid

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [PeriFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class PeriFragment : Fragment() {
    private lateinit var formatterClass: FormatterClass
    private lateinit var mainViewModel: MainViewModel
    private lateinit var binding: FragmentPeriBinding
    private val risk_factors = HashSet<String>()
    private var mListener: OnFragmentInteractionListener? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnFragmentInteractionListener) {
            mListener = context
        } else {
            throw RuntimeException("$context must implement OnFragmentInteractionListener")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentPeriBinding.inflate(layoutInflater)
        mainViewModel = ViewModelProvider(this).get(MainViewModel::class.java)
        formatterClass = FormatterClass()
        handleListeners()
        binding.btnSubmit.apply {
            setOnClickListener {
                if (validate()) {
                    val user = formatterClass.getSharedPref("username", requireContext())
                    if (user != null) {
                        val patient = formatterClass.getSharedPref("patient", requireContext())
                        val selectedLabels = risk_factors.toTypedArray()
                        val commaSeparatedString = selectedLabels.joinToString(", ")
                        val measured =
                            if (binding.radioButtonBloodGlucoseNo.isChecked) "No" else "Yes"
                        val level = binding.edtGlucose.text.toString()
                        val intervention = binding.edtIntervention.text.toString()
                        val enc = generateUuid()
                        formatterClass.saveSharedPref("encounter", enc, requireContext())
                        val peri =
                            PeriData(
                                userId = user,
                                patientId = patient.toString(),
                                encounterId = enc,
                                risk_factors = commaSeparatedString,
                                glucose_measured = measured,
                                glucose_level = level,
                                intervention = intervention,
                            )
                        val added = mainViewModel.addPeriData(peri)
                        if (added) {
                            Toast.makeText(
                                requireContext(),
                                "Record Successfully saved",
                                Toast.LENGTH_SHORT
                            )
                                .show()
                            mListener?.nextFragment(PatientPreparationFragment())

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
        }
        controlCheckBoxes()
        return binding.root
    }

    private fun controlCheckBoxes() {
        binding.checkBoxHealthyPerson.setOnCheckedChangeListener { _, isChecked ->
            updateSelectedCheckboxes(getString(R.string.healthy_person), isChecked)
        }
        binding.checkBoxHypertension.setOnCheckedChangeListener { _, isChecked ->
            updateSelectedCheckboxes(getString(R.string.hypertension), isChecked)
        }
        binding.checkBoxDiabetes.setOnCheckedChangeListener { _, isChecked ->
            updateSelectedCheckboxes(getString(R.string.diabetes), isChecked)
        }
        binding.checkBoxCOPD.setOnCheckedChangeListener { _, isChecked ->
            updateSelectedCheckboxes(getString(R.string.copd), isChecked)
        }
        binding.checkBoxMajorTrauma.setOnCheckedChangeListener { _, isChecked ->
            updateSelectedCheckboxes(getString(R.string.major_trauma), isChecked)
        }
        binding.checkBoxAgeOver75.setOnCheckedChangeListener { _, isChecked ->
            updateSelectedCheckboxes(getString(R.string.age_75_yrs), isChecked)
        }
        binding.checkBoxImmunocompromised.setOnCheckedChangeListener { _, isChecked ->
            updateSelectedCheckboxes(getString(R.string.immunocompromised), isChecked)
        }
        binding.checkBoxMultipleFractures.setOnCheckedChangeListener { _, isChecked ->
            updateSelectedCheckboxes(getString(R.string._7_multiple_fractures), isChecked)
        }
        binding.checkBoxHeartFailure.setOnCheckedChangeListener { _, isChecked ->
            updateSelectedCheckboxes(getString(R.string.heart_failure), isChecked)
        }
        binding.checkBoxKidneyFailure.setOnCheckedChangeListener { _, isChecked ->
            updateSelectedCheckboxes(getString(R.string.kidney_failure), isChecked)
        }
    }

    private fun updateSelectedCheckboxes(checkboxName: String, isChecked: Boolean) {
        if (isChecked) {
            risk_factors.add(checkboxName)
        } else {
            risk_factors.remove(checkboxName)
        }
    }

    private fun handleListeners() {
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

    private fun validate(): Boolean {
        val glucose = binding.edtGlucose.text?.toString()
        val intervention = binding.edtIntervention.text?.toString()
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
        return true
    }
}