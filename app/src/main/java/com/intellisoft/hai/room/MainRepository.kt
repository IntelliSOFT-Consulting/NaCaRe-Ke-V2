package com.intellisoft.hai.room

import android.content.Context
import android.util.Log
import com.intellisoft.hai.helper_class.FormatterClass

class MainRepository(private val roomDao: RoomDao) {
  private val formatterClass = FormatterClass()
  fun addPatient(patientData: RegistrationData): Boolean {

    val userId = patientData.patientId
    val exist = roomDao.checkPatientExists(userId)
    Log.e("TAG","addPatient $userId")
    if (!exist) {
      roomDao.addPatient(patientData)
      return true
    }
    return false
  }

  fun getPatients(context: Context): List<RegistrationData>? {
    val userId = formatterClass.getSharedPref("username", context)

      return roomDao.getPatients(userId.toString())

  }

}
