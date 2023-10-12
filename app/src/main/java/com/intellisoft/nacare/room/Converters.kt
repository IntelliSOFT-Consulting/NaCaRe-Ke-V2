package com.intellisoft.nacare.room

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.intellisoft.nacare.helper_class.*
import org.hisp.dhis.model.TrackedEntityAttribute
import java.util.Date

class Converters {
    private val gson = Gson()

    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }

    @TypeConverter
    fun toJsonOrganization(data: OrganizationResponse): String {
        return gson.toJson(data)
    }

    @TypeConverter
    fun toJsonProgram(data: ProgramResponse): String {
        return gson.toJson(data)
    }

    @TypeConverter
    fun toJsonTrackedEntityType(data: TrackedEntityAttribute): String {
        return gson.toJson(data)
    }
    @TypeConverter
    fun toJsonEntityAttributes(data: List<EntityAttributes>): String {
        return gson.toJson(data)
    }

    fun toJsonElements(data: List<ProgramStageSections>): String {
        return gson.toJson(data)
    }

    fun toJsonEvent(data: EventData): String {
        return gson.toJson(data)
    }

    fun toJsonOrgUnit(data: OrganizationUnitResponse): String {
        return gson.toJson(data)
    }

    @TypeConverter
    fun fromJsonOrgUnit(json: String): OrganizationUnitResponse {
        // convert json to MyJsonData object
        return gson.fromJson(json, OrganizationUnitResponse::class.java)

    }

    fun toJsonPatientSearch(data: SearchPatientResponse): String {
        return gson.toJson(data)
    }

}