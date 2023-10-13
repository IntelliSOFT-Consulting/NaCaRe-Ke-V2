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
    @ColumnInfo(name = "children")
    val children: String,
    @ColumnInfo(name = "created_at")
    val createdAt: Date = Date(),
    @ColumnInfo(name = "updated_at")
    val updatedAt: Date = Date(),
) : Serializable {
    @PrimaryKey(autoGenerate = true)
    var id: Int? = null
}

@Entity(tableName = "programs")
data class ProgramData(
    val type: String,
    val name: String,
    val code: String,
    @ColumnInfo(name = "programStages")
    val programStages: String,
    @ColumnInfo(name = "programTrackedEntityAttributes")
    val programTrackedEntityAttributes: String,
    @ColumnInfo(name = "created_at")
    val createdAt: Date = Date(),
    @ColumnInfo(name = "updated_at")
    val updatedAt: Date = Date(),
) : Serializable {
    @PrimaryKey(autoGenerate = true)
    var id: Int? = null
}

@Entity(tableName = "responses")
data class ElementResponse(
    var userId: String,
    val eventId: String,
    var patientId: String,
    val indicatorId: String,
    val value: String,
    val isPatient: Boolean = false,
) {
    @PrimaryKey(autoGenerate = true)
    var id: Int? = null
}

@Entity(tableName = "facility_event_data")
data class FacilityEventData(
    var userId: String,
    val event: String,
    var status: String,
    val programStage: String,
    val orgUnit: String,
    @ColumnInfo(name = "dataValues")
    val dataValues: String,
    @ColumnInfo(name = "responses")
    val responses: String,
) {
    @PrimaryKey(autoGenerate = true)
    var id: Int? = null
}

@Entity(tableName = "events")
data class EventData(
    val date: String,
    val orgUnitCode: String,
    val orgUnitName: String,
    val patientId: String,
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

