package com.intellisoft.nacare.main.registry

import android.app.Application
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.button.MaterialButton
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.intellisoft.nacare.adapter.ProgramAdapter
import com.intellisoft.nacare.helper_class.DataElementItem
import com.intellisoft.nacare.helper_class.FormatterClass
import com.intellisoft.nacare.helper_class.ProgramCategory
import com.intellisoft.nacare.helper_class.ProgramSections
import com.intellisoft.nacare.helper_class.ProgramStageSections
import com.intellisoft.nacare.helper_class.ProgramStages
import com.intellisoft.nacare.main.dialogs.ConfirmCancelDialog
import com.intellisoft.nacare.main.dialogs.ConfirmCancelDialogListener
import com.intellisoft.nacare.main.facility.FacilityActivity
import com.intellisoft.nacare.main.ui.cases.FilterBottomSheetFragment
import com.intellisoft.nacare.models.Constants
import com.intellisoft.nacare.models.Constants.PATIENT_ID
import com.intellisoft.nacare.models.Constants.PATIENT_REGISTRATION
import com.intellisoft.nacare.room.Converters
import com.intellisoft.nacare.room.EventData
import com.intellisoft.nacare.room.MainViewModel
import com.intellisoft.nacare.room.ProgramData
import com.intellisoft.nacare.util.AppUtils
import com.nacare.capture.R
import com.nacare.capture.databinding.ActivityRegistryBinding
import com.nacare.capture.databinding.ConfirmCancelDialogBinding

class RegistryActivity : AppCompatActivity(), ConfirmCancelDialogListener {
    private lateinit var binding: ActivityRegistryBinding
    private lateinit var program: ProgramData
    private lateinit var eventData: EventData
    private lateinit var event: String
    private lateinit var viewModel: MainViewModel
    private val formatterClass = FormatterClass()
    private val dataList: MutableList<ProgramCategory> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegistryBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        val receivedIntent = intent
        if (receivedIntent != null) {
            val dataBundle = receivedIntent.getBundleExtra("data")
            if (dataBundle != null) {
                val ev = dataBundle.getString("event")
                if (ev != null) {
                    event = ev
                    eventData = Gson().fromJson(event, EventData::class.java)
                }
            }
        }
        viewModel = MainViewModel((this.applicationContext as Application))
        loadInitialData()

