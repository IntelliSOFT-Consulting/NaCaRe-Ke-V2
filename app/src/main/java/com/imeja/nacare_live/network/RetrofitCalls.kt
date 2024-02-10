package com.imeja.nacare_live.network

import android.app.Application
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.google.android.material.button.MaterialButton
import com.imeja.nacare_live.R
import com.imeja.nacare_live.auth.SyncActivity
import com.imeja.nacare_live.data.Constants
import com.imeja.nacare_live.data.FormatterClass
import com.imeja.nacare_live.room.Converters
import com.imeja.nacare_live.room.MainViewModel
import com.imeja.nacare_live.room.ProgramData
import com.imeja.nacare_live.ui.patients.PatientRegistrationActivity
import com.imeja.nacare_live.ui.patients.PatientSearchActivity
import com.imeja.nacare_live.ui.patients.PatientSearchResultsActivity
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

    fun signIn(context: Context) {
        CoroutineScope(Dispatchers.IO).launch {
            val formatter = FormatterClass()

            val apiService = RetrofitBuilder.getRetrofit(context, BASE_URL = Constants.BASE_URL)
                .create(Interface::class.java)
            try {
                val apiInterface = apiService.signIn()
                if (apiInterface.isSuccessful) {
                    val statusCode = apiInterface.code()
                    val body = apiInterface.body()
                    if (statusCode == 200 || statusCode == 201) {
                        formatter.saveSharedPref("username", "admin", context)
                        formatter.saveSharedPref("password", "district", context)
                        formatter.saveSharedPref("isLoggedIn", "true", context)
                        val intent = Intent(context, SyncActivity::class.java)
                        context.startActivity(intent)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
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
}


