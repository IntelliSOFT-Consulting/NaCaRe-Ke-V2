package com.imeja.nacare_live.ui.facility

import android.app.Application
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.NavHostFragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.imeja.nacare_live.R
import com.imeja.nacare_live.adapters.FacilityAdapter
import com.imeja.nacare_live.adapters.TrackedEntityAdapter
import com.imeja.nacare_live.data.FormatterClass
import com.imeja.nacare_live.databinding.FragmentFacilityListBinding
import com.imeja.nacare_live.model.DataElements
import com.imeja.nacare_live.model.DataValue
import com.imeja.nacare_live.model.EntityData
import com.imeja.nacare_live.model.EventUploadData
import com.imeja.nacare_live.model.FacilitySummary
import com.imeja.nacare_live.network.RetrofitCalls
import com.imeja.nacare_live.room.Converters
import com.imeja.nacare_live.room.EventData
import com.imeja.nacare_live.room.MainViewModel

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [FacilityListFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class FacilityListFragment : Fragment() {
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

    private lateinit var binding: FragmentFacilityListBinding
    private lateinit var viewModel: MainViewModel
    private val formatter = FormatterClass()
    private val dataList = ArrayList<EventData>()
    private val facilityList = ArrayList<FacilitySummary>()
    private val retrofitCalls = RetrofitCalls()
    private val formFieldsData = ArrayList<DataElements>()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentFacilityListBinding.inflate(layoutInflater)
        viewModel = MainViewModel(requireContext().applicationContext as Application)
        loadEvents()
        return binding.root
    }


    override fun onResume() {
        super.onResume()
        try {
            loadEvents()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun loadEvents() {
        val orgUnit = formatter.getSharedPref("orgCode", requireContext())
        if (orgUnit != null) {
            val data = viewModel.loadEvents(orgUnit, requireContext())
            Log.e("TAG", " Facility Data Saved $data")
            if (data != null) {

                if (data.isEmpty()) {
                    binding.apply {
                        eventButton.visibility = View.VISIBLE
                        eventsNotificator.visibility = View.VISIBLE
                    }
                } else {
                    binding.apply {
                        eventButton.visibility = View.GONE
                        eventsNotificator.visibility = View.GONE
                    }
                }
                facilityList.clear()
                dataList.clear()
                data.forEach {
                    val fc = FacilitySummary(uid = it.uid, date = it.eventDate, status = it.status)
                    facilityList.add(fc)
                }

                val adapterProgram =
                    FacilityAdapter(facilityList, requireContext(), this::handleClick)
                Log.e("TAG", " Facility Data Saved $facilityList")
                binding.apply {
                    val manager = LinearLayoutManager(requireContext())
                    eventsRecyclerView.apply {
                        adapter = adapterProgram
                        layoutManager = manager
                        val divider = DividerItemDecoration(context, manager.orientation)
                        divider.setDrawable(context.getDrawable(R.drawable.divider)!!)
                        addItemDecoration(divider)
                    }

                }
            } else {
                binding.apply {
                    eventButton.visibility = View.VISIBLE
                    eventsNotificator.visibility = View.VISIBLE
                }
            }
        }
    }

    private fun handleClick(facilitySummary: FacilitySummary) {
        val orgUnit = formatter.getSharedPref("orgCode", requireContext())
        val data = viewModel.loadEvent(facilitySummary.uid, requireContext())

        if (orgUnit != null) {
            if (data != null) {

                formatter.saveSharedPref("current_event", facilitySummary.uid, requireContext())
                formatter.saveSharedPref("existing_event", "true", requireContext())

                val typeToken = object : TypeToken<List<DataValue>>() {}.type
                var dataValuesList: List<DataValue> = Gson().fromJson(data.dataValues, typeToken)
                dataValuesList = cleanDataValuesList(dataValuesList)
                val payload = EventUploadData(
                    eventDate = data.eventDate,
                    orgUnit = data.orgUnit,
                    program = data.program,
                    status = data.status,
                    dataValues = dataValuesList
                )
                Log.e("TAG", " Facility Data Saved $payload")
                uploadFacilityData(payload)
                startActivity(Intent(requireContext(), FacilityDetailActivity::class.java))
            }
        }
    }

    private fun cleanDataValuesList(dataValuesList: List<DataValue>): List<DataValue> {
        val newList = mutableListOf<DataValue>()
        val data = viewModel.loadSingleProgram(requireContext(), "facility")
        if (data != null) {
            val converters = Converters().fromJson(data.jsonData)
            converters.programs.forEach { program ->
                program.programStages.forEach { stage ->
                    stage.programStageSections.forEach { section ->
                        val sectionDataElements = section.dataElements

                        // Check if the id from dataValuesList matches id in sectionDataElements
                        val filteredData = dataValuesList
                            .filter { dataValue -> sectionDataElements.any { field -> field.id == dataValue.dataElement } }

                        // Add the filtered data to the new list
                        newList.addAll(filteredData)
                    }
                }
            }
        }

        return newList

    }

    private fun uploadFacilityData(data: EventUploadData) {
        retrofitCalls.uploadFacilityData(requireContext(), data)

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.apply {
            textViewWithArrow.apply {
                setOnClickListener {
                    NavHostFragment.findNavController(this@FacilityListFragment).navigateUp()
                }
            }
            eventButton.apply {
                setOnClickListener {
                    val intent = Intent(requireContext(), FacilityDetailActivity::class.java)
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
         * @return A new instance of fragment FacilityListFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) = FacilityListFragment().apply {
            arguments = Bundle().apply {
                putString(ARG_PARAM1, param1)
                putString(ARG_PARAM2, param2)
            }
        }
    }
}