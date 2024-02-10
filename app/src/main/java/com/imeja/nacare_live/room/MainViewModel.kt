package com.imeja.nacare_live.room

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import com.imeja.nacare_live.model.TrackedEntityInstance
import kotlinx.coroutines.isActive
import kotlinx.coroutines.runBlocking
import okhttp3.internal.wait

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

    fun createUpdateOrg(context: Context, orgUid: String, json: String) = runBlocking{
        repository.createUpdateOrg(context, orgUid,json)
    }

    fun loadOrganization(context: Context) = runBlocking{
        repository.loadOrganization(context)
    }
    fun saveTrackedEntity(context: Context, data: TrackedEntityInstance) = runBlocking{

        repository.saveTrackedEntity(context,data)
    }

    fun loadTrackedEntities(context: Context)= runBlocking {
        repository.loadTrackedEntities(context)
    }

    fun wipeData(context: Context) = runBlocking{
        repository.wipeData(context)

    }
}
