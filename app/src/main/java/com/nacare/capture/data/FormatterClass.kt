package com.nacare.capture.data

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import android.net.ParseException
import com.nacare.capture.R
import com.nacare.capture.model.HomeData
import com.nacare.capture.room.MainViewModel
import java.text.MessageFormat
import java.text.SimpleDateFormat
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


}