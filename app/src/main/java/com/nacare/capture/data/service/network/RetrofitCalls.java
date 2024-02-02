package com.nacare.capture.data.service.network;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import androidx.databinding.adapters.Converters;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.nacare.capture.data.model.FormatterClass;
import com.nacare.capture.data.response.ProgramResponse;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import retrofit2.Response;

public class RetrofitCalls {

    private final ExecutorService executor = Executors.newFixedThreadPool(2);

    public void loaProgram(Context context, String program) {
        Log.e("TAG", "Loading Program ***** " + program);
        String fields = "id,name,trackedEntityType,programStages[id,name,programStageSections[id,displayName,dataElements[id,attributeValues[attribute[id,name],value]]]],programSections[name,trackedEntityAttributes[id,attributeValues[attribute[id,name],value]]]";
        String filter = "name:ilike:" + program;
        executor.execute(() -> {
            FormatterClass formatterClass = new FormatterClass();
            String baseUrl = formatterClass.getSharedPref("serverUrl", context);

            String username = formatterClass.getSharedPref("username", context);
            if (baseUrl != null && username != null) {
                Interface apiService = RetrofitBuilder.getRetrofit(context, baseUrl).create(Interface.class);
                try {
                    Response<ProgramResponse> apiInterface = apiService.loadProgram(fields, filter).execute();
                    if (apiInterface.isSuccessful()) {
                        int statusCode = apiInterface.code();
                        ProgramResponse body = apiInterface.body();
                        if (statusCode == 200 || statusCode == 201) {
                            if (body != null) {
                                String responseBodyString = new Gson().toJson(body);
                                Log.e("TAG", "Response Data *****" + responseBodyString);
                                new FormatterClass().saveSharedPref(program + "_attribute_data", responseBodyString, context);

                            }
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.e("TAG", "Exception: " + e.getMessage());
                }
            }
        });
    }
}