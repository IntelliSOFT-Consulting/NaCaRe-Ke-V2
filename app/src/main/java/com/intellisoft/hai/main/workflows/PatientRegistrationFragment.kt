package com.intellisoft.hai.main.workflows

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.intellisoft.hai.R
import com.intellisoft.hai.databinding.FragmentOutcomeBinding
import com.intellisoft.hai.databinding.FragmentPatientRegistrationBinding
import com.intellisoft.hai.helper_class.FormatterClass
import com.intellisoft.hai.room.MainViewModel
import com.intellisoft.hai.room.PatientData
import com.intellisoft.hai.room.RegistrationData
import com.intellisoft.hai.util.AppUtils
import com.intellisoft.hai.util.AppUtils.controlData
import com.intellisoft.hai.util.AppUtils.controlSelectionData
import com.intellisoft.hai.util.AppUtils.disableTextInputEditText
import com.intellisoft.hai.util.AppUtils.showDatePickerDialog


/**
 * A simple [Fragment] subclass.
 * Use the [PatientRegistrationFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class PatientRegistrationFragment : Fragment() {
    private lateinit var binding: FragmentPatientRegistrationBinding
    private lateinit var formatterClass: FormatterClass
    private lateinit var mainViewModel: MainViewModel
    private var selectedGender: String? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentPatientRegistrationBinding.inflate(layoutInflater)
        disableTextInputEditText(binding.edtDob)
        mainViewModel = ViewModelProvider(this).get(MainViewModel::class.java)
        formatterClass = FormatterClass()
        binding.edtDob.apply {
            setOnClickListener {
                showDatePickerDialog(
                    requireContext(), binding.edtDob, setMaxNow = true, setMinNow = false
                )
            }
        }
        val genders = arrayOf("Male", "Female")
        val adapter =
            ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, genders)
        binding.actGender.setAdapter(adapter)
        binding.actGender.setOnItemClickListener { _, _, position, _ ->
            // Handle the selected item
            selectedGender = genders[position]
        }
        controlData(
            binding.edtDob,
            binding.dobHolder,
            "Please provide date of birth",
            hasMin = false,
            hasMax = false,
            min = 0,
            max = 0
        )
        binding.cancelButton.apply {
            setOnClickListener {
                navigateBack()
            }
        }
        binding.saveButton.apply {
            setOnClickListener {
                val user = formatterClass.getSharedPref("username", requireContext())
                if (user != null) {
                    if (validate()) {

                        val patientData =
                            PatientData(
                                userId = user,
                                patientName = binding.edtPatientName.text.toString(),
                                patientId = binding.edtPatientId.text.toString(),
                                patientGender = selectedGender.toString(),
                                secondaryId = binding.edtSecondaryId.text.toString(),
                                patientDob = binding.edtDob.text.toString(),
                            )
                        val added = mainViewModel.addNewPatient(patientData)
                        if (added) {
                            showCustomDialog()
                        } else {
                            Toast.makeText(
                                requireContext(),
                                "Encountered problems registering patient",
                                Toast.LENGTH_SHORT
                            )
                                .show()
                        }
                    }
                } else {
                    Toast.makeText(
                        requireContext(), "Please check user account", Toast.LENGTH_SHORT
                    )
                        .show()
                }
            }
        }
        controlData(
            binding.edtPatientId,
            binding.patientHolder,
            "Please provide patient ID",
            hasMin = false,
            hasMax = false,
            min = 0,
            max = 0
        )
        controlData(
            binding.edtPatientName,
            binding.nameHolder,
            "Please provide patient name",
            hasMin = false,
            hasMax = false,
            min = 0,
            max = 0
        )
        controlData(
            binding.edtDob,
            binding.dobHolder,
            "Please provide dob",
            hasMin = false,
            hasMax = false,
            min = 0,
            max = 0
        )
        return binding.root
    }

    private fun showCustomDialog() {

        val dialogView: View =
            LayoutInflater.from(requireContext()).inflate(R.layout.custom_dialog_layout, null)

        val builder = AlertDialog.Builder(requireContext())
            .setView(dialogView)
        val alertDialog = builder.create()
        // Customize the dialog view
        dialogView.findViewById<ImageView>(R.id.dialog_cancel_image).apply {
            setOnClickListener {
                alertDialog.dismiss()
                navigateBack()
            }
        }
        dialogView.findViewById<MaterialButton>(R.id.cancelButton).apply {
            setOnClickListener {
                alertDialog.dismiss()
                navigateBack()
            }
        }
        dialogView.findViewById<MaterialButton>(R.id.saveButton).apply {
            setOnClickListener {
                alertDialog.dismiss()
                showNewCaseDialog()
            }
        }

        alertDialog.show()

    }

    private fun showNewCaseDialog() {

        val dialogView: View =
            LayoutInflater.from(requireContext()).inflate(R.layout.case_dialog_layout, null)

        val builder = AlertDialog.Builder(requireContext())
            .setView(dialogView)
        val alertDialog = builder.create()
        alertDialog.setCancelable(false)
        val surgeries = listOf(
            "Hip Replacement Surgery",
            "Knee Replacement Surgery",
            "Open Reduction Internal Fixation (ORIF) Surgery",
            "ACL Reconstruction Surgery",
            "Shoulder Replacement Surgery",
            "Joint Arthroscopy Surgery",
            "Ankle Repair Surgery",
            "Joint Fusion Surgery",
            "Spinal Surgery",
            "Other (Specify)"
        )
        val loc = listOf(
            "Orthopedic ward", " General surgery ward"
        )
        val sched = listOf(
            "Elective", "Emergency"
        )
        val adapter =
            ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, surgeries)

        dialogView.findViewById<AutoCompleteTextView>(R.id.procedure).apply {
            setAdapter(adapter)
        }
        val location =
            ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, loc)

        dialogView.findViewById<AutoCompleteTextView>(R.id.location).apply {
            setAdapter(location)
        }
        val schedule =
            ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, sched)

        dialogView.findViewById<AutoCompleteTextView>(R.id.act_schedule).apply {
            setAdapter(schedule)
        }

        disableTextInputEditText(dialogView.findViewById(R.id.edt_adm))
        disableTextInputEditText(dialogView.findViewById(R.id.edt_surgery))

        controlData(
            dialogView.findViewById(R.id.edt_adm),
            dialogView.findViewById(R.id.admHolder),
            "Please provide admission date",
            hasMin = false,
            hasMax = false,
            min = 0,
            max = 0
        )
        controlData(
            dialogView.findViewById(R.id.edt_surgery),
            dialogView.findViewById(R.id.sgrHolder),
            "Please provide surgery date",
            hasMin = false,
            hasMax = false,
            min = 0,
            max = 0
        )
        controlData(
            dialogView.findViewById(R.id.edt_surgery),
            dialogView.findViewById(R.id.sgrHolder),
            "Please provide surgery procedure",
            hasMin = false,
            hasMax = false,
            min = 0,
            max = 0
        )
        controlData(
            dialogView.findViewById(R.id.edt_surgery),
            dialogView.findViewById(R.id.sgrHolder),
            "Please provide surgery location",
            hasMin = false,
            hasMax = false,
            min = 0,
            max = 0
        )

        controlSelectionData(
            dialogView.findViewById(R.id.procedure),
            dialogView.findViewById(R.id.procedureHolder),
            "Please provide procedure",
            hasMin = false,
            hasMax = false,
            min = 0,
            max = 0
        )

        controlSelectionData(
            dialogView.findViewById(R.id.location),
            dialogView.findViewById(R.id.locationHolder),
            "Please provide location",
            hasMin = false,
            hasMax = false,
            min = 0,
            max = 0
        )

        controlSelectionData(
            dialogView.findViewById(R.id.act_schedule),
            dialogView.findViewById(R.id.scheduleHolder),
            "Please provide scheduling",
            hasMin = false,
            hasMax = false,
            min = 0,
            max = 0
        )

        dialogView.findViewById<TextInputEditText>(R.id.edt_adm).apply {
            setOnClickListener {
                showDatePickerDialog(
                    requireContext(),
                    dialogView.findViewById(R.id.edt_adm),
                    setMaxNow = false,
                    setMinNow = false
                )
            }
        }
        dialogView.findViewById<TextInputEditText>(R.id.edt_surgery).apply {
            setOnClickListener {
                showDatePickerDialog(
                    requireContext(),
                    dialogView.findViewById(R.id.edt_surgery),
                    setMaxNow = false,
                    setMinNow = false
                )
            }
        }
        // Customize the dialog view
        dialogView.findViewById<ImageView>(R.id.dialog_cancel_image).apply {
            setOnClickListener {
                alertDialog.dismiss()
                navigateBack()
            }
        }
        dialogView.findViewById<MaterialButton>(R.id.cancelButton).apply {
            setOnClickListener {
                alertDialog.dismiss()
                navigateBack()
            }
        }
        dialogView.findViewById<MaterialButton>(R.id.saveButton).apply {
            setOnClickListener {
                val adm = dialogView.findViewById<TextInputEditText>(R.id.edt_adm).text?.toString()
                val procedure =
                    dialogView.findViewById<AutoCompleteTextView>(R.id.procedure).text?.toString()
                val loca =
                    dialogView.findViewById<AutoCompleteTextView>(R.id.location).text?.toString()
                val sgr =
                    dialogView.findViewById<TextInputEditText>(R.id.edt_surgery).text?.toString()
                val schedu =
                    dialogView.findViewById<AutoCompleteTextView>(R.id.act_schedule).text?.toString()
                if (adm.isNullOrEmpty()) {
                    dialogView.findViewById<TextInputLayout>(R.id.admHolder).error =
                        "Please provide admission date"
                    dialogView.findViewById<TextInputEditText>(R.id.edt_adm).requestFocus()
                    return@setOnClickListener
                }
                if (procedure.isNullOrEmpty()) {
                    dialogView.findViewById<TextInputLayout>(R.id.procedureHolder).error =
                        "Please provide procedure"
                    dialogView.findViewById<AutoCompleteTextView>(R.id.procedure).requestFocus()
                    return@setOnClickListener
                }
                if (loca.isNullOrEmpty()) {
                    dialogView.findViewById<TextInputLayout>(R.id.locationHolder).error =
                        "Please provide procedure"
                    dialogView.findViewById<AutoCompleteTextView>(R.id.location).requestFocus()
                    return@setOnClickListener
                }
                if (sgr.isNullOrEmpty()) {
                    dialogView.findViewById<TextInputLayout>(R.id.sgrHolder).error =
                        "Please provide surgery date"
                    dialogView.findViewById<TextInputEditText>(R.id.edt_surgery).requestFocus()
                    return@setOnClickListener
                }
                if (schedu.isNullOrEmpty()) {
                    dialogView.findViewById<TextInputLayout>(R.id.scheduleHolder).error =
                        "Please provide schedule"
                    dialogView.findViewById<AutoCompleteTextView>(R.id.act_schedule).requestFocus()
                    return@setOnClickListener
                }

                val user = formatterClass.getSharedPref("username", requireContext())
                if (user != null) {
                    val patientData =
                        RegistrationData(
                            userId = user,
                            patientId = binding.edtPatientId.text.toString(),
                            secondaryId = binding.edtSecondaryId.text.toString(),
                            gender = selectedGender.toString(),
                            date_of_birth = binding.edtDob.text.toString(),
                            date_of_admission = adm,
                            date_of_surgery = sgr,
                            procedure = procedure,
                            procedure_other = null,
                            scheduling = schedu,
                            location = loca,
                        )
                    val added = mainViewModel.addPatient(patientData)
                    if (added) {
                        Toast.makeText(
                            requireContext(),
                            "Case Successfully added",
                            Toast.LENGTH_SHORT
                        )
                            .show()
                        val hostNavController =
                            requireActivity().findNavController(R.id.nav_host_fragment_content_dashboard)
                        hostNavController.navigate(R.id.nav_gallery)
                    } else {
                        Toast.makeText(
                            requireContext(),
                            "Encountered problems adding a case",
                            Toast.LENGTH_SHORT
                        )
                            .show()
                    }
                    alertDialog.dismiss()
                } else {
                    Toast.makeText(
                        requireContext(), "Please check user account", Toast.LENGTH_SHORT
                    )
                        .show()
                }

            }
        }

        alertDialog.show()
    }

    private fun navigateBack() {
        val hostNavController =
            requireActivity().findNavController(R.id.nav_host_fragment_content_dashboard)
        hostNavController.navigate(R.id.nav_gallery)
    }

    private fun validate(): Boolean {
        val name = binding.edtPatientName.text?.toString()
        val primary = binding.edtPatientId.text?.toString()
        val dob = binding.edtDob.text?.toString()
        if (name.isNullOrEmpty()) {
            binding.nameHolder.error = "Please provide patient name"
            binding.edtPatientName.requestFocus()
            return false
        }
        if (primary.isNullOrEmpty()) {
            binding.patientHolder.error = "Please provide primary ID"
            binding.edtPatientId.requestFocus()
            return false
        }
        if (selectedGender.isNullOrEmpty()) {
            Toast.makeText(requireContext(), "Please select Gender", Toast.LENGTH_SHORT).show()
            return false
        }

        if (dob.isNullOrEmpty()) {
            binding.dobHolder.error = "Please provide date"
            binding.edtDob.requestFocus()
            return false
        }
        return true
    }

}