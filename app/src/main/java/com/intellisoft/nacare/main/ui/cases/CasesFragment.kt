package com.intellisoft.nacare.main.ui.cases

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.intellisoft.nacare.adapter.EventAdapter
import com.intellisoft.nacare.helper_class.EventPayload
import com.intellisoft.nacare.helper_class.FormatterClass
import com.intellisoft.nacare.helper_class.MultipleEvents
import com.intellisoft.nacare.helper_class.PayloadDataValues
import com.intellisoft.nacare.helper_class.ProgramStages
import com.intellisoft.nacare.main.registry.RegistryActivity
import com.intellisoft.nacare.models.Constants
import com.intellisoft.nacare.models.Constants.PROGRAM_TRACKED_ENTITY_TYPE
import com.intellisoft.nacare.models.Constants.TRACKED_ENTITY_TYPE
import com.intellisoft.nacare.room.Converters
import com.intellisoft.nacare.room.EventData
import com.intellisoft.nacare.room.MainViewModel
import com.intellisoft.nacare.viewmodels.NetworkViewModel
import com.nacare.capture.R
import com.nacare.capture.databinding.FragmentCasesBinding
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class CasesFragment : Fragment(), FilterBottomSheetListener {
    private lateinit var binding: FragmentCasesBinding
    private lateinit var mRecyclerView: RecyclerView
    private lateinit var viewModel: MainViewModel
    private lateinit var networkViewModel: NetworkViewModel
    private lateinit var formatterClass: FormatterClass
    private lateinit var dataList: List<EventData>

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewModel = ViewModelProvider(this).get(MainViewModel::class.java)

        networkViewModel = ViewModelProvider(this).get(NetworkViewModel::class.java)
        binding = FragmentCasesBinding.inflate(inflater, container, false)
        formatterClass = FormatterClass()
        val root: View = binding.root
        mRecyclerView = binding.recyclerView
        mRecyclerView.setHasFixedSize(true)
        mRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        binding.addFab.apply {
            setOnClickListener {
                val event = viewModel.loadLatestEvent(requireContext())
                loadCurrentEvent(event)
            }
        }
        loadEventData("ALL")
        setHasOptionsMenu(true)
        return root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.menu_search, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle item selection
        return when (item.itemId) {
            R.id.action_filter -> {
                // Do something when the menu item is clicked
                showFilterBottomSheet()
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun showFilterBottomSheet() {
        val bottomSheetFragment = FilterBottomSheetFragment()
        bottomSheetFragment.setFilterBottomSheetListener(this)
        bottomSheetFragment.show(parentFragmentManager, bottomSheetFragment.tag)

    }

    private fun loadCurrentEvent(event: EventData?) {
        if (event != null) {

            formatterClass.saveSharedPref("date", event.date, requireContext())
            formatterClass.saveSharedPref("code", event.orgUnitCode, requireContext())
            formatterClass.saveSharedPref("name", event.orgUnitName, requireContext())

            val bundle = Bundle()
            val converters = Converters().toJsonEvent(event)
            bundle.putString("event", converters)
            formatterClass.saveSharedPref("event", converters, requireContext())
            networkViewModel.updateData(event)
            val intent = Intent(requireContext(), RegistryActivity::class.java)
            intent.putExtra("data", bundle)
            startActivity(intent)
        } else {
            Toast.makeText(
                requireContext(),
                "Event Error, please try again",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun loadEventData(status: String) {
        val data = viewModel.loadEvents(requireContext(), status)
        if (!data.isNullOrEmpty()) {
            binding.recyclerView.visibility = View.VISIBLE
            binding.tvNoCases.visibility = View.GONE
            dataList = data
            val adapter =
                EventAdapter(dataList, requireContext(), this::handleClick, this::syncEvent)
            mRecyclerView.adapter = adapter
            mRecyclerView.layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
            adapter.notifyDataSetChanged()
        } else {
            binding.recyclerView.visibility = View.GONE
            binding.tvNoCases.visibility = View.VISIBLE
        }

    }


    private fun handleClick(data: EventData) {
        val event = viewModel.loadCurrentEvent(requireContext(), data.id.toString())
        loadCurrentEvent(event)

    }

    private fun syncEvent(event: EventData) {
        Log.e("TAG", "Event Sync $event")
        val data = viewModel.loadProgram(requireContext(), "notification")
        if (data != null) {
            val json = data.programStages
            val gson = Gson()
            val items = gson.fromJson(json, Array<ProgramStages>::class.java)
            val multiple = mutableListOf<EventPayload>()
            items.forEach {
                val dataValues = mutableListOf<PayloadDataValues>()
                val sections = it.programStageSections
                sections.first().dataElements.forEach { k ->
                    val valueData = PayloadDataValues(
                        dataElement = k.id,
                        value = k.valueType,
                    )
                    dataValues.add(valueData)
                }
                val entity = formatterClass.getSharedPref(TRACKED_ENTITY_TYPE, requireContext())
                if (entity != null) {
                    val programCode =
                        formatterClass.getSharedPref(
                            PROGRAM_TRACKED_ENTITY_TYPE,
                            requireContext()
                        )
                    if (programCode != null) {
                        val payload = EventPayload(
                            program = programCode,
                            orgUnit = event.orgUnitCode,
                            eventDate = event.date,
                            status = "COMPLETED",
                            programStage = it.id,
                            trackedEntityInstance = event.entityId,
                            dataValues = dataValues
                        )
                        multiple.add(payload)

                    }
                }
            }

            val allEventData = MultipleEvents(
                events = multiple
            )

            val end = gson.toJson(allEventData)
            println(end)
            allEventData.events.forEach {
                val dv = gson.toJson(it)
                Log.e("TAG", "Payload Data $dv")
            }


        }

    }


    override fun onStatusClicked(status: String) {
        loadEventData(status)
    }

    override fun onDateClick() {

    }

    override fun onDateRangeClicked() {

    }


}