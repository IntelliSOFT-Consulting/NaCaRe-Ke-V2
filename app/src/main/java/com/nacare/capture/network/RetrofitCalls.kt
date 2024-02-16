package com.nacare.capture.network

import android.app.Application
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.google.android.material.button.MaterialButton
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.nacare.capture.R
import com.nacare.capture.auth.SyncActivity
import com.nacare.capture.data.Constants
import com.nacare.capture.data.FormatterClass
import com.nacare.capture.model.DataValue
import com.nacare.capture.model.EnrollmentEventUploadData
import com.nacare.capture.model.EventUploadData
import com.nacare.capture.model.MultipleTrackedEntityInstances
import com.nacare.capture.model.TrackedEntityInstancePostData
import com.nacare.capture.room.Converters
import com.nacare.capture.room.DataStoreData
import com.nacare.capture.room.EventData
import com.nacare.capture.room.MainViewModel
import com.nacare.capture.room.ProgramData
import com.nacare.capture.ui.patients.PatientRegistrationActivity
import com.nacare.capture.ui.patients.PatientSearchActivity
import com.nacare.capture.ui.patients.PatientSearchResultsActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

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
                        Log.e("TAG From", "Retrieved Data $body")
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
                        formatter.saveSharedPref("username", "admin", context)
                        formatter.saveSharedPref("password", "district", context)
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
                }

            } catch (e: Exception) {
                e.printStackTrace()
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
        layoutInflater: LayoutInflater
    ) {
        CoroutineScope(Dispatchers.Main).launch {
            val formatter = FormatterClass()

            val apiService =
                RetrofitBuilder.getRetrofit(context, Constants.BASE_URL)
                    .create(Interface::class.java)
            try {
                val apiInterface =
                    apiService.searchPatient(filter = searchParametersString, program = programUid)
                if (apiInterface.isSuccessful) {
                    val statusCode = apiInterface.code()
                    val body = apiInterface.body()
                    when (statusCode) {
                        200 -> {

                            if (body != null) {
                                try {
                                    if (body.trackedEntityInstances.isEmpty()) {
                                        noPatientRecordFound(context, layoutInflater)
                                    } else {
                                        val converters = Converters().toJsonPatientSearch(body)
                                        Log.e("TAG", "Search Results ***** $converters")
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
                                    Log.e("TAG", "json err:::: ${e.message}")
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
                Log.e("TAG", "Success Error:::: ${e.message}")

            }
        }
    }

    private fun noPatientRecordFound(context: Context, layoutInflater: LayoutInflater) {
        val dialog: AlertDialog
        val dialogBuilder = AlertDialog.Builder(context)
        val dialogView = layoutInflater.inflate(R.layout.confirmation_dialog, null)
        dialogBuilder.setView(dialogView)

        val tvTitle: TextView = dialogView.findViewById(R.id.tv_title)
        val tvMessage: TextView = dialogView.findViewById(R.id.tv_message)
        val nextButton: MaterialButton = dialogView.findViewById(R.id.next_button)
        dialog = dialogBuilder.create()
        tvTitle.text = context.getString(R.string.search_results)
        tvMessage.text =
            context.getString(R.string.no_record_found_of_patient_searched_with_those_parameters)
        nextButton.text = context.getString(R.string.register_new_patient)
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
                Log.e("TAG", "Results $orgUid")
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
                                                Log.e("TAG", "child units error:::: ${e.message}")
                                            }
                                        } catch (e: Exception) {
                                            e.printStackTrace()
                                            Log.e("TAG", "json:::: ${e.message}")
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
                        Log.e("TAG", "Success Error:::: ${e.message}")

                    }
                }
            }
        }
    }

    fun uploadTrackedEntity(context: Context, payload: MultipleTrackedEntityInstances) {
        CoroutineScope(Dispatchers.Main).launch {
            val formatter = FormatterClass()
            val viewModel = MainViewModel(context.applicationContext as Application)

            val apiService =
                RetrofitBuilder.getRetrofit(context, Constants.BASE_URL)
                    .create(Interface::class.java)
            try {
                val apiInterface =
                    apiService.uploadMultipleTrackedEntity(payload)
                if (apiInterface.isSuccessful) {
                    val statusCode = apiInterface.code()
                    val body = apiInterface.body()
                    when (statusCode) {
                        200 -> {
                            if (body != null) {

//                                try {
//                                    val conf = Converters().toJsonOrgUnit(body)
//                                    try {
//                                        val json = Gson().fromJson(
//                                            conf,
//                                            JsonObject::class.java
//                                        )
//                                        viewModel.createUpdateOrg(
//                                            context,
//                                            orgUid,
//                                            json.toString()
//                                        )
//                                    } catch (e: Exception) {
//                                        e.printStackTrace()
//                                        Log.e("TAG", "child units error:::: ${e.message}")
//                                    }
//                                } catch (e: Exception) {
//                                    e.printStackTrace()
//                                    Log.e("TAG", "json:::: ${e.message}")
//                                }
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
                Log.e("TAG", "Success Error:::: ${e.message}")


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
                                Log.e("TAG", "Upload Data Here **** Server Response $body")
                                val data = body.response.importSummaries.forEach {
                                    val serverReference = it.reference
                                    viewModel.updateEntity(trackedEntity, it.reference)
                                    it.enrollments.importSummaries.forEach {
                                        Log.e(
                                            "TAG",
                                            "Upload Data Here **** Enrollment Id ${it.reference} Tracked $serverReference"
                                        )
                                        viewModel.updateEnrollmentEntity(
                                            serverReference,
                                            it.reference
                                        )
                                    }
//                                    retrieveServerEnrollments(context, it.reference)
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
                Log.e("TAG", "Success Error:::: ${e.message}")


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
                Log.e("TAG", "Success Error:::: ${e.message}")


            }
        }
    }

    private fun uploadTrackedEntityRetry(context: Context, payload: TrackedEntityInstancePostData) {
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
                                Log.e("TAG", "Data Response Created ***** $body")
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
                Log.e("TAG", "Success Error:::: ${e.message}")


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
                                Log.e("TAG", "Event Upload Response:::: Event $id ****  $body")
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
                Log.e("TAG", "Success Error:::: ${e.message}")


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
                Log.e("TAG", "Success Error:::: ${e.message}")
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
                Log.e("TAG", "Success Error:::: ${e.message}")


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
                                Log.e("TAG", "Data Response **** $body")
                                val data =
                                    DataStoreData(uid = "site", dataValues = Gson().toJson(body))
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
                Log.e("TAG", "Success Error:::: ${e.message}")


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
                Log.e("TAG", "Success Error:::: ${e.message}")


            }
        }
    }

    fun uploadEnrollmentData(
        context: Context,
        payload: EnrollmentEventUploadData,
        uid: String,
        initialUpload: Boolean,
        eventUid: String
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
                                        viewModel.updateNotificationEvent(uid, it.reference, true)
                                    }
                                }
                            }
                        }
                    }
                } else {
                    val statusCode = apiInterface.code()
                    val errorBody = apiInterface.errorBody()?.string()
                    Log.e("TAG", "Server Data Response **** Error $errorBody")
                    when (statusCode) {
                        409 -> {}
                        500 -> {}
                    }
                }
            } catch (e: Exception) {
                print(e)
                Log.e("TAG", "Server Data Response ****:::: ${e.message}")


            }
        }
    }


}


