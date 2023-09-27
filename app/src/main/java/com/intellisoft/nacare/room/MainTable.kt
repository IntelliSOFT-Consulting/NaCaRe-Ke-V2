package com.intellisoft.nacare.room

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable
import java.util.Date

@Entity(tableName = "organizations")
data class OrganizationData(
    val name: String,
    val code: String,
    @ColumnInfo(name = "created_at")
    val createdAt: Date = Date(),
    @ColumnInfo(name = "updated_at")
    val updatedAt: Date = Date(),
) : Serializable {
    @PrimaryKey(autoGenerate = true)
    var id: Int? = null
}

@Entity(tableName = "events")
data class EventData(
    val date: String,
    val orgUnitCode: String,
    val orgUnitName: String,
    val saved: Boolean = false,
    val synced: Boolean = false,
    @ColumnInfo(name = "created_at")
    val createdAt: Date = Date(),
    @ColumnInfo(name = "updated_at")
    val updatedAt: Date = Date(),
) : Serializable {
    @PrimaryKey(autoGenerate = true)
    var id: Int? = null
}

