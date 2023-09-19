package com.intellisoft.hai.room

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.intellisoft.hai.helper_class.*
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
    fun fromJson(json: String): HomeItem {
        // convert json to MyJsonData object
        return gson.fromJson(json, HomeItem::class.java)
    }

    fun toPeriJson(data: PeriData): String {
        // convert MyJsonData object to json
        return gson.toJson(data)
    }

    @TypeConverter
    fun periFromJson(json: String): PeriData {
        // convert json to MyJsonData object
        return gson.fromJson(json, PeriData::class.java)
    }

    fun toPostJson(data: PostOperativeData): String {
        return gson.toJson(data)
    }

    fun postFromJson(data: String): PostOperativeData {
        return gson.fromJson(data, PostOperativeData::class.java)
    }
}