package com.imeja.nacare_live.room

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.imeja.nacare_live.model.ProgramData
import com.imeja.nacare_live.response.ProgramResponse
import com.imeja.nacare_live.response.SearchPatientResponse


class Converters {
    private val gson = Gson()

    @TypeConverter
    fun fromJson(json: String): ProgramResponse {
        // convert json to MyJsonData object
        return gson.fromJson(json, ProgramResponse::class.java)
    }

    @TypeConverter
    fun toJson(json: ProgramResponse): String {
        // convert json to MyJsonData object
        return gson.toJson(json, ProgramResponse::class.java)
    }

    @TypeConverter
    fun toJsonPatientSearch(json: SearchPatientResponse): String {
        // convert json to MyJsonData object
        return gson.toJson(json, SearchPatientResponse::class.java)
    }

    @TypeConverter
    fun fromJsonPatientSearch(json: String): SearchPatientResponse {
        // convert json to MyJsonData object
        return gson.fromJson(json, SearchPatientResponse::class.java)
    }
}