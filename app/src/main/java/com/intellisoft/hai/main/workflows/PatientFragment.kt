package com.intellisoft.hai.main.workflows

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.intellisoft.hai.databinding.FragmentPatientBinding
import com.intellisoft.hai.helper_class.FormatterClass
import com.intellisoft.hai.room.MainViewModel
import com.intellisoft.hai.room.PreparationData
import com.intellisoft.hai.util.AppUtils
import com.intellisoft.hai.util.AppUtils.controlData
import com.intellisoft.hai.util.AppUtils.disableTextInputEditText
import com.intellisoft.hai.util.AppUtils.generateUuid

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass. Use the [PatientFragment.newInstance] factory method to create an
 * instance of this fragment.
 */
class PatientFragment : Fragment() {
  private lateinit var binding: FragmentPatientBinding
  private lateinit var formatterClass: FormatterClass
  private lateinit var mainViewModel: MainViewModel
  override fun onCreateView(
      inflater: LayoutInflater,
      container: ViewGroup?,
      savedInstanceState: Bundle?
  ): View? {
    // Inflate the layout for this fragment
    binding = FragmentPatientBinding.inflate(layoutInflater)
    disableTextInputEditText(binding.edtDate)
    mainViewModel = ViewModelProvider(this).get(MainViewModel::class.java)
    formatterClass = FormatterClass()
    binding.edtDate.apply {
      setOnClickListener {
        AppUtils.showDatePickerDialog(
            requireContext(), binding.edtDate, setMaxNow = false, setMinNow = true)
      }
    }
    binding.btnSubmit.apply {
      setOnClickListener {
        if (validate()) {
          val user = formatterClass.getSharedPref("username", requireContext())
          val patient = formatterClass.getSharedPref("patient", requireContext())
          val date = binding.edtDate.text?.toString()
          if (user != null) {
            val pre_bath = if (binding.radioNo.isChecked) "No" else "Yes"
            val soap_used = if (binding.radioAntibacterialNo.isChecked) "No" else "Yes"
            val hair_removal =
                if (binding.radioHairNo.isChecked) {
                  "No"
                } else if (binding.radioHairRazor.isChecked) {
                  "Razor"
                } else {
                  "Clippers"
                }

            val peri =
                PreparationData(
                    userId = user,
                    patientId = patient.toString(),
                    encounterId = generateUuid(),
                    pre_bath = pre_bath,
                    soap_used = soap_used,
                    hair_removal = hair_removal,
                    date_of_removal = date)
            val added = mainViewModel.addPreparationData(peri)
            if (added) {
              Toast.makeText(requireContext(), "Record Successfully saved", Toast.LENGTH_SHORT)
                  .show()
              clearData()
            } else {
              Toast.makeText(
                      requireContext(),
                      "Encountered problems registering patient",
                      Toast.LENGTH_SHORT)
                  .show()
            }
          } else {
            Toast.makeText(requireContext(), "Please check user account", Toast.LENGTH_SHORT).show()
          }
        }
      }
    }

    controlData(
        binding.edtDate,
        binding.dateHolder,
        "Please provide date",
        hasMin = false,
        hasMax = false,
        min = 0,
        max = 0)
    return binding.root
    //    return inflater.inflate(R.layout.fragment_patient, container, false)
  }

  private fun clearData() {}

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
        !binding.radioHairRazor.isChecked) {
      Toast.makeText(requireContext(), "please specify hair removal", Toast.LENGTH_SHORT).show()
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
