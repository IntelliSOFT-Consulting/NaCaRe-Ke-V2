package com.nacare.capture.ui.patients

import android.app.Application
import android.content.Intent
import android.os.Bundle
import android.text.Html
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.NavHostFragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.button.MaterialButton
import com.google.gson.Gson
import com.nacare.capture.R
import com.nacare.capture.adapters.TrackedEntityAdapter
import com.nacare.capture.data.FormatterClass
import com.nacare.capture.databinding.FragmentPatientListBinding
import com.nacare.capture.model.EntityData
import com.nacare.capture.model.TrackedEntityInstance
import com.nacare.capture.model.TrackedEntityInstanceAttributes
import com.nacare.capture.room.Converters
import com.nacare.capture.room.EnrollmentEventData
import com.nacare.capture.room.MainViewModel
import java.util.Date

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [PatientListFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class PatientListFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    private lateinit var binding: FragmentPatientListBinding
    private lateinit var viewModel: MainViewModel
    private val formatter = FormatterClass()
    private val dataList = ArrayList<EntityData>()
    private lateinit var adapterProgram: TrackedEntityAdapter
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentPatientListBinding.inflate(layoutInflater)
        viewModel = MainViewModel(requireContext().applicationContext as Application)

        loadTrackedEntities()
        return binding.root
    }


    override fun onResume() {
        super.onResume()
        try {
            loadTrackedEntities()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun loadTrackedEntities() {
        val orgUnit = formatter.getSharedPref("orgCode", requireContext())
        if (orgUnit != null) {
            val data = viewModel.loadAllTrackedEntities(orgUnit, requireContext())
            if (data != null) {
                if (data.isEmpty()) {
                    binding.apply {

                        trackedEntityInstancesNotificator.visibility = View.VISIBLE
                    }
                } else {
                    binding.apply {
                        trackedEntityInstancesNotificator.visibility = View.GONE
                    }
                }
                dataList.clear()
                data.forEach {
                    val single = EntityData(
                        id = it.id.toString(),
                        uid = it.trackedEntity,
                        date = it.enrollDate,
                        fName = extractValueFromAttributes("R1vaUuILrDy", it.attributes),
                        lName = extractValueFromAttributes("hzVijy6tEUF", it.attributes),
                        diagnosis = extractValueFromAttributes("BzhDnF5fG4x", it.attributes),
                        attributes = it.attributes

                    )
                    dataList.add(single)

                }
            }

            adapterProgram = TrackedEntityAdapter(dataList, requireContext(), this::handleClick)

            binding.apply {
                val manager = LinearLayoutManager(requireContext())
                trackedEntityInstancesRecyclerView.apply {
                    adapter = adapterProgram
                    layoutManager = manager
                    val divider = DividerItemDecoration(context, manager.orientation)
                    divider.setDrawable(context.getDrawable(R.drawable.divider)!!)
//                addItemDecoration(divider)
                }

            }
        }
    }

    private fun extractValueFromAttributes(s: String, attributes: String): String {
        var data = ""
        val converters = Converters().fromJsonAttribute(attributes)
        val single = converters.find { it.attribute == s }
        if (single != null) {
            data = single.value
        }

        return data

    }

    private fun handleClick(data: EntityData) {
        formatter.deleteSharedPref("underTreatment", requireContext())
        formatter.deleteSharedPref("isRegistration", requireContext())
        val builder = AlertDialog.Builder(requireContext())
        val inflater = LayoutInflater.from(requireContext())
        val customView: View = inflater.inflate(R.layout.custom_layout_cases, null)
        builder.setView(customView)
        val alertDialog = builder.create()
        val tvTitle = customView.findViewById<TextView>(R.id.tv_title)
        val tvMessage = customView.findViewById<TextView>(R.id.tv_message)
        val noButton = customView.findViewById<MaterialButton>(R.id.no_button)
        val yesButton = customView.findViewById<MaterialButton>(R.id.yes_button)

        val htmlText = "Please select an action for the selected record:<br><br>1." +
                "<b>Add new primary cancer information for an existing patient:</b> " +
                "Choose this option if this is new primary cancer information for an existing patient.<br><br> " +
                "2.<b>Update an Existing Cancer Case:</b> Choose this option if you want to update any other additional information " +
                "relating to an existing cancer case."

        tvTitle.textAlignment = View.TEXT_ALIGNMENT_TEXT_START
        tvTitle.setText(R.string.alert)
        tvMessage.text = Html.fromHtml(htmlText)
        tvMessage.textAlignment = View.TEXT_ALIGNMENT_TEXT_START
        noButton.setText(R.string.add_new_primary_cancer_info)
        yesButton.setText(R.string.update_an_existing_cancer_case)

        noButton.apply {
            setOnClickListener {
                alertDialog.dismiss()
                // add new event
                val attributes = Converters().fromJsonAttribute(data.attributes)
                val trackedEntityInstance = formatter.generateUUID(11)
                val orgCode = formatter.getSharedPref("orgCode", context).toString()

                val refinedAttributes = formatter.excludeBareMinimumInformation(attributes)
                val entityData = TrackedEntityInstance(
                    trackedEntity = trackedEntityInstance,
                    enrollment = trackedEntityInstance,
                    enrollDate = formatter.formatCurrentDate(Date()),
                    orgUnit = orgCode,
                    attributes = refinedAttributes
                )
                viewModel.saveTrackedEntity(
                    context,
                    entityData,
                    orgCode
                )

                startActivity(
                    Intent(
                        context, PatientResponderActivity::class.java
                    )
                )


            }
        }
        yesButton.apply {
            setOnClickListener {
                alertDialog.dismiss()
                // get latest event
                formatter.saveSharedPref("current_patient", data.uid, requireContext())
                formatter.saveSharedPref("current_patient_id", data.id, requireContext())
                val single = viewModel.getLatestEnrollment(requireContext(), data.id)
                if (single != null) {
                    formatter.saveSharedPref("eventUid", single.eventUid, requireContext())
                    startActivity(Intent(requireContext(), PatientResponderActivity::class.java))
                } else {
                    Toast.makeText(
                        requireContext(),
                        "Loading... please refresh",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }

        alertDialog.show()
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        buildFilters()
        binding.apply {
            textViewWithArrow.apply {
                setOnClickListener {
                    NavHostFragment.findNavController(this@PatientListFragment).navigateUp()
                }
            }
            enrollmentButton.apply {
                setOnClickListener {
                    val intent = Intent(requireContext(), PatientSearchActivity::class.java)
                    startActivity(intent)
                }
            }
        }
    }

    private fun buildFilters() {
        val upDrawable = ContextCompat.getDrawable(requireContext(), R.drawable.resized_icon_tinny)
        val downDrawable =
            ContextCompat.getDrawable(requireContext(), R.drawable.resized_icon_down_tinny)

        binding.apply {
            inc.dateTextView.apply {
                val filterFirstName = formatter.getSharedPref("filterDateName", requireContext())
                if (filterFirstName != null) {
                    setCompoundDrawablesRelativeWithIntrinsicBounds(
                        downDrawable,
                        null,
                        null,
                        null
                    )
                } else {
                    setCompoundDrawablesRelativeWithIntrinsicBounds(
                        upDrawable,
                        null,
                        null,
                        null
                    )
                }
                setOnClickListener {
                    val afterClick = formatter.getSharedPref("filterDateName", requireContext())
                    if (afterClick != null) {
                        formatter.deleteSharedPref("filterDateName", requireContext())
                        setCompoundDrawablesRelativeWithIntrinsicBounds(
                            upDrawable,
                            null,
                            null,
                            null
                        )
                        filterResultsByStatus("date", true)
                    } else {
                        formatter.saveSharedPref("filterDateName", "true", requireContext())
                        setCompoundDrawablesRelativeWithIntrinsicBounds(
                            downDrawable,
                            null,
                            null,
                            null
                        )
                        filterResultsByStatus("date", false)
                    }

                }
            }
            inc.firstnameTextView.apply {
                val filterFirstName = formatter.getSharedPref("filterFirstName", requireContext())
                if (filterFirstName != null) {
                    setCompoundDrawablesRelativeWithIntrinsicBounds(
                        downDrawable,
                        null,
                        null,
                        null
                    )
                } else {
                    setCompoundDrawablesRelativeWithIntrinsicBounds(
                        upDrawable,
                        null,
                        null,
                        null
                    )
                }
                setOnClickListener {
                    val afterClick = formatter.getSharedPref("filterFirstName", requireContext())
                    if (afterClick != null) {
                        formatter.deleteSharedPref("filterFirstName", requireContext())
                        setCompoundDrawablesRelativeWithIntrinsicBounds(
                            upDrawable,
                            null,
                            null,
                            null
                        )
                        filterResultsByStatus("firstname", true)
                    } else {
                        formatter.saveSharedPref("filterFirstName", "true", requireContext())
                        setCompoundDrawablesRelativeWithIntrinsicBounds(
                            downDrawable,
                            null,
                            null,
                            null
                        )
                        filterResultsByStatus("firstname", false)
                    }

                }
            }
            inc.lastnameTextView.apply {
                val filterFirstName = formatter.getSharedPref("filterLastName", requireContext())
                if (filterFirstName != null) {
                    setCompoundDrawablesRelativeWithIntrinsicBounds(
                        downDrawable,
                        null,
                        null,
                        null
                    )
                } else {
                    setCompoundDrawablesRelativeWithIntrinsicBounds(
                        upDrawable,
                        null,
                        null,
                        null
                    )
                }
                setOnClickListener {
                    val afterClick = formatter.getSharedPref("filterLastName", requireContext())
                    if (afterClick != null) {
                        formatter.deleteSharedPref("filterLastName", requireContext())
                        setCompoundDrawablesRelativeWithIntrinsicBounds(
                            upDrawable,
                            null,
                            null,
                            null
                        )
                        filterResultsByStatus("lastname", true)
                    } else {
                        formatter.saveSharedPref("filterLastName", "true", requireContext())
                        setCompoundDrawablesRelativeWithIntrinsicBounds(
                            downDrawable,
                            null,
                            null,
                            null
                        )
                        filterResultsByStatus("lastname", false)
                    }

                }
            }
        }
    }

    private fun filterResultsByStatus(uid: String, isDescending: Boolean) {
        if (dataList.isNotEmpty()) {

            val sortedList = when (uid) {
                "firstname" -> if (isDescending) {
                    dataList.sortedByDescending { it.fName }
                } else {
                    dataList.sortedBy { it.fName }
                }

                "lastname" -> if (isDescending) {
                    dataList.sortedByDescending { it.lName }
                } else {
                    dataList.sortedBy { it.lName }
                }

                "date" -> if (isDescending) {
                    dataList.sortedByDescending { it.date }
                } else {
                    dataList.sortedBy { it.date }
                }

                else -> dataList.sortedBy { it.date }

            }
            adapterProgram = TrackedEntityAdapter(sortedList, requireContext(), this::handleClick)

            binding.apply {
                val manager = LinearLayoutManager(requireContext())
                trackedEntityInstancesRecyclerView.apply {
                    adapter = adapterProgram
                    layoutManager = manager
                    val divider = DividerItemDecoration(context, manager.orientation)
                    divider.setDrawable(context.getDrawable(R.drawable.divider)!!)
//                addItemDecoration(divider)
                }

            }

        }
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment PatientListFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            PatientListFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}