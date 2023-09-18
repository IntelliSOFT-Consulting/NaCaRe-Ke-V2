package com.intellisoft.hai.room

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.room.Room
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

    fun addNewPatient(data: PatientData): Boolean {
        return repository.addNewPatient(data)
    }

    fun getPatientsData(context: Context) = runBlocking {
        repository.getPatientsData(context)
    }

    fun getCaseDetails(context: Context, caseId: String) = runBlocking {
        repository.getCaseDetails(context, caseId)
    }

    fun loadPeriData(context: Context, caseId: String) = runBlocking {
        repository.loadPeriData(context, caseId)
    }

    fun loadPreparationData(context: Context, patientId: String, caseId: String?) = runBlocking {
        repository.loadPreparationData(context, patientId, caseId.toString())
    }

    fun loadSkinPreparationData(context: Context, patientId: String, caseId: String?) =
        runBlocking {
            repository.loadSkinPreparationData(context, patientId, caseId.toString())
        }

    fun loadHandPreparationData(context: Context, patientId: String, caseId: String?) =
        runBlocking {
            repository.loadHandPreparationData(context, patientId, caseId.toString())
        }

    fun loadPrePostPreparationData(context: Context, patientId: String, caseId: String?) =
        runBlocking {
            repository.loadPrePostPreparationData(context, patientId, caseId.toString())
        }


}
