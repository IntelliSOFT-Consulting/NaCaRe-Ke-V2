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

            val single =
                roomDao.checkExistsPatientPreparation(data.userId, data.patientId, data.encounterId)
            if (!single) {
                roomDao.addPreparationData(data)
            } else {
                roomDao.updatePreparationData(
                    userId = data.userId,
                    patientId = data.patientId,
                    encounterId = data.encounterId,
                    pre_bath = data.pre_bath,
                    soap_used = data.soap_used,
                    hair_removal = data.hair_removal,
                    date_of_removal = data.date_of_removal
                )
            }
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
            } else {
                roomDao.updatePeriData(
                    risk = data.risk_factors,
                    measured = data.glucose_measured,
                    level = data.glucose_level,
                    intervention = data.intervention,
                    userId = data.userId,
                    patientId = data.patientId,
                    encounterId = data.encounterId
                )
            }
            return true
        }
        return false
    }

    fun addSkinPreparationData(data: SkinPreparationData): Boolean {

        val userId = data.patientId
        val exist = roomDao.checkPatientExists(userId)

        if (exist) {
            val single = roomDao.checkExistsSkinData(data.userId, data.patientId, data.encounterId)
            if (!single) {
                roomDao.addSkinPreparationData(data)
            } else {
                roomDao.updateSkinPreparationData(
                    userId = data.userId,
                    patientId = data.patientId,
                    encounterId = data.encounterId,
                    chlorhexidine_alcohol = data.chlorhexidine_alcohol,
                    iodine_alcohol = data.iodine_alcohol,
                    chlorhexidine_aq = data.chlorhexidine_aq,
                    iodine_aq = data.iodine_aq,
                    skin_fully_dry = data.skin_fully_dry
                )
            }
            return true
        }
        return false
    }

    fun addHandPreparationData(data: HandPreparationData): Boolean {
        val userId = data.patientId
        val exist = roomDao.checkPatientExists(userId)

        if (exist) {
            val single = roomDao.checkExistsHandData(data.userId, data.patientId, data.encounterId)
            if (!single) {
                roomDao.addHandPreparationData(data)
            } else {
                roomDao.updateHandPreparationData(
                    userId = data.userId,
                    patientId = data.patientId,
                    encounterId = data.encounterId,
                    practitioner = data.practitioner,
                    time_spent = data.time_spent,
                    plain_soap_water = data.plain_soap_water,
                    antimicrobial_soap_water = data.antimicrobial_soap_water,
                    hand_rub = data.hand_rub,
                )
            }
            return true
        }
        return false
    }

    fun addPrePostOperativeData(data: PrePostOperativeData): Boolean {
        val userId = data.patientId
        val exist = roomDao.checkPatientExists(userId)

        if (exist) {
            val single = roomDao.checkExistsPrePostOperativeData(
                data.userId,
                data.patientId,
                data.encounterId
            )
            if (!single) {
                roomDao.addPrePostOperativeData(data)
            } else {
                roomDao.updatePrePostOperativeData(
                    userId = data.userId,
                    patientId = data.patientId,
                    encounterId = data.encounterId,
                    pre_antibiotic_prophylaxis = data.pre_antibiotic_prophylaxis,
                    pre_antibiotic_prophylaxis_other = data.pre_antibiotic_prophylaxis_other,
                    pre_other_antibiotic_given = data.pre_other_antibiotic_given,
                    antibiotics_ceased = data.antibiotics_ceased,
                    post_antibiotic_prophylaxis = data.post_antibiotic_prophylaxis,
                    post_antibiotic_prophylaxis_other = data.post_antibiotic_prophylaxis_other,
                    post_other_antibiotic_given = data.post_other_antibiotic_given,
                    post_reason = data.post_reason,
                    post_reason_other = data.post_reason_other,
                    drain_inserted = data.drain_inserted,
                    drain_location = data.drain_location,
                    drain_antibiotic = data.drain_antibiotic,
                    implant_used = data.implant_used,
                    implant_other = data.implant_other,
                )
            }

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

    fun loadPeriData(context: Context, caseId: String): List<PeriData>? {
        val userId = formatterClass.getSharedPref("username", context)
        return roomDao.loadPeriData(userId.toString(), caseId)
    }

    fun loadPreparationData(
        context: Context,
        patientId: String,
        caseId: String
    ): PreparationData? {
        val userId = formatterClass.getSharedPref("username", context)
        return roomDao.getLatestPreparation(userId.toString(), patientId, caseId)
    }

    fun loadSkinPreparationData(
        context: Context,
        patientId: String,
        caseId: String
    ): SkinPreparationData? {
        val userId = formatterClass.getSharedPref("username", context)
        return roomDao.loadSkinPreparationData(userId.toString(), patientId, caseId)
    }

    fun loadHandPreparationData(
        context: Context,
        patientId: String,
        caseId: String
    ): HandPreparationData? {
        val userId = formatterClass.getSharedPref("username", context)
        return roomDao.loadHandPreparationData(userId.toString(), patientId, caseId)
    }

    fun loadPrePostPreparationData(context: Context, patientId: String, caseId: String):PrePostOperativeData? {
        val userId = formatterClass.getSharedPref("username", context)
        return roomDao.loadPrePostPreparationData(userId.toString(), patientId, caseId)
    }
}
