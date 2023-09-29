package com.intellisoft.nacare.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface RoomDao {
    @Query("SELECT EXISTS (SELECT 1 FROM organizations WHERE code =:code)")
    fun checkOrganizationExists(code: String): Boolean

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun addOrganization(data: OrganizationData)

    @Query("UPDATE organizations SET name =:name WHERE code =:code")
    fun updateOrganization(name: String, code: String)

    @Query("SELECT * FROM organizations ORDER BY id DESC")
    fun loadOrganizations(): List<OrganizationData>?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun addEvent(data: EventData)
    @Query("SELECT * FROM events ORDER BY id DESC")
    fun loadEvents(): List<EventData>?
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun addProgram(data: ProgramData)

    @Query("SELECT EXISTS (SELECT 1 FROM programs WHERE code =:code)")
    fun checkProgramExists(code: String): Boolean
    @Query("UPDATE programs SET name =:name,programTrackedEntityAttributes =:programTrackedEntityAttributes,programStages =:programStages WHERE code =:code")
    fun updateProgram(
        name: String,
        programStages: String,
        programTrackedEntityAttributes: String,
        code: String
    )
    @Query("SELECT * FROM programs ORDER BY id DESC LIMIT 1")
    fun loadProgram(): ProgramData?
    @Query("SELECT * FROM events ORDER BY id DESC LIMIT 1")
    fun loadLatestEvent(): EventData?
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun addResponse(res: ElementResponse)
    @Query("SELECT EXISTS (SELECT 1 FROM responses WHERE userId =:userId AND eventId =:event AND indicatorId =:element)")
    fun checkResponse(userId: String, event: String, element: String): Boolean
    @Query("UPDATE responses SET value =:response WHERE  userId =:userId AND eventId =:event AND indicatorId =:element")
    fun updateResponse(response: String, userId: String, event: String, element: String)
    @Query("DELETE FROM responses  WHERE  userId =:userId AND eventId =:event AND indicatorId =:element")
    fun deleteResponse(userId: String, event: String, element: String)
    @Query("UPDATE organizations SET children =:children WHERE  code =:code")
    fun updateChildOrgUnits(code: String, children: String)

}
