package com.intellisoft.hai.room

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "registration")
data class RegistrationData(
    var userId: String,
    val patientId: String,
    val secondaryId: String,
    val gender: String,
    val date_of_birth: String,
    val date_of_admission: String,
    val date_of_surgery: String,
    @ColumnInfo(name = "procedure") val procedure: String,
    val procedure_other: String?,
    val scheduling: String,
    val location: String,

) {
    @PrimaryKey(autoGenerate = true) var id: Int? = null
}


