package com.intellisoft.hai.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface RoomDao {

  @Insert(onConflict = OnConflictStrategy.REPLACE) fun addPatient(patientData: RegistrationData)
  @Query("SELECT EXISTS (SELECT 1 FROM registration WHERE patientId =:patientId)")
  fun checkPatientExists(patientId: String): Boolean
  @Query("SELECT * FROM registration WHERE userId =:userId")
  fun getPatients(userId: String): List<RegistrationData>?
}
