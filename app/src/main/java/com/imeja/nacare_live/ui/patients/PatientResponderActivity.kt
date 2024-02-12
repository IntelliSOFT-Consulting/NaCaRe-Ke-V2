package com.imeja.nacare_live.ui.patients

import android.app.Application
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import com.imeja.nacare_live.R
import com.imeja.nacare_live.adapters.ExpandableListAdapter
import com.imeja.nacare_live.data.FormatterClass
import com.imeja.nacare_live.databinding.ActivityPatientResponderBinding
import com.imeja.nacare_live.model.ExpandableItem
import com.imeja.nacare_live.model.TrackedEntityAttributes
import com.imeja.nacare_live.room.Converters
import com.imeja.nacare_live.room.MainViewModel


class PatientResponderActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPatientResponderBinding

    private lateinit var viewModel: MainViewModel
    private val formatter = FormatterClass()
    private val emptyList = ArrayList<TrackedEntityAttributes>()
    private val completeList = ArrayList<TrackedEntityAttributes>()
    private val searchList = ArrayList<TrackedEntityAttributes>()
    private val expandableList = ArrayList<ExpandableItem>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPatientResponderBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = MainViewModel(this.applicationContext as Application)
        binding.apply {
            setSupportActionBar(trackedEntityInstanceSearchToolbar)
            supportActionBar?.apply {
                title = getString(R.string.cancer_notification_tool)
                setDisplayHomeAsUpEnabled(true)
            }
            trackedEntityInstanceSearchToolbar.setNavigationOnClickListener {
                // Handle back arrow click here
                onBackPressed() // Or implement your own logic
            }
        }

        loadProgramDetails()

    }

    private fun loadProgramDetails() {

        val patientUid = formatter.getSharedPref("current_patient", this)
        if (patientUid != null) {
            val data = viewModel.loadSingleProgram(this, "notification")
            if (data != null) {
                val converters = Converters().fromJson(data.jsonData)
                searchList.clear()
                emptyList.clear()
                converters.programs.forEach { it ->
                    it.programSections.forEach {
                        if (it.name == "SEARCH PATIENT") {
                            val section = it.trackedEntityAttributes
                            Log.e("TAG", "Program Data Retrieved $section")
                            searchList.addAll(section)

                        } else {
                            val section = it.trackedEntityAttributes
                            Log.e("TAG", "Program Data Retrieved Other  $section")
                            emptyList.addAll(section)

                        }
                    }
                }
                completeList.addAll(searchList)
                completeList.addAll(emptyList)



                expandableList.add(
                    ExpandableItem(
                        groupName = "Patient Details and Cancer Information",
                        dataElements = Gson().toJson(completeList),
                        programUid = formatter.getSharedPref("programUid", this).toString(),
                        programStageUid = formatter.getSharedPref("programUid", this)
                            .toString(),
                        selectedOrgUnit = formatter.getSharedPref("orgCode", this).toString(),
                        selectedTei = patientUid,
                        isExpanded = false,
                        isProgram = false
                    )
                )


                val adapterProgram = ExpandableListAdapter(expandableList, this)

                binding.apply {
                    val manager = LinearLayoutManager(this@PatientResponderActivity)
                    trackedEntityInstanceRecyclerView.apply {
                        adapter = adapterProgram
                        layoutManager = manager
                    }

                }
            }
        } else {
            Toast.makeText(this, "Please select Patient to proceed", Toast.LENGTH_SHORT).show()
        }
    }
}