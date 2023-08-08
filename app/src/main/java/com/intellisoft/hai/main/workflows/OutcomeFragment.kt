package com.intellisoft.hai.main.workflows

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.intellisoft.hai.databinding.FragmentOutcomeBinding
import com.intellisoft.hai.helper_class.FormatterClass
import com.intellisoft.hai.listeners.OnFragmentInteractionListener
import com.intellisoft.hai.room.MainViewModel
import com.intellisoft.hai.room.OutcomeData
import com.intellisoft.hai.util.AppUtils
import com.intellisoft.hai.util.AppUtils.controlData
import com.intellisoft.hai.util.AppUtils.disableTextInputEditText
import com.intellisoft.hai.util.AppUtils.generateUuid
import com.intellisoft.hai.util.AppUtils.showDatePickerDialog

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass. Use the [OutcomeFragment.newInstance] factory method to create an
 * instance of this fragment.
 */
class OutcomeFragment : Fragment() {
  // TODO: Rename and change types of parameters
  private lateinit var binding: FragmentOutcomeBinding
  private lateinit var formatterClass: FormatterClass
  private lateinit var mainViewModel: MainViewModel
  private var mListener: OnFragmentInteractionListener? = null
  private lateinit var selectedOutcome: String

  override fun onAttach(context: Context) {
    super.onAttach(context)
    if (context is OnFragmentInteractionListener) {
      mListener = context
    } else {
      throw RuntimeException("$context must implement OnFragmentInteractionListener")
    }
  }
  override fun onCreateView(
      inflater: LayoutInflater,
      container: ViewGroup?,
      savedInstanceState: Bundle?
  ): View? {
    // Inflate the layout for this fragment
    binding = FragmentOutcomeBinding.inflate(layoutInflater)
    disableTextInputEditText(binding.edtOutcomeDate)
    mainViewModel = ViewModelProvider(this).get(MainViewModel::class.java)
    formatterClass = FormatterClass()

    controlData(
      binding.edtOutcomeDate,
      binding.dateHolder,
      "Please provide date",
      hasMin = false,
      hasMax = false,
      min = 0,
      max = 0
    )
    binding.radioGroupOutcome.setOnCheckedChangeListener { _, checkedId ->
      val selectedRadioButton = binding.root.findViewById<RadioButton>(checkedId)
      selectedOutcome = selectedRadioButton.text.toString()

      // Now you can use the selectedOutcome as needed
      // For example, you can display it or perform further actions
    }
    binding.edtOutcomeDate.apply {
      setOnClickListener {
        showDatePickerDialog(
            requireContext(), binding.edtOutcomeDate, setMaxNow = false, setMinNow = false)
      }
    }
    binding.btnSubmit.apply {
      setOnClickListener {
        if (validate()) {
          val user = formatterClass.getSharedPref("username", requireContext())
          val patient = formatterClass.getSharedPref("patient", requireContext())
          val date = binding.edtOutcomeDate.text?.toString()
          if (user != null) {
            val outcome =
                OutcomeData(
                    userId = user,
                    patientId = patient.toString(),
                    encounterId = generateUuid(),
                    date = date.toString(),
                    status = selectedOutcome,
                )
            val added = mainViewModel.addOutcomeData(outcome)
            if (added) {
              Toast.makeText(requireContext(), "Record Successfully saved", Toast.LENGTH_SHORT)
                .show()
              mListener?.launchAction()
            } else {
              Toast.makeText(
                requireContext(),
                "Encountered problems registering patient",
                Toast.LENGTH_SHORT)
                .show()
            }
          }
        }
      }
    }
    return binding.root
  }
  private fun validate(): Boolean {
    val date = binding.edtOutcomeDate.text?.toString()
    if (date.isNullOrEmpty()) {
      binding.dateHolder.error = "Please provide outcome date"
      binding.edtOutcomeDate.requestFocus()
      return false
    }
    if (!binding.radioDischarged.isChecked &&
        !binding.radioTransferredAnother.isChecked &&
        !binding.radioTransferredWithin.isChecked &&
        !binding.radioDied.isChecked &&
        !binding.radioAbsconded.isChecked &&
        !binding.radioUnknown.isChecked) {
      Toast.makeText(requireContext(), "please specify outcome", Toast.LENGTH_SHORT).show()
      return false
    }

    return true
  }
}
