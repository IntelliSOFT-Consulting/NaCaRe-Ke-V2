package com.intellisoft.hai.helper_class

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import android.os.Build
import androidx.annotation.RequiresApi
import com.intellisoft.hai.R
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.collections.ArrayList


class FormatterClass {
    fun generateHomeData(context: Context): ArrayList<HomeItem> {
        return arrayListOf(
            HomeItem(R.drawable.baseline_pie_chart_24, "Data Entry"),
//            HomeItem(R.drawable.cases, "Patients"),
            HomeItem(R.drawable.baseline_arrow_right_alt_24, "Reports"),
        )
    }

    fun generateRiskFactors(context: Context): Array<String> {
        return arrayOf(
            "Healthy person",
            "Hypertension",
            "Diabetes",
            "COPD",
            "Major trauma",
            "Age >75 yrs",
            "Immunocompromised",
            "Multiple fractures",
            "Heart Failure",
            "Kidney failure"
        )
    }

    fun generateTimings(context: Context): Array<String> {
        return arrayOf(
            "0 < 1 minute",
            "1 minute - 2 minutes",
            "> 2 minutes - 3 minutes",
            "> 3 minutes - 4 minutes",
            "> 4 minutes - 5 minutes",
            "> 5 minutes"
        )
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

    @RequiresApi(Build.VERSION_CODES.O)
    fun getCurrentDate(): String {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val currentDate = LocalDate.now()
        return currentDate.format(formatter)
    }

    fun getYear(): String {
        return Calendar.getInstance().get(Calendar.YEAR).toString()
    }

    fun generateAntibiotics(context: Context): Array<String> {
        return arrayOf(
            "None given",
            "Gentamicin",
            "Amoxi/clavulanic acid",
            "Ciprofloxacin",
            "Cefazolin",
            "Cloxacillin",
            "Vancomycin",
            "Metronidazole",
            "Penicillin",
            "Ceftriaxone",
            "Cefepime",
            "Cefuroxime",
            "Other (specify)"
        )
    }

    fun generateReasons(requireContext: Context): Array<String> {
        return arrayOf(
            "Prophylaxis",
            "Treating suspected/known infection",
            "Drain/implant inserted",
            "Standard Practice",
            "Other",
        )
    }

    fun generateImplants(requireContext: Context): Array<String> {
        return arrayOf(
            "No",
            "Metal (ortho)",
            "Skin graft",
            "Mesh",
            "Other"
        )
    }


}