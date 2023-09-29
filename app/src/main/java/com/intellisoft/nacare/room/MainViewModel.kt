package com.intellisoft.nacare.room

import android.app.Application
import android.content.Context
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

    fun loadOrganizations(context: Context)= runBlocking {
        repository.loadOrganizations(context)
    }

    fun addEvent(context: Context, data: EventData)= runBlocking {
        repository.addEvent(context,data)
    }

    fun loadEvents(context: Context)= runBlocking {
        repository.loadEvents(context)
    }

    fun addProgram(context: Context, data: ProgramData)= runBlocking {
        repository.addProgram(context,data)
    }

    fun loadProgram(context: Context)= runBlocking {
        repository.loadProgram(context)
    }

    fun loadLatestEvent(context: Context)= runBlocking {
        repository.loadLatestEvent(context)
    }

    fun addResponse(context: Context, event: String, element: String, response: String)= runBlocking {
        repository.addResponse(context,event,element,response)
    }

    fun deleteResponse(context: Context, event: String, element: String)= runBlocking {
        repository.deleteResponse(context,event,element)
    }

    fun updateChildOrgUnits(context: Context, code: String, children: String)= runBlocking {
        repository.updateChildOrgUnits(context,code,children)
    }

}
