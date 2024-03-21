package com.capture.app.room

import android.content.Context
import android.util.Log
import com.capture.app.data.Constants.DATE_OF_REPORTING
import com.google.gson.Gson
import com.capture.app.data.FormatterClass
import com.capture.app.model.TrackedEntityInstance
import com.capture.app.model.TrackedEntityInstanceServer
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
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

    fun saveTrackedEntityServer(
        context: Context,
        data: TrackedEntityInstanceServer,
        parentOrg: String,
        patientIdentification: String
    ) {
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
                parentOrgUnit = parentOrg,
                enrollment = data.enrollment,
                enrollDate = data.enrollDate,
                isLocal = false,
                isSubmitted = true,
                attributes = Gson().toJson(data.attributes),
                trackedUnique = patientIdentification
            )
            val savedItemId = roomDao.saveTrackedEntity(save)

            val enrollment = EnrollmentEventData(
                dataValues = Gson().toJson(data.dataValues),
                uid = data.enrollmentUid,
                eventUid = data.eventUid,
                program = data.program,
                programStage = data.programStage,
                orgUnit = data.orgUnit,
                eventDate = data.enrollDate,
                status = data.status,
                initialUpload = true,
                trackedEntity = savedItemId.toString()
            )
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

    fun getTrackedEntities(): List<TrackedEntityInstanceData>? {
        return roomDao.getTrackedEntities()
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

    fun loadAllSystemTrackedEntities(): List<TrackedEntityInstanceData>? {
        return roomDao.loadAllSystemTrackedEntities()
    }

    fun saveEvent(data: EventData) {
        val exists = roomDao.checkEvent(data.orgUnit)
        if (exists) {
            val local = roomDao.loadEvent(data.uid)
            if (local != null) {
                if (local.isSynced) {
                    roomDao.updateEvent(
                        data.dataValues,
                        data.orgUnit,
                        data.isServerSide,
                        data.isSynced
                    )
                }
            } else {
                roomDao.updateEvent(data.dataValues, data.orgUnit, data.isServerSide, data.isSynced)
            }
        } else {
            roomDao.saveEvent(data)
        }
    }

    fun saveEventUpdated(context: Context, data: EventData, id: String) {
        val exists = roomDao.checkEvent(data.orgUnit)
        if (exists) {
            roomDao.updateEvent(data.dataValues, data.orgUnit, data.isServerSide, false)
        } else {
            val uid = roomDao.saveEvent(data)
            formatterClass.saveSharedPref("current_event", data.uid, context)
            formatterClass.saveSharedPref("current_event_id", "$uid", context)
        }
    }

    fun loadEvents(orgUnit: String): List<EventData>? {
        return roomDao.loadEvents(orgUnit)

    }

    fun countEntities(level: String?, code: String?): String {
//        val data = roomDao.countAllEntities() ->ideal results
        val data = if (level != "5") {
            val open = roomDao.countByStatusEnrollments("ACTIVE")
            val closed = roomDao.countByStatusEnrollments("COMPLETED")
            open + closed
        } else {
            val open = roomDao.countByStatusEnrollmentsByOrg("ACTIVE", code.toString())
            val closed = roomDao.countByStatusEnrollmentsByOrg("COMPLETED", code.toString())
            open + closed
        }
        return "$data"
    }

    fun countByStatusEnrollments(status: String): String {
        val data = roomDao.countByStatusEnrollments(status)
        return "$data"
    }

    fun countDeceasedEntities(level: String?, code: String?): String {
        var data = 0
        try {
            val allEnrollments =
                if (level != "5") roomDao.getAllTrackedEvents() else roomDao.getAllTrackedEventsByOrg(
                    code.toString()
                )


            if (allEnrollments != null) {
                allEnrollments.forEach { q ->
                    if (q.dataValues.isNotEmpty()) {
                        val converters = Converters().fromJsonDataAttribute(q.dataValues)
                        val counter = converters.count { it.value.contains("Dead") }
                        data += counter

                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return "$data"
    }


    fun countLateNotificationsEntities(level: String?, code: String?): String {
        var data = 0
        try {
            val allEnrollments =
                if (level != "5") roomDao.getTrackedEntities() else roomDao.getTrackedEntitiesByOrg(
                    code.toString()
                )

            if (allEnrollments != null) {
                allEnrollments.forEach { q ->

                    // creation date

                    if (q.attributes.isNotEmpty()) {
                        val attributes = Converters().fromJsonAttribute(q.attributes)
                        val eachReporting = attributes.find { it.attribute == "k5cjujLd0nd" }

                        if (eachReporting != null) {

//                            val event =                                formatterClass.convertDateFormat(eachReporting.value, "yyyy-MM-dd")
//
                            val eventDate = LocalDate.parse(q.enrollDate)
                            val reportingDate = LocalDate.parse(eachReporting.value)
                            val difference = ChronoUnit.DAYS.between(reportingDate, eventDate)
                            val moreThan60Days = difference > 60
                            if (moreThan60Days) {
                                data++
                            }
                        }

                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e("TAG", "Event Date ** Error ${e.message}")
        }
        return "$data"
    }

    fun countSurvivorsEntities(age: Int, level: String?, code: String?): String {
        var data = 0
        try {
            val allEnrollments =
                if (level != "5") roomDao.getTrackedEntities() else roomDao.getTrackedEntitiesByOrg(
                    code.toString()
                )

            if (allEnrollments != null) {
                allEnrollments.forEach { q ->

                    // creation date

                    if (q.attributes.isNotEmpty()) {
                        val attributes = Converters().fromJsonAttribute(q.attributes)
                        val eachReporting = attributes.find { it.attribute == DATE_OF_REPORTING }

                        if (eachReporting != null) {
                            val currentDate = LocalDate.now()
                            val formattedDate = currentDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                            println(formattedDate)
                            val eventDate = LocalDate.parse(formattedDate)
                            val reportingDate = LocalDate.parse(eachReporting.value)
                            val difference = ChronoUnit.YEARS.between(eventDate,reportingDate)

                            val moreThan60Days = difference > age
                            if (moreThan60Days) {
                                data++
                            }
                        }

                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e("TAG", "Event Date ** Error ${e.message}")
        }
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
        roomDao.updateTrackedAttributes(attributes, patientUid, true)

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

    fun deleteEvent(id: Int) {
        roomDao.deleteEvent(id)

    }

    fun getTrackedEntity(id: String) {
        val data = roomDao.loadPatientById(id)
        if (data != null) {
            roomDao.updateDeadPatients(data.trackedUnique, true, false)
        }

    }

}
