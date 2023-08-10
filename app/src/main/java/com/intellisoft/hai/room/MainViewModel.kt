package com.intellisoft.hai.room

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

    fun addPatient(patientData: RegistrationData): Boolean {
        return repository.addPatient(patientData)
    }

    fun addPreparationData(data: PreparationData): Boolean {
        return repository.addPreparationData(data)
    }

    fun addOutcomeData(data: OutcomeData): Boolean {
        return repository.addOutcomeData(data)
    }

    fun getPatients(context: Context) = runBlocking {
        repository.getPatients(context)
    }

    fun addSurgicalSiteData(data: SurgicalSiteData): Boolean {
        return repository.addSurgicalSiteData(data)
    }

    fun addPeriData(data: PeriData): Boolean {
        return repository.addPeriData(data)
    }

    fun addSkinPreparationData(data: SkinPreparationData): Boolean {
        return repository.addSkinPreparationData(data)
    }

    fun addHandPreparationData(data: HandPreparationData): Boolean {
        return repository.addHandPreparationData(data)
    }

    fun addPrePostOperativeData(data: PrePostOperativeData): Boolean {
        return repository.addPrePostOperativeData(data)
    }

    fun addPostOperativeData(data: PostOperativeData): Boolean {
        return repository.addPostOperativeData(data)
    }


    fun getEncounters(context: Context) = runBlocking {
        repository.getEncounters(context)
    }

    fun getOutcomes(context: Context, encounterId: String) = runBlocking {
        repository.getOutcomes(context, encounterId)
    }


}
