package com.imeja.nacare_live.room

import android.content.Context
import com.imeja.nacare_live.data.FormatterClass
import com.imeja.nacare_live.model.ProgramDetails

class MainRepository(private val roomDao: RoomDao) {

    private val formatterClass = FormatterClass()

    fun addIndicators(data: ProgramData) {
        val exists = roomDao.checkProgramExist(data.userId)
        if (exists) {
            roomDao.updateProgram(data.jsonData, data.userId)
        } else {
            roomDao.addIndicators(data)
        }

    }

    fun loadPrograms(context: Context): List<ProgramData> {
        return roomDao.loadPrograms()
    }

    fun deletePrograms() {
        return roomDao.deletePrograms()
    }

    fun loadSingleProgram(context: Context, userId: String): ProgramData? {
        return roomDao.loadSingleProgram(userId)

    }

}
