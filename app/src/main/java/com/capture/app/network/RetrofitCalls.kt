package com.capture.app.network

import android.app.Application
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.google.android.material.button.MaterialButton
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.capture.app.R
import com.capture.app.auth.SyncActivity
import com.capture.app.data.Constants
import com.capture.app.data.Constants.PATIENT_UNIQUE
import com.capture.app.data.FormatterClass
import com.capture.app.model.ChildOrgUnit
import com.capture.app.model.DataValue
import com.capture.app.model.EnrollmentEventUploadData
import com.capture.app.model.Enrollments
import com.capture.app.model.EventUploadData
import com.capture.app.model.TrackedEntityInstanceAttributes
import com.capture.app.model.TrackedEntityInstancePostData
import com.capture.app.model.TrackedEntityInstanceServer
import com.capture.app.model.TrackedEntityInstances
import com.capture.app.response.OrgUnitResponse
import com.capture.app.room.Converters
import com.capture.app.room.DataStoreData
import com.capture.app.room.EventData
import com.capture.app.room.MainViewModel
import com.capture.app.room.ProgramData
import com.capture.app.room.TrackedEntityInstanceData
import com.capture.app.room.orgUnit
import com.capture.app.ui.patients.PatientRegistrationActivity
import com.capture.app.ui.patients.PatientSearchActivity
import com.capture.app.ui.patients.PatientSearchResultsActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

class RetrofitCalls {


