package com.intellisoft.hai.room

import android.content.Context
import android.util.Log
import com.intellisoft.hai.helper_class.FormatterClass

class MainRepository(private val roomDao: RoomDao) {
  private val formatterClass = FormatterClass()
  fun addPatient(patientData: RegistrationData): Boolean {

    val userId = patientData.patientId
    val exist = roomDao.checkPatientExists(userId)
    Log.e("TAG", "addPatient $userId")
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
        roomDao.addPeriData(data)
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
}
