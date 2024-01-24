package com.nacare.capture.data.model;

import android.content.Context;
import android.content.SharedPreferences;

import com.nacare.capture.R;
import com.nacare.capture.data.service.SyncStatusHelper;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

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

    public List<HomeData> generateHomeData() {
        // Replace this logic with your actual implementation
        List<HomeData> homeDataList = new ArrayList<>();
        // Example: Adding dummy data
        homeDataList.add(new HomeData(MessageFormat.format("{0}", SyncStatusHelper.trackedEntityInstanceCount()), "Number of notifications made by facility"));
        homeDataList.add(new HomeData(MessageFormat.format("{0}", SyncStatusHelper.trackedEntityInstanceCount()), "Number of active notifications made by facility (not closed within 60 days)"));

        return homeDataList;
    }
}
