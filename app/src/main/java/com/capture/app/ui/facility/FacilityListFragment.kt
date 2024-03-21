package com.capture.app.ui.facility

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
import com.capture.app.R
import com.capture.app.adapters.FacilityAdapter
import com.capture.app.data.FormatterClass
import com.capture.app.data.PermissionManager
import com.capture.app.databinding.FragmentFacilityListBinding
import com.capture.app.model.DataElements
import com.capture.app.model.DataValue
import com.capture.app.model.FacilitySummary
import com.capture.app.network.RetrofitCalls
import com.capture.app.room.EventData
import com.capture.app.room.MainViewModel
import java.util.Date

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

            if (data != null) {
                if (data.isEmpty()) {
                    loadLiveEvents(orgUnit)
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
                    val fc = FacilitySummary(
                        id = it.id.toString(),
                        uid = it.uid,
                        date = it.eventDate,
                        status = it.status
                    )
                    facilityList.add(fc)
                }

                val hasWriteAccess = PermissionManager().hadWriteAccess(requireContext())
                val adapterProgram =
                    FacilityAdapter(facilityList, requireContext(), this::handleClick,hasWriteAccess)

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

    private fun loadLiveEvents(orgUnit: String) {
        val program = formatter.getSharedPref("programUid", requireContext())
        retrofitCalls.loadFacilityEvents(
            requireContext(),
            program.toString(),
            orgUnit,
            binding.progressBar
        )

//        loadEvents()

    }

    private fun handleClick(facilitySummary: FacilitySummary) {
        val orgUnit = formatter.getSharedPref("orgCode", requireContext())
        val data = viewModel.loadEventById(facilitySummary.id, requireContext())
        if (orgUnit != null) {
            if (data != null) {
                formatter.saveSharedPref("current_event", facilitySummary.uid, requireContext())
                formatter.saveSharedPref(
                    "current_event_date",
                    facilitySummary.date,
                    requireContext()
                )
                formatter.saveSharedPref("existing_event", "true", requireContext())

                formatter.saveSharedPref("current_data", data.dataValues, requireContext())
                formatter.saveSharedPref("current_event_id", "${data.id}", requireContext())
                formatter.deleteSharedPref("reload", requireContext())
                formatter.deleteSharedPref("current_facility_data", requireContext())
                startActivity(Intent(requireContext(), FacilityDetailActivity::class.java))
            }
        }
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
                    val orgCode = formatter.getSharedPref("orgCode", requireContext())
                    val programUid = formatter.getSharedPref("programUid", requireContext())
                    formatter.deleteSharedPref("current_event", requireContext())
                    formatter.deleteSharedPref("current_event_id", requireContext())
                    formatter.deleteSharedPref(
                        "current_event_date",
                        requireContext()
                    )
                    val data = EventData(
                        uid = formatter.generateUUID(11),
                        program = programUid.toString(),
                        orgUnit = orgCode.toString(),
                        eventDate = formatter.formatCurrentDate(Date()),
                        status = "ACTIVE",
                        isServerSide = false,
                        dataValues = "[]"
                    )

                    formatter.deleteSharedPref("current_facility_data", requireContext())
                    viewModel.saveEventUpdated(requireContext(), data, formatter.generateUUID(11))
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