    fun loadProgram(context: Context, program: String) {
        CoroutineScope(Dispatchers.IO).launch {
            val formatter = FormatterClass()
            val viewModel = MainViewModel(context.applicationContext as Application)
            val username = formatter.getSharedPref("username", context)
            val apiService = RetrofitBuilder.getRetrofit(context, BASE_URL = Constants.BASE_URL)
                .create(Interface::class.java)
            try {
                val apiInterface = apiService.loadProgram(filter = "name:ilike:$program")
                if (apiInterface.isSuccessful) {

                    val statusCode = apiInterface.code()
                    val body = apiInterface.body()

                    if (statusCode == 200 || statusCode == 201) {

                        if (body != null) {
                            val converters = Converters().toJson(body)
                            val data =
                                ProgramData(jsonData = converters, userId = program)
                            viewModel.addIndicators(data)
                        }

                    } else {
                        Log.e("TAG From", "Retrieved Data Error Code $statusCode")
                    }
                } else {
                    Log.e("TAG From", "Retrieved Data Not Successful")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Log.e("TAG From", "Retrieved Data Error ${e.message}")
            }
        }
    }

    fun signIn(context: Context, progressDialog: ProgressDialog) {
        CoroutineScope(Dispatchers.Main).launch {
            val formatter = FormatterClass()

            val apiService = RetrofitBuilder.getRetrofit(context, BASE_URL = Constants.BASE_URL)
                .create(Interface::class.java)
            try {
                progressDialog.show()
                val apiInterface = apiService.signIn()
                if (apiInterface.isSuccessful) {
                    val statusCode = apiInterface.code()
                    val body = apiInterface.body()
                    if (progressDialog.isShowing) {
                        progressDialog.dismiss()
                    }
                    if (statusCode == 200 || statusCode == 201) {
                        formatter.deleteSharedPref("orgName", context)
                        formatter.deleteSharedPref("orgCode", context)
                        formatter.saveSharedPref("isLoggedIn", "true", context)

                        if (body != null) {
                            val converters = Converters().toUserJson(body)
                            formatter.saveSharedPref("user_data", converters, context)
                        }
                        val intent = Intent(context, SyncActivity::class.java)
                        context.startActivity(intent)
                    } else {
                        if (progressDialog.isShowing) {
                            progressDialog.dismiss()
                        }
                        Toast.makeText(
                            context,
                            "Experienced Problems Authenticating ",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    if (progressDialog.isShowing) {
                        progressDialog.dismiss()
                    }
                    Toast.makeText(
                        context,
                        "Invalid username or password",
                        Toast.LENGTH_SHORT
                    ).show()
                }

            } catch (e: Exception) {
                e.printStackTrace()
                if (progressDialog.isShowing) {
                    progressDialog.dismiss()
                }
            }
        }

    }

    fun performPatientSearchOld(
        context: Context,
        programUid: String,
        trackedEntity: String,
        searchParametersString: String,
        layoutInflater: LayoutInflater,
        progressDialog: ProgressDialog
    ) {
        CoroutineScope(Dispatchers.Main).launch {
            val formatter = FormatterClass()

            val apiService =
                RetrofitBuilder.getRetrofit(context, Constants.BASE_URL)
                    .create(Interface::class.java)
            try {
                progressDialog.show()
                val apiInterface =
                    apiService.searchPatient(filter = searchParametersString, program = programUid)
                if (apiInterface.isSuccessful) {
                    val statusCode = apiInterface.code()
                    val body = apiInterface.body()
                    if (progressDialog.isShowing) {
                        progressDialog.dismiss()
                    }
                    when (statusCode) {
                        200 -> {
                            if (body != null) {
                                try {
                                    if (body.trackedEntityInstances.isEmpty()) {
                                        noPatientRecordFound(context, layoutInflater)
                                    } else {
                                        val converters = Converters().toJsonPatientSearch(body)

                                        formatter.saveSharedPref(
                                            "search_results",
                                            converters,
                                            context
                                        )
                                        context.startActivity(
                                            Intent(
                                                context,
                                                PatientSearchResultsActivity::class.java
                                            )
                                        )
                                        (context as PatientSearchActivity).finish()
                                    }
                                } catch (e: Exception) {
                                    e.printStackTrace()

                                }
                            }
                        }
                    }
                } else {
                    if (progressDialog.isShowing) {
                        progressDialog.dismiss()
                    }
                    val statusCode = apiInterface.code()
                    val errorBody = apiInterface.errorBody()?.string()
                    when (statusCode) {
                        409 -> {}
                        500 -> {}
                    }
                }
            } catch (e: Exception) {
                print(e)

                if (progressDialog.isShowing) {
                    progressDialog.dismiss()
                }

            }
        }
    }

    fun performPatientSearch(
        context: Context,
        programUid: String,
        trackedEntity: String,
        searchParametersString: String,
        searchParametersStringMiddle: String,
        searchParametersStringLast: String,
        layoutInflater: LayoutInflater,
        progressDialog: ProgressDialog
    ) {
        CoroutineScope(Dispatchers.Main).launch {
            val formatter = FormatterClass()

            val apiService =
                RetrofitBuilder.getRetrofit(context, Constants.BASE_URL)
                    .create(Interface::class.java)
            try {
                progressDialog.show()

                val deferredResults = listOf(
                    async {
                        apiService.searchPatient(
                            filter = searchParametersString,
                            program = programUid
                        )
                    },
                    async {
                        apiService.searchPatient(
                            filter = searchParametersStringMiddle,
                            program = programUid
                        )
                    },
                    async {
                        apiService.searchPatient(
                            filter = searchParametersStringLast,
                            program = programUid
                        )
                    }

                )

                val results = deferredResults.awaitAll()

                if (progressDialog.isShowing) {
                    progressDialog.dismiss()
                }

                // Process the results
                // Process the results
                val successfulResults = results.filter { it.isSuccessful }

                // If all API calls are successful
                if (successfulResults.size == 3) {
                    val bodies = successfulResults.mapNotNull { it.body() }

                    val body1 = bodies.getOrNull(0)
                    val body2 = bodies.getOrNull(1)
                    val body3 = bodies.getOrNull(2)

                    // Check if all trackedEntityInstances are empty
                    val noPatientRecords = bodies.all { it.trackedEntityInstances.isEmpty() }

                    if (noPatientRecords) {
                        // Show "No Patient Record Found" message
                        noPatientRecordFound(context, layoutInflater)
                    } else {
                        // Manipulate the results
                        val mergedResults = mutableListOf<TrackedEntityInstances>()
                        body1?.takeIf { it.trackedEntityInstances.isNotEmpty() }?.let {
                            mergedResults.addAll(it.trackedEntityInstances)
                        }
                        body2?.takeIf { it.trackedEntityInstances.isNotEmpty() }?.let {
                            mergedResults.addAll(it.trackedEntityInstances)
                        }
                        body3?.takeIf { it.trackedEntityInstances.isNotEmpty() }?.let {
                            mergedResults.addAll(it.trackedEntityInstances)
                        }
                        val uniqueResults = mergedResults.distinctBy { it.trackedEntityInstance }

                        formatter.saveSharedPref(
                            "search_results",
                            Gson().toJson(uniqueResults),
                            context
                        )
                        context.startActivity(
                            Intent(
                                context,
                                PatientSearchResultsActivity::class.java
                            )
                        )
                        (context as PatientSearchActivity).finish()
                    }
                    // Manipulate the results as needed
                } else {
                    if (progressDialog.isShowing) {
                        progressDialog.dismiss()
                    }
                }

            } catch (e: Exception) {
                print(e)

                if (progressDialog.isShowing) {
                    progressDialog.dismiss()
                }

            }
        }
    }

    fun loadTrackedEntities(
        context: Context
    ) {
        CoroutineScope(Dispatchers.Main).launch {
            val formatter = FormatterClass()

            val apiService =
                RetrofitBuilder.getRetrofit(context, Constants.BASE_URL)
                    .create(Interface::class.java)
            try {
                val apiInterface =
                    apiService.loadTrackedEntities()
                if (apiInterface.isSuccessful) {
                    val statusCode = apiInterface.code()
                    val body = apiInterface.body()
                    when (statusCode) {
                        200 -> {
                            if (body != null) {
                                try {
                                    if (body.trackedEntityInstances.isEmpty()) {

                                    } else {
                                        val converters = Converters().toJsonPatientSearch(body)

                                        body.trackedEntityInstances.forEach {
                                            saveUpdatePatient(context, it)
                                        }
                                    }
                                } catch (e: Exception) {
                                    e.printStackTrace()

                                }
                            }
                        }
                    }
                } else {
                    val statusCode = apiInterface.code()
                    val errorBody = apiInterface.errorBody()?.string()
                    when (statusCode) {
                        409 -> {}
                        500 -> {}
                    }
                }
            } catch (e: Exception) {
                print(e)


            }
        }
    }


    private fun saveUpdatePatient(context: Context, data: TrackedEntityInstances) {
        val viewModel = MainViewModel(context.applicationContext as Application)
        val program = extractInitialEnrollment(data.enrollments, "program")
        val orgUnit = extractInitialEnrollment(data.enrollments, "orgUnit")
        val patientUnique = extractInitialEnrollmentSpecific(data.enrollments, PATIENT_UNIQUE)
        var enrollDate = extractInitialEnrollment(data.enrollments, "enrollDate")
        if (enrollDate.isNotEmpty()) {
            enrollDate = FormatterClass().convertDateFormat(enrollDate).toString()
        }
        val entityData = TrackedEntityInstanceServer(
            trackedEntity = data.trackedEntityInstance,
            enrollment = extractInitialEnrollment(data.enrollments, "enrollmentUid"),
            enrollDate = enrollDate,
            orgUnit = orgUnit,
            attributes = extractInitialEnrollmentAttribute(data.enrollments),
            program = program,
            programStage = extractInitialEnrollmentEvent(data.enrollments, "programStage"),
            dataValues = extractInitialEnrollmentEventValues(data.enrollments),
            enrollmentUid = extractInitialEnrollmentEvent(data.enrollments, "enrollmentUid"),
            eventUid = extractInitialEnrollmentEvent(data.enrollments, "eventUid"),
            status = extractInitialEnrollmentEvent(data.enrollments, "status"),

            )
        viewModel.saveTrackedEntityServer(
            context,
            entityData, orgUnit, patientUnique
        )
    }

    private fun extractInitialEnrollmentEvent(
        enrollments: List<Enrollments>,
        type: String
    ): String {
        var data = ""
        enrollments.forEach { q ->
            q.events.forEach {
                data = when (type) {
                    "status" -> q.status
                    "enrollmentUid" -> it.enrollment
                    "eventUid" -> it.event
                    "programStage" -> it.programStage
                    else -> ""
                }
            }
        }

        return data
    }

    private fun extractInitialEnrollmentSpecific(
        enrollments: List<Enrollments>,
        uid: String
    ): String {
        var data = ""
        enrollments.forEach {
            val unique = it.attributes.find { it.attribute == uid }
            if (unique != null) {
                data = unique.value
            }
        }
        return data
    }

    private fun isDateType(uid: String, excludeHiddenFields: List<String>): Boolean {
        return excludeHiddenFields.any { it == uid }
    }

    private fun extractInitialEnrollmentAttribute(enrollments: List<Enrollments>): List<TrackedEntityInstanceAttributes> {
        val dataList = ArrayList<TrackedEntityInstanceAttributes>()
        enrollments.forEach { q ->
            q.attributes.forEach {
                val isDateType = isDateType(it.attribute, FormatterClass().dateFields())
                if (isDateType) {
                    try {
                        val inputDateFormats = listOf(
                            "yyyy-MM-dd",
                            "MM/dd/yyyy",
                            "yyyyMMdd",
                            "dd-MM-yyyy",
                            "yyyy/MM/dd",
                            "MM-dd-yyyy",
                            "dd/MM/yyyy",
                            "yyyyMMddHHmmss",
                            "yyyy-MM-dd HH:mm:ss",
                            "EEE, dd MMM yyyy HH:mm:ss Z",
                            "yyyy-MM-dd'T'HH:mm:ssXXX",
                            "EEE MMM dd HH:mm:ss zzz yyyy",
                        )
                        val outputDateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH)
                        val inputDateFormat = SimpleDateFormat()

                        var date: java.util.Date? = null
                        if (it.value.isNotEmpty()) {
                            for (pattern in inputDateFormats) {
                                inputDateFormat.applyPattern(pattern)
                                try {
                                    date = inputDateFormat.parse(it.value)
                                    break // If parsing succeeds, exit the loop
                                } catch (e: Exception) {
                                    // If parsing fails, continue to the next pattern
                                    continue
                                }
                            }
                            val outputDateString = outputDateFormat.format(date)
                            val data = TrackedEntityInstanceAttributes(
                                attribute = it.attribute,
                                value = outputDateString
                            )
                            dataList.add(data)
                        }
                    } catch (e: Exception) {
                        println("Error parsing or formatting date: ${e.message}")
                    }
                } else {
                    dataList.add(it)
                }
            }
        }
        return dataList
    }

    private fun extractInitialEnrollmentEventValues(enrollments: List<Enrollments>): List<DataValue> {
        val dataList = ArrayList<DataValue>()
        enrollments.forEach { q ->
            q.events.forEach {
                dataList.addAll(it.dataValues)
            }
        }
        return dataList
    }

    private fun extractInitialEnrollment(enrollments: List<Enrollments>, type: String): String {
        var data = ""
        enrollments.forEach {
            data = when (type) {
                "enrollmentUid" -> it.enrollment
                "enrollDate" -> it.enrollmentDate
                "orgUnit" -> it.orgUnit
                "program" -> it.program
                else -> ""
            }
        }

        return data
    }

    private fun noPatientRecordFound(context: Context, layoutInflater: LayoutInflater) {
        val dialog: AlertDialog
        val dialogBuilder = AlertDialog.Builder(context)
        val dialogView = layoutInflater.inflate(R.layout.confirmation_dialog, null)
        dialogBuilder.setView(dialogView)

        val tvTitle: TextView = dialogView.findViewById(R.id.tv_title)
        val tvMessage: TextView = dialogView.findViewById(R.id.tv_message)
        val nextButton: MaterialButton = dialogView.findViewById(R.id.next_button)
        val cancelButton: ImageView = dialogView.findViewById(R.id.cancel_button)
        dialog = dialogBuilder.create()
        tvTitle.text = context.getString(R.string.search_results)
        tvMessage.text =
            context.getString(R.string.no_record_found_of_patient_searched_with_those_parameters)
        nextButton.text = context.getString(R.string.register_new_patient)
        cancelButton.apply {
            setOnClickListener {
                dialog.dismiss()
            }
        }
        nextButton.setOnClickListener {
            dialog.dismiss()
            context.startActivity(
                Intent(
                    context,
                    PatientRegistrationActivity::class.java
                )
            )
            (context as PatientSearchActivity).finish()

        }
        dialog.show()
    }

    fun loadOrganization(context: Context) {
        CoroutineScope(Dispatchers.Main).launch {
            val formatter = FormatterClass()
            val viewModel = MainViewModel(context.applicationContext as Application)
            val results = formatter.getSharedPref("user_data", context)
            var orgUid: String = ""
            if (results != null) {
                val converters = Converters().fromJsonUser(results)
                converters.organisationUnits.forEach {
                    orgUid = it.id
                }

                if (orgUid.isNotEmpty()) {

                    val apiService =
                        RetrofitBuilder.getRetrofit(context, Constants.BASE_URL)
                            .create(Interface::class.java)
                    try {
                        val apiInterface =
                            apiService.loadChildUnits(orgUid)
                        if (apiInterface.isSuccessful) {
                            val statusCode = apiInterface.code()
                            val body = apiInterface.body()
                            when (statusCode) {
                                200 -> {
                                    if (body != null) {
                                        try {
                                            val conf = Converters().toJsonOrgUnit(body)
                                            try {
                                                val json = Gson().fromJson(
                                                    conf,
                                                    JsonObject::class.java
                                                )
                                                viewModel.createUpdateOrg(
                                                    context,
                                                    orgUid,
                                                    json.toString()
                                                )
                                            } catch (e: Exception) {
                                                e.printStackTrace()
                                                Log.e(
                                                    "TAG",
                                                    "child units error:::: ${e.message}"
                                                )
                                            }
                                        } catch (e: Exception) {
                                            e.printStackTrace()

                                        }
                                    }
                                }
                            }
                        } else {
                            val statusCode = apiInterface.code()
                            val errorBody = apiInterface.errorBody()?.string()
                            when (statusCode) {
                                409 -> {}
                                500 -> {}
                            }
                        }
                    } catch (e: Exception) {
                        print(e)


                    }
                }
            }
        }
    }


    fun uploadSingleTrackedEntity(
        context: Context,
        payload: TrackedEntityInstancePostData,
        trackedEntity: String
    ) {
        CoroutineScope(Dispatchers.Main).launch {
            val formatter = FormatterClass()
            val viewModel = MainViewModel(context.applicationContext as Application)

            val apiService =
                RetrofitBuilder.getRetrofit(context, Constants.BASE_URL)
                    .create(Interface::class.java)
            try {
                val apiInterface =
                    apiService.uploadTrackedEntity(payload)
                if (apiInterface.isSuccessful) {
                    val statusCode = apiInterface.code()
                    val body = apiInterface.body()
                    when (statusCode) {
                        200 -> {
                            if (body != null) {
                                val data = body.response.importSummaries.forEach {
                                    val serverReference = it.reference
                                    viewModel.updateEntity(trackedEntity, it.reference)
                                    it.enrollments.importSummaries.forEach {

                                        viewModel.updateEnrollmentEntity(
                                            serverReference,
                                            it.reference
                                        )
                                    }
                                }
                            }
                        }
                    }
                } else {
                    val statusCode = apiInterface.code()
                    val errorBody = apiInterface.errorBody()?.string()
                    when (statusCode) {
                        409 -> {
                            uploadTrackedEntityRetry(context, payload)
                        }

                        500 -> {}
                    }
                }
            } catch (e: Exception) {
                print(e)


            }
        }
    }

    private fun retrieveServerEnrollments(
        context: Context,
        entityReference: String
    ) {
        CoroutineScope(Dispatchers.Main).launch {
            val formatter = FormatterClass()
            val viewModel = MainViewModel(context.applicationContext as Application)
            val program = formatter.getSharedPref("programUid", context)
            val orgCode = formatter.getSharedPref("orgCode", context)
            val apiService =
                RetrofitBuilder.getRetrofit(context, Constants.BASE_URL)
                    .create(Interface::class.java)
            try {
                val apiInterface = apiService.getTrackedEntiry(
                    program = program.toString(),
                    reference = entityReference
                )
                if (apiInterface.isSuccessful) {
                    val statusCode = apiInterface.code()
                    val body = apiInterface.body()
                    when (statusCode) {
                        200 -> {
                            if (body != null) {
                                Log.e(
                                    "TAG",
                                    "Upload Data Here Enrolled **** $entityReference **** $body"
                                )
                                body.trackedEntityInstances.forEach {
                                    it.enrollments.forEach {
                                        viewModel.updateEnrollmentPerOrgAndProgram(
                                            entityReference,
                                            it.enrollment,
                                            program.toString(),
                                            orgCode.toString()
                                        )
                                    }
                                }

                            }
                        }
                    }
                } else {
                    val statusCode = apiInterface.code()
                    val errorBody = apiInterface.errorBody()?.string()
                    when (statusCode) {
                        409 -> {
//                            uploadTrackedEntityRetry(context, payload)
                        }

                        500 -> {}
                    }
                }
            } catch (e: Exception) {
                print(e)


            }
        }
    }

    private fun uploadTrackedEntityRetry(
        context: Context,
        payload: TrackedEntityInstancePostData
    ) {
        CoroutineScope(Dispatchers.Main).launch {
            val formatter = FormatterClass()
            val viewModel = MainViewModel(context.applicationContext as Application)

            val apiService =
                RetrofitBuilder.getRetrofit(context, Constants.BASE_URL)
                    .create(Interface::class.java)
            try {
                val apiInterface =
                    apiService.uploadTrackedEntityRetry(payload)
                if (apiInterface.isSuccessful) {
                    val statusCode = apiInterface.code()
                    val body = apiInterface.body()
                    when (statusCode) {
                        200 -> {
                            if (body != null) {

                            }
                        }
                    }
                } else {
                    val statusCode = apiInterface.code()
                    val errorBody = apiInterface.errorBody()?.string()
                    when (statusCode) {
                        409 -> {
                        }

                        500 -> {}
                    }
                }
            } catch (e: Exception) {
                print(e)


            }
        }
    }

    fun uploadFacilityData(
        context: Context,
        data: EventUploadData,
        id: String,
        serverSide: Boolean,
        eventUid: String,
    ) {
        CoroutineScope(Dispatchers.Main).launch {
            val formatter = FormatterClass()
            val viewModel = MainViewModel(context.applicationContext as Application)

            val apiService =
                RetrofitBuilder.getRetrofit(context, Constants.BASE_URL)
                    .create(Interface::class.java)
            try {
                val apiInterface =
                    if (serverSide) apiService.uploadKnownFacilityData(
                        data,
                        eventUid
                    ) else apiService.uploadFacilityData(
                        data
                    )
                if (apiInterface.isSuccessful) {
                    val statusCode = apiInterface.code()
                    val body = apiInterface.body()
                    when (statusCode) {
                        200 -> {
                            if (body != null) {

                                if (!serverSide) {
                                    body.response.importSummaries.forEach {

                                        viewModel.updateFacilityEvent(id, it.reference)
                                    }
                                } else {
                                    viewModel.updateFacilityEventSynced(id, true)
                                }
                            }
                        }
                    }
                } else {
                    val statusCode = apiInterface.code()
                    val errorBody = apiInterface.errorBody()?.string()
                    when (statusCode) {
                        409 -> {
                            viewModel.updateFacilityEvent(id, data.eventDate)
                        }

                        500 -> {}
                    }
                }
            } catch (e: Exception) {
                print(e)


            }
        }
    }

    fun loadFacilityEvents(
        context: Context,
        program: String,
        orgUnit: String,
        progressBar: ProgressBar
    ) {
        CoroutineScope(Dispatchers.Main).launch {
            val formatter = FormatterClass()
            val viewModel = MainViewModel(context.applicationContext as Application)
            val apiService =
                RetrofitBuilder.getRetrofit(context, Constants.BASE_URL)
                    .create(Interface::class.java)
            progressBar.visibility = View.VISIBLE
            try {

                val apiInterface =
                    apiService.loadFacilityEvents(program, orgUnit)
                if (apiInterface.isSuccessful) {
                    val statusCode = apiInterface.code()
                    val body = apiInterface.body()
                    progressBar.visibility = View.GONE
                    when (statusCode) {
                        200 -> {
                            if (body != null) {
                                body.instances.forEach { q ->
                                    val innerValues = mutableListOf<DataValue>()
                                    q.dataValues.forEach {
                                        val da = DataValue(
                                            dataElement = it.dataElement,
                                            value = it.value
                                        )
                                        innerValues.add(da)
                                    }
                                    val eventData = EventData(
                                        dataValues = Gson().toJson(innerValues),
                                        uid = q.event,
                                        program = q.program,
                                        orgUnit = q.orgUnit,
                                        eventDate = q.createdAt,
                                        status = q.status,
                                        isSynced = true,
                                        isServerSide = true
                                    )

                                    viewModel.addUpdateFacilityEvent(eventData)
                                }

                            }
                        }
                    }
                } else {
                    progressBar.visibility = View.GONE
                    val statusCode = apiInterface.code()
                    val errorBody = apiInterface.errorBody()?.string()
                    when (statusCode) {
                        409 -> {}
                        500 -> {}
                    }
                }
            } catch (e: Exception) {
                print(e)

                progressBar.visibility = View.GONE

            }
        }
    }

    fun loadAllEvents(context: Context) {
        CoroutineScope(Dispatchers.Main).launch {
            val formatter = FormatterClass()
            val viewModel = MainViewModel(context.applicationContext as Application)
            val apiService =
                RetrofitBuilder.getRetrofit(context, Constants.BASE_URL)
                    .create(Interface::class.java)
            try {
                val apiInterface =
                    apiService.loadAllFacilityEvents()
                if (apiInterface.isSuccessful) {
                    val statusCode = apiInterface.code()
                    val body = apiInterface.body()

                    when (statusCode) {
                        200 -> {
                            if (body != null) {
                                body.instances.forEach { q ->
                                    val innerValues = mutableListOf<DataValue>()
                                    q.dataValues.forEach {
                                        val da = DataValue(
                                            dataElement = it.dataElement,
                                            value = it.value
                                        )
                                        innerValues.add(da)
                                    }
                                    val eventData = EventData(
                                        dataValues = Gson().toJson(innerValues),
                                        uid = q.event,
                                        program = q.program,
                                        orgUnit = q.orgUnit,
                                        eventDate = q.createdAt,
                                        status = q.status,
                                        isServerSide = true,
                                        isSynced = true,
                                    )
                                    viewModel.addUpdateFacilityEvent(eventData)
                                }

                            }
                        }
                    }
                } else {

                    val statusCode = apiInterface.code()
                    val errorBody = apiInterface.errorBody()?.string()
                    when (statusCode) {
                        409 -> {}
                        500 -> {}
                    }
                }
            } catch (e: Exception) {
                print(e)


            }
        }
    }

    fun loadAllFacilities(context: Context) {
        CoroutineScope(Dispatchers.Main).launch {
            val formatter = FormatterClass()
            val viewModel = MainViewModel(context.applicationContext as Application)
            val apiService =
                RetrofitBuilder.getRetrofit(context, Constants.BASE_URL)
                    .create(Interface::class.java)
            try {
                val apiInterface =
                    apiService.loadAllFacilityEvents()
                if (apiInterface.isSuccessful) {
                    val statusCode = apiInterface.code()
                    val body = apiInterface.body()

                    when (statusCode) {
                        200 -> {
                            if (body != null) {
                                body.instances.forEach { q ->
                                    val innerValues = mutableListOf<DataValue>()
                                    q.dataValues.forEach {
                                        val da = DataValue(
                                            dataElement = it.dataElement,
                                            value = it.value
                                        )
                                        innerValues.add(da)
                                    }
                                    val eventData = EventData(
                                        dataValues = Gson().toJson(innerValues),
                                        uid = q.event,
                                        program = q.program,
                                        orgUnit = q.orgUnit,
                                        eventDate = q.createdAt,
                                        status = q.status,
                                        isServerSide = true,
                                        isSynced = true,
                                    )
                                    viewModel.addUpdateFacilityEvent(eventData)
                                }

                            }
                        }
                    }
                } else {

                    val statusCode = apiInterface.code()
                    val errorBody = apiInterface.errorBody()?.string()
                    when (statusCode) {
                        409 -> {}
                        500 -> {}
                    }
                }
            } catch (e: Exception) {
                print(e)


            }
        }
    }

    fun loadAllSites(context: Context) {
        CoroutineScope(Dispatchers.Main).launch {
            val formatter = FormatterClass()
            val viewModel = MainViewModel(context.applicationContext as Application)
            val apiService =
                RetrofitBuilder.getRetrofit(context, Constants.BASE_URL)
                    .create(Interface::class.java)
            try {
                val apiInterface =
                    apiService.loadAllSites()
                if (apiInterface.isSuccessful) {
                    val statusCode = apiInterface.code()
                    val body = apiInterface.body()

                    when (statusCode) {
                        200 -> {
                            if (body != null) {

                                val data =
                                    DataStoreData(
                                        uid = "site",
                                        dataValues = Gson().toJson(body)
                                    )
                                viewModel.addDataStore(data)

                            }
                        }
                    }
                } else {

                    val statusCode = apiInterface.code()
                    val errorBody = apiInterface.errorBody()?.string()
                    when (statusCode) {
                        409 -> {}
                        500 -> {}
                    }
                }
            } catch (e: Exception) {
                print(e)


            }
        }
    }

    fun loadAllCategories(context: Context) {
        CoroutineScope(Dispatchers.Main).launch {
            val formatter = FormatterClass()
            val viewModel = MainViewModel(context.applicationContext as Application)
            val apiService =
                RetrofitBuilder.getRetrofit(context, Constants.BASE_URL)
                    .create(Interface::class.java)
            try {
                val apiInterface =
                    apiService.loadAllCategories()
                if (apiInterface.isSuccessful) {
                    val statusCode = apiInterface.code()
                    val body = apiInterface.body()
                    when (statusCode) {
                        200 -> {
                            if (body != null) {
                                val data = DataStoreData(
                                    uid = "category",
                                    dataValues = Gson().toJson(body)
                                )
                                viewModel.addDataStore(data)
                            }
                        }
                    }
                } else {
                    val statusCode = apiInterface.code()
                    val errorBody = apiInterface.errorBody()?.string()
                    when (statusCode) {
                        409 -> {}
                        500 -> {}
                    }
                }
            } catch (e: Exception) {
                print(e)


            }
        }
    }

    fun uploadEnrollmentData(
        context: Context,
        payload: EnrollmentEventUploadData,
        uid: String,
        initialUpload: Boolean,
        eventUid: String,
        trackedEntity: TrackedEntityInstanceData
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            val formatter = FormatterClass()
            val viewModel = MainViewModel(context.applicationContext as Application)
            val apiService =
                RetrofitBuilder.getRetrofit(context, Constants.BASE_URL)
                    .create(Interface::class.java)
            try {
                val apiInterface =
                    if (!initialUpload) apiService.uploadEnrollmentData(payload) else apiService.uploadEnrollmentDataUpdate(
                        payload,
                        eventUid
                    )
                if (apiInterface.isSuccessful) {
                    val statusCode = apiInterface.code()
                    val body = apiInterface.body()
                    when (statusCode) {
                        200 -> {
                            if (body != null) {
                                if (!initialUpload) {
                                    body.response.importSummaries.forEach {
                                        viewModel.updateNotificationEvent(
                                            uid,
                                            it.reference,
                                            true
                                        )
                                    }
                                }

                                /**
                                 * Check for reporting events
                                 */

                                checkAndUploadReportsEvents(context, uid, trackedEntity)
                            }
                        }
                    }
                } else {
                    val statusCode = apiInterface.code()
                    val errorBody = apiInterface.errorBody()?.string()

                    when (statusCode) {
                        409 -> {}
                        500 -> {}
                    }
                }
            } catch (e: Exception) {
                print(e)


            }
        }
    }

    private fun checkAndUploadReportsEvents(
        context: Context,
        uid: String,
        trackedEntity: TrackedEntityInstanceData
    ) {

        val viewModel = MainViewModel(context.applicationContext as Application)

        val reportsData = viewModel.loadReportEvent(uid)
        if (reportsData.isNotEmpty()) {

            reportsData.forEach {

                val attributes =
                    Converters().fromJsonDataAttribute(it.dataValues)
                val payload = EnrollmentEventUploadData(
                    eventDate = it.eventDate,
                    orgUnit = it.orgUnit,
                    program = it.program,
                    programStage = "HfVOnjywr82",
                    enrollment = trackedEntity.enrollment,
                    trackedEntityInstance = trackedEntity.trackedEntity,
                    status = it.status,
                    dataValues = attributes
                )
                CoroutineScope(Dispatchers.IO).launch {
                    val formatter = FormatterClass()
                    val apiService =
                        RetrofitBuilder.getRetrofit(context, Constants.BASE_URL)
                            .create(Interface::class.java)
                    try {
                        val apiInterface =
                            if (!it.initialUpload) apiService.uploadEnrollmentData(payload) else apiService.uploadEnrollmentDataUpdate(
                                payload,
                                it.eventUid
                            )
                        if (apiInterface.isSuccessful) {
                            val statusCode = apiInterface.code()
                            val body = apiInterface.body()
                            when (statusCode) {
                                200 -> {
                                    if (body != null) {
                                        if (!it.initialUpload) {
                                            body.response.importSummaries.forEach {k->
                                                viewModel.updateNotificationReportEvent(
                                                    it.id.toString(),
                                                    k.reference,
                                                    true
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        } else {
                            val statusCode = apiInterface.code()
                            val errorBody = apiInterface.errorBody()?.string()

                            when (statusCode) {
                                409 -> {}
                                500 -> {}
                            }
                        }
                    } catch (e: Exception) {
                        print(e)


                    }
                }

            }

        }

    }


    fun loadTopography(context: Context) {
        CoroutineScope(Dispatchers.IO).launch {
            val formatter = FormatterClass()
            val viewModel = MainViewModel(context.applicationContext as Application)
            val apiService =
                RetrofitBuilder.getRetrofit(context, Constants.BASE_URL)
                    .create(Interface::class.java)
            try {
                val apiInterface = apiService.loadTopographies()
                if (apiInterface.isSuccessful) {
                    val statusCode = apiInterface.code()
                    val body = apiInterface.body()
                    when (statusCode) {
                        200 -> {
                            if (body != null) {
                                val converters = Converters().toTopographyJson(body)
                                formatter.saveSharedPref("topography", converters, context)
                            }
                        }
                    }
                } else {
                    val statusCode = apiInterface.code()
                    val errorBody = apiInterface.errorBody()?.string()
                    when (statusCode) {
                        409 -> {}
                        500 -> {}
                    }
                }
            } catch (e: Exception) {
                print(e)

            }
        }
    }

    fun loadAllOrganizations(context: Context) {
        CoroutineScope(Dispatchers.IO).launch {
            val formatter = FormatterClass()
            val viewModel = MainViewModel(context.applicationContext as Application)
            val apiService =
                RetrofitBuilder.getRetrofit(context, Constants.BASE_URL)
                    .create(Interface::class.java)
            try {
                val apiInterface = apiService.loadOrgUnits()
                if (apiInterface.isSuccessful) {
                    val statusCode = apiInterface.code()
                    val body = apiInterface.body()
                    when (statusCode) {
                        200 -> {
                            if (body != null) {
                                val converters = Converters().toOrgUnitsJson(body)
                                manipulateResponse(context, body)
//                                formatter.saveSharedPref("topography", converters, context)
                            }
                        }
                    }
                } else {
                    val statusCode = apiInterface.code()
                    val errorBody = apiInterface.errorBody()?.string()
                    when (statusCode) {
                        409 -> {}
                        500 -> {}
                    }
                }
            } catch (e: Exception) {
                print(e)

            }
        }
    }

    private fun manipulateResponse(context: Context, body: OrgUnitResponse) {
        val viewModel = MainViewModel(context.applicationContext as Application)
        viewModel.deleteOrgUnits()
        body.organisationUnits.forEach {
            val parent = it.parent?.id ?: ""
            val orgUnit = orgUnit(
                uuid = it.id,
                level = it.level,
                name = it.name,
                parentOrgUnit = parent
            )
            if (it.children.isNotEmpty()) {
                saveChildData(context, it.children, it.id)
            }


            viewModel.updateOrCreate(orgUnit)
        }

    }

    private fun saveChildData(context: Context, data: List<ChildOrgUnit>, parent: String) {

        val viewModel = MainViewModel(context.applicationContext as Application)
        data.forEach {
            val orgUnit = orgUnit(
                uuid = it.id,
                level = it.level,
                name = it.name,
                parentOrgUnit = parent
            )
            viewModel.updateOrCreate(orgUnit)
        }
    }


}


