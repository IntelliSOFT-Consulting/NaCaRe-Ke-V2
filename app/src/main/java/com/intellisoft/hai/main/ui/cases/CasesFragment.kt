package com.intellisoft.hai.main.ui.cases

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.intellisoft.hai.R
import com.intellisoft.hai.adapter.DividerItemDecoration
import com.intellisoft.hai.adapter.PatientAdapter
import com.intellisoft.hai.databinding.FragmentCasesBinding
import com.intellisoft.hai.helper_class.FormatterClass
import com.intellisoft.hai.room.MainViewModel
import com.intellisoft.hai.room.PatientData
import com.intellisoft.hai.room.RegistrationData
import com.intellisoft.hai.util.AppUtils

class CasesFragment : Fragment() {
    private lateinit var binding: FragmentCasesBinding
    private lateinit var mRecyclerView: RecyclerView
    private lateinit var viewModel: MainViewModel
    private var dataList: List<PatientData>? = null
    private lateinit var formatterClass: FormatterClass
    private lateinit var adapter: PatientAdapter
    override fun onStart() {
        super.onStart()
        loadData()
    }

    private fun loadData() {
        try {
            viewModel = ViewModelProvider(this).get(MainViewModel::class.java)
            dataList = viewModel.getPatientsData(requireContext())

            if (dataList!!.isNotEmpty()) {
                adapter = PatientAdapter(dataList!!, requireContext(), this::onclick)
                mRecyclerView.adapter = adapter
                mRecyclerView.addItemDecoration(
                    DividerItemDecoration(
                        requireContext(),
                        R.drawable.divider
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun onclick(data: PatientData) {
        val hasCase = viewModel.getCaseDetailsFound(requireContext(), data.id.toString())
        if (hasCase) {
            formatterClass.saveSharedPref("patient", data.patientId, requireContext())
            formatterClass.saveSharedPref("caseId", data.userId, requireContext())
            val bundle = Bundle()
            bundle.putString("caseId", data.id.toString())
            val hostNavController =
                requireActivity().findNavController(R.id.nav_host_fragment_content_dashboard)
            hostNavController.navigate(R.id.nav_slideshow, bundle)
        } else {
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
                ArrayAdapter(
                    requireContext(),
                    android.R.layout.simple_dropdown_item_1line,
                    surgeries
                )

            dialogView.findViewById<AutoCompleteTextView>(R.id.procedure).apply {
                setAdapter(adapter)
            }
            val location =
                ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, loc)

            dialogView.findViewById<AutoCompleteTextView>(R.id.location).apply {
                setAdapter(location)
            }
            val schedule =
                ArrayAdapter(
                    requireContext(),
                    android.R.layout.simple_dropdown_item_1line,
                    sched
                )

            dialogView.findViewById<AutoCompleteTextView>(R.id.act_schedule).apply {
                setAdapter(schedule)
            }

            AppUtils.disableTextInputEditText(dialogView.findViewById(R.id.edt_adm))
            AppUtils.disableTextInputEditText(dialogView.findViewById(R.id.edt_surgery))

            AppUtils.controlData(
                dialogView.findViewById(R.id.edt_adm),
                dialogView.findViewById(R.id.admHolder),
                "Please provide admission date",
                hasMin = false,
                hasMax = false,
                min = 0,
                max = 0
            )
            AppUtils.controlData(
                dialogView.findViewById(R.id.edt_surgery),
                dialogView.findViewById(R.id.sgrHolder),
                "Please provide surgery date",
                hasMin = false,
                hasMax = false,
                min = 0,
                max = 0
            )
            AppUtils.controlData(
                dialogView.findViewById(R.id.edt_surgery),
                dialogView.findViewById(R.id.sgrHolder),
                "Please provide surgery procedure",
                hasMin = false,
                hasMax = false,
                min = 0,
                max = 0
            )
            AppUtils.controlData(
                dialogView.findViewById(R.id.edt_surgery_other),
                dialogView.findViewById(R.id.textInputOther),
                "Please specify surgery",
                hasMin = false,
                hasMax = false,
                min = 0,
                max = 0
            )

            AppUtils.controlSelectionData(
                dialogView.findViewById(R.id.procedure),
                dialogView.findViewById(R.id.procedureHolder),
                "Please provide procedure",
                hasMin = false,
                hasMax = false,
                min = 0,
                max = 0
            )

            AppUtils.controlSelectionData(
                dialogView.findViewById(R.id.location),
                dialogView.findViewById(R.id.locationHolder),
                "Please provide location",
                hasMin = false,
                hasMax = false,
                min = 0,
                max = 0
            )

            AppUtils.controlSelectionData(
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
                    AppUtils.showDatePickerDialog(
                        requireContext(),
                        dialogView.findViewById(R.id.edt_adm),
                        setMaxNow = false,
                        setMinNow = false
                    )
                }
            }

            dialogView.findViewById<AutoCompleteTextView>(R.id.procedure).apply {
                setOnItemClickListener { parent, view, position, id ->
                    val selectedItem = adapter.getItem(position)
                    if (selectedItem == "Other (Specify)") {
                        dialogView.findViewById<TextInputLayout>(R.id.textInputOther).apply {
                            visibility = View.VISIBLE
                        }
                    } else {
                        dialogView.findViewById<TextInputLayout>(R.id.textInputOther).apply {
                            visibility = View.GONE
                        }
                    }
                }
            }
            dialogView.findViewById<TextInputEditText>(R.id.edt_surgery).apply {
                setOnClickListener {
                    AppUtils.showDatePickerDialog(
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

                }
            }
            dialogView.findViewById<MaterialButton>(R.id.cancelButton).apply {
                setOnClickListener {
                    alertDialog.dismiss()

                }
            }
            dialogView.findViewById<MaterialButton>(R.id.saveButton).apply {
                setOnClickListener {
                    val adm =
                        dialogView.findViewById<TextInputEditText>(R.id.edt_adm).text?.toString()
                    val procedure =
                        dialogView.findViewById<AutoCompleteTextView>(R.id.procedure).text?.toString()
                    val other =
                        dialogView.findViewById<TextInputEditText>(R.id.edt_surgery_other).text?.toString()
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
                        dialogView.findViewById<AutoCompleteTextView>(R.id.procedure)
                            .requestFocus()
                        return@setOnClickListener
                    }
                    if (procedure == "Other (Specify)") {
                        if (other.isNullOrEmpty()) {
                            dialogView.findViewById<TextInputLayout>(R.id.textInputOther).error =
                                "Please specify procedure"
                            dialogView.findViewById<TextInputEditText>(R.id.edt_surgery_other)
                                .requestFocus()
                            return@setOnClickListener
                        }
                    }
                    if (loca.isNullOrEmpty()) {
                        dialogView.findViewById<TextInputLayout>(R.id.locationHolder).error =
                            "Please provide procedure"
                        dialogView.findViewById<AutoCompleteTextView>(R.id.location)
                            .requestFocus()
                        return@setOnClickListener
                    }
                    if (sgr.isNullOrEmpty()) {
                        dialogView.findViewById<TextInputLayout>(R.id.sgrHolder).error =
                            "Please provide surgery date"
                        dialogView.findViewById<TextInputEditText>(R.id.edt_surgery)
                            .requestFocus()
                        return@setOnClickListener
                    }
                    if (schedu.isNullOrEmpty()) {
                        dialogView.findViewById<TextInputLayout>(R.id.scheduleHolder).error =
                            "Please provide schedule"
                        dialogView.findViewById<AutoCompleteTextView>(R.id.act_schedule)
                            .requestFocus()
                        return@setOnClickListener
                    }

                    val user = formatterClass.getSharedPref("username", requireContext())
                    if (user != null) {
                        val patientData =
                            RegistrationData(
                                userId = user,
                                caseId = data.id.toString(),
                                patientId = data.patientId,
                                secondaryId = data.secondaryId,
                                gender = data.patientGender,
                                date_of_birth = data.patientDob,
                                date_of_admission = adm,
                                date_of_surgery = sgr,
                                procedure = procedure,
                                procedure_other = null,
                                scheduling = schedu,
                                location = loca,
                            )
                        val added = viewModel.addPatient(patientData)
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
    }

    override fun onResume() {
        super.onResume()
        loadData()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewModel = ViewModelProvider(this).get(MainViewModel::class.java)

        binding = FragmentCasesBinding.inflate(inflater, container, false)
        formatterClass = FormatterClass()
        val root: View = binding.root
        mRecyclerView = binding.recyclerView
        mRecyclerView.setHasFixedSize(true)
        mRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        binding.btnAction.apply {
            setOnClickListener {
                val hostNavController =
                    requireActivity().findNavController(R.id.nav_host_fragment_content_dashboard)
                hostNavController.navigate(R.id.patientRegistrationFragment)
            }
        }
        binding.edtSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
//                adapter.filter(s.toString())

            }

            override fun afterTextChanged(s: Editable?) {}
        })
        return root
    }


}