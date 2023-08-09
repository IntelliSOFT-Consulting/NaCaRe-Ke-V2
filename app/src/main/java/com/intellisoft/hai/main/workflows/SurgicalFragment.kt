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
import com.intellisoft.hai.util.AppUtils.showDatePickerDialog

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
    private lateinit var acine: String
    private lateinit var entero: String
    private lateinit var patho: String
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
                    val specimen_other = binding.edtSpecimenOther.text?.toString()
                    val organism_other = binding.edtOrganismOther.text?.toString()
                    if (user != null) {
                        val sur =
                            SurgicalSiteData(
                                userId = user,
                                patientId = patient.toString(),
                                encounterId = generateUuid(),
                                lab_type = lab,
                                specimen = specimen,
                                specimen_other = specimen_other.toString(),
                                sample_collection_date = date.toString(),
                                sample_reception_date = reception.toString(),
                                sample_processing_date = processing.toString(),
                                culture_finding_date = finding.toString(),
                                culture_finding = culture,
                                organism_isolated = micro,
                                organism_other = organism_other.toString(),
                                acinetobacter_species = acine,
                                entero_bacter = entero,
                                other_pathogen_species = patho,
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
                            Toast.makeText(
                                requireContext(),
                                "Record Successfully saved",
                                Toast.LENGTH_SHORT
                            )
                                .show()
                            mListener?.launchAction()
                        } else {
                            Toast.makeText(
                                requireContext(),
                                "Encountered problems saving the data",
                                Toast.LENGTH_SHORT
                            )
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
            max = 0
        )
        controlData(
            binding.edtReceptionDate,
            binding.receptionHolder,
            "Please provide date",
            hasMin = false,
            hasMax = false,
            min = 0,
            max = 0
        )
        controlData(
            binding.edtFindingsDate,
            binding.findingsHolder,
            "Please provide date",
            hasMin = false,
            hasMax = false,
            min = 0,
            max = 0
        )
        controlData(
            binding.edtProcessingDate,
            binding.processingHolder,
            "Please provide date",
            hasMin = false,
            hasMax = false,
            min = 0,
            max = 0
        )
        controlData(
            binding.edtSpecimenOther,
            binding.specimenOtherHolder,
            "Please specify specimen",
            hasMin = false,
            hasMax = false,
            min = 0,
            max = 0
        )
        controlData(
            binding.edtOrganismOther,
            binding.otherOrganismHolder,
            "Please specify specimen",
            hasMin = false,
            hasMax = false,
            min = 0,
            max = 0
        )
        controlData(
            binding.edtAcinetobacter,
            binding.acinetobacterHolder,
            "Please specify acine",
            hasMin = false,
            hasMax = false,
            min = 0,
            max = 0
        )
        controlData(
            binding.edtEnterobacter,
            binding.enterobacterHolder,
            "Please specify acine",
            hasMin = false,
            hasMax = false,
            min = 0,
            max = 0
        )
        controlData(
            binding.edtPathogen,
            binding.pathogenHolder,
            "Please specify pathogen",
            hasMin = false,
            hasMax = false,
            min = 0,
            max = 0
        )
    }

    private fun validate(): Boolean {
        val date = binding.edtDate.text?.toString()
        val reception = binding.edtReceptionDate.text?.toString()
        val processing = binding.edtProcessingDate.text?.toString()
        val finding = binding.edtFindingsDate.text?.toString()
        val other_spceciment = binding.edtSpecimenOther.text?.toString()

        // check lab bsi
        if (!binding.radioButtonKnownPathogen.isChecked &&
            !binding.radioButtonCommonCommensal.isChecked
        ) {
            Toast.makeText(requireContext(), "Please select lab BSI", Toast.LENGTH_SHORT).show()
            return false
        }
        if (!binding.radioButtonPus.isChecked &&
            !binding.radioButtonExudate.isChecked &&
            !binding.radioButtonTissue.isChecked &&
            !binding.radioButtonPurulentDrainage.isChecked &&
            !binding.radioButtonBone.isChecked &&
            !binding.radioButtonOther.isChecked
        ) {
            Toast.makeText(requireContext(), "Please specify specimen", Toast.LENGTH_SHORT).show()
            return false
        }
        if (binding.radioButtonOther.isChecked) {
            if (other_spceciment.isNullOrEmpty()) {
                binding.specimenOtherHolder.error = "Please specify specimen"
                binding.edtSpecimenOther.requestFocus()
                return false
            }
        }
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
        if (!binding.radioCulture0.isChecked && !binding.radioCulture1.isChecked) {
            Toast.makeText(requireContext(), "Please select culture ", Toast.LENGTH_SHORT).show()
            return false
        }
        if (!binding.radioButtonNoGrowth.isChecked &&
            !binding.radioButtonStaphCoagNeg.isChecked &&
            !binding.radioButtonEnterococcusFaecium.isChecked &&
            !binding.radioButtonEnterococcusFaecalis.isChecked &&
            !binding.radioButtonEnterococcusSpp.isChecked &&
            !binding.radioButtonStaphAureus.isChecked &&
            !binding.radioButtonAcinetobacterBaumannii.isChecked &&
            !binding.radioButtonAcinetobacterSpp.isChecked &&
            !binding.radioButtonEscherichiaColi.isChecked &&
            !binding.radioButtonEnterobacterSpp.isChecked &&
            !binding.radioButtonOtherSpecify.isChecked
        ) {
            Toast.makeText(requireContext(), "Please select organism ", Toast.LENGTH_SHORT).show()
            return false
        }
        if (binding.radioButtonOtherSpecify.isChecked) {
            val _other = binding.edtOrganismOther.text?.toString()
            if (_other.isNullOrEmpty()) {
                binding.otherOrganismHolder.error = "Please provide input"
                binding.edtOrganismOther.requestFocus()
                return false
            }
        }
        acine = binding.edtAcinetobacter.text.toString()
        if (acine.isEmpty()) {
            binding.acinetobacterHolder.error = "Please provide input"
            binding.edtAcinetobacter.requestFocus()
            return false
        }

        entero = binding.edtEnterobacter.text.toString()
        if (entero.isEmpty()) {
            binding.enterobacterHolder.error = "Please provide input"
            binding.edtEnterobacter.requestFocus()
            return false
        }
        patho = binding.edtPathogen.text.toString()
        if (patho.isEmpty()) {
            binding.pathogenHolder.error = "Please provide input"
            binding.edtPathogen.requestFocus()
            return false
        }
        if (!binding.radioAmoxicillin0.isChecked &&
            !binding.radioAmoxicillin1.isChecked &&
            !binding.radioAmoxicillin2.isChecked
        ) {
            Toast.makeText(requireContext(), "Please specify Amoxicillin", Toast.LENGTH_SHORT).show()
            return false
        }
        if (!binding.radioAmikacin0.isChecked && !binding.radioAmikacin1.isChecked && !binding.radioAmikacin2.isChecked
        ) {
            Toast.makeText(requireContext(), "Please specify Amikacin", Toast.LENGTH_SHORT).show()
            return false
        }
        if (!binding.radioAmpicillin0.isChecked && !binding.radioAmpicillin1.isChecked && !binding.radioAmpicillin2.isChecked
        ) {
            Toast.makeText(requireContext(), "Please specify Ampicillin", Toast.LENGTH_SHORT).show()
            return false
        }
        if (!binding.radioCloxacillin0.isChecked && !binding.radioCloxacillin1.isChecked && !binding.radioCloxacillin2.isChecked
        ) {
            Toast.makeText(requireContext(), "Please specify Cloxacillin", Toast.LENGTH_SHORT).show()
            return false
        }
        if (!binding.radioCotrimoxazole0.isChecked && !binding.radioCotrimoxazole1.isChecked && !binding.radioCotrimoxazole2.isChecked
        ) {
            Toast.makeText(requireContext(), "Please specify Cotrimoxazole", Toast.LENGTH_SHORT).show()
            return false
        }
        if (!binding.radioCephalexin0.isChecked && !binding.radioCephalexin1.isChecked && !binding.radioCephalexin2.isChecked
        ) {
            Toast.makeText(requireContext(), "Please specify Cephalexin", Toast.LENGTH_SHORT).show()
            return false
        }
        if (!binding.radioCiprofloxacin0.isChecked && !binding.radioCiprofloxacin1.isChecked && !binding.radioCiprofloxacin2.isChecked
        ) {
            Toast.makeText(requireContext(), "Please specify Ciprofloxacin", Toast.LENGTH_SHORT).show()
            return false
        }
        if (!binding.radioColistinSulphate0.isChecked && !binding.radioColistinSulphate1.isChecked && !binding.radioColistinSulphate2.isChecked
        ) {
            Toast.makeText(requireContext(), "Please specify Colistin Sulphate", Toast.LENGTH_SHORT).show()
            return false
        }
        if (!binding.radioCefotaxime0.isChecked && !binding.radioCefotaxime1.isChecked && !binding.radioCefotaxime2.isChecked
        ) {
            Toast.makeText(requireContext(), "Please specify Cefotaxime", Toast.LENGTH_SHORT).show()
            return false
        }
        if (!binding.radioErythromycin0.isChecked && !binding.radioErythromycin1.isChecked && !binding.radioErythromycin2.isChecked
        ) {
            Toast.makeText(requireContext(), "Please specify Erythromycin", Toast.LENGTH_SHORT).show()
            return false
        }
        if (!binding.radioGentamycin0.isChecked && !binding.radioGentamycin1.isChecked && !binding.radioGentamycin2.isChecked
        ) {
            Toast.makeText(requireContext(), "Please specify Gentamycin", Toast.LENGTH_SHORT).show()
            return false
        }
        if (!binding.radioNalidixicAcid0.isChecked && !binding.radioNalidixicAcid1.isChecked && !binding.radioNalidixicAcid1.isChecked
        ) {
            Toast.makeText(requireContext(), "Please specify Nalidixic Acid ", Toast.LENGTH_SHORT).show()
            return false
        }
        if (!binding.radioNorfloxacin0.isChecked && !binding.radioNorfloxacin1.isChecked && !binding.radioNorfloxacin2.isChecked
        ) {
            Toast.makeText(requireContext(), "Please specify Norfloxacin ", Toast.LENGTH_SHORT).show()
            return false
        }
        if (!binding.radioPenicillin0.isChecked && !binding.radioPenicillin1.isChecked && !binding.radioPenicillin2.isChecked
        ) {
            Toast.makeText(requireContext(), "Please specify Penicillin ", Toast.LENGTH_SHORT).show()
            return false
        }
        if (!binding.radioTobramycin0.isChecked && !binding.radioTobramycin1.isChecked && !binding.radioTobramycin2.isChecked
        ) {
            Toast.makeText(requireContext(), "Please specify Tobramycin ", Toast.LENGTH_SHORT).show()
            return false
        }
        if (!binding.radioVancomycin0.isChecked && !binding.radioVancomycin1.isChecked && !binding.radioVancomycin2.isChecked
        ) {
            Toast.makeText(requireContext(), "Please specify Vancomycin ", Toast.LENGTH_SHORT).show()
            return false
        }
        if (!binding.radioCeftazidime0.isChecked && !binding.radioCeftazidime1.isChecked && !binding.radioCeftazidime2.isChecked
        ) {
            Toast.makeText(requireContext(), "Please specify Ceftazidime ", Toast.LENGTH_SHORT).show()
            return false
        }
        if (!binding.radioCeftriaxone0.isChecked && !binding.radioCeftriaxone1.isChecked && !binding.radioCeftriaxone2.isChecked
        ) {
            Toast.makeText(requireContext(), "Please specify Ceftriaxone ", Toast.LENGTH_SHORT).show()
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
            if (micro == "Other (specify)") {
                binding.edtOrganismOther.text = null
                binding.otherOrganismHolder.visibility = View.VISIBLE
            } else {
                binding.otherOrganismHolder.visibility = View.GONE
            }
        }
        binding.radioGroupAmoxicillin.setOnCheckedChangeListener { _, checkedId ->
            val selectedRadioButton = binding.root.findViewById<RadioButton>(checkedId)
            amox = selectedRadioButton.text.toString()
        }
        binding.radioGroupAmikacin.setOnCheckedChangeListener { _, checkedId ->
            val selectedRadioButton = binding.root.findViewById<RadioButton>(checkedId)
            armi = selectedRadioButton.text.toString()
        }
        binding.radioGroupAmpicillin.setOnCheckedChangeListener { _, checkedId ->
            val selectedRadioButton = binding.root.findViewById<RadioButton>(checkedId)
            amp = selectedRadioButton.text.toString()
        }

        binding.radioGroupCloxacillin.setOnCheckedChangeListener { _, checkedId ->
            val selectedRadioButton = binding.root.findViewById<RadioButton>(checkedId)
            clo = selectedRadioButton.text.toString()
        }

        binding.radioGroupCotrimoxazole.setOnCheckedChangeListener { _, checkedId ->
            val selectedRadioButton = binding.root.findViewById<RadioButton>(checkedId)
            cot = selectedRadioButton.text.toString()
        }
        binding.radioGroupCephalexin.setOnCheckedChangeListener { _, checkedId ->
            val selectedRadioButton = binding.root.findViewById<RadioButton>(checkedId)
            cep = selectedRadioButton.text.toString()
        }

        binding.radioGroupCiprofloxacin.setOnCheckedChangeListener { _, checkedId ->
            val selectedRadioButton = binding.root.findViewById<RadioButton>(checkedId)
            cip = selectedRadioButton.text.toString()
        }

        binding.radioGroupColistinSulphate.setOnCheckedChangeListener { _, checkedId ->
            val selectedRadioButton = binding.root.findViewById<RadioButton>(checkedId)
            coli = selectedRadioButton.text.toString()
        }
        binding.radioGroupCefotaxime.setOnCheckedChangeListener { _, checkedId ->
            val selectedRadioButton = binding.root.findViewById<RadioButton>(checkedId)
            cefo = selectedRadioButton.text.toString()
        }
        binding.radioGroupErythromycin.setOnCheckedChangeListener { _, checkedId ->
            val selectedRadioButton = binding.root.findViewById<RadioButton>(checkedId)
            ery = selectedRadioButton.text.toString()
        }
        binding.radioGroupGentamycin.setOnCheckedChangeListener { _, checkedId ->
            val selectedRadioButton = binding.root.findViewById<RadioButton>(checkedId)
            gen = selectedRadioButton.text.toString()
        }
        binding.radioGroupNalidixicAcid.setOnCheckedChangeListener { _, checkedId ->
            val selectedRadioButton = binding.root.findViewById<RadioButton>(checkedId)
            nali = selectedRadioButton.text.toString()
        }
        binding.radioGroupNorfloxacin.setOnCheckedChangeListener { _, checkedId ->
            val selectedRadioButton = binding.root.findViewById<RadioButton>(checkedId)
            nor = selectedRadioButton.text.toString()
        }
        binding.radioGroupPenicillin.setOnCheckedChangeListener { _, checkedId ->
            val selectedRadioButton = binding.root.findViewById<RadioButton>(checkedId)
            peni = selectedRadioButton.text.toString()
        }

        binding.radioGroupTobramycin.setOnCheckedChangeListener { _, checkedId ->
            val selectedRadioButton = binding.root.findViewById<RadioButton>(checkedId)
            tob = selectedRadioButton.text.toString()
        }

        binding.radioGroupVancomycin.setOnCheckedChangeListener { _, checkedId ->
            val selectedRadioButton = binding.root.findViewById<RadioButton>(checkedId)
            van = selectedRadioButton.text.toString()
        }
        binding.radioGroupCeftazidime.setOnCheckedChangeListener { _, checkedId ->
            val selectedRadioButton = binding.root.findViewById<RadioButton>(checkedId)
            cefta = selectedRadioButton.text.toString()
        }
        binding.radioGroupCeftriaxone.setOnCheckedChangeListener { _, checkedId ->
            val selectedRadioButton = binding.root.findViewById<RadioButton>(checkedId)
            ceftri = selectedRadioButton.text.toString()
        }

        binding.edtDate.apply {
            setOnClickListener {
                showDatePickerDialog(
                    requireContext(), binding.edtDate, setMaxNow = false, setMinNow = false
                )
            }
        }
        binding.edtReceptionDate.apply {
            setOnClickListener {
                showDatePickerDialog(
                    requireContext(), binding.edtReceptionDate, setMaxNow = false, setMinNow = false
                )
            }
        }
        binding.edtProcessingDate.apply {
            setOnClickListener {
                showDatePickerDialog(
                    requireContext(),
                    binding.edtProcessingDate,
                    setMaxNow = false,
                    setMinNow = false
                )
            }
        }
        binding.edtFindingsDate.apply {
            setOnClickListener {
                showDatePickerDialog(
                    requireContext(), binding.edtFindingsDate, setMaxNow = false, setMinNow = false
                )
            }
        }
    }
}
