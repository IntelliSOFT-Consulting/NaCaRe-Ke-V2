package com.intellisoft.hai.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface RoomDao {

  @Insert(onConflict = OnConflictStrategy.REPLACE) fun addPatient(patientData: RegistrationData)
  @Query("SELECT EXISTS (SELECT 1 FROM registration WHERE patientId =:id)")
  fun checkPatientExists(id: String): Boolean
  @Query("SELECT * FROM registration WHERE userId =:userId")
  fun getPatients(userId: String): List<RegistrationData>?
  @Insert(onConflict = OnConflictStrategy.REPLACE) fun addPreparationData(data: PreparationData)
  @Insert(onConflict = OnConflictStrategy.REPLACE) fun addOutcomeData(data: OutcomeData)
  @Insert(onConflict = OnConflictStrategy.REPLACE)  fun addSurgicalSiteData(data: SurgicalSiteData)
  @Insert(onConflict = OnConflictStrategy.REPLACE)  fun addPeriData(data: PeriData)
  @Insert(onConflict = OnConflictStrategy.REPLACE)  fun addSkinPreparationData(data: SkinPreparationData)
  @Insert(onConflict = OnConflictStrategy.REPLACE)   fun addHandPreparationData(data: HandPreparationData)
}
