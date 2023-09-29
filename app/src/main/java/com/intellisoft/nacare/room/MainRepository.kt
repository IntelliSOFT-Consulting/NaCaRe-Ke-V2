package com.intellisoft.nacare.room

import android.content.Context
import com.intellisoft.nacare.helper_class.FormatterClass

class MainRepository(private val roomDao: RoomDao) {
    private val formatterClass = FormatterClass()
    fun addOrganization(context: Context, data: OrganizationData) {
        val userId = formatterClass.getSharedPref("username", context)
        if (userId != null) {
            //if it exists update
            val exists = roomDao.checkOrganizationExists(data.code)
            if (!exists) {
                roomDao.addOrganization(data)
            } else {
                roomDao.updateOrganization(data.name, data.code)
            }
        }

    }

    fun loadOrganizations(context: Context): List<OrganizationData>? {
        val userId = formatterClass.getSharedPref("username", context)
        if (userId != null) {
            return roomDao.loadOrganizations()
        }
        return emptyList()
    }

    fun addEvent(context: Context, data: EventData) {
        val userId = formatterClass.getSharedPref("username", context)
        if (userId != null) {
            roomDao.addEvent(data)
        }
    }

    fun loadEvents(context: Context): List<EventData>? {
        val userId = formatterClass.getSharedPref("username", context)
        if (userId != null) {
            return roomDao.loadEvents()
        }
        return emptyList()
    }

    fun addProgram(context: Context, data: ProgramData) {
        val userId = formatterClass.getSharedPref("username", context)
        if (userId != null) {
            //if it exists update
            val exists = roomDao.checkProgramExists(data.code)
            if (!exists) {
                roomDao.addProgram(data)
            } else {
                roomDao.updateProgram(
                    data.name,
                    data.programStages,
                    data.programTrackedEntityAttributes,
                    data.code
                )
            }
        }
    }

    fun loadProgram(context: Context): ProgramData? {
        val userId = formatterClass.getSharedPref("username", context)
        return if (userId != null) {
            roomDao.loadProgram()
        } else {
            null

        }
    }

    fun loadLatestEvent(context: Context): EventData? {
        val userId = formatterClass.getSharedPref("username", context)
        return if (userId != null) {
            roomDao.loadLatestEvent()
        } else {
            null

        }
    }

    fun addResponse(context: Context, event: String, element: String, response: String) {
        val userId = formatterClass.getSharedPref("username", context)
        if (userId != null) {
            val exists = roomDao.checkResponse(userId, event, element)
            if (exists) {
                roomDao.updateResponse(response, userId, event, element)
            } else {
                val res = ElementResponse(
                    eventId = event,
                    userId = userId,
                    indicatorId = element,
                    value = response
                )
                roomDao.addResponse(res)
            }
        }

    }

    fun deleteResponse(context: Context, event: String, element: String) {
        val userId = formatterClass.getSharedPref("username", context)
        if (userId != null) {
            roomDao.deleteResponse(userId, event, element)
        }
    }

    fun updateChildOrgUnits(context: Context, code: String, children: String) {
        val userId = formatterClass.getSharedPref("username", context)
        if (userId != null) {
            roomDao.updateChildOrgUnits(code, children)
        }
    }

}
