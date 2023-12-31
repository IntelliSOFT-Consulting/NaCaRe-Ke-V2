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

    @Query("SELECT * FROM events WHERE userId =:userId ORDER BY id DESC")
    fun loadEvents(userId: String): List<EventData>?

    @Query("SELECT * FROM events WHERE status =:status AND userId =:userId ORDER BY id DESC")
    fun loadStatusEvents(status: String, userId: String): List<EventData>?

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

    @Query("SELECT * FROM programs where type=:type ORDER BY id DESC LIMIT 1")
    fun loadProgram(type: String): ProgramData?

    @Query("SELECT * FROM events ORDER BY id DESC LIMIT 1")
    fun loadLatestEvent(): EventData?

    @Query("SELECT * FROM events  WHERE id =:id ORDER BY id DESC LIMIT 1")
    fun loadCurrentEvent(id: String): EventData?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun addResponse(res: ElementResponse)

    @Query("SELECT EXISTS (SELECT 1 FROM responses WHERE userId =:userId AND patientId=:patientId AND eventId =:event AND indicatorId =:element)")
    fun checkResponse(userId: String, patientId: String, event: String, element: String): Boolean

    @Query("UPDATE responses SET value =:response, isPatient =:isPatient WHERE  userId =:userId AND patientId =:patientId AND eventId =:event AND indicatorId =:element")
    fun updateResponse(
        response: String,
        userId: String,
        isPatient: Boolean,
        patientId: String,
        event: String,
        element: String
    )

    @Query("DELETE FROM responses  WHERE  userId =:userId AND eventId =:event AND indicatorId =:element")
    fun deleteResponse(userId: String, event: String, element: String)

    @Query("UPDATE organizations SET children =:children WHERE  code =:code")
    fun updateChildOrgUnits(code: String, children: String)

    @Query(
        "SELECT value FROM responses WHERE userId =:userId AND patientId =:patientId AND indicatorId =:indicatorId AND eventId =:eventId"
    )
    fun getEventResponse(
        userId: String,
        patientId: String,
        indicatorId: String,
        eventId: String
    ): String?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun addFacilityEventData(data: FacilityEventData)

    @Query("SELECT EXISTS (SELECT 1 FROM facility_event_data WHERE event =:event)")
    fun checkFacility(event: String): Boolean

    @Query("UPDATE facility_event_data SET dataValues =:dataValues WHERE  event =:event")
    fun updateFacilityEventData(event: String, dataValues: String)

    @Query("UPDATE facility_event_data SET responses =:responses WHERE  event =:event")
    fun updateFacilityEventResponseData(event: String, responses: String)

    @Query("SELECT * FROM facility_event_data where orgUnit =:orgUnit ORDER BY id DESC LIMIT 1")
    fun loadFacilityEvents(orgUnit: String): FacilityEventData?

    @Query("SELECT * FROM responses  WHERE  isPatient =:b ORDER BY id DESC ")
    fun getAllPatientsData(b: Boolean): List<ElementResponse>?

    @Query("SELECT EXISTS (SELECT 1 FROM responses WHERE isPatient =:b AND eventId =:code)")
    fun getEventPatientDetails(code: String, b: Boolean): Boolean

    @Query("UPDATE events SET saved =:saved, status =:status WHERE  id =:eventId")
    fun competeEvent(eventId: String, saved: Boolean, status: String)

    @Query("UPDATE responses SET patientId =:reference WHERE  eventId =:eventId AND isPatient =:isPatient")
    fun updatePatientEventResponse(eventId: String, reference: String, isPatient: Boolean)

    @Query("UPDATE events SET patientId =:reference WHERE  id =:eventId")
    fun updatePatientToEventResponse(eventId: String, reference: String)
    @Query("UPDATE events SET entityId =:entityId WHERE  id =:eventId AND userId =:userId")
    fun tiePatientToEvent(userId: String, eventId: String, entityId: String)
    @Query("UPDATE events SET date =:date WHERE  id =:eventId AND userId =:userId")
    fun updateEventData(userId: String, eventId: String, date: String)
    @Query("UPDATE events SET saved =:saved, status=:status WHERE userId =:userId")
    fun resetAllEvents(userId: String, status: String, saved: Boolean)

}
