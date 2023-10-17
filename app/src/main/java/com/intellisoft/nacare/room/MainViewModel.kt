package com.intellisoft.nacare.room

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.runBlocking

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: MainRepository

    init {
        val roomDao = MainDatabase.getDatabase(application).roomDao()
        repository = MainRepository(roomDao)
    }

    fun addOrganization(context: Context, data: OrganizationData) {
        repository.addOrganization(context, data)
    }

    fun loadOrganizations(context: Context) = runBlocking {
        repository.loadOrganizations(context)
    }

    fun addEvent(context: Context, data: EventData) = runBlocking {
        repository.addEvent(context, data)
    }

    fun loadEvents(context: Context,status: String) = runBlocking {
        repository.loadEvents(context,status)
    }

    fun addProgram(context: Context, data: ProgramData) = runBlocking {
        repository.addProgram(context, data)
    }

    fun loadProgram(context: Context, type: String) = runBlocking {
        repository.loadProgram(context, type)
    }

    fun loadLatestEvent(context: Context) = runBlocking {
        repository.loadLatestEvent(context)
    }

    fun loadCurrentEvent(context: Context, id: String) = runBlocking {
        repository.loadCurrentEvent(context, id)
    }

    fun addResponse(context: Context, event: EventData, element: String, response: String) =
        runBlocking {
            repository.addResponse(context, event, element, response)
        }

    fun getEventResponse(context: Context, event: EventData, element: String) = runBlocking {
        repository.getEventResponse(context, event, element)
    }

    fun deleteResponse(context: Context, event: String, element: String) = runBlocking {
        repository.deleteResponse(context, event, element)
    }

    fun updateChildOrgUnits(context: Context, code: String, children: String) = runBlocking {
        repository.updateChildOrgUnits(context, code, children)
    }

    fun addFacilityEventData(context: Context, data: FacilityEventData) = runBlocking {
        repository.addFacilityEventData(context, data)
    }

    fun updateEventDataValues(context: Context, event: String, responses: String) = runBlocking {
        repository.updateEventDataValues(context, event, responses)
    }

    fun loadFacilityEvents(context: Context, code: String) = runBlocking {
        repository.loadFacilityEvents(context, code)
    }

    fun getFacilityResponse(context: Context, org: String, code: String) = runBlocking {
        repository.getFacilityResponse(context, org, code)
    }

    fun getAllPatientsData(context: Context) = runBlocking {
        repository.getAllPatientsData(context)
    }

    fun getPatientDetails(context: Context, eventData: EventData) = runBlocking {
        repository.getPatientDetails(context, eventData.id.toString())
    }

    fun competeEvent(context: Context, eventData: EventData) = runBlocking {
        repository.competeEvent(context, eventData.id.toString())
    }

    fun updatePatientEventResponse(context: Context, event: String, reference: String) =
        runBlocking {
            repository.updatePatientEventResponse(context, event, reference)
        }

    fun updateEventWithPatientId(context: Context, eventData: EventData, uuid: String) =
        runBlocking {
            repository.updateEventWithPatientId(context, eventData, uuid)
        }

    fun tiePatientToEvent(context: Context, eventData: EventData, trackedEntityInstance: String)=
        runBlocking {
            repository.tiePatientToEvent(context, eventData, trackedEntityInstance)
    }

    fun updateEventData(context: Context, id: String, outputDateString: String) = runBlocking{
        repository.updateEventData(context, id, outputDateString)
    }

    fun resetAllEvents(context: Context)= runBlocking {
        repository.resetAllEvents(context)
    }
}
