package com.imeja.nacare_live.room

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "program")
data class ProgramData(
    @ColumnInfo(name = "program_data") val jsonData: String,
    var userId: String,
) {
    @PrimaryKey(autoGenerate = true)
    var id: Int? = null
}
