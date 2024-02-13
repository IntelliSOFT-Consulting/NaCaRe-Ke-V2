package com.nacare.capture.room

import android.content.Context
import com.google.gson.Gson
import com.nacare.capture.data.FormatterClass
import com.nacare.capture.model.TrackedEntityInstance
import java.util.Date

class MainRepository(private val roomDao: RoomDao) {

    private val formatterClass = FormatterClass()

    fun addIndicators(data: ProgramData) {
        val exists = roomDao.checkProgramExist(data.userId)
        if (exists) {
            roomDao.updateProgram(data.jsonData, data.userId)
        } else {
            roomDao.addIndicators(data)
        }

    }

    fun loadPrograms(context: Context): List<ProgramData> {
        return roomDao.loadPrograms()
    }

    fun deletePrograms() {
        return roomDao.deletePrograms()
    }

    fun loadSingleProgram(context: Context, userId: String): ProgramData? {
        return roomDao.loadSingleProgram(userId)

    }

    fun createUpdateOrg(context: Context, orgUid: String, json: String) {
        val exists = roomDao.checkOrganizationExist(orgUid)
        if (exists) {
            roomDao.updateOrganization(json, orgUid)
        } else {
            val data = OrganizationData(parentUid = orgUid, jsonData = json)
            roomDao.createOrganization(data)
        }
    }

    fun loadOrganization(context: Context): List<OrganizationData>? {
        return roomDao.loadOrganization()
    }

    fun saveTrackedEntity(context: Context, data: TrackedEntityInstance) {
        val formatter = FormatterClass()
        val exists = roomDao.checkTrackedEntity(data.orgUnit, data.trackedEntity)
        if (exists) {
            roomDao.updateTrackedEntity(
                data.orgUnit,
                data.trackedEntity,
                Gson().toJson(data.attributes)
            )
        } else {
            val save = TrackedEntityInstanceData(
                trackedEntity = data.trackedEntity,
                orgUnit = data.orgUnit,
                enrollment = data.enrollment,
                enrollDate = data.enrollDate,
                attributes = Gson().toJson(data.attributes)

            )
            val savedItemId = roomDao.saveTrackedEntity(save)
            val enrollmentUid = formatter.generateUUID(11)
            val eventUid = formatter.generateUUID(11)
            val enrollment = EnrollmentEventData(
                dataValues = "",
                uid = enrollmentUid,
                eventUid = eventUid,
                program = formatter.getSharedPref("programUid", context).toString(),
                programStage = formatter.getSharedPref("programUid", context).toString(),
                orgUnit = formatter.getSharedPref("orgCode", context).toString(),
                eventDate = formatter.formatCurrentDate(Date()),
                status = "ACTIVE",
                trackedEntity = savedItemId.toString()
            )
            formatter.saveSharedPref("eventUid", eventUid, context)
            formatter.saveSharedPref("enrollmentUid", enrollmentUid, context)
            roomDao.saveEnrollment(enrollment)
        }
    }

    fun loadTrackedEntities(context: Context, isSynced: Boolean): List<TrackedEntityInstanceData>? {
        return roomDao.loadTrackedEntities(isSynced)
    }

    fun loadAllTrackedEntity(uid: String): TrackedEntityInstanceData? {
        return roomDao.loadAllTrackedEntity(uid)
    }

    fun wipeData(context: Context) {
        roomDao.wipeData()
    }

    fun loadAllTrackedEntities(orgUnit: String): List<TrackedEntityInstanceData>? {
        return roomDao.loadAllTrackedEntities(orgUnit)
    }

    fun saveEvent(data: EventData) {
        val exists = roomDao.checkEvent(data.program, data.orgUnit)
        if (exists) {
            roomDao.updateEvent(data.dataValues, data.program, data.orgUnit)
        } else {
            roomDao.saveEvent(data)
        }
    }

    fun loadEvents(orgUnit: String): List<EventData>? {
        return roomDao.loadEvents(orgUnit)

    }

    fun countEntities(): String {
        val data = roomDao.countEntities()
        return "$data"
    }

    fun loadEvent(uid: String): EventData? {
        return roomDao.loadEvent(uid)
    }

    fun addDataStore(data: DataStoreData) {
        val exists = roomDao.checkDataStore(data.uid)
        if (exists) {
            roomDao.updateDataStore(data.dataValues, data.uid)
        } else {
            roomDao.addDataStore(data)
        }
    }

    fun loadDataStore(uid: String): DataStoreData? {
        return roomDao.loadDataStore(uid)
    }

    fun addProgramStage(payload: EnrollmentEventData) {
        val exists = roomDao.checkProgramStageEnrollment(
            payload.eventUid,
            payload.program,
            payload.programStage,
            payload.orgUnit
        )
        if (exists) {
            roomDao.updateProgramStageEnrollment(
                payload.dataValues,
                payload.eventUid,
                payload.program,
                payload.programStage,
                payload.orgUnit
            )
        } else {
            roomDao.addProgramStageEnrollment(payload)
        }
    }

    fun updateEntity(trackedEntity: String, reference: String) {
        roomDao.updateEntity(trackedEntity, reference, true)

    }

    fun updateEnrollmentEntity(trackedEntity: String, reference: String) {
        roomDao.updateEnrollmentEntity(trackedEntity, reference, true)

    }

    fun getTrackedEvents(context: Context, synced: Boolean): List<EnrollmentEventData>? {
        return roomDao.getTrackedEvents(synced)
    }

    fun getLatestEnrollment(
        context: Context,
        trackedEntity: String,
        programUid: String,
        orgUnit: String
    ): EnrollmentEventData? {
        return roomDao.getLatestEnrollment(trackedEntity, programUid, orgUnit)
    }

    fun loadLatestEvent(eventUid: String): EnrollmentEventData? {
        return roomDao.loadLatestEvent(eventUid)
    }

    fun updateEnrollmentPerOrgAndProgram(
        entityReference: String,
        enrollment: String,
        orgUnit: String
    ) {
        roomDao.updateEnrollmentPerOrgAndProgram(entityReference, enrollment, orgUnit)
    }

    fun loadAllEvents(synced: Boolean): List<EventData>? {
        return roomDao.loadAllEvents(synced)
    }

    fun updateFacilityEvent(id: String, reference: String) {
        roomDao.updateFacilityEvent(id, reference, true)
    }

    fun loadTrackedEntity(id: String): TrackedEntityInstanceData? {
        return roomDao.loadTrackedEntity(id)
    }

    fun loadEnrollment(context: Context, eventUid: String): EnrollmentEventData? {
        return roomDao.loadEnrollment(eventUid)
    }

    fun addEnrollmentData(data: EnrollmentEventData) {
        roomDao.saveEnrollment(data)

    }

}
