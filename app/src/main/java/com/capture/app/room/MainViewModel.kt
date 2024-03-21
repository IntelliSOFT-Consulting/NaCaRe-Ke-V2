package com.capture.app.room

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import com.capture.app.model.TrackedEntityInstance
import com.capture.app.model.TrackedEntityInstanceServer
import kotlinx.coroutines.runBlocking

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: MainRepository

    init {
        val roomDao = MainDatabase.getDatabase(application).roomDao()
        repository = MainRepository(roomDao)
    }

    fun addIndicators(data: ProgramData) {

        repository.addIndicators(data)
    }

    fun loadPrograms(context: Context) = runBlocking {
        repository.loadPrograms(context)

    }

    fun deletePrograms() = runBlocking {
        repository.deletePrograms()
    }

    fun loadSingleProgram(context: Context, userId: String) = runBlocking {
        repository.loadSingleProgram(context, userId)
    }

    fun createUpdateOrg(context: Context, orgUid: String, json: String) = runBlocking {
        repository.createUpdateOrg(context, orgUid, json)
    }

    fun loadOrganization(context: Context) = runBlocking {
        repository.loadOrganization(context)
    }

    fun saveTrackedEntity(
        context: Context,
        data: TrackedEntityInstance,
        parentOrg: String,
        patientIdentification: String
    ) = runBlocking {

        repository.saveTrackedEntity(context, data, parentOrg, patientIdentification)
    }

    fun saveTrackedEntityServer(
        context: Context,
        data: TrackedEntityInstanceServer,
        parentOrg: String,
        patientIdentification: String
    ) = runBlocking {

        repository.saveTrackedEntityServer(context, data, parentOrg, patientIdentification)
    }

    fun loadTrackedEntities(context: Context, isSynced: Boolean) = runBlocking {
        repository.loadTrackedEntities(context, isSynced)
    }  fun getTrackedEntities() = runBlocking {
        repository.getTrackedEntities()
    }

    fun loadAllTrackedEntity(context: String) = runBlocking {
        repository.loadAllTrackedEntity(context)
    }

    fun wipeData(context: Context) = runBlocking {
        repository.wipeData(context)

    }

    fun loadAllTrackedEntities(orgUnit: String, context: Context) = runBlocking {
        repository.loadAllTrackedEntities(orgUnit)
    }

    fun loadAllSystemTrackedEntities() = runBlocking {
        repository.loadAllSystemTrackedEntities()
    }

    fun saveEvent(context: Context, data: EventData) = runBlocking {
        repository.saveEvent(data)
    }

    fun saveEventUpdated(context: Context, data: EventData, id: String) = runBlocking {
        repository.saveEventUpdated(context, data, id)
    }

    fun loadEvents(orgUnit: String, context: Context) = runBlocking {
        repository.loadEvents(orgUnit)
    }

    fun countEntities(level: String?, code: String?) = runBlocking {
        repository.countEntities(level, code)
    }

    fun countCompleted(level: String?, code: String?) = runBlocking {
        repository.countByStatusEnrollments(level,code,"COMPLETED")
    }

    fun countOpenEntities(level: String?, code: String?) = runBlocking {
        repository.countByStatusEnrollments(level,code,"ACTIVE")
    }

    fun countDeceasedEntities(level: String?, code: String?) = runBlocking {
        repository.countDeceasedEntities(level,code)
    }

    fun countLateNotificationsEntities(level: String?, code: String?) = runBlocking {
        repository.countLateNotificationsEntities(level, code)
    }

    fun countTwoSurvivorsEntities(level: String?, code: String?) = runBlocking {
        repository.countSurvivorsEntities(2,level, code)
    }

    fun countFiveSurvivorsEntities(level: String?, code: String?) = runBlocking {
        repository.countSurvivorsEntities(5,level, code)
    }

    fun loadEvent(uid: String, requireContext: Context) = runBlocking {
        repository.loadEvent(uid)
    }

    fun addUpdateFacilityEvent(data: EventData) = runBlocking {
        repository.saveEvent(data)
    }

    fun addDataStore(data: DataStoreData) = runBlocking {
        repository.addDataStore(data)
    }

    fun loadDataStore(context: Context, uid: String) = runBlocking {
        repository.loadDataStore(uid)
    }

    fun addProgramStage(context: Context, payload: EnrollmentEventData) = runBlocking {
        repository.addProgramStage(payload)
    }

    fun updateEntity(trackedEntity: String, reference: String) = runBlocking {
        repository.updateEntity(trackedEntity, reference)
    }

    fun updateEnrollmentEntity(trackedEntity: String, reference: String) = runBlocking {
        repository.updateEnrollmentEntity(trackedEntity, reference)
    }

    fun getTrackedEvents(context: Context, isSynced: Boolean) = runBlocking {
        repository.getTrackedEvents(context, isSynced)
    }

    fun getLatestEnrollment(
        context: Context,
        trackedEntity: String
    ) =
        runBlocking {
            repository.getLatestEnrollment(context, trackedEntity)
        }

    fun getLatestEnrollmentByTrackedEntity(
        context: Context,
        trackedEntity: String
    ) =
        runBlocking {
            repository.getLatestEnrollmentByTrackedEntity(context, trackedEntity)
        }

    fun loadLatestEvent(eventUid: String) = runBlocking {
        repository.loadLatestEvent(eventUid)

    }

    fun resetEnrollments(context: Context) = runBlocking {
//repository.resetEnrollments()
    }

    fun updateEnrollmentPerOrgAndProgram(
        entityReference: String,
        enrollment: String,
        programUid: String,
        orgUnit: String
    ) {
        repository.updateEnrollmentPerOrgAndProgram(entityReference, enrollment, orgUnit)
    }

    fun loadAllEvents(context: Context, isSynced: Boolean) = runBlocking {
        repository.loadAllEvents(isSynced)
    }

    fun updateFacilityEvent(id: String, reference: String) = runBlocking {
        repository.updateFacilityEvent(id, reference)
    }

    fun updateFacilityEventSynced(id: String, synced: Boolean) = runBlocking {
        repository.updateFacilityEventSynced(id, synced)
    }

    fun loadTrackedEntity(id: String) = runBlocking {
        repository.loadTrackedEntity(id)
    }

    fun loadEnrollment(context: Context, eventUid: String) = runBlocking {
        repository.loadEnrollment(context, eventUid)
    }

    fun addEnrollmentData(data: EnrollmentEventData) = runBlocking {
        repository.addEnrollmentData(data)
    }

    fun saveTrackedEntityWithEnrollment(
        context: Context,
        data: TrackedEntityInstance,
        enrollment: EnrollmentEventData,
        parentOrg: String,
        patientIdentification: String
    ) =
        runBlocking {
            repository.saveTrackedEntityWithEnrollment(
                context,
                data,
                enrollment,
                parentOrg,
                patientIdentification
            )
        }

    fun getAllFacilityData(context: Context, isSynced: Boolean) = runBlocking {
        repository.loadAllEvents(isSynced)
    }

    fun updateEnrollment(enrollment: String, uid: String) = runBlocking {
        repository.updateEnrollment(enrollment, uid)
    }

    fun updateNotificationEvent(uid: String, reference: String, initialUpload: Boolean) =
        runBlocking {
            repository.updateNotificationEvent(reference, uid, initialUpload)
        }

    fun updateTrackedAttributes(attributes: String, patientUid: String) = runBlocking {
        repository.updateTrackedAttributes(attributes, patientUid)
    }

    fun loadEventById(id: String, context: Context) = runBlocking {
        repository.loadEventById(id)
    }

    fun getPatientExistingCases(context: Context, patientUid: String) =
        runBlocking {
            repository.loadPatientEventById(patientUid)
        }

    fun loadPatientById(context: Context, patientUid: String) = runBlocking {
        repository.loadPatientById(patientUid)
    }

    fun deleteCurrentSimilarCase(context: Context, patientUid: String) = runBlocking {
        repository.deleteCurrentSimilarCase(patientUid)
    }

    fun deleteEvent(id: Int) = runBlocking {
        repository.deleteEvent(id)
    }

    fun getTrackedEntity(id: String) = runBlocking {
        repository.getTrackedEntity(id)
    }


}
