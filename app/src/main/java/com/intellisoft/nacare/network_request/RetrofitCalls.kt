package com.intellisoft.nacare.network_request

import android.app.Application
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.google.android.material.button.MaterialButton
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.intellisoft.nacare.helper_class.DataElementItem
import com.intellisoft.nacare.helper_class.FormatterClass
import com.intellisoft.nacare.helper_class.PatientEnrollmentResponse
import com.intellisoft.nacare.helper_class.PatientPayload
import com.intellisoft.nacare.helper_class.ProgramCategory
import com.intellisoft.nacare.helper_class.ProgramSections
import com.intellisoft.nacare.helper_class.ProgramStageSections
import com.intellisoft.nacare.main.registry.PatientListActivity
import com.intellisoft.nacare.main.registry.PatientSearchActivity
import com.intellisoft.nacare.main.registry.ResponderActivity
import com.intellisoft.nacare.models.Constants
import com.intellisoft.nacare.models.Constants.FACILITY_TOOL
import com.intellisoft.nacare.models.Constants.PATIENT_REGISTRATION
import com.intellisoft.nacare.models.Constants.PROGRAM_TRACKED_ENTITY_TYPE
import com.intellisoft.nacare.models.Constants.TRACKED_ENTITY_TYPE
import com.intellisoft.nacare.room.Converters
import com.intellisoft.nacare.room.EventData
import com.intellisoft.nacare.room.FacilityEventData
import com.intellisoft.nacare.room.MainViewModel
import com.intellisoft.nacare.room.OrganizationData
import com.intellisoft.nacare.room.ProgramData
import com.intellisoft.nacare.viewmodels.NetworkViewModel
import com.nacare.ke.capture.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*

class RetrofitCalls {

    private fun formatDateInput(inputDate: String?): String {
        val inputFormat = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"
        val outputFormat = "yyyy-MM-dd"
        var date = ""
        val dateFormat = SimpleDateFormat(inputFormat, Locale.ENGLISH)
        dateFormat.timeZone =
            TimeZone.getTimeZone("UTC") // Assuming the input date is in UTC timezone

        try {
            val parsedDate = dateFormat.parse(inputDate)
            val outputDateFormat = SimpleDateFormat(outputFormat, Locale.US)
            val formattedDate = outputDateFormat.format(parsedDate)

            println(formattedDate) // Output: "2023-5-25"
            date = formattedDate
        } catch (e: Exception) {
            date = "$inputDate"
            println("Error occurred while parsing or formatting the date")
        }
        return date
    }

    fun loadOrganization(context: Context) {

        CoroutineScope(Dispatchers.IO).launch {
            val viewModel = MainViewModel(context.applicationContext as Application)
            val formatterClass = FormatterClass()
            val baseUrl = formatterClass.getSharedPref("serverUrl", context)

            val username = formatterClass.getSharedPref("username", context)
            if (baseUrl != null && username != null) {
                val apiService =
                    RetrofitBuilder.getRetrofit(context, baseUrl).create(Interface::class.java)
                try {
                    val apiInterface = apiService.loadOrganization()
                    if (apiInterface.isSuccessful) {
                        val statusCode = apiInterface.code()
                        val body = apiInterface.body()
                        if (statusCode == 200 || statusCode == 201) {
                            if (body != null) {
                                val converters = Converters().toJsonOrganization(body)
                                try {
                                    val json = Gson().fromJson(converters, JsonObject::class.java)
                                    val data = json.getAsJsonArray("organisationUnits")
                                    data.forEach {
                                        if (it is JsonObject) {
                                            val code = it.get("id").asString
                                            val name = it.get("name").asString
                                            val org = OrganizationData(
                                                name = name,
                                                code = code,
                                                children = ""
                                            )
                                            viewModel.addOrganization(context, org)
                                            handleChildOrganizationUnits(context, code)
                                        }

                                    }
//                  myViewModel.addPatient(context, id, indicatorsObject.toString())
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                    Log.e("TAG", "json:::: ${e.message}")
                                }
                            }
                        }
                    }
                } catch (e: Exception) {
                    print(e)
                }
            }
        }
    }

    private fun handleChildOrganizationUnits(context: Context, code: String) {

        CoroutineScope(Dispatchers.IO).launch {
            val viewModel = MainViewModel(context.applicationContext as Application)
            val formatterClass = FormatterClass()
            val baseUrl = formatterClass.getSharedPref("serverUrl", context)

            val username = formatterClass.getSharedPref("username", context)
            if (baseUrl != null && username != null) {
                val apiService =
                    RetrofitBuilder.getRetrofit(context, baseUrl).create(Interface::class.java)
                try {
                    val apiInterface = apiService.loadChildUnits(code)
                    if (apiInterface.isSuccessful) {
                        val statusCode = apiInterface.code()
                        val body = apiInterface.body()
                        if (statusCode == 200 || statusCode == 201) {
                            if (body != null) {
                                val converters = Converters().toJsonOrgUnit(body)
                                try {
                                    val json = Gson().fromJson(converters, JsonObject::class.java)
                                    viewModel.updateChildOrgUnits(context, code, json.toString())
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                    Log.e("TAG", "child units error:::: ${e.message}")
                                }
                            }
                        }
                    }
                } catch (e: Exception) {
                    print(e)
                }
            }
        }
    }

    fun loadPrograms(context: Context) {
        CoroutineScope(Dispatchers.IO).launch {
            val viewModel = MainViewModel(context.applicationContext as Application)
            val formatterClass = FormatterClass()
            val baseUrl = formatterClass.getSharedPref("serverUrl", context)
            val username = formatterClass.getSharedPref("username", context)
            if (baseUrl != null && username != null) {
                val apiService =
                    RetrofitBuilder.getRetrofit(context, baseUrl).create(Interface::class.java)
                try {
                    val apiInterface = apiService.loadPrograms()
                    if (apiInterface.isSuccessful) {
                        val statusCode = apiInterface.code()
                        val body = apiInterface.body()
                        if (statusCode == 200) {
                            if (body != null) {
                                val converters = Converters().toJsonProgram(body)
                                try {
                                    val json = Gson().fromJson(converters, JsonObject::class.java)
                                    val data = json.getAsJsonArray("programs")
                                    data.forEach {
                                        if (it is JsonObject) {
                                            val code = it.get("id").asString
                                            val name = it.get("name").asString
                                            val trackedEntityType =
                                                it.getAsJsonObject("trackedEntityType")
                                            val attribute =
                                                trackedEntityType.getAsJsonPrimitive("id").asString
                                            formatterClass.saveSharedPref(
                                                TRACKED_ENTITY_TYPE,
                                                attribute, context
                                            )
                                            formatterClass.saveSharedPref(
                                                PROGRAM_TRACKED_ENTITY_TYPE,
                                                code, context
                                            )
                                            val programStages = it.get("programStages").asJsonArray
                                            val programSections =
                                                it.get("programSections").asJsonArray
                                            val org = ProgramData(
                                                type = "notification",
                                                name = name,
                                                code = code,
                                                programStages = programStages.toString(),
                                                programTrackedEntityAttributes = programSections.toString()
                                            )
                                            viewModel.addProgram(context, org)
                                        }
                                    }
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                    Log.e("TAG", "json err:::: ${e.message}")
                                }
                            }
                        }
                    }
                } catch (e: Exception) {
                    print(e)
                    Log.e("TAG", "Success Error:::: ${e.message}")

                }
            }
        }
    }

    fun loadFacilityTool(context: Context) {
        CoroutineScope(Dispatchers.IO).launch {
            val viewModel = MainViewModel(context.applicationContext as Application)
            val formatterClass = FormatterClass()
            val baseUrl = formatterClass.getSharedPref("serverUrl", context)
            val username = formatterClass.getSharedPref("username", context)
            if (baseUrl != null && username != null) {
                val apiService =
                    RetrofitBuilder.getRetrofit(context, baseUrl).create(Interface::class.java)
                try {
                    val apiInterface = apiService.loadFacility()
                    if (apiInterface.isSuccessful) {
                        val statusCode = apiInterface.code()
                        val body = apiInterface.body()
                        if (statusCode == 200) {
                            if (body != null) {

                                val converters = Converters().toJsonFacilityProgram(body)
                                try {
                                    val json = Gson().fromJson(converters, JsonObject::class.java)
                                    val data = json.getAsJsonArray("programs")
                                    data.forEach {
                                        if (it is JsonObject) {
                                            val code = it.get("id").asString
                                            val name = it.get("name").asString
                                            formatterClass.saveSharedPref(
                                                FACILITY_TOOL,
                                                code, context
                                            )
                                            val programStages = it.get("programStages").asJsonArray
                                            val org = ProgramData(
                                                type = "facility",
                                                name = name,
                                                code = code,
                                                programStages = programStages.toString(),
                                                programTrackedEntityAttributes = programStages.toString()
                                            )
                                            viewModel.addProgram(context, org)
                                            loadFacilityEvents(context, code)
                                        }
                                    }
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                    Log.e("TAG", "json err:::: ${e.message}")
                                }
                            }
                        }
                    }
                } catch (e: Exception) {
                    print(e)
                    Log.e("TAG", "Success Error:::: ${e.message}")

                }
            }
        }
    }


    private fun loadFacilityEvents(context: Context, code: String) {
        CoroutineScope(Dispatchers.IO).launch {
            val viewModel = MainViewModel(context.applicationContext as Application)
            val formatterClass = FormatterClass()
            val baseUrl = formatterClass.getSharedPref("serverUrl", context)
            val username = formatterClass.getSharedPref("username", context)
            if (baseUrl != null && username != null) {
                val apiService =
                    RetrofitBuilder.getRetrofit(context, baseUrl).create(Interface::class.java)
                try {
                    val apiInterface = apiService.loadEvents(code)
                    if (apiInterface.isSuccessful) {
                        val statusCode = apiInterface.code()
                        val body = apiInterface.body()
                        if (statusCode == 200) {
                            if (body != null) {

                                val converters = Converters().toJsonFacilityEventProgram(body)
                                try {
                                    val json = Gson().fromJson(converters, JsonObject::class.java)
                                    val data = json.getAsJsonArray("instances")
                                    data.forEach {
                                        if (it is JsonObject) {
                                            val event = it.get("event").asString
                                            val status = it.get("status").asString
                                            val programStage = it.get("programStage").asString
                                            val orgUnit = it.get("orgUnit").asString
                                            val dataValues = it.get("dataValues").asJsonArray
                                            val org = FacilityEventData(
                                                userId = "",
                                                event = event,
                                                status = status,
                                                programStage = programStage,
                                                orgUnit = orgUnit,
                                                dataValues = dataValues.toString(),
                                                responses = "",
                                            )
                                            viewModel.addFacilityEventData(context, org)
                                            updateEventResponses(context, event)

                                        }
                                    }
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                    Log.e("TAG", "json err:::: ${e.message}")
                                }
                            }
                        }
                    }
                } catch (e: Exception) {
                    print(e)
                    Log.e("TAG", "Facility Events Error:::: ${e.message}")

                }
            }
        }
    }

    private fun updateEventResponses(context: Context, code: String) {
        CoroutineScope(Dispatchers.IO).launch {
            val viewModel = MainViewModel(context.applicationContext as Application)
            val formatterClass = FormatterClass()
            val baseUrl = formatterClass.getSharedPref("serverUrl", context)
            val username = formatterClass.getSharedPref("username", context)
            if (baseUrl != null && username != null) {
                val apiService =
                    RetrofitBuilder.getRetrofit(context, baseUrl).create(Interface::class.java)
                try {
                    val apiInterface = apiService.loadEventData(code)
                    if (apiInterface.isSuccessful) {
                        val statusCode = apiInterface.code()
                        val body = apiInterface.body()
                        if (statusCode == 200) {
                            if (body != null) {

                                val converters = Converters().toJsonFacilityEventData(body)
                                val json = Gson().fromJson(converters, JsonObject::class.java)
                                viewModel.updateEventDataValues(context, code, json.toString())

                            }
                        }
                    }
                } catch (e: Exception) {
                    print(e)
                    Log.e("TAG", "Facility Events Error:::: ${e.message}")

                }
            }
        }
    }

    fun performPatientSearch(
        context: Context,
        eventData: EventData,
        progressBar: ProgressBar,
        searchParametersString: String,
        networkModel: NetworkViewModel,
        layoutInflater: LayoutInflater,
        eventData1: EventData,
        program: String
    ) {
        CoroutineScope(Dispatchers.Main).launch {
            val formatterClass = FormatterClass()
            val baseUrl = formatterClass.getSharedPref("serverUrl", context)
            val username = formatterClass.getSharedPref("username", context)

            if (baseUrl != null && username != null) {
                val apiService =
                    RetrofitBuilder.getRetrofit(context, baseUrl).create(Interface::class.java)
                try {

                    val apiInterface =
                        apiService.searchPatient(filter = searchParametersString, program = program)
                    if (apiInterface.isSuccessful) {
                        val statusCode = apiInterface.code()
                        val body = apiInterface.body()
                        if (statusCode == 200) {
                            networkModel.setBooleanValue(false)
                            if (body != null) {
                                val converters = Converters().toJsonPatientSearch(body)
                                try {
                                    if (body.trackedEntityInstances.isEmpty()) {
                                        noPatientRecordFound(context, layoutInflater, eventData)
                                    } else {
                                        val bundle = Bundle()
                                        val cc = Converters().toJsonEvent(eventData)
                                        bundle.putString("event", cc)
                                        bundle.putString("patients", converters)
                                        val intent = Intent(
                                            context,
                                            PatientListActivity::class.java
                                        )
                                        intent.putExtra("data", bundle)
                                        context.startActivity(intent)
                                        (context as PatientSearchActivity).finish()
                                    }
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                    Log.e("TAG", "json err:::: ${e.message}")
                                }
                            }
                        }
                    }
                } catch (e: Exception) {
                    print(e)
                    Log.e("TAG", "Success Error:::: ${e.message}")
                    networkModel.setBooleanValue(false)

                }
            }
        }
    }

    private fun noPatientRecordFound(
        context: Context,
        layoutInflater: LayoutInflater,
        eventData: EventData
    ) {
        val dialog: AlertDialog
        val dialogBuilder = AlertDialog.Builder(context)
        val dialogView = layoutInflater.inflate(R.layout.confirmation_dialog, null)
        dialogBuilder.setView(dialogView)

        val tvTitle: TextView = dialogView.findViewById(R.id.tv_title)
        val tvMessage: TextView = dialogView.findViewById(R.id.tv_message)
        val nextButton: MaterialButton = dialogView.findViewById(R.id.next_button)
        dialog = dialogBuilder.create()
        tvMessage.text = "No Record found of Patient Searched with those parameters"
        nextButton.text = "Register New Patient"
        nextButton.setOnClickListener {
            FormatterClass().saveSharedPref(Constants.PATIENT_ID, "new", context)
            handlePatientRegistration(context, dialog, eventData)

        }
        dialog.show()
    }

    private fun loadInitialData(context: Context, viewModel: MainViewModel): ProgramCategory? {

        val formatterClass = FormatterClass()
        val data = viewModel.loadProgram(context, "notification")
        if (data != null) {
            val org = formatterClass.getSharedPref("name", context)
            val date = formatterClass.getSharedPref("date", context)
            return loadProgramData(data)
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
            done = "0",
            total = "0",
            elements = treeNodes,
            position = "0",
            altElements = treeNodes
        )
    }


    private fun handlePatientRegistration(
        context: Context,
        dialog: AlertDialog,
        eventData: EventData
    ) {
        val viewModel = MainViewModel(context.applicationContext as Application)
        val data = loadInitialData(context, viewModel)

        if (data != null) {
            val converters = Converters().toJsonElements(data.elements)
            val json = Gson().fromJson(converters, JsonArray::class.java)
            val bundle = Bundle()
            bundle.putString("code", data.id)
            bundle.putString("name", data.name)
            bundle.putString("programStageDataElements", json.toString())
            bundle.putString("attribute", null)

            val cc = Converters().toJsonEvent(eventData)
            bundle.putString("event", cc)
            val formatter = FormatterClass()
            formatter.saveSharedPref(PATIENT_REGISTRATION, "true", context)

            val intent = Intent(context, ResponderActivity::class.java)
            intent.putExtra("data", bundle)
            context.startActivity(intent)
            (context as PatientSearchActivity).finish()
            dialog.dismiss()

        } else {
            Toast.makeText(
                context,
                "No Indicator Elements, please try again later",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    fun registerPatient(context: Context, payload: PatientPayload, event: EventData) {
        CoroutineScope(Dispatchers.IO).launch {
            val formatterClass = FormatterClass()
            val baseUrl = formatterClass.getSharedPref("serverUrl", context)
            val username = formatterClass.getSharedPref("username", context)
            Log.e("TAG", "status code:::: $payload")
            if (baseUrl != null && username != null) {
                val apiService =
                    RetrofitBuilder.getRetrofit(context, baseUrl).create(Interface::class.java)
                /*  try {*/
                val apiInterface = apiService.registerPatient(payload)
                val statusCode = apiInterface.code()
                Log.e("TAG", "Done Posting:::: $statusCode")
//                    if (apiInterface.isSuccessful) {
//
//                        val body = apiInterface.body()
//                        if (statusCode == 200) {
//                            if (body != null) {
//                                Log.e("TAG", "json data:::: $body")
//                            }
//                        } else {
//                            Log.e("TAG", "json data:::: error $statusCode")
//                        }
//                    }
                /*   } catch (e: Exception) {
                       print(e)
                       Log.e("TAG", "Exception Error:::: ${e.message}")
                   }*/
            }
        }

    }


}
