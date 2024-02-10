package com.imeja.nacare_live.room

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.imeja.nacare_live.model.CountyUnit
import com.imeja.nacare_live.model.ProgramData
import com.imeja.nacare_live.response.OrganizationUnitResponse
import com.imeja.nacare_live.response.ProgramResponse
import com.imeja.nacare_live.response.SearchPatientResponse
import com.imeja.nacare_live.response.UserLoginData


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
    fun fromJsonOrgUnit(json: String): CountyUnit {
        // convert json to MyJsonData object
        return gson.fromJson(json, CountyUnit::class.java)
    }
}