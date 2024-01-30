package com.nacare.capture.data.model;

import android.content.Context;
import android.content.SharedPreferences;

import com.nacare.capture.R;
import com.nacare.capture.data.service.SyncStatusHelper;

import java.text.MessageFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

public class FormatterClass {
    public void saveSharedPref(String key, String value, Context context) {
        SharedPreferences sharedPreferences =
                context.getSharedPreferences(context.getString(R.string.app_name), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(key, value);
        editor.apply();
    }

    public String getSharedPref(String key, Context context) {
        SharedPreferences sharedPreferences =
                context.getSharedPreferences(context.getString(R.string.app_name), Context.MODE_PRIVATE);
        return sharedPreferences.getString(key, null);
    }

    public void deleteSharedPref(String key, Context context) {
        SharedPreferences sharedPreferences =
                context.getSharedPreferences(context.getString(R.string.app_name), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(key);
        editor.apply();
    }
    public Date getNowWithoutTime() {
        final GregorianCalendar gc = new GregorianCalendar();
        gc.setTime(new Date());
        gc.set(Calendar.HOUR_OF_DAY, 0);
        gc.set(Calendar.MINUTE, 0);
        gc.set(Calendar.SECOND, 0);
        gc.set(Calendar.MILLISECOND, 0);
        return gc.getTime();
    }

    public List<HomeData> generateHomeData() {
        // Replace this logic with your actual implementation
        List<HomeData> homeDataList = new ArrayList<>();
        // Example: Adding dummy data
        homeDataList.add(new HomeData(MessageFormat.format("{0}", SyncStatusHelper.trackedEntityInstanceCount()), "Number of notifications made by facility"));
        homeDataList.add(new HomeData(MessageFormat.format("{0}", SyncStatusHelper.trackedEntityInstanceCount()), "Number of active notifications made by facility (not closed within 60 days)"));

        return homeDataList;
    }

    public String extractValid(String inputDate) {
        // Define the input date formats to check
        String[] inputDateFormats = {
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
                "EEE MMM dd HH:mm:ss zzz yyyy" // Example: "Sun Jan 01 00:00:00 GMT+03:00 2023"
                // Add more formats as needed
        };

        // Try parsing the input date with each format
        for (String format : inputDateFormats) {
            try {
                SimpleDateFormat dateFormat = new SimpleDateFormat(format, Locale.getDefault());
                dateFormat.setLenient(false); // Set lenient to false
                java.util.Date parsedDate = dateFormat.parse(inputDate);

                // If parsing succeeds, format and return the date in the desired format
                if (parsedDate != null) {
                    return new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(parsedDate);
                }
            } catch (ParseException e) {
                // Continue to the next format if parsing fails
            }
        }

        // If none of the formats match, return an error message or handle it as needed
        return null;
    }
}
