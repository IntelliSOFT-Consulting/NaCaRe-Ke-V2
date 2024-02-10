package com.imeja.nacare_live.room

import android.content.Context
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query


@Dao
interface RoomDao {
    @Query("SELECT EXISTS (SELECT 1 FROM program WHERE userId =:userId)")
    fun checkProgramExist(userId: String): Boolean

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun addIndicators(indicatorsData: ProgramData)

    @Query("UPDATE program SET program_data =:value WHERE userId =:userId")
    fun updateProgram(value: String, userId: String)

    @Query("SELECT * FROM program")
    fun loadPrograms(): List<ProgramData>

    @Query("DELETE FROM program")
    fun deletePrograms()

    @Query("SELECT * FROM program where userId =:userId LIMIT 1")
    fun loadSingleProgram(userId: String): ProgramData?
}
