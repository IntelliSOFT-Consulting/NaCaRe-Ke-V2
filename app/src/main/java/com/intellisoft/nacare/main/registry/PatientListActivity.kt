package com.intellisoft.nacare.main.registry

import android.app.Application
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.intellisoft.nacare.adapter.PersonAdapter
import com.intellisoft.nacare.helper_class.DataElementItem
import com.intellisoft.nacare.helper_class.FormatterClass
import com.intellisoft.nacare.helper_class.Person
import com.intellisoft.nacare.helper_class.ProgramCategory
import com.intellisoft.nacare.helper_class.ProgramSections
import com.intellisoft.nacare.helper_class.ProgramStageSections
import com.intellisoft.nacare.room.Converters
import com.intellisoft.nacare.room.EventData
import com.intellisoft.nacare.room.MainViewModel
import com.intellisoft.nacare.room.ProgramData
import com.intellisoft.nacare.util.AppUtils.generateDummyData
import com.nacare.ke.capture.R
import com.nacare.ke.capture.databinding.ActivityPatientListBinding

class PatientListActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPatientListBinding
    private lateinit var viewModel: MainViewModel
    private lateinit var eventData: EventData
    private val formatterClass = FormatterClass()
    private lateinit var dialog: AlertDialog
    private lateinit var event: String
    private lateinit var program: ProgramData
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPatientListBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        val receivedIntent = intent
        if (receivedIntent != null) {
            val dataBundle = receivedIntent.getBundleExtra("data")
            if (dataBundle != null) {
                val code = dataBundle.getString("code")
                val name = dataBundle.getString("name")
                val ev = dataBundle.getString("event")
                if (ev != null) {
                    eventData = Gson().fromJson(ev, EventData::class.java)
                }
            }
        }
        viewModel = MainViewModel((this.applicationContext as Application))
        val people = generateDummyData()
        val adapter = PersonAdapter(people, this::handlePatient)
        binding.recyclerView.adapter = adapter
        binding.recyclerView.layoutManager = LinearLayoutManager(this)

    }

    private fun handlePatient(person: Person) {
        val data = loadInitialData()

        if (data != null) {
            val converters = Converters().toJsonElements(data.elements)
            val json = Gson().fromJson(converters, JsonArray::class.java)
            val bundle = Bundle()
            bundle.putString("code", data.id)
            bundle.putString("name", data.name)
            bundle.putString("programStageDataElements", json.toString())

            val cc = Converters().toJsonEvent(eventData)
            bundle.putString("event", cc)

            val intent = Intent(this@PatientListActivity, ResponderActivity::class.java)
            intent.putExtra("data", bundle)
            startActivity(intent)
            this@PatientListActivity.finish()
        } else {
            Toast.makeText(
                this@PatientListActivity,
                "No Indicator Elements, please try again later",
                Toast.LENGTH_SHORT
            ).show()
        }

    }

    private fun loadInitialData(): ProgramCategory? {
        val data = viewModel.loadProgram(this)

        if (data != null) {
            program = data
            val org = formatterClass.getSharedPref("name", this)
            val date = formatterClass.getSharedPref("date", this)

            return loadProgramData(program)

        }
        return null
    }

    private fun loadProgramData(program: ProgramData): ProgramCategory {
        val treeNodes = mutableListOf<ProgramStageSections>()

        val gson = Gson()
        val json1 = program.programTrackedEntityAttributes
        val items1 = gson.fromJson(json1, Array<ProgramSections>::class.java)
        items1.forEach {
            val elements = mutableListOf<DataElementItem>()
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
        val distinctTreeNodes = treeNodes.distinctBy { it.id }
        treeNodes.clear()
        treeNodes.addAll(distinctTreeNodes)
        return ProgramCategory(
            iconResId = R.drawable.home,
            name = "Patient Details",
            id = "patient-detail",
            done = retrieveUserResponses(treeNodes),
            total = calculateTotalElements(treeNodes),
            elements = treeNodes
        )
    }

    private fun retrieveUserResponses(data: List<ProgramStageSections>): String {
        var count = 0
        if (data.isNotEmpty()) {
            data.first().dataElements.forEach {
                val response =
                    viewModel.getEventResponse(
                        this@PatientListActivity,
                        eventData.id.toString(),
                        it.id
                    )
                if (response != null) {
                    count++
                }
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
}