        binding.apply {
            materialCardView.setOnClickListener {
                val code = formatterClass.getSharedPref("code", this@RegistryActivity)
                val name = formatterClass.getSharedPref("name", this@RegistryActivity)
                if (code != null && name != null) {
                    loadFacilityEvents(code, name)
                } else {
                    Toast.makeText(this@RegistryActivity, "Please try again", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }

        binding.fab.apply {
            setOnClickListener {

                val bottomSheetFragment = ConfirmCancelDialog()
                bottomSheetFragment.setFilterBottomSheetListener(this@RegistryActivity)
                bottomSheetFragment.show(supportFragmentManager, bottomSheetFragment.tag)

            }
        }

    }

    private fun loadFacilityEvents(code: String, name: String) {
        val data = viewModel.loadFacilityEvents(this@RegistryActivity, code)
        if (data != null) {

            if (data.dataValues.isNotEmpty()) {
                val bundle = Bundle()
                bundle.putString("code", code)
                bundle.putString("name", name)
                bundle.putString("dataValues", data.dataValues)
                val intent = Intent(this@RegistryActivity, FacilityActivity::class.java)
                intent.putExtra("data", bundle)
                startActivity(intent)
            } else {
                Toast.makeText(
                    this@RegistryActivity,
                    "No Facility details found. Please try again later!!",
                    Toast.LENGTH_SHORT
                )
                    .show()
            }
        } else {
            Toast.makeText(
                this@RegistryActivity,
                "No Facility details found. Please try again later!!",
                Toast.LENGTH_SHORT
            )
                .show()
        }

    }

    private fun loadInitialData() {
        val data = viewModel.loadProgram(this, "notification")

        if (data != null) {
            program = data
            val org = formatterClass.getSharedPref("name", this)
            val date = formatterClass.getSharedPref("date", this)
            supportActionBar?.apply {
                setDisplayHomeAsUpEnabled(true)

            }
            binding.apply {
                tvTitle.text = program.name
                tvSubTitle.text = "$date | $org"
            }
            loadProgramData(program)

        }
    }

    override fun onResume() {
        loadInitialData()
        super.onResume()
    }

    private fun loadProgramData(program: ProgramData) {
//        try {
        var counter = 0
        var complete = 0
        val treeNodes = mutableListOf<ProgramStageSections>()
        val allTreeNodes = mutableListOf<ProgramStageSections>()

        val json = program.programStages
        val gson = Gson()
        val items = gson.fromJson(json, Array<ProgramStages>::class.java)

        val json1 = program.programTrackedEntityAttributes
        val items1 = gson.fromJson(json1, Array<ProgramSections>::class.java)
        items1.forEach {
            val combined = mutableListOf<DataElementItem>()
            val elements = mutableListOf<DataElementItem>()
            if (it.name == "SEARCH PATIENT") {
                it.trackedEntityAttributes.forEach { k ->
                    val del = DataElementItem(
                        k.id,
                        k.displayName,
                        k.valueType,
                        optionSet = k.optionSet
                    )
                    combined.add(del)
                }

                val pd = ProgramStageSections(
                    id = it.name,
                    displayName = it.name,
                    dataElements = combined
                )
                allTreeNodes.add(pd)
            }
            it.trackedEntityAttributes.forEach { k ->
                val del = DataElementItem(
                    k.id,
                    k.displayName,
                    k.valueType,
                    optionSet = k.optionSet
                )
                elements.add(del)
            }

            val pd = ProgramStageSections(
                id = it.name,
                displayName = it.name,
                dataElements = elements
            )
            treeNodes.add(pd)
        }

        dataList.clear()

        val done1 = retrievePatientResponses(treeNodes)
        val total1 = calculateTotalElements(treeNodes)
        counter = total1.toInt()
        complete = done1.toInt()
        val pr = ProgramCategory(
            iconResId = R.drawable.home,
            name = "Patient Details",
            id = "patient-detail",
            done = done1,
            total = total1,
            elements = allTreeNodes,
            position = "0",
            altElements = treeNodes
        )
        dataList.add(pr)

        items.forEachIndexed { index, it ->

            val done = retrieveUserResponses(it.programStageSections)
            val total = calculateTotalElements(it.programStageSections)
            counter += total.toInt()
            complete += done.toInt()
            val pd = ProgramCategory(
                iconResId = R.drawable.home,
                name = it.name,
                id = it.id,
                done = done,
                total = total,
                elements = it.programStageSections,
                position = index.toString(),
                altElements = emptyList()

            )
            dataList.add(pd)
        }
        val ad = ProgramAdapter(this, dataList, this::handleClick, eventData)

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(this@RegistryActivity)
            adapter = ad

        }
        ad.notifyDataSetChanged()

        binding.apply {


            val percent = if (counter != 0) {
                (complete.toDouble() / counter.toDouble()) * 100
            } else {
                0.0 // handle division by zero if necessary
            }
            Log.e("TAG", "Percentage $percent Done $complete Total $counter")

            progressBar.progress = percent.toInt()
            textViewProgress.text = "${percent.toInt()}%"
        }


    }

    private fun retrievePatientResponses(data: List<ProgramStageSections>): String {
        var count = 0
        if (data.isNotEmpty()) {
            data.forEach {
                var each = 0
                it.dataElements.forEach {
                    val response =
                        viewModel.getEventResponse(
                            this@RegistryActivity,
                            eventData,
                            it.id
                        )

                    if (response != null) {
                        each++
                    }
                }
                count += each
            }
        }

        return "$count"
    }

    private fun calculateTotalElements(data: List<ProgramStageSections>): String {
        var count = 0
        if (data.isNotEmpty()) {
            data.forEach {
                val total = it.dataElements.size
                count += total
            }
        }
        return "$count"

    }

    private fun retrieveUserResponses(data: List<ProgramStageSections>): String {
        var count = 0
        if (data.isNotEmpty()) {
            data.first().dataElements.forEach {
                val response =
                    viewModel.getEventResponse(
                        this@RegistryActivity,
                        eventData,
                        it.id
                    )
                if (response != null) {
                    count++
                }
            }
        }

        return "$count"
    }

    private fun handleClick(data: ProgramCategory) {

        if (data.total!! > 0.toString()) {

            val converters = Converters().toJsonElements(data.elements)
            val json = Gson().fromJson(converters, JsonArray::class.java)
            val bundle = Bundle()
            bundle.putString("code", data.id)
            bundle.putString("name", data.name)
            bundle.putString(
                "programStageDataElements",
                checkOptions(data.name, json.toString(), data.altElements)
            )
            bundle.putString(
                "event", event
            )
            controlNavigation(data)

            val d = generatePath(data.name)

            if (data.name != "Patient Details") {
                val patient = formatterClass.getSharedPref(PATIENT_ID, this@RegistryActivity)
                if (patient.isNullOrEmpty()) {
                    Toast.makeText(
                        this@RegistryActivity,
                        "Please specify patient details",
                        Toast.LENGTH_SHORT
                    ).show()

                    return
                }
            }
            val intent = Intent(this@RegistryActivity, d)
            intent.putExtra("data", bundle)
            startActivity(intent)
        } else {
            Toast.makeText(
                this@RegistryActivity,
                "No Indicator Elements, please try again later",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun checkOptions(
        name: String,
        initial: String,
        altElements: List<ProgramStageSections>?
    ): String {
        if (name == "Patient Details") {
            val exists = viewModel.getPatientDetails(this@RegistryActivity, eventData)
            return if (!exists) {
                initial
            } else {
                if (altElements != null) {
                    val converters = Converters().toJsonElements(altElements)
                    val json = Gson().fromJson(converters, JsonArray::class.java)
                    json.toString()
                } else {
                    initial
                }
            }
        }
        return initial
    }

    private fun generatePath(name: String): Class<*>? {
        return if (name == "Patient Details") {
            formatterClass.saveSharedPref(
                PATIENT_REGISTRATION, "true",
                this@RegistryActivity,
            )
            val patient =
                viewModel.updateEventWithPatientId(
                    this@RegistryActivity,
                    eventData,
                    AppUtils.generateUuid()
                )
            if (patient != null) {
                formatterClass.saveSharedPref(
                    PATIENT_ID, patient,
                    this@RegistryActivity
                )
                eventData.patientId = patient
            }
            val exists = viewModel.getPatientDetails(this@RegistryActivity, eventData)
            if (!exists) {
                PatientSearchActivity::class.java
            } else {
                ResponderActivity::class.java
            }
        } else {
            formatterClass.deleteSharedPref(
                PATIENT_REGISTRATION,
                this@RegistryActivity
            )
            ResponderActivity::class.java
        }

    }

    private fun controlNavigation(data: ProgramCategory) {
        formatterClass.saveSharedPref("program", data.name, this@RegistryActivity)
        if (data.name == "Patient Details") {
            val exists = viewModel.getPatientDetails(this@RegistryActivity, eventData)
            if (!exists) {
                formatterClass.deleteSharedPref(PATIENT_ID, this@RegistryActivity)
            }
        }

    }

    override fun onBackPressed() {
        super.onBackPressed()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return super.onSupportNavigateUp()
    }

    // You can override onOptionsItemSelected to handle toolbar item clicks (e.g., back button)
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                // Handle the back button click (if needed)
                onBackPressed()
                return true
            }
            // Handle other menu item clicks if you have any
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onSubmitClick() {
        try {
            val saved = viewModel.competeEvent(this@RegistryActivity, eventData)
            if (saved) {
                Toast.makeText(
                    this@RegistryActivity,
                    "Event saved",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                Toast.makeText(
                    this@RegistryActivity,
                    "Error Encountered saving event, please try again later",
                    Toast.LENGTH_SHORT
                ).show()
            }
        } catch (e: Exception) {
            Toast.makeText(
                this@RegistryActivity,
                "Error Encountered saving event, please try again later",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    override fun onCancelClick() {

    }
}
