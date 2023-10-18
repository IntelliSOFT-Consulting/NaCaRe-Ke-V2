package com.intellisoft.nacare.main.registry

import android.app.Application
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.intellisoft.nacare.adapter.PersonAdapter
import com.intellisoft.nacare.helper_class.DataElementItem
import com.intellisoft.nacare.helper_class.EntityAttributes
import com.intellisoft.nacare.helper_class.FormatterClass
import com.intellisoft.nacare.helper_class.Person
import com.intellisoft.nacare.helper_class.ProgramCategory
import com.intellisoft.nacare.helper_class.ProgramSections
import com.intellisoft.nacare.helper_class.ProgramStageSections
import com.intellisoft.nacare.helper_class.SearchPatientResponse
import com.intellisoft.nacare.main.DashboardActivity
import com.intellisoft.nacare.models.Constants.PATIENT_ID
import com.intellisoft.nacare.room.Converters
import com.intellisoft.nacare.room.EventData
import com.intellisoft.nacare.room.MainViewModel
import com.intellisoft.nacare.room.ProgramData
import com.nacare.capture.R
import com.nacare.capture.databinding.ActivityPatientListBinding

class PatientListActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPatientListBinding
    private lateinit var viewModel: MainViewModel
    private lateinit var eventData: EventData
    private lateinit var seachData: SearchPatientResponse
    private val formatterClass = FormatterClass()
    private lateinit var dialog: AlertDialog
    private lateinit var event: String
    private lateinit var program: ProgramData
    private val patientList: MutableList<Person> = mutableListOf()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPatientListBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        val receivedIntent = intent
        if (receivedIntent != null) {
            val dataBundle = receivedIntent.getBundleExtra("data")
            if (dataBundle != null) {
                val ev = dataBundle.getString("event")
                val patients = dataBundle.getString("patients")
                if (ev != null) {
                    eventData = Gson().fromJson(ev, EventData::class.java)
                }else{
                    Toast.makeText(
                        this@PatientListActivity,
                        "No Event Data Received",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                if (patients != null) {
                    seachData = Gson().fromJson(patients, SearchPatientResponse::class.java)
                    generatePatientList(seachData)
                }
            }
        }
        viewModel = MainViewModel((this.applicationContext as Application))

        val adapter = PersonAdapter(patientList, this::handlePatient)
        binding.recyclerView.adapter = adapter
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        supportActionBar?.apply {
            title = "Patient List"
            setDisplayHomeAsUpEnabled(true)
        }
    }

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

    private fun generatePatientList(data: SearchPatientResponse) {
        data.trackedEntityInstances.forEach {
            val person = Person(
                trackedEntityInstance=it.trackedEntityInstance,
                patientId = retrieveAttribute("AP13g7NcBOf", it.attributes),
                firstName = retrieveAttribute("R1vaUuILrDy", it.attributes),
                middleName = retrieveAttribute("hn8hJsBAKrh", it.attributes),
                lastName = retrieveAttribute("hzVijy6tEUF", it.attributes),
                document = retrieveAttribute("oob3a4JM7H6", it.attributes),
                attribute = it.attributes
            )
            patientList.add(person)
        }

    }

    private fun retrieveAttribute(s: String, attributes: List<EntityAttributes>): String {
        if (attributes.isNotEmpty()) {
            val matchingAttribute = attributes.find { it.attribute == s }
            return matchingAttribute?.value ?: ""
        }
        return ""
    }

    private fun handlePatient(person: Person) {
        val data = loadInitialData()
        formatterClass.saveSharedPref(
            PATIENT_ID,
            person.trackedEntityInstance,
            this@PatientListActivity
        )

        if (data != null) {
            val attribute = Converters().toJsonEntityAttributes(person.attribute)
            val converters = Converters().toJsonElements(data.elements)
            val json = Gson().fromJson(converters, JsonArray::class.java)
            val jsonAttribute = Gson().fromJson(attribute, JsonArray::class.java)
            val bundle = Bundle()
            bundle.putString("code", data.id)
            bundle.putString("name", data.name)
            bundle.putString("programStageDataElements", json.toString())
            bundle.putString("attribute", jsonAttribute.toString())
            val event =
                viewModel.loadCurrentEvent(this@PatientListActivity, eventData.id.toString())
            if (event != null) {
                eventData = event
                val cc = Converters().toJsonEvent(eventData)
                bundle.putString("event", cc)
                formatterClass.saveSharedPref("event", cc, this@PatientListActivity)
                viewModel.tiePatientToEvent(
                    this@PatientListActivity,
                    eventData,
                    person.trackedEntityInstance
                )
                manipulateRetrievedAttribute(jsonAttribute.toString(),eventData)
                val intent = Intent(this@PatientListActivity, DashboardActivity::class.java)
                intent.putExtra("searchPatient", "searchPatient")
                startActivity(intent)
                this@PatientListActivity.finish()
            }
        } else {
            Toast.makeText(
                this@PatientListActivity,
                "No Indicator Elements, please try again later",
                Toast.LENGTH_SHORT
            ).show()
        }

    }
    private fun manipulateRetrievedAttribute(json: String, eventData: EventData) {
        val gson = Gson()
        val items = gson.fromJson(json, Array<EntityAttributes>::class.java)
        items.forEach {
            viewModel.addResponse(
                this@PatientListActivity,
                eventData,
                it.attribute,
                it.value
            )
        }
    }
    private fun loadInitialData(): ProgramCategory? {
        val data = viewModel.loadProgram(this, "notification")

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
            elements = treeNodes,
            position = "0",
            altElements = treeNodes
        )
    }

    private fun retrieveUserResponses(data: List<ProgramStageSections>): String {
        var count = 0
        if (data.isNotEmpty()) {
            data.first().dataElements.forEach {
                val response =
                    viewModel.getEventResponse(
                        this@PatientListActivity,
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