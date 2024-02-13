package com.nacare.capture.ui.patients

import android.content.Intent
import android.os.Bundle
import android.text.Html
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.button.MaterialButton
import com.nacare.capture.R
import com.nacare.capture.adapters.SearchResultsAdapter
import com.nacare.capture.data.FormatterClass
import com.nacare.capture.databinding.ActivityPatientSearchResultsBinding
import com.nacare.capture.model.Attributes
import com.nacare.capture.model.SearchResult
import com.nacare.capture.room.Converters


class PatientSearchResultsActivity : AppCompatActivity() {
    val formatter = FormatterClass()
    private lateinit var binding: ActivityPatientSearchResultsBinding
    private val searchResult = ArrayList<SearchResult>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPatientSearchResultsBinding.inflate(layoutInflater)
        setContentView(binding.root)
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

        displayResults()
    }

    private fun displayResults() {
        val results = formatter.getSharedPref("search_results", this)
        Log.e("TAG", "Results $results")
        if (results != null) {
            val converters = Converters().fromJsonPatientSearch(results)
            searchResult.clear()
            Log.e("TAG", "Results $converters")
            converters.trackedEntityInstances.forEach {
                val data = SearchResult(
                    trackedEntityInstance = it.trackedEntityInstance,
                    enrollmentUid = it.enrollments.getOrNull(0)?.enrollment.orEmpty(),
                    eventUid = it.enrollments.getOrNull(0)?.events?.getOrNull(0)?.event.orEmpty(),
                    uniqueId = extractValue("AP13g7NcBOf", it.attributes, false),
                    hospitalNo = extractValue("MiXrdHDZ6Hw", it.attributes, false),
                    patientName = extractValue("R1vaUuILrDy", it.attributes, true),
                    identification = extractValue("oob3a4JM7H6", it.attributes, false),
                    diagnosis = extractValue("BzhDnF5fG4x", it.attributes, false)
                )
                searchResult.add(data)

            }

            Log.e("TAG", "Results Final **** $searchResult")
            val adapterProgram =
                SearchResultsAdapter(searchResult, this, this::handleClick)
            binding.apply {
                val manager = LinearLayoutManager(this@PatientSearchResultsActivity)
                trackedEntityInstanceRecyclerView.apply {
                    adapter = adapterProgram
                    layoutManager = manager
                    val divider = DividerItemDecoration(context, manager.orientation)
                    divider.setDrawable(context.getDrawable(R.drawable.divider)!!)
                    addItemDecoration(divider)
                }
            }
        }
    }

    private fun handleClick(data: SearchResult) {
        val builder = AlertDialog.Builder(this)
        val inflater = LayoutInflater.from(this)
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
                formatter.saveSharedPref(
                    "current_patient",
                    data.trackedEntityInstance,
                    this@PatientSearchResultsActivity
                )
                startActivity(Intent(this@PatientSearchResultsActivity, PatientResponderActivity::class.java))
                Log.e(
                    "TAG",
                    "Creation Data **** Only need enrollment ${data.enrollmentUid}"

                )
                val eventUid = formatter.generateUUID(11)
                formatter.saveSharedPref(
                    "current_patient",
                    data.trackedEntityInstance,
                    this@PatientSearchResultsActivity
                )
                startActivity(Intent(this@PatientSearchResultsActivity, PatientResponderActivity::class.java))
                Log.e(
                    "TAG",
                    "Creation Data **** Only need enrollment ${data.enrollmentUid} Generated UID $eventUid"

                )
            }
        }
        yesButton.apply {
            setOnClickListener {
                alertDialog.dismiss()
                // get latest event
                Log.e(
                    "TAG",
                    "Creation Data **** ${data.eventUid} for Enrollment ${data.enrollmentUid}"
                )
            }
        }

        alertDialog.show()

    }

    private fun extractValue(
        itemUid: String,
        attributes: List<Attributes>,
        multiple: Boolean
    ): String {
        val value = if (multiple) {
            val firstNameAttribute = attributes.find { it.attribute == itemUid }
            val middleNameAttribute = attributes.find { it.attribute == "hn8hJsBAKrh" }
            val lastNameAttribute = attributes.find { it.attribute == "hzVijy6tEUF" }

            val fName = firstNameAttribute?.value ?: ""
            val mName = middleNameAttribute?.value ?: ""
            val lName = lastNameAttribute?.value ?: ""
            " $fName $mName $lName"

        } else {
            val matchingAttribute = attributes.find { it.attribute == itemUid }
            matchingAttribute?.value ?: ""
        }
        return value
    }
}