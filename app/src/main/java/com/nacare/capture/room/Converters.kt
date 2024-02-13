package com.nacare.capture.room

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.nacare.capture.model.CountyUnit
import com.nacare.capture.model.DataValue
import com.nacare.capture.model.TrackedEntityInstanceAttributes
import com.nacare.capture.response.OrganizationUnitResponse
import com.nacare.capture.response.ProgramResponse
import com.nacare.capture.response.SearchPatientResponse
import com.nacare.capture.response.UserLoginData


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
    fun toUserJson(json: UserLoginData): String {
        // convert json to MyJsonData object
        return gson.toJson(json, UserLoginData::class.java)
    }

    @TypeConverter
    fun toJsonOrgUnit(json: OrganizationUnitResponse): String {
        // convert json to MyJsonData object
        return gson.toJson(json, OrganizationUnitResponse::class.java)
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

    @TypeConverter
    fun fromJsonUser(json: String): UserLoginData {
        // convert json to MyJsonData object
        return gson.fromJson(json, UserLoginData::class.java)
    }

    @TypeConverter
    fun fromJsonAttribute(json: String): List<TrackedEntityInstanceAttributes> {
        val listType = object : TypeToken<List<TrackedEntityInstanceAttributes>>() {}.type
        return gson.fromJson(json, listType)
    }

    @TypeConverter
    fun fromJsonDataAttribute(json: String): List<DataValue> {
        val listType = object : TypeToken<List<DataValue>>() {}.type
        return gson.fromJson(json, listType)
    }


    @TypeConverter
    fun fromJsonOrgUnit(json: String): CountyUnit {
        // convert json to MyJsonData object
        return gson.fromJson(json, CountyUnit::class.java)
    }
}