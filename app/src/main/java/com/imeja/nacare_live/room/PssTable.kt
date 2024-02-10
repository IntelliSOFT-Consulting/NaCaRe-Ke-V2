package com.imeja.nacare_live.room

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

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
    var trackedEntity: String,
    var orgUnit: String,
    var enrollment: String,
    var enrollDate: String,
    val isSynced: Boolean = false,
) {
    @PrimaryKey(autoGenerate = true)
    var id: Int? = null
}
