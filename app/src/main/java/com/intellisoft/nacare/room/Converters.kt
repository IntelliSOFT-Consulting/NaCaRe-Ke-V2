package com.intellisoft.nacare.room

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.intellisoft.nacare.helper_class.*
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

    fun toJsonElements(data: List<ProgramStageDataElements>): String {
        return gson.toJson(data)
    }
}