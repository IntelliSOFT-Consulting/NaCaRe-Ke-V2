package com.intellisoft.hai.room

import android.content.Context
import android.util.Log
import com.intellisoft.hai.helper_class.FormatterClass
import com.intellisoft.hai.util.AppUtils.currentTimestamp

class MainRepository(private val roomDao: RoomDao) {
    private val formatterClass = FormatterClass()
    fun addPatient(patientData: RegistrationData): Boolean {

        val userId = patientData.patientId
        val exist = roomDao.checkPatientExists(userId)
        if (!exist) {
            roomDao.addPatient(patientData)
            return true
        }
        return false
    }

    fun addPreparationData(data: PreparationData): Boolean {

        val userId = data.patientId
        val exist = roomDao.checkPatientExists(userId)

        if (exist) {
            roomDao.addPreparationData(data)
            return true
        }
        return false
    }

    fun getPatients(context: Context): List<RegistrationData>? {
        val userId = formatterClass.getSharedPref("username", context)

        return roomDao.getPatients(userId.toString())
    }

    fun addOutcomeData(data: OutcomeData): Boolean {

        val userId = data.patientId
        val exist = roomDao.checkPatientExists(userId)

        if (exist) {
            roomDao.addOutcomeData(data)
            return true
        }
        return false
    }

    fun addSurgicalSiteData(data: SurgicalSiteData): Boolean {

        val userId = data.patientId
        val exist = roomDao.checkPatientExists(userId)

        if (exist) {
            roomDao.addSurgicalSiteData(data)
            return true
        }
        return false
    }

    fun addPeriData(data: PeriData): Boolean {

        val userId = data.patientId
        val exist = roomDao.checkPatientExists(userId)
        if (exist) {
            val enc = EncounterData(
                userId = userId,
                patientId = data.patientId,
                date = currentTimestamp(),
                type = data.encounterId,
            )
            val single = roomDao.checkExistsPeri(data.userId, data.patientId)
            if (!single) {
                roomDao.addPeriData(data)
                roomDao.addEncounterData(enc)
            }
            return true
        }
        return false
    }

    fun addSkinPreparationData(data: SkinPreparationData): Boolean {

        val userId = data.patientId
        val exist = roomDao.checkPatientExists(userId)

        if (exist) {
            roomDao.addSkinPreparationData(data)
            return true
        }
        return false
    }

    fun addHandPreparationData(data: HandPreparationData): Boolean {
        val userId = data.patientId
        val exist = roomDao.checkPatientExists(userId)

        if (exist) {
            roomDao.addHandPreparationData(data)
            return true
        }
        return false
    }

    fun addPrePostOperativeData(data: PrePostOperativeData): Boolean {
        val userId = data.patientId
        val exist = roomDao.checkPatientExists(userId)

        if (exist) {
            roomDao.addPrePostOperativeData(data)
            return true
        }
        return false
    }

    fun addPostOperativeData(data: PostOperativeData): Boolean {
        val userId = data.patientId
        val exist = roomDao.checkPatientExists(userId)

        if (exist) {
            roomDao.addPostOperativeData(data)
            return true
        }
        return false
    }

    fun getEncounters(context: Context): List<EncounterData>? {
        val userId = formatterClass.getSharedPref("patient", context)
        return roomDao.getEncounters(userId.toString())
    }

    fun getOutcomes(context: Context, encounterId: String): List<OutcomeData>? {
        val userId = formatterClass.getSharedPref("patient", context)
        return roomDao.getOutcomes(userId.toString(), encounterId)
    }

    fun addNewPatient(data: PatientData): Boolean {
        val userId = data.patientId
        val exist = roomDao.checkPatient(userId)
        if (!exist) {
            roomDao.addNewPatient(data)
            return true
        }
        return false
    }


    fun getPatientsData(context: Context): List<PatientData>? {
        val userId = formatterClass.getSharedPref("username", context)

        return roomDao.getPatientsData(userId.toString())
    }

    fun getCaseDetails(context: Context, caseId: String): RegistrationData {
        val userId = formatterClass.getSharedPref("username", context)

        return roomDao.getCaseDetails(userId.toString(), caseId)
    }
}
