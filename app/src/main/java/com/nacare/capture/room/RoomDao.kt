package com.nacare.capture.room

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

    @Query("SELECT EXISTS (SELECT 1 FROM organization WHERE parentUid =:orgUid)")
    fun checkOrganizationExist(orgUid: String): Boolean

    @Query("UPDATE organization SET children =:json WHERE parentUid =:orgUid")
    fun updateOrganization(json: String, orgUid: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun createOrganization(data: OrganizationData)

    @Query("SELECT * FROM organization ORDER BY id DESC")
    fun loadOrganization(): List<OrganizationData>?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun saveTrackedEntity(data: TrackedEntityInstanceData): Long

    @Query("SELECT EXISTS (SELECT 1 FROM trackedEntity WHERE orgUnit =:orgUnit AND trackedEntity =:trackedEntity)")
    fun checkTrackedEntity(orgUnit: String, trackedEntity: String): Boolean

    @Query("UPDATE trackedEntity SET attributes =:attributes WHERE  orgUnit =:orgUnit AND trackedEntity =:trackedEntity")
    fun updateTrackedEntity(orgUnit: String, trackedEntity: String, attributes: String)

    @Query("SELECT * FROM trackedEntity WHERE isSynced =:isSynced AND isLocal=:isLocal ORDER BY id DESC")
    fun loadTrackedEntities(isSynced: Boolean, isLocal: Boolean): List<TrackedEntityInstanceData>?

    @Query("SELECT * FROM trackedEntity WHERE trackedEntity =:uid  ORDER BY id DESC")
    fun loadAllTrackedEntity(uid: String): TrackedEntityInstanceData?

    @Query("SELECT * FROM trackedEntity WHERE orgUnit =:orgUnit ORDER BY id DESC")
    fun loadAllTrackedEntities(orgUnit: String): List<TrackedEntityInstanceData>?

    @Query("DELETE FROM trackedEntity")
    fun wipeData()

    @Query("SELECT EXISTS (SELECT 1 FROM event WHERE program =:program AND orgUnit =:orgUnit)")
    fun checkEvent(program: String, orgUnit: String): Boolean

    @Query("UPDATE event SET dataValues =:dataValues WHERE program =:program AND orgUnit =:orgUnit")
    fun updateEvent(dataValues: String, program: String, orgUnit: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun saveEvent(data: EventData)

    @Query("SELECT * FROM event WHERE orgUnit =:orgUnit ORDER BY id DESC")
    fun loadEvents(orgUnit: String): List<EventData>?

    @Query("SELECT * FROM event WHERE isSynced =:isSynced  ORDER BY id DESC")
    fun loadAllEvents(isSynced: Boolean): List<EventData>?

    @Query("SELECT COUNT(*) FROM trackedEntity")
    fun countEntities(): Int

    @Query("SELECT * FROM event WHERE uid =:uid ORDER BY id DESC")
    fun loadEvent(uid: String): EventData?

    @Query("SELECT EXISTS (SELECT 1 FROM dataStore WHERE uid =:uid)")
    fun checkDataStore(uid: String): Boolean

    @Query("UPDATE dataStore SET dataValues =:dataValues WHERE uid =:uid")
    fun updateDataStore(dataValues: String, uid: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun addDataStore(data: DataStoreData)

    @Query("SELECT * FROM dataStore WHERE uid =:uid ORDER BY id DESC")
    fun loadDataStore(uid: String): DataStoreData?

    @Query("SELECT EXISTS (SELECT 1 FROM enrollmentevent WHERE eventUid =:eventUid AND program =:program AND programStage =:programStage AND orgUnit =:orgUnit)")
    fun checkProgramStageEnrollment(
        eventUid: String,
        program: String,
        programStage: String,
        orgUnit: String
    ): Boolean

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun addProgramStageEnrollment(payload: EnrollmentEventData)

    @Query(" UPDATE enrollmentevent SET dataValues =:dataValues WHERE eventUid =:eventUid AND program =:program AND programStage =:programStage AND orgUnit =:orgUnit")
    fun updateProgramStageEnrollment(
        dataValues: String,
        eventUid: String,
        program: String,
        programStage: String,
        orgUnit: String
    )

    @Query("UPDATE trackedEntity SET isSynced=:isSynced, trackedEntity =:reference WHERE trackedEntity =:trackedEntity")
    fun updateEntity(trackedEntity: String, reference: String, isSynced: Boolean)

    @Query("UPDATE trackedEntity SET isSynced=:isSynced, enrollment =:reference WHERE trackedEntity =:trackedEntity")
    fun updateEnrollmentEntity(trackedEntity: String, reference: String, isSynced: Boolean)

    @Query("SELECT * FROM enrollmentevent WHERE isSynced =:synced ORDER BY id DESC")
    fun getTrackedEvents(synced: Boolean): List<EnrollmentEventData>?

    @Query("SELECT * FROM enrollmentevent WHERE trackedEntity =:trackedEntity AND program =:programUid AND orgUnit=:orgUnit  ORDER BY id DESC LIMIT 1")
    fun getLatestEnrollment(
        trackedEntity: String,
        programUid: String,
        orgUnit: String
    ): EnrollmentEventData?

    @Query("SELECT * FROM enrollmentevent WHERE eventUid =:eventUid  ORDER BY id DESC LIMIT 1")
    fun loadLatestEvent(eventUid: String): EnrollmentEventData?

    @Query(" UPDATE trackedEntity SET enrollment =:enrollment WHERE  trackedEntity =:trackedEntity  AND orgUnit =:orgUnit")
    fun updateEnrollmentPerOrgAndProgram(trackedEntity: String, enrollment: String, orgUnit: String)

    @Query("UPDATE event SET isSynced=:isSynced, uid =:reference WHERE id =:id")
    fun updateFacilityEvent(id: String, reference: String, isSynced: Boolean)

    @Query("SELECT * FROM trackedEntity WHERE id=:id")
    fun loadTrackedEntity(id: String): TrackedEntityInstanceData?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun saveEnrollment(enrollment: EnrollmentEventData)

    @Query("SELECT * FROM enrollmentevent WHERE eventUid=:eventUid  ORDER BY id DESC LIMIT 1")
    fun loadEnrollment(eventUid: String): EnrollmentEventData?

}
