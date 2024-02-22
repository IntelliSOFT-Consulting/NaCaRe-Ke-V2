package com.capture.app.room

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.capture.app.data.FormatterClass
import com.capture.app.model.TrackedEntityInstance
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

    fun saveTrackedEntity(
        context: Context,
        data: TrackedEntityInstance,
        parentOrg: String,
        patientIdentification: String
    ) {
        val formatter = FormatterClass()
        val exists = false// roomDao.checkTrackedEntity(data.orgUnit, data.trackedEntity)
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
                parentOrgUnit = parentOrg,
                enrollment = data.enrollment,
                enrollDate = data.enrollDate,
                isLocal = true,
                attributes = Gson().toJson(data.attributes),
                trackedUnique = patientIdentification
            )
            val savedItemId = roomDao.saveTrackedEntity(save)
            val eventUid = formatter.generateUUID(11)
            val enrollmentUid = formatter.generateUUID(11)
            val enrollment = EnrollmentEventData(
                dataValues = "",
                uid = enrollmentUid,
                eventUid = eventUid,
                program = formatter.getSharedPref("programUid", context).toString(),
                programStage = formatter.getSharedPref("programStage", context).toString(),
                orgUnit = formatter.getSharedPref("orgCode", context).toString(),
                eventDate = formatter.formatCurrentDate(Date()),
                status = "ACTIVE",
                trackedEntity = savedItemId.toString()
            )
            formatter.saveSharedPref("current_patient_id", "$savedItemId", context)
            formatter.saveSharedPref("eventUid", eventUid, context)
            formatter.saveSharedPref("enrollmentUid", data.enrollment, context)
            roomDao.saveEnrollment(enrollment)
        }
    }

    fun saveTrackedEntityWithEnrollment(
        context: Context,
        data: TrackedEntityInstance,
        enrollment: EnrollmentEventData,
        parentOrg: String,
        patientIdentification: String
    ) {

        val exists = false//roomDao.checkTrackedEntity(data.orgUnit, data.trackedEntity)
        if (exists) {
            roomDao.updateTrackedEntity(
                data.orgUnit,
                data.trackedEntity,
                Gson().toJson(data.attributes)
            )
            val patient = roomDao.getSpecificTracked(data.orgUnit, data.trackedEntity)
            if (patient != null) {
                completeRegistration(context, "${patient.id}", enrollment)
            }
        } else {
            val save = TrackedEntityInstanceData(
                trackedEntity = data.trackedEntity,
                orgUnit = data.orgUnit,
                parentOrgUnit = parentOrg,
                enrollment = data.enrollment,
                enrollDate = data.enrollDate,
                attributes = Gson().toJson(data.attributes), trackedUnique = patientIdentification
            )
            val savedItemId = roomDao.saveTrackedEntity(save)
            completeRegistration(context, "$savedItemId", enrollment)

        }
    }

    private fun completeRegistration(
        context: Context,
        savedItemId: String,
        enrollment: EnrollmentEventData
    ) {
        val formatter = FormatterClass()
        formatter.saveSharedPref("current_patient_id", "$savedItemId", context)
        val enrollmentUid = enrollment.uid
        val eventUid = enrollment.eventUid
        val child = EnrollmentEventData(
            dataValues = "",
            uid = enrollmentUid,
            eventUid = eventUid,
            program = enrollment.program,
            programStage = enrollment.programStage,
            orgUnit = enrollment.orgUnit,
            eventDate = formatter.formatCurrentDate(Date()),
            status = "ACTIVE",
            trackedEntity = savedItemId.toString()
        )
        val eventExists = roomDao.checkEnrollmentEvent(eventUid)
        Log.e("TAG", "There is an existing Event $eventExists")
        if (eventExists) {
            roomDao.updateEnrollmentEvent(true, eventUid)

        } else {
            child.initialUpload = true
            roomDao.saveEnrollment(child)
        }
        formatter.saveSharedPref("eventUid", eventUid, context)
        formatter.saveSharedPref("enrollmentUid", enrollmentUid, context)
    }

    fun loadTrackedEntities(context: Context, isSynced: Boolean): List<TrackedEntityInstanceData>? {
        return roomDao.loadTrackedEntities(isSynced, true)
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
        val exists = roomDao.checkEvent(data.orgUnit)
        if (exists) {
            roomDao.updateEvent(data.dataValues, data.orgUnit, data.isServerSide, false)
        } else {
            roomDao.saveEvent(data)
        }
    }

    fun saveEventUpdated(data: EventData, id: String) {
        val exists = roomDao.checkEvent(data.orgUnit)
        if (exists) {

            roomDao.updateEvent(data.dataValues, data.orgUnit, data.isServerSide, false)
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
    ): EnrollmentEventData? {
        return roomDao.getLatestEnrollment(trackedEntity)
    }

    fun getLatestEnrollmentByTrackedEntity(
        context: Context,
        trackedEntity: String,
    ): EnrollmentEventData? {
        return roomDao.getLatestEnrollment(trackedEntity)
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

    fun updateFacilityEventSynced(id: String, isSynced: Boolean) {
        roomDao.updateFacilityEventSynced(id, isSynced)
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

    fun updateEnrollment(enrollment: String, uid: String) {
        roomDao.updateEnrollment(enrollment, uid)
    }

    fun updateNotificationEvent(reference: String, uid: String, initialUpload: Boolean) {
        roomDao.updateNotificationEvent(reference, uid, true, initialUpload)
    }

    fun updateTrackedAttributes(attributes: String, patientUid: String) {
        roomDao.updateTrackedAttributes(attributes, patientUid)

    }

    fun loadEventById(id: String): EventData? {
        return roomDao.loadEventById(id)
    }

    fun loadPatientEventById(trackedUnique: String): List<TrackedEntityInstanceData>? {
        return roomDao.loadPatientEventById(trackedUnique)
    }

    fun loadPatientById(trackedUnique: String): TrackedEntityInstanceData? {
        return roomDao.loadPatientById(trackedUnique)
    }

    fun deleteCurrentSimilarCase(patientUid: String) {
        roomDao.deleteTracked(patientUid)
        roomDao.deleteEnrollment(patientUid)

    }

}
