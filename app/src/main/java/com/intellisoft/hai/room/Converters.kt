package com.intellisoft.hai.room

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.intellisoft.hai.helper_class.*

class Converters {
    private val gson = Gson()

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
}