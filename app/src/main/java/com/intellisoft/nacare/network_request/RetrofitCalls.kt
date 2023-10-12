package com.intellisoft.nacare.network_request

import android.app.Application
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.intellisoft.nacare.helper_class.FormatterClass
import com.intellisoft.nacare.main.registry.PatientListActivity
import com.intellisoft.nacare.main.registry.PatientSearchActivity
import com.intellisoft.nacare.models.Constants.TRACKED_ENTITY_TYPE
import com.intellisoft.nacare.room.Converters
import com.intellisoft.nacare.room.EventData
import com.intellisoft.nacare.room.MainViewModel
import com.intellisoft.nacare.room.OrganizationData
import com.intellisoft.nacare.room.ProgramData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class RetrofitCalls {

//  fun submitData(context: Context, dbSaveDataEntry: DbSaveDataEntry) {
//    CoroutineScope(Dispatchers.Main).launch {
//      val job = Job()
//      CoroutineScope(Dispatchers.IO + job)
//          .launch { submitDataBackground(context, dbSaveDataEntry) }
//          .join()
//    }
//  }


//  private suspend fun submitDataBackground(context: Context, dbSaveDataEntry: DbSaveDataEntry) {
//
//    val job1 = Job()
//    CoroutineScope(Dispatchers.Main + job1).launch {
//      var progressDialog = ProgressDialog(context)
//      progressDialog.setTitle("Please wait..")
//      progressDialog.setMessage("Posting data in progress..")
//      progressDialog.setCanceledOnTouchOutside(false)
//      progressDialog.show()
//      Log.e("Submit", "Submitted Report $dbSaveDataEntry")
//
//      var messageToast = ""
//      val job = Job()
//      CoroutineScope(Dispatchers.IO + job)
//          .launch {
//            val formatterClass = FormatterClass()
//            val baseUrl = formatterClass.getSharedPref("serverUrl", context)
//
//            if (baseUrl != null) {
//              val apiService =
//                  RetrofitBuilder.getRetrofit(context, baseUrl).create(Interface::class.java)
//              try {
//                val apiInterface = apiService.submitData(dbSaveDataEntry)
//                messageToast =
//                    if (apiInterface.isSuccessful) {
//                      val statusCode = apiInterface.code()
//                      val body = apiInterface.body()
//                      if (statusCode == 200 || statusCode == 201) {
//                        "Saved and synced successfully"
//                      } else {
//                        "Error: Body is null"
//                      }
//                    } else {
//                      "Error: The request was not successful"
//                    }
//              } catch (e: Exception) {
//                messageToast = "There was an issue with the server"
//              }
//            }
//          }
//          .join()
//      CoroutineScope(Dispatchers.Main).launch {
//        progressDialog.dismiss()
//        Toast.makeText(context, messageToast, Toast.LENGTH_LONG).show()
//      }
//    }
//  }

//  private fun loadSpecificResponses(context: Context, id: String) {
//    CoroutineScope(Dispatchers.IO).launch {
//      val myViewModel = MainViewModel(context.applicationContext as Application)
//
//      val formatterClass = FormatterClass()
//      val baseUrl = formatterClass.getSharedPref("serverUrl", context)
//
//      val username = formatterClass.getSharedPref("username", context)
//      if (baseUrl != null && username != null) {
//        val apiService = RetrofitBuilder.getRetrofit(context, baseUrl).create(Interface::class.java)
//        try {
//          val apiInterface = apiService.getResponseDetails(id)
//          if (apiInterface.isSuccessful) {
//            val statusCode = apiInterface.code()
//            val body = apiInterface.body()
//            if (statusCode == 200 || statusCode == 201) {
//              if (body != null) {
//
//                val converters = Converters().toJsonResponseDetails(body)
//                try {
//                  val json = Gson().fromJson(converters, JsonObject::class.java)
//                  val indicatorsObject = json.getAsJsonObject("indicators")
////                  myViewModel.addPatient(context, id, indicatorsObject.toString())
//                } catch (e: Exception) {
//                  e.printStackTrace()
//                  Log.e("TAG", "json:::: ${e.message}")
//                }
//              }
//            }
//          }
//        } catch (e: Exception) {
//          print(e)
//        }
//      }
//    }
//  }

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
                                            val programStages = it.get("programStages").asJsonArray
                                            val programSections =
                                                it.get("programSections").asJsonArray
                                            val org = ProgramData(
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

    fun performPatientSearch(
        context: Context,
        eventData: EventData,
        progressBar: ProgressBar,
        searchParametersString: String
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            val viewModel = MainViewModel(context.applicationContext as Application)
            val formatterClass = FormatterClass()
            val baseUrl = formatterClass.getSharedPref("serverUrl", context)
            val username = formatterClass.getSharedPref("username", context)

            if (baseUrl != null && username != null) {
                val apiService =
                    RetrofitBuilder.getRetrofit(context, baseUrl).create(Interface::class.java)
                try {
                    progressBar.visibility = View.VISIBLE
                    val apiInterface = apiService.searchPatient(filter = searchParametersString)
                    if (apiInterface.isSuccessful) {
                        val statusCode = apiInterface.code()
                        val body = apiInterface.body()
                        if (statusCode == 200) {
                            if (body != null) {
                                progressBar.visibility = View.GONE
                                val converters = Converters().toJsonPatientSearch(body)
                                try {
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
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                    progressBar.visibility = View.GONE
                                    Log.e("TAG", "json err:::: ${e.message}")
                                }
                            }
                        }
                    }
                } catch (e: Exception) {
                    print(e)
                    Log.e("TAG", "Success Error:::: ${e.message}")
                    progressBar.visibility = View.GONE

                }
            }
        }
    }


}
