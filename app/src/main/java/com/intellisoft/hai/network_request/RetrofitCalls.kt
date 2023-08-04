package com.intellisoft.hai.network_request

import android.app.Application
import android.app.ProgressDialog
import android.content.Context
import android.util.Log
import android.widget.Toast
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.intellisoft.hai.helper_class.DbSaveDataEntry
import com.intellisoft.hai.helper_class.FormatterClass
import com.intellisoft.hai.helper_class.ImageResponse
import com.intellisoft.hai.helper_class.SubmissionsStatus
import com.intellisoft.hai.room.*
import java.text.SimpleDateFormat
import java.util.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody

class RetrofitCalls {

  fun submitData(context: Context, dbSaveDataEntry: DbSaveDataEntry) {
    CoroutineScope(Dispatchers.Main).launch {
      val job = Job()
      CoroutineScope(Dispatchers.IO + job)
          .launch { submitDataBackground(context, dbSaveDataEntry) }
          .join()
    }
  }


  private suspend fun submitDataBackground(context: Context, dbSaveDataEntry: DbSaveDataEntry) {

    val job1 = Job()
    CoroutineScope(Dispatchers.Main + job1).launch {
      var progressDialog = ProgressDialog(context)
      progressDialog.setTitle("Please wait..")
      progressDialog.setMessage("Posting data in progress..")
      progressDialog.setCanceledOnTouchOutside(false)
      progressDialog.show()
      Log.e("Submit", "Submitted Report $dbSaveDataEntry")

      var messageToast = ""
      val job = Job()
      CoroutineScope(Dispatchers.IO + job)
          .launch {
            val formatterClass = FormatterClass()
            val baseUrl = formatterClass.getSharedPref("serverUrl", context)

            if (baseUrl != null) {
              val apiService =
                  RetrofitBuilder.getRetrofit(context, baseUrl).create(Interface::class.java)
              try {
                val apiInterface = apiService.submitData(dbSaveDataEntry)
                messageToast =
                    if (apiInterface.isSuccessful) {
                      val statusCode = apiInterface.code()
                      val body = apiInterface.body()
                      if (statusCode == 200 || statusCode == 201) {
                        "Saved and synced successfully"
                      } else {
                        "Error: Body is null"
                      }
                    } else {
                      "Error: The request was not successful"
                    }
              } catch (e: Exception) {
                messageToast = "There was an issue with the server"
              }
            }
          }
          .join()
      CoroutineScope(Dispatchers.Main).launch {
        progressDialog.dismiss()
        Toast.makeText(context, messageToast, Toast.LENGTH_LONG).show()
      }
    }
  }

  private fun loadSpecificResponses(context: Context, id: String) {
    CoroutineScope(Dispatchers.IO).launch {
      val myViewModel = MainViewModel(context.applicationContext as Application)

      val formatterClass = FormatterClass()
      val baseUrl = formatterClass.getSharedPref("serverUrl", context)

      val username = formatterClass.getSharedPref("username", context)
      if (baseUrl != null && username != null) {
        val apiService = RetrofitBuilder.getRetrofit(context, baseUrl).create(Interface::class.java)
        try {
          val apiInterface = apiService.getResponseDetails(id)
          if (apiInterface.isSuccessful) {
            val statusCode = apiInterface.code()
            val body = apiInterface.body()
            if (statusCode == 200 || statusCode == 201) {
              if (body != null) {

                val converters = Converters().toJsonResponseDetails(body)
                try {
                  val json = Gson().fromJson(converters, JsonObject::class.java)
                  val indicatorsObject = json.getAsJsonObject("indicators")
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

  private fun formatDateInput(inputDate: String?): String {
    val inputFormat = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"
    val outputFormat = "yyyy-MM-dd"
    var date = ""
    val dateFormat = SimpleDateFormat(inputFormat, Locale.ENGLISH)
    dateFormat.timeZone = TimeZone.getTimeZone("UTC") // Assuming the input date is in UTC timezone

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

}
