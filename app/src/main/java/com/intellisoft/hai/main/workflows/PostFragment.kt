package com.intellisoft.hai.main.workflows

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.intellisoft.hai.databinding.FragmentPostBinding
import com.intellisoft.hai.helper_class.FormatterClass
import com.intellisoft.hai.listeners.OnFragmentInteractionListener
import com.intellisoft.hai.room.HandPreparationData
import com.intellisoft.hai.room.MainViewModel
import com.intellisoft.hai.room.PostOperativeData
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
 * A simple [Fragment] subclass.
 * Use the [PostFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class PostFragment : Fragment() {
    private lateinit var formatterClass: FormatterClass
    private lateinit var mainViewModel: MainViewModel
    private lateinit var binding: FragmentPostBinding
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
        binding = FragmentPostBinding.inflate(layoutInflater)
        mainViewModel = ViewModelProvider(this).get(MainViewModel::class.java)
        formatterClass = FormatterClass()
        binding.btnSubmit.apply {
            setOnClickListener {
                if (validate()) {
                    saveData()
                }
            }
        }
        disableTextInputEditText(binding.edtWound)
        disableTextInputEditText(binding.edtEventDate)
        binding.edtWound.apply {
            setOnClickListener {
                showDatePickerDialog(
                    requireContext(), binding.edtWound, setMaxNow = false, setMinNow = false
                )
            }
        }
        binding.edtEventDate.apply {
            setOnClickListener {
                showDatePickerDialog(
                    requireContext(), binding.edtEventDate, setMaxNow = false, setMinNow = false
                )
            }
        }
        dataControl()
        return binding.root
    }

    private fun dataControl() {
        controlData(
            binding.edtWound,
            binding.woundHolder,
            "Please provide date",
            hasMin = false,
            hasMax = false,
            min = 0,
            max = 0
        )
        controlData(
            binding.edtEventDate,
            binding.eventHolder,
            "Please provide date",
            hasMin = false,
            hasMax = false,
            min = 0,
            max = 0
        )
    }

    private fun saveData() {
        val user = formatterClass.getSharedPref("username", requireContext())
        if (user != null) {
            val patient = formatterClass.getSharedPref("patient", requireContext())
            val ssi =
                if (binding.radioButtonSIP.isChecked) "Superficial Incisional Primary (SIP)" else (if (binding.radioButtonDIP.isChecked) "Deep Incisional Primary (DIP)" else "Organ/Space")
            val enc = formatterClass.getSharedPref("encounter", requireContext())
            val data =
                PostOperativeData(
                    userId = user,
                    patientId = patient.toString(),
                    encounterId = enc.toString(),
                    check_up_date = binding.edtWound.text.toString(),
                    infection_signs = if (binding.radioButtonNo.isChecked) "No" else "Yes",
                    event_date = binding.edtEventDate.text.toString(),
                    ssi = ssi,
                    infection_surgery_time = if (binding.radioButtonNoInfection.isChecked) "No" else "Yes",
                    drainage = if (binding.radioButtonNoDrainage.isChecked) "No" else "Yes",
                    pain = if (binding.radioButtonNoPain.isChecked) "No" else "Yes",
                    erythema = if (binding.radioButtonNoErythema.isChecked) "No" else "Yes",
                    heat = if (binding.radioButtonNoHeat.isChecked) "No" else "Yes",
                    fever = if (binding.radioButtonNoFever.isChecked) "No" else "Yes",
                    incision_opened = if (binding.radioButtonNoIncisionDrained.isChecked) "No" else "Yes",
                    wound_dehisces = if (binding.radioButtonNoWoundDehisces.isChecked) "No" else "Yes",
                    abscess = if (binding.radioButtonNoAbscess.isChecked) "No" else "Yes",
                    sinus = if (binding.radioButtonNoSinusTract.isChecked) "No" else "Yes",
                    hypothermia = if (binding.radioButtonNoHypothermia.isChecked) "No" else "Yes",
                    apnea = if (binding.radioButtonNoApnea.isChecked) "No" else "Yes",
                    bradycardia = if (binding.radioButtonNoBradycardia.isChecked) "No" else "Yes",
                    lethargy = if (binding.radioButtonNoLethargy.isChecked) "No" else "Yes",
                    cough = if (binding.radioButtonNoCough.isChecked) "No" else "Yes",
                    nausea = if (binding.radioButtonNoNausea.isChecked) "No" else "Yes",
                    vomiting = if (binding.radioButtonNoVomiting.isChecked) "No" else "Yes",
                    symptom_other = binding.edtEventDate.text.toString(),
                    samples_sent = if (binding.radioButtonNoSamplesSent.isChecked) "No" else "Yes",
                )
            val added = mainViewModel.addPostOperativeData(data)
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

    private fun validate(): Boolean {

        if (binding.edtWound.text?.toString().isNullOrEmpty()) {
            binding.woundHolder.error = "Please provide date"
            binding.edtWound.requestFocus()
            return false
        }

        if (binding.woundStatusRadioGroup.checkedRadioButtonId == -1) {
            Toast.makeText(
                requireContext(),
                "Please select if wound has signs",
                Toast.LENGTH_SHORT
            ).show()
            return false
        }

        if (binding.edtEventDate.text?.toString().isNullOrEmpty()) {
            binding.eventHolder.error = "Please provide date"
            binding.edtEventDate.requestFocus()
            return false
        }
        if (binding.ssiTypeRadioGroup.checkedRadioButtonId == -1) {
            Toast.makeText(
                requireContext(),
                "Please select Type of SSI",
                Toast.LENGTH_SHORT
            ).show()
            return false
        }

        if (binding.infectionPresenceRadioGroup.checkedRadioButtonId == -1) {
            Toast.makeText(
                requireContext(),
                "Please select Infection Presence",
                Toast.LENGTH_SHORT
            ).show()
            return false
        }

        if (binding.drainageRadioGroup.checkedRadioButtonId == -1) {
            Toast.makeText(
                requireContext(),
                "Please select Symptoms: Drainage or material",
                Toast.LENGTH_SHORT
            ).show()
            return false
        }

        if (binding.painRadioGroup.checkedRadioButtonId == -1) {
            Toast.makeText(
                requireContext(),
                "Please select Symptoms: Pain or tenderness",
                Toast.LENGTH_SHORT
            ).show()
            return false
        }

        if (binding.erythemaRadioGroup.checkedRadioButtonId == -1) {
            Toast.makeText(
                requireContext(),
                "Please select Erythema or redness",
                Toast.LENGTH_SHORT
            ).show()
            return false
        }

        if (binding.heatRadioGroup.checkedRadioButtonId == -1) {
            Toast.makeText(
                requireContext(),
                "Please select Heat",
                Toast.LENGTH_SHORT
            ).show()
            return false
        }

        if (binding.feverRadioGroup.checkedRadioButtonId == -1) {
            Toast.makeText(
                requireContext(),
                "Please select Fever",
                Toast.LENGTH_SHORT
            ).show()
            return false
        }

        if (binding.incisionDrainedRadioGroup.checkedRadioButtonId == -1) {
            Toast.makeText(
                requireContext(),
                "Please select Incision deliberately opened/drained",
                Toast.LENGTH_SHORT
            ).show()
            return false
        }

        if (binding.woundDehiscesRadioGroup.checkedRadioButtonId == -1) {
            Toast.makeText(
                requireContext(),
                "Please select Wound spontaneously dehisces",
                Toast.LENGTH_SHORT
            ).show()
            return false
        }

        if (binding.abscessRadioGroup.checkedRadioButtonId == -1) {
            Toast.makeText(
                requireContext(),
                "Please select Abscess",
                Toast.LENGTH_SHORT
            ).show()
            return false
        }

        if (binding.sinusTractRadioGroup.checkedRadioButtonId == -1) {
            Toast.makeText(
                requireContext(),
                "Please select Sinus tract",
                Toast.LENGTH_SHORT
            ).show()
            return false
        }

        if (binding.hypothermiaRadioGroup.checkedRadioButtonId == -1) {
            Toast.makeText(
                requireContext(),
                "Please select Hypothermia",
                Toast.LENGTH_SHORT
            ).show()
            return false
        }

        if (binding.apneaRadioGroup.checkedRadioButtonId == -1) {
            Toast.makeText(
                requireContext(),
                "Please select Apnea",
                Toast.LENGTH_SHORT
            ).show()
            return false
        }

        if (binding.bradycardiaRadioGroup.checkedRadioButtonId == -1) {
            Toast.makeText(
                requireContext(),
                "Please select Bradycardia",
                Toast.LENGTH_SHORT
            ).show()
            return false
        }

        if (binding.lethargyRadioGroup.checkedRadioButtonId == -1) {
            Toast.makeText(
                requireContext(),
                "Please select Lethargy",
                Toast.LENGTH_SHORT
            ).show()
            return false
        }

        if (binding.coughRadioGroup.checkedRadioButtonId == -1) {
            Toast.makeText(
                requireContext(),
                "Please select Cough",
                Toast.LENGTH_SHORT
            ).show()
            return false
        }

        if (binding.nauseaRadioGroup.checkedRadioButtonId == -1) {
            Toast.makeText(
                requireContext(),
                "Please select Nausea",
                Toast.LENGTH_SHORT
            ).show()
            return false
        }

        if (binding.vomitingRadioGroup.checkedRadioButtonId == -1) {
            Toast.makeText(
                requireContext(),
                "Please select Vomiting",
                Toast.LENGTH_SHORT
            ).show()
            return false
        }
        if (binding.edtOther.text?.toString().isNullOrEmpty()) {
            binding.otherHolder.error = "Please specify if other symptoms are present"
            binding.edtOther.requestFocus()
            return false
        }
        if (binding.samplesSentForCultureRadioGroup.checkedRadioButtonId == -1) {
            Toast.makeText(
                requireContext(),
                "Please select If Samples sent for culture?",
                Toast.LENGTH_SHORT
            ).show()
            return false
        }

        return true
    }
}