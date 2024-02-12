package com.imeja.nacare_live.room

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
    fun saveTrackedEntity(data: TrackedEntityInstanceData)

    @Query("SELECT EXISTS (SELECT 1 FROM trackedEntity WHERE orgUnit =:orgUnit AND trackedEntity =:trackedEntity)")
    fun checkTrackedEntity(orgUnit: String, trackedEntity: String): Boolean

    @Query("UPDATE trackedEntity SET attributes =:attributes WHERE  orgUnit =:orgUnit AND trackedEntity =:trackedEntity")
    fun updateTrackedEntity(orgUnit: String, trackedEntity: String, attributes: String)

    @Query("SELECT * FROM trackedEntity WHERE isSynced =:isSynced  ORDER BY id DESC")
    fun loadTrackedEntities(isSynced: Boolean): List<TrackedEntityInstanceData>?

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

    @Query("SELECT COUNT(*) FROM trackedEntity")
    fun countEntities(): Int
    @Query("SELECT * FROM event WHERE uid =:uid ORDER BY id DESC")
    fun loadEvent(uid: String): EventData?

}
