package com.intellisoft.hai.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface RoomDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun addPatient(patientData: RegistrationData)

    @Query("SELECT EXISTS (SELECT 1 FROM registration WHERE patientId =:id)")
    fun checkPatientExists(id: String): Boolean

    @Query("SELECT EXISTS (SELECT 1 FROM patients WHERE patientId =:id)")
    fun checkPatient(id: String): Boolean

    @Query("SELECT * FROM registration WHERE userId =:userId")
    fun getPatients(userId: String): List<RegistrationData>?
    @Query("SELECT * FROM patients WHERE userId =:userId")
    fun getPatientsData(userId: String): List<PatientData>?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun addPreparationData(data: PreparationData)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun addOutcomeData(data: OutcomeData)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun addSurgicalSiteData(data: SurgicalSiteData)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun addPeriData(data: PeriData)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun addSkinPreparationData(data: SkinPreparationData)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun addHandPreparationData(data: HandPreparationData)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun addPrePostOperativeData(data: PrePostOperativeData)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun addPostOperativeData(data: PostOperativeData)

    @Query("SELECT * FROM encounter WHERE patientId =:patientId")
    fun getEncounters(patientId: String): List<EncounterData>?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun addEncounterData(enc: EncounterData)

    @Query("SELECT * FROM outcome WHERE patientId =:patientId AND encounterId =:encounterId")
    fun getOutcomes(patientId: String, encounterId: String): List<OutcomeData>?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun addNewPatient(data: PatientData)
    @Query("SELECT * FROM registration WHERE id =:caseId AND userId =:userId")
    fun getCaseDetails(userId: String, caseId: String): RegistrationData
}
