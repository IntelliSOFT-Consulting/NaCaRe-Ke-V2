package com.nacare.capture.ui.patients

import android.app.Application
import android.content.Intent
import android.os.Bundle
import android.text.Html
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.navigation.fragment.NavHostFragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.button.MaterialButton
import com.nacare.capture.R
import com.nacare.capture.adapters.TrackedEntityAdapter
import com.nacare.capture.data.FormatterClass
import com.nacare.capture.databinding.FragmentPatientListBinding
import com.nacare.capture.model.EntityData
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

            val adapterProgram = TrackedEntityAdapter(dataList, requireContext(), this::handleClick)

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
                val eventUid = formatter.generateUUID(11)

                formatter.saveSharedPref("eventUid", eventUid, requireContext())
                formatter.deleteSharedPref("current_data", requireContext())
                formatter.saveSharedPref("current_patient", data.uid, requireContext())
                formatter.saveSharedPref("current_patient_id", data.id, requireContext())

                val enrollment = EnrollmentEventData(
                    dataValues = "",
                    uid = formatter.generateUUID(11),
                    eventUid = eventUid,
                    program = formatter.getSharedPref("programUid", context).toString(),
                    programStage = formatter.getSharedPref("programUid", context).toString(),
                    orgUnit = formatter.getSharedPref("orgCode", context).toString(),
                    eventDate = formatter.formatCurrentDate(Date()),
                    status = "ACTIVE",
                    trackedEntity = data.id
                )
                viewModel.addEnrollmentData(enrollment)
                startActivity(Intent(requireContext(), PatientResponderActivity::class.java))

            }
        }
        yesButton.apply {
            setOnClickListener {
                alertDialog.dismiss()
                // get latest event
                formatter.saveSharedPref("current_patient", data.uid, requireContext())
                formatter.saveSharedPref("current_patient_id", data.id, requireContext())
                val latestEnrollment = viewModel.getLatestEnrollment(
                    requireContext(),
                    data.id,
                    formatter.getSharedPref("programUid", requireContext()).toString(),
                    formatter.getSharedPref("orgCode", requireContext()).toString(),
                )

                if (latestEnrollment != null) {
                    formatter.saveSharedPref(
                        "eventUid",
                        latestEnrollment.eventUid,
                        requireContext()
                    )
                } else {
                    val eventUid = formatter.generateUUID(11)
                    formatter.saveSharedPref("eventUid", eventUid, requireContext())
                }
                startActivity(Intent(requireContext(), PatientResponderActivity::class.java))
//                Toast.makeText(requireContext(), "Under development", Toast.LENGTH_SHORT).show()
            }
        }

        alertDialog.show()
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
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