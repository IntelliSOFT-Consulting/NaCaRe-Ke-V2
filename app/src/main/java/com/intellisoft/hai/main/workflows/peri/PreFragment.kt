package com.intellisoft.hai.main.workflows.peri

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import com.intellisoft.hai.R
import com.intellisoft.hai.databinding.FragmentPreBinding
import com.intellisoft.hai.helper_class.FormatterClass
import com.intellisoft.hai.room.MainViewModel
import com.intellisoft.hai.room.PrePostOperativeData
import com.intellisoft.hai.util.AppUtils

/**
 * A simple [Fragment] subclass.
 * Use the [PreFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class PreFragment : Fragment() {
    private lateinit var formatterClass: FormatterClass
    private lateinit var mainViewModel: MainViewModel
    private lateinit var binding: FragmentPreBinding
    private lateinit var reason: String
    private lateinit var drain: String
    private lateinit var implant: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentPreBinding.inflate(layoutInflater)
        mainViewModel = ViewModelProvider(this).get(MainViewModel::class.java)
        formatterClass = FormatterClass()
        controlListeners()


        val conditions = formatterClass.generateAntibiotics(requireContext())
        val adapter =
            ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, conditions)
        binding.aucAntibiotic.setAdapter(adapter)
        binding.aucAntibiotic.setOnItemClickListener { _, _, position, _ ->
            val selectedOption = adapter.getItem(position).toString()
            if (selectedOption == "Other (specify)") {
                binding.preOtherHolder.visibility = View.VISIBLE
            } else {
                binding.preOtherHolder.visibility = View.GONE
            }
        }


        val post = formatterClass.generateAntibiotics(requireContext())
        val adapter2 =
            ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, post)
        binding.aucPostAntibiotic.setAdapter(adapter2)
        binding.aucPostAntibiotic.setOnItemClickListener { _, _, position, _ ->
            val selectedOption = adapter2.getItem(position).toString()
            if (selectedOption == "Other (specify)") {
                binding.postOtherHolder.visibility = View.VISIBLE
            } else {
                binding.postOtherHolder.visibility = View.GONE
            }
        }


        val reasons = formatterClass.generateReasons(requireContext())
        val adapter3 =
            ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, reasons)
        binding.aucReason.setAdapter(adapter3)
        binding.aucReason.setOnItemClickListener { _, _, position, _ ->
            val selectedOption = adapter3.getItem(position).toString()
            if (selectedOption == "Other") {
                binding.reasonOtherHolder.visibility = View.VISIBLE
            } else {
                binding.reasonOtherHolder.visibility = View.GONE
            }
        }
        val implants = formatterClass.generateImplants(requireContext())
        val adapter4 =
            ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, implants)
        binding.aucImplant.setAdapter(adapter4)
        binding.aucImplant.setOnItemClickListener { _, _, position, _ ->
            val selectedOption = adapter4.getItem(position).toString()
            if (selectedOption == "Other") {
                binding.implantTypeHolder.visibility = View.VISIBLE
            } else {
                binding.implantTypeHolder.visibility = View.GONE
            }
        }

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
        handleVisibles()
        val data = formatterClass.getSharedPref("patient", requireContext())
        if (data != null) {
            loadInitialData(data)
        }
        return binding.root
    }

    private fun loadInitialData(patient: String) {
        val caseId = formatterClass.getSharedPref("caseId", requireContext())
        val data = mainViewModel.loadPrePostPreparationData(requireContext(), patient, caseId)

        binding.apply {
            if (data != null) {
                aucAntibiotic.setAdapter(
                    loadAdapter(
                        formatterClass.generateAntibiotics(
                            requireContext()
                        )
                    )
                )
                aucAntibiotic.setText(data.pre_antibiotic_prophylaxis, false)

                if (data.pre_antibiotic_prophylaxis == "Other (specify)") {
                    preOtherHolder.visibility = View.VISIBLE
                    edtPreOther.setText(data.pre_antibiotic_prophylaxis_other)
                }
                if (data.antibiotics_ceased == "Yes") {
                    radioButtonAntibioticsCeasedYes.isChecked = true
                }
                if (data.antibiotics_ceased == "No") {
                    radioButtonAntibioticsCeasedNo.isChecked = true
                }
                aucPostAntibiotic.setAdapter(
                    loadAdapter(
                        formatterClass.generateAntibiotics(
                            requireContext()
                        )
                    )
                )
                aucPostAntibiotic.setText(data.post_antibiotic_prophylaxis, false)
                if (data.post_antibiotic_prophylaxis == "Other (specify)") {
                    postOtherHolder.visibility = View.VISIBLE
                    edtPostOther.setText(data.post_antibiotic_prophylaxis_other)
                }

//                aucAntibiotic.setText(data.post_other_antibiotic_given)
                aucReason.setAdapter(
                    loadAdapter(
                        formatterClass.generateReasons(
                            requireContext()
                        )
                    )
                )
                aucReason.setText(data.post_reason, false)
                if (data.post_reason == "Other") {
                    reasonOtherHolder.visibility = View.VISIBLE
                    edtReasonOther.setText(data.post_reason_other)
                }

                if (data.drain_inserted == "Yes") {
                    radioButtonDrainInserted.isChecked = true
                }
                if (data.drain_inserted == "No") {
                    radioButtonDrainNotInserted.isChecked = true
                }
                edtDrain.setText(data.drain_location)
                if (data.drain_antibiotic == "Yes") {
                    radioButtonYesAntibioticWithDrain.isChecked = true
                }
                if (data.drain_antibiotic == "No") {
                    radioButtonNoAntibioticWithDrain.isChecked = true
                }
                aucImplant.setAdapter(
                    loadAdapter(
                        formatterClass.generateImplants(
                            requireContext()
                        )
                    )
                )
                aucImplant.setText(data.implant_used, false)
                if (data.implant_used == "Other") {
                    implantTypeHolder.visibility = View.VISIBLE
                    edtImplantType.setText(data.implant_other)
                }
            }
        }
    }

    private fun loadAdapter(data: Array<String>): ArrayAdapter<String> {
        return ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, data)
    }

    private fun saveData() {
        if (validate()) {
            val user = formatterClass.getSharedPref("username", requireContext())
            if (user != null) {
                val patient = formatterClass.getSharedPref("patient", requireContext())

                val pre_other = binding.edtPreOther.text?.toString()
                val post_other = binding.edtPostOther.text?.toString()
                val res = binding.edtReasonOther.text?.toString()
                val drain_loc = binding.edtDrain.text?.toString()
                val type = binding.edtImplantType.text?.toString()
                val ceased =
                    if (binding.radioButtonAntibioticsCeasedNo.isChecked) "No" else "Yes"
                val antibiotic =
                    if (binding.radioButtonNoAntibioticWithDrain.isChecked) "No" else "Yes"
                val enc = formatterClass.getSharedPref("caseId", requireContext())
                val data =
                    PrePostOperativeData(
                        userId = user,
                        patientId = patient.toString(),
                        encounterId = enc.toString(),
                        pre_antibiotic_prophylaxis = binding.aucAntibiotic.text.toString(),
                        pre_antibiotic_prophylaxis_other = pre_other.toString(),
                        pre_other_antibiotic_given = "",
                        antibiotics_ceased = ceased,
                        post_antibiotic_prophylaxis = binding.aucPostAntibiotic.text.toString(),
                        post_antibiotic_prophylaxis_other = post_other.toString(),
                        post_other_antibiotic_given = "",
                        post_reason = binding.aucReason.text.toString(),
                        post_reason_other = res.toString(),
                        drain_inserted = getData(binding.drainInsertedRadioGroup),
                        drain_location = drain_loc.toString(),
                        drain_antibiotic = antibiotic,
                        implant_used = binding.aucImplant.text.toString(),
                        implant_other = type.toString()
                    )
                val added = mainViewModel.addPrePostOperativeData(data)
                if (added) {
                    val hostNavController =
                        requireActivity().findNavController(R.id.nav_host_fragment_content_dashboard)
                    hostNavController.navigate(R.id.caseSummaryFragment)

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

    private fun getData(parent: RadioGroup): String {
        for (i in 0 until parent.childCount) {
            val radioButton = parent.getChildAt(i) as RadioButton
            if (radioButton.isChecked) {
                // Get the text of the selected RadioButton
                reason = radioButton.text.toString()
                break
            }
        }
        return reason;
    }

    private fun controlListeners() {


        AppUtils.controlData(
            binding.edtImplantType,
            binding.implantTypeHolder,
            "Please provide input",
            hasMin = false,
            hasMax = false,
            min = 0,
            max = 0
        )
        AppUtils.controlSelectionData(
            binding.aucAntibiotic,
            binding.antibioticHolder,
            "Please provide antibiotic",
            hasMin = false,
            hasMax = false,
            min = 0,
            max = 0
        )
        AppUtils.controlSelectionData(
            binding.aucReason,
            binding.reasonHolder,
            "Please provide reason",
            hasMin = false,
            hasMax = false,
            min = 0,
            max = 0
        )
        AppUtils.controlData(
            binding.edtDrain,
            binding.drainHolder,
            "Please provide input",
            hasMin = false,
            hasMax = false,
            min = 0,
            max = 0
        )
        AppUtils.controlData(
            binding.edtReasonOther,
            binding.reasonOtherHolder,
            "Please provide input",
            hasMin = false,
            hasMax = false,
            min = 0,
            max = 0
        )
        AppUtils.controlData(
            binding.edtReasonOther,
            binding.reasonOtherHolder,
            "Please provide input",
            hasMin = false,
            hasMax = false,
            min = 0,
            max = 0
        )
        AppUtils.controlData(
            binding.edtPreOther,
            binding.preOtherHolder,
            "Please provide input",
            hasMin = false,
            hasMax = false,
            min = 0,
            max = 0
        )
        AppUtils.controlData(
            binding.edtPostOther,
            binding.postOtherHolder,
            "Please provide input",
            hasMin = false,
            hasMax = false,
            min = 0,
            max = 0
        )

        AppUtils.controlSelectionData(
            binding.aucPostAntibiotic,
            binding.postAntibioticHolder,
            "Please provide input",
            hasMin = false,
            hasMax = false,
            min = 0,
            max = 0
        )
        AppUtils.controlSelectionData(
            binding.aucImplant,
            binding.implantHolder,
            "Please provide implant used",
            hasMin = false,
            hasMax = false,
            min = 0,
            max = 0
        )
    }

    private fun handleVisibles() {
        // Set an item click listener to handle item selection

        binding.drainInsertedRadioGroup.setOnCheckedChangeListener { _, checkedId ->
            if (checkedId == binding.radioButtonDrainInserted.id) {
                binding.drainHolder.visibility = View.VISIBLE
            } else {
                binding.drainHolder.visibility = View.GONE
            }
        }


    }


    private fun handleClicks() {
        binding.drainInsertedRadioGroup.setOnCheckedChangeListener { _, checkedId ->
            val selectedRadioButton = binding.root.findViewById<RadioButton>(checkedId)
            drain = selectedRadioButton.text.toString()
        }

    }


    private fun validate(): Boolean {

        val anti = binding.aucAntibiotic.text.toString()
        val post = binding.aucPostAntibiotic.text.toString()
        val reason = binding.aucReason.text.toString()
        val imp = binding.aucImplant.text.toString()
        if (anti.isNullOrEmpty()) {
            binding.antibioticHolder.error = "Please select antibiotic"
            binding.aucAntibiotic.requestFocus()
            return false
        }
        if (anti == "Other (specify)") {
            val j = binding.edtPreOther.text.toString()
            if (j.isNullOrEmpty()) {
                binding.preOtherHolder.error = "Please specify"
                binding.edtPreOther.requestFocus()
                return false
            }
        }
        if (binding.antibioticsCeasedRadioGroup.checkedRadioButtonId == -1) {
            Toast.makeText(
                requireContext(),
                "Please select if ceased",
                Toast.LENGTH_SHORT
            ).show()
            return false
        }
        if (post.isNullOrEmpty()) {
            binding.postAntibioticHolder.error = "Please select antibiotic"
            binding.aucPostAntibiotic.requestFocus()
            return false
        }
        if (post == "Other (specify)") {
            val post_other = binding.edtPostOther.text?.toString()
            if (post_other.isNullOrEmpty()) {
                binding.postOtherHolder.error = "Please provide input"
                binding.edtPostOther.requestFocus()
                return false
            }
        }

        if (reason.isNullOrEmpty()) {
            binding.reasonHolder.error = "Please provide reason"
            binding.aucReason.requestFocus()
            return false
        }

        if (reason == "Other") {
            val res = binding.edtReasonOther.text?.toString()
            if (res.isNullOrEmpty()) {
                binding.reasonOtherHolder.error = "Please provide input"
                binding.edtReasonOther.requestFocus()
                return false
            }
        }
        if (binding.drainInsertedRadioGroup.checkedRadioButtonId == -1) {
            Toast.makeText(
                requireContext(),
                "Please select if drain inserted",
                Toast.LENGTH_SHORT
            ).show()
            return false
        }
        if (binding.radioButtonDrainInserted.isChecked) {
            val dr = binding.edtDrain.text?.toString()
            if (dr.isNullOrEmpty()) {
                binding.drainHolder.error = "Please provide input"
                binding.edtDrain.requestFocus()
                return false
            }
        }
        if (binding.antibioticGivenWithDrainRadioGroup.checkedRadioButtonId == -1) {
            Toast.makeText(
                requireContext(),
                "Please select if given in presence of drain",
                Toast.LENGTH_SHORT
            ).show()
            return false
        }
        if (imp.isNullOrEmpty()) {
            binding.implantHolder.error = "Please select implant"
            binding.aucImplant.requestFocus()
            return false
        }
        if (imp == "Other") {
            val cc = binding.edtImplantType.text?.toString()
            if (cc.isNullOrEmpty()) {
                binding.implantTypeHolder.error = "Please provide input"
                binding.edtImplantType.requestFocus()
                return false
            }
        }

        return true
    }

}