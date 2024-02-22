package com.capture.app.room

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "program")
data class ProgramData(
    @ColumnInfo(name = "program_data") val jsonData: String,
    var userId: String

) {
    @PrimaryKey(autoGenerate = true)
    var id: Int? = null
}

@Entity(tableName = "organization")
data class OrganizationData(
    @ColumnInfo(name = "children") val jsonData: String,
    var parentUid: String
) {
    @PrimaryKey(autoGenerate = true)
    var id: Int? = null
}

@Entity(tableName = "trackedEntity")
data class TrackedEntityInstanceData(
    @ColumnInfo(name = "attributes") val attributes: String,
    var trackedUnique: String,
    var trackedEntity: String,
    var orgUnit: String,
    var parentOrgUnit: String,
    var enrollment: String,
    var enrollDate: String,
    val isLocal: Boolean = false,
    val isSynced: Boolean = false,
) {
    @PrimaryKey(autoGenerate = true)
    var id: Int? = null
}

@Entity(tableName = "event")
data class EventData(
    @ColumnInfo(name = "dataValues") val dataValues: String,
    var uid: String,
    var program: String,
    var orgUnit: String,
    var eventDate: String,
    var status: String,
    val isServerSide: Boolean = false,
    val isSynced: Boolean = false,
) {
    @PrimaryKey(autoGenerate = true)
    var id: Int? = null
}

@Entity(tableName = "dataStore")
data class DataStoreData(
    @ColumnInfo(name = "dataValues") val dataValues: String,
    var uid: String
) {
    @PrimaryKey(autoGenerate = true)
    var id: Int? = null
}

@Entity(tableName = "enrollmentevent")
data class EnrollmentEventData(
    @ColumnInfo(name = "dataValues") val dataValues: String,
    var uid: String,
    var eventUid: String,
    var program: String,
    var programStage: String,
    var orgUnit: String,
    var eventDate: String,
    var status: String,
    var trackedEntity: String,
    var initialUpload: Boolean = false,
    val isSynced: Boolean = false,
) {
    @PrimaryKey(autoGenerate = true)
    var id: Int? = null
}
