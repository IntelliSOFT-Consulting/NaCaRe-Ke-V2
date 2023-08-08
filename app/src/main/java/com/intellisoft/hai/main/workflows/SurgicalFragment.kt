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
import com.intellisoft.hai.databinding.FragmentSurgicalBinding
import com.intellisoft.hai.helper_class.FormatterClass
import com.intellisoft.hai.listeners.OnFragmentInteractionListener
import com.intellisoft.hai.room.MainViewModel
import com.intellisoft.hai.room.SurgicalSiteData
import com.intellisoft.hai.util.AppUtils
import com.intellisoft.hai.util.AppUtils.controlData
import com.intellisoft.hai.util.AppUtils.disableTextInputEditText
import com.intellisoft.hai.util.AppUtils.generateUuid

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass. Use the [SurgicalFragment.newInstance] factory method to create an
 * instance of this fragment.
 */
class SurgicalFragment : Fragment() {
  private lateinit var binding: FragmentSurgicalBinding
  private lateinit var formatterClass: FormatterClass
  private lateinit var mainViewModel: MainViewModel
  private var mListener: OnFragmentInteractionListener? = null
  private lateinit var specimen: String
  private lateinit var lab: String
  private lateinit var culture: String
  private lateinit var micro: String
  private lateinit var amox: String
  private lateinit var armi: String
  private lateinit var amp: String
  private lateinit var clo: String
  private lateinit var cot: String
  private lateinit var cep: String
  private lateinit var cip: String
  private lateinit var coli: String
  private lateinit var cefo: String
  private lateinit var ery: String
  private lateinit var gen: String
  private lateinit var nali: String
  private lateinit var nor: String
  private lateinit var peni: String
  private lateinit var tob: String
  private lateinit var van: String
  private lateinit var cefta: String
  private lateinit var ceftri: String
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
    binding = FragmentSurgicalBinding.inflate(layoutInflater)
    mainViewModel = ViewModelProvider(this).get(MainViewModel::class.java)
    formatterClass = FormatterClass()
    disableTextInputEditText(binding.edtDate)
    disableTextInputEditText(binding.edtReceptionDate)
    disableTextInputEditText(binding.edtFindingsDate)
    disableTextInputEditText(binding.edtProcessingDate)
    binding.btnSubmit.apply {
      setOnClickListener {
        if (validate()) {
          val user = formatterClass.getSharedPref("username", requireContext())
          val patient = formatterClass.getSharedPref("patient", requireContext())
          val date = binding.edtDate.text?.toString()
          val reception = binding.edtReceptionDate.text?.toString()
          val processing = binding.edtProcessingDate.text?.toString()
          val finding = binding.edtFindingsDate.text?.toString()
          if (user != null) {
            val sur =
                SurgicalSiteData(
                    userId = user,
                    patientId = patient.toString(),
                    encounterId = generateUuid(),
                    lab_type = lab,
                    specimen = specimen,
                    sample_collection_date = date.toString(),
                    sample_reception_date = reception.toString(),
                    sample_processing_date = processing.toString(),
                    culture_finding_date = finding.toString(),
                    culture_finding = culture,
                    organism_isolated = micro,
                    acinetobacter_species = "",
                    other_pathogen_species = "",
                    amoxicillin = amox,
                    Amikacin = armi,
                    ampicillin = amp,
                    cloxacillin = clo,
                    cotrimoxazole = cot,
                    cephalexin = cep,
                    ciprofloxacin = cip,
                    colistin_sulphate = coli,
                    cefotaxime = cefo,
                    erythromycin = ery,
                    gentamycin = gen,
                    nalidixic_acid = nali,
                    norfloxacin = nor,
                    penicillin = peni,
                    tobramycin = tob,
                    vancomycin = van,
                    ceftazidime = cefta,
                    ceftriaxone = ceftri,
                )
            val added = mainViewModel.addSurgicalSiteData(sur)
            if (added) {
              Toast.makeText(requireContext(), "Record Successfully saved", Toast.LENGTH_SHORT)
                  .show()
              mListener?.launchAction()
            } else {
              Toast.makeText(
                      requireContext(), "Encountered problems saving the data", Toast.LENGTH_SHORT)
                  .show()
            }
          }
        }
      }
    }
    dataController()
    handleOnClicks()
    return binding.root
  }

  private fun dataController() {
    controlData(
        binding.edtDate,
        binding.dateHolder,
        "Please provide date",
        hasMin = false,
        hasMax = false,
        min = 0,
        max = 0)
    controlData(
        binding.edtReceptionDate,
        binding.receptionHolder,
        "Please provide date",
        hasMin = false,
        hasMax = false,
        min = 0,
        max = 0)
    controlData(
        binding.edtFindingsDate,
        binding.findingsHolder,
        "Please provide date",
        hasMin = false,
        hasMax = false,
        min = 0,
        max = 0)
    controlData(
        binding.edtProcessingDate,
        binding.processingHolder,
        "Please provide date",
        hasMin = false,
        hasMax = false,
        min = 0,
        max = 0)
  }

  private fun validate(): Boolean {
    val date = binding.edtDate.text?.toString()
    val reception = binding.edtReceptionDate.text?.toString()
    val processing = binding.edtProcessingDate.text?.toString()
    val finding = binding.edtFindingsDate.text?.toString()
    if (date.isNullOrEmpty()) {
      binding.dateHolder.error = "Please provide  date"
      binding.edtDate.requestFocus()
      return false
    }
    if (reception.isNullOrEmpty()) {
      binding.receptionHolder.error = "Please provide  date"
      binding.edtReceptionDate.requestFocus()
      return false
    }
    if (processing.isNullOrEmpty()) {
      binding.processingHolder.error = "Please provide  date"
      binding.edtProcessingDate.requestFocus()
      return false
    }
    if (finding.isNullOrEmpty()) {
      binding.findingsHolder.error = "Please provide  date"
      binding.edtFindingsDate.requestFocus()
      return false
    }

    return true
  }
  private fun handleOnClicks() {
    binding.specimenRadioGroup.setOnCheckedChangeListener { _, checkedId ->
      val selectedRadioButton = binding.root.findViewById<RadioButton>(checkedId)
      specimen = selectedRadioButton.text.toString()
      if (specimen == "Other") {
        binding.edtSpecimenOther.text = null
        binding.specimenOtherHolder.visibility = View.VISIBLE
      } else {
        binding.specimenOtherHolder.visibility = View.GONE
      }
    }
    binding.typeOfBSIRadioGroup.setOnCheckedChangeListener { _, checkedId ->
      val selectedRadioButton = binding.root.findViewById<RadioButton>(checkedId)
      lab = selectedRadioButton.text.toString()
    }

    binding.radioGroupCulture.setOnCheckedChangeListener { _, checkedId ->
      val selectedRadioButton = binding.root.findViewById<RadioButton>(checkedId)
      culture = selectedRadioButton.text.toString()
    }
    binding.microorganismRadioGroup.setOnCheckedChangeListener { _, checkedId ->
      val selectedRadioButton = binding.root.findViewById<RadioButton>(checkedId)
      micro = selectedRadioButton.text.toString()
    }
    binding.radioGroupAmoxicillin.setOnCheckedChangeListener { _, checkedId ->
      val selectedRadioButton = binding.root.findViewById<RadioButton>(checkedId)
      amox = selectedRadioButton.text.toString()
    }

    /*  private lateinit var : String
    private lateinit var : String
    private lateinit var : String
    private lateinit var : String
    private lateinit var armi: String
    private lateinit var amp: String
    private lateinit var clo: String
    private lateinit var cot: String
    private lateinit var cep: String
    private lateinit var cip: String
    private lateinit var coli: String
    private lateinit var cefo: String
    private lateinit var ery: String
    private lateinit var gen: String
    private lateinit var nali: String
    private lateinit var nor: String
    private lateinit var peni: String
    private lateinit var tob: String
    private lateinit var van: String
    private lateinit var cefta: String
    private lateinit var ceftri: String*/
    binding.edtDate.apply {
      setOnClickListener {
        AppUtils.showDatePickerDialog(
            requireContext(), binding.edtDate, setMaxNow = false, setMinNow = false)
      }
    }
    binding.edtReceptionDate.apply {
      setOnClickListener {
        AppUtils.showDatePickerDialog(
            requireContext(), binding.edtReceptionDate, setMaxNow = false, setMinNow = false)
      }
    }
    binding.edtProcessingDate.apply {
      setOnClickListener {
        AppUtils.showDatePickerDialog(
            requireContext(), binding.edtProcessingDate, setMaxNow = false, setMinNow = false)
      }
    }
    binding.edtFindingsDate.apply {
      setOnClickListener {
        AppUtils.showDatePickerDialog(
            requireContext(), binding.edtFindingsDate, setMaxNow = false, setMinNow = false)
      }
    }
  }
}
