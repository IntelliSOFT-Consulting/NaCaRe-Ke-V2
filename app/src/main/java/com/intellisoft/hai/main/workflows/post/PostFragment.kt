package com.intellisoft.hai.main.workflows.post

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import com.intellisoft.hai.R
import com.intellisoft.hai.databinding.FragmentPostBinding
import com.intellisoft.hai.helper_class.FormatterClass
import com.intellisoft.hai.room.Converters
import com.intellisoft.hai.room.MainViewModel
import com.intellisoft.hai.room.PostOperativeData
import com.intellisoft.hai.util.AppUtils

/**
 * A simple [Fragment] subclass.
 * Use the [PostFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class PostFragment : Fragment() {
    private lateinit var formatterClass: FormatterClass
    private lateinit var mainViewModel: MainViewModel
    private lateinit var binding: FragmentPostBinding
    private lateinit var encounterId: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentPostBinding.inflate(layoutInflater)
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
                if (validate()) {
                    saveData()
                }
            }
        }

        val data = formatterClass.getSharedPref("post", requireContext())
        if (data != null) {
            loadInitialData(data)
        } else {
            encounterId = AppUtils.generateUuid()
        }
        return binding.root
    }

    private fun loadInitialData(data: String) {
        val converters = Converters()
        val data: PostOperativeData = converters.postFromJson(data)
        encounterId = data.encounterId
        binding.apply {
            edtOther.setText(data.symptom_other)

            //No Responses
            if (data.drainage == "No") {
                radioButtonNoDrainage.isChecked = true
            }
            if (data.pain == "No") {
                radioButtonNoPain.isChecked = true
            }
            if (data.erythema == "No") {
                radioButtonNoErythema.isChecked = true
            }
            if (data.heat == "No") {
                radioButtonNoHeat.isChecked = true
            }
            if (data.fever == "No") {
                radioButtonNoFever.isChecked = true
            }
            if (data.incision_opened == "No") {
                radioButtonNoIncisionDrained.isChecked = true
            }
            if (data.wound_dehisces == "No") {
                radioButtonNoWoundDehisces.isChecked = true
            }
            if (data.abscess == "No") {
                radioButtonNoAbscess.isChecked = true
            }
            if (data.sinus == "No") {
                radioButtonNoSinusTract.isChecked = true
            }
            if (data.hypothermia == "No") {
                radioButtonNoHypothermia.isChecked = true
            }
            if (data.apnea == "No") {
                radioButtonNoApnea.isChecked = true
            }
            if (data.bradycardia == "No") {
                radioButtonNoBradycardia.isChecked = true
            }
            if (data.lethargy == "No") {
                radioButtonNoLethargy.isChecked = true
            }
            if (data.cough == "No") {
                radioButtonNoCough.isChecked = true
            }
            if (data.nausea == "No") {
                radioButtonNoNausea.isChecked = true
            }
            if (data.vomiting == "No") {
                radioButtonNoVomiting.isChecked = true
            }
            if (data.samples_sent == "No") {
                radioButtonNoSamplesSent.isChecked = true
            }


            //Yes Responses
            if (data.drainage == "Yes") {
                radioButtonYesDrainage.isChecked = true
            }
            if (data.pain == "Yes") {
                radioButtonYesPain.isChecked = true
            }
            if (data.erythema == "Yes") {
                radioButtonYesErythema.isChecked = true
            }
            if (data.heat == "Yes") {
                radioButtonYesHeat.isChecked = true
            }
            if (data.fever == "Yes") {
                radioButtonYesFever.isChecked = true
            }
            if (data.incision_opened == "Yes") {
                radioButtonYesIncisionDrained.isChecked = true
            }
            if (data.wound_dehisces == "Yes") {
                radioButtonYesWoundDehisces.isChecked = true
            }
            if (data.abscess == "Yes") {
                radioButtonYesAbscess.isChecked = true
            }
            if (data.sinus == "Yes") {
                radioButtonYesSinusTract.isChecked = true
            }
            if (data.hypothermia == "Yes") {
                radioButtonYesHypothermia.isChecked = true
            }
            if (data.apnea == "Yes") {
                radioButtonYesApnea.isChecked = true
            }
            if (data.bradycardia == "Yes") {
                radioButtonYesBradycardia.isChecked = true
            }
            if (data.lethargy == "Yes") {
                radioButtonYesLethargy.isChecked = true
            }
            if (data.cough == "Yes") {
                radioButtonYesCough.isChecked = true
            }
            if (data.nausea == "Yes") {
                radioButtonYesNausea.isChecked = true
            }
            if (data.vomiting == "Yes") {
                radioButtonYesVomiting.isChecked = true
            }
            if (data.samples_sent == "Yes") {
                radioButtonYesSamplesSent.isChecked = true
            }
        }
    }


    private fun saveData() {
        val user = formatterClass.getSharedPref("username", requireContext())
        if (user != null) {
            val patient = formatterClass.getSharedPref("patient", requireContext())
            val caseId = formatterClass.getSharedPref("caseId", requireContext())

            val data = formatterClass.getSharedPref("post", requireContext())
            if (data != null) {
                val converters = Converters()
                val data: PostOperativeData = converters.postFromJson(data)
                val post =
                    PostOperativeData(
                        userId = user,
                        patientId = patient.toString(),
                        encounterId = encounterId,
                        caseId = caseId.toString(),
                        check_up_date = data.check_up_date,
                        infection_signs = data.infection_signs,
                        event_date = data.event_date,
                        ssi = data.ssi,
                        infection_surgery_time = data.infection_surgery_time,
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
                        symptom_other = binding.edtOther.text.toString(),
                        samples_sent = if (binding.radioButtonNoSamplesSent.isChecked) "No" else "Yes",
                    )
                val added = mainViewModel.completePostOperative(requireContext(), post)
                if (added) {
                    val dt = mainViewModel.getLatestPostData(requireContext(), encounterId)
                    if (dt != null) {
                        val converters = Converters()
                        val jeff = converters.toPostJson(dt)
                        formatterClass.saveSharedPref("post", jeff, requireContext())
                        val hostNavController =
                            requireActivity().findNavController(R.id.nav_host_fragment_content_dashboard)
                        hostNavController.navigate(R.id.postSummaryFragment)
                    }

                } else {
                    Toast.makeText(
                        requireContext(),
                        "Encountered problems saving data",
                        Toast.LENGTH_SHORT
                    )
                        .show()
                }
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