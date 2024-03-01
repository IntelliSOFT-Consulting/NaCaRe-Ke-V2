package com.capture.app.data

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.ParseException
import android.os.Build
import android.text.InputFilter
import android.text.Spanned
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.capture.app.R
import com.capture.app.model.HomeData
import com.capture.app.model.TrackedEntityInstanceAttributes
import com.capture.app.response.DataStoreResponse
import com.capture.app.room.DataStoreData
import com.capture.app.room.MainViewModel
import java.text.MessageFormat
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.Period
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.UUID


class FormatterClass {

    fun generateHomeData(viewModel: MainViewModel): List<HomeData> {
        // Replace this logic with your actual implementation
        val homeDataList: MutableList<HomeData> = ArrayList()
        // Example: Adding dummy data
        homeDataList.add(
            HomeData(
                MessageFormat.format(
                    "{0}",
                    viewModel.countEntities()
                ), "Number of notifications made by facility"
            )
        )
        homeDataList.add(
            HomeData(
                MessageFormat.format(
                    "{0}",
                    viewModel.countEntities()
                ), "Number of active notifications made by facility (not closed within 60 days)"
            )
        )
        return homeDataList
    }

    private val dateFormat: SimpleDateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.US)
    private val dateInverseFormat: SimpleDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)

    fun getDate(year: Int, month: Int, day: Int): String {
        val calendar = Calendar.getInstance()
        calendar[year, month] = day
        val date: Date = calendar.time
        return FormatterClass().formatCurrentDate(date)
    }

    fun formatDate(date: Date?): String? {
        if (date == null) {
            return null
        }
        val dateFormat = SimpleDateFormat("MM-dd hh:mm:ss", Locale.US)
        return dateFormat.format(date)
    }

    fun formatSimpleDate(date: Date): String? {
        return dateFormat.format(date)
    }

    fun formatCurrentDate(date: Date): String {
        return dateInverseFormat.format(date)
    }

    @Throws(ParseException::class)
    fun parseSimpleDate(date: String): Date? {
        return dateFormat.parse(date)
    }

    fun saveSharedPref(key: String, value: String, context: Context) {
        val sharedPreferences: SharedPreferences =
            context.getSharedPreferences(context.getString(R.string.app_name), MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString(key, value);
        editor.apply();
    }

    fun getSharedPref(key: String, context: Context): String? {
        val sharedPreferences: SharedPreferences =
            context.getSharedPreferences(context.getString(R.string.app_name), MODE_PRIVATE)
        return sharedPreferences.getString(key, null)

    }

    fun deleteSharedPref(key: String, context: Context) {
        val sharedPreferences: SharedPreferences =
            context.getSharedPreferences(context.getString(R.string.app_name), MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.remove(key);
        editor.apply();

    }

    fun generateUUID(length: Int): String {
        val uuid = UUID.randomUUID()
        val uuidString = uuid.toString().replace("-", "") // Remove hyphens
        return uuidString.take(length)
    }

    fun convertDateFormat(inputDate: String): String? {
        // Define the input date formats to check
        val inputDateFormats = arrayOf(
            "yyyy-MM-dd",
            "MM/dd/yyyy",
            "yyyyMMdd",
            "dd-MM-yyyy",
            "yyyy/MM/dd",
            "MM-dd-yyyy",
            "dd/MM/yyyy",
            "yyyyMMddHHmmss",
            "yyyy-MM-dd HH:mm:ss",
            "EEE, dd MMM yyyy HH:mm:ss Z",  // Example: "Mon, 25 Dec 2023 12:30:45 +0000"
            "yyyy-MM-dd'T'HH:mm:ssXXX",     // ISO 8601 with time zone offset (e.g., "2023-11-29T15:44:00+03:00")
            "EEE MMM dd HH:mm:ss zzz yyyy", // Example: "Sun Jan 01 00:00:00 GMT+03:00 2023"

            // Add more formats as needed
        )

        // Try parsing the input date with each format
        for (format in inputDateFormats) {
            try {
                val dateFormat = SimpleDateFormat(format, Locale.getDefault())
                dateFormat.isLenient = false // Set lenient to false
                val parsedDate = dateFormat.parse(inputDate)

                // If parsing succeeds, format and return the date in the desired format
                parsedDate?.let {
                    return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(it)
                }
            } catch (e: ParseException) {
                // Continue to the next format if parsing fails
            }
        }

        // If none of the formats match, return an error message or handle it as needed
        return null
    }

    fun calculateAge(birthDate: LocalDate, currentDate: LocalDate): Pair<Int, Int> {
        val period = Period.between(birthDate, currentDate)
        val years = period.years
        val months = period.months
        return Pair(years, months)
    }

    fun generateRespectiveValue(site: DataStoreData, dataValue: String): String {
        return try {
            val type = object : TypeToken<List<DataStoreResponse>>() {}.type
            val codesList: List<DataStoreResponse> = Gson().fromJson(site.dataValues, type)
            var validAnswer = ""
            for (code in codesList) {
                if (code.codes.contains(dataValue)) {
                    validAnswer = code.name
                }
            }
            validAnswer
        } catch (e: Exception) {
            ""
        }
    }

    fun maleCancers(): List<String> {
        return listOf(
            "C60",
            "C61",
            "C62",
            "C63"
        )
    }

    fun femaleCancers(): List<String> {
        return listOf(
            "C51",
            "C52",
            "C53",
            "C54",
            "C55",
            "C56",
            "C57",
            "C58"
        )
    }

    fun excludeHiddenFields(): List<String> {
        return listOf(
            "AP13g7NcBOf",
            "uR2Mnlh7sqn",
            "R1vaUuILrDy",
            "hn8hJsBAKrh",
            "mPpjmOxwsEZ",
            "jf04xeJYfIU",
            "vgK2f8ampTy",
            "xED9XkpCeUe",
            "hzVijy6tEUF",
            "oob3a4JM7H6",
            "eFbT7iTnljR",
        )
    }

    fun excludeBareMinimumInformation(attributes: List<TrackedEntityInstanceAttributes>): List<TrackedEntityInstanceAttributes> {
        val attributesToExclude = excludeHiddenFields()
        val refinedList = mutableListOf<TrackedEntityInstanceAttributes>()

        try {
            attributes.forEach { attribute ->
                val id = attribute.attribute
                if (attributesToExclude.contains(id)) {
                    refinedList.add(attribute)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return refinedList
    }

    fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val networkCapabilities = connectivityManager.activeNetwork ?: return false
            val activeNetwork =
                connectivityManager.getNetworkCapabilities(networkCapabilities) ?: return false
            return when {
                activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                // for other device how are able to connect with Ethernet
                activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
                else -> false
            }
        } else {
            // For devices running versions prior to Android M
            val networkInfo = connectivityManager.activeNetworkInfo
            return networkInfo != null && networkInfo.isConnected
        }
    }

    fun showInternetConnectionRequiredDialog(context: Context) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Internet Connection Required")
            .setMessage("Please check your internet connection and try again.")
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
            }
            .setCancelable(false)
            .show()
    }

    fun onlyAcceptLetters(id: String): Boolean {
        val itemsList = listOf(
            "R1vaUuILrDy",
            "hn8hJsBAKrh",
            "hzVijy6tEUF",

            ) // Add your items here
        // Check if the passed parameter is found in the list
        return itemsList.contains(id)
    }

    fun retrieveAllowedToTypeItem(id: String): Boolean {
        val itemsList = listOf(
            "BzhDnF5fG4x",
            "wzHl7HdsSlO",
            "PdDmTsAjysh",
            "uR2Mnlh7sqn"

        ) // Add your items here
        // Check if the passed parameter is found in the list
        return itemsList.contains(id)
    }


    fun setLettersOnly(editText: EditText) {
        val filter = object : InputFilter {
            override fun filter(
                source: CharSequence?,
                start: Int,
                end: Int,
                dest: Spanned?,
                dstart: Int,
                dend: Int
            ): CharSequence? {
                // Iterate over the characters being added
                for (i in start until end) {
                    // Check if the character is not a letter
                    if (!Character.isLetter(source?.get(i) ?: ' ')) {
                        // Return an empty string to reject the character
                        return ""
                    }
                }
                // Accept the character(s)
                return null
            }
        }
        // Set the filter to the EditText
        editText.filters = arrayOf(filter)
    }

    fun treatmentMinimum(): List<String> {
        return listOf(
            "JkSdwVJ0Cvd",
            "Bv1QzBVyXo3",
            "oNPuF57JWui",
            "fljqvOpheUV",
            "SW7Nxz6M64z",
            "qA5u5KweqU0",
            "LTALxLrNHNB",
            "yUFcwIifeA9", "ZOdPQ6iLMV4", "FqimnFgeqq1"
        )
    }

    fun selectedYesEntries(): List<String> {
        return listOf(
            "xMDNydpyKcj",
            "Cj3inBBEqoN",
            "GWzmO1k8WIX",
            "uG0nhxpDCEp",
            "RYMNbfQI6Xj",
            "vtU3HWpm3VQ",
            "pe5Qlr09BBd",
            "uQmp4kURCCQ",
            "jYoWCxPFInU",
            "ZoWjQn9uDfS",""
        )
    }

}