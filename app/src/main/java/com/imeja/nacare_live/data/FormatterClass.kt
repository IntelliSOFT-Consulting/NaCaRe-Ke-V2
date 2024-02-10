package com.imeja.nacare_live.data

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import android.net.ParseException
import com.imeja.nacare_live.R
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.UUID


class FormatterClass {
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
}