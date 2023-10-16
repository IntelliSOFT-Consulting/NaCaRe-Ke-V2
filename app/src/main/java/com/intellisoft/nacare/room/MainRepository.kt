package com.intellisoft.nacare.room

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.intellisoft.nacare.helper_class.DataValueData
import com.intellisoft.nacare.helper_class.FormatterClass
import com.intellisoft.nacare.models.Constants.PATIENT_ID
import com.intellisoft.nacare.models.Constants.PATIENT_REGISTRATION

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

    fun loadProgram(context: Context, type: String): ProgramData? {
        val userId = formatterClass.getSharedPref("username", context)
        return if (userId != null) {
            roomDao.loadProgram(type)
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

    fun loadCurrentEvent(context: Context, id: String): EventData? {
        val userId = formatterClass.getSharedPref("username", context)
        return if (userId != null) {
            roomDao.loadCurrentEvent(id)
        } else {
            null

        }
    }

    fun addResponse(context: Context, ev: EventData, element: String, response: String) {
        val userId = formatterClass.getSharedPref("username", context)
        if (userId != null) {
            val event = roomDao.loadCurrentEvent(ev.id.toString())
            if (event != null) {
                val registration = formatterClass.getSharedPref(PATIENT_REGISTRATION, context)
                if (registration == null) {
                    val patient = formatterClass.getSharedPref(PATIENT_ID, context)
                    if (patient != null) {
                        val exists = roomDao.checkResponse(
                            userId,
                            patient.toString(),
                            event.id.toString(),
                            element
                        )
                        if (exists) {
                            roomDao.updateResponse(
                                response,
                                userId,
                                false,
                                patient.toString(),
                                event.id.toString(),
                                element
                            )
                        } else {
                            val res = ElementResponse(
                                eventId = event.id.toString(),
                                userId = userId,
                                indicatorId = element,
                                value = response,
                                patientId = patient.toString()
                            )
                            roomDao.addResponse(res)
                        }
                    }
                } else {
                    val exists =
                        roomDao.checkResponse(userId, event.patientId, event.id.toString(), element)
                    if (exists) {
                        roomDao.updateResponse(
                            response,
                            userId,
                            true,
                            event.patientId,
                            event.id.toString(),
                            element
                        )
                    } else {
                        val res = ElementResponse(
                            eventId = event.id.toString(),
                            userId = userId,
                            indicatorId = element,
                            value = response,
                            patientId = event.patientId,
                            isPatient = true
                        )
                        roomDao.addResponse(res)
                    }
                }
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

    fun getEventResponse(context: Context, event: EventData, code: String): String? {
        val userId = formatterClass.getSharedPref("username", context)
        if (userId != null) {
            return roomDao.getEventResponse(userId, event.patientId, code, event.id.toString())
        }
        return null
    }

    fun addFacilityEventData(context: Context, data: FacilityEventData) {
        val userId = formatterClass.getSharedPref("username", context)
        if (userId != null) {
            data.userId = userId
            val exists = roomDao.checkFacility(data.event)
            if (exists) {
                roomDao.updateFacilityEventData(data.event, data.dataValues)
            } else {
                roomDao.addFacilityEventData(data)
            }
        }
    }

    fun updateEventDataValues(context: Context, event: String, responses: String) {
        val userId = formatterClass.getSharedPref("username", context)
        if (userId != null) {
            val exists = roomDao.checkFacility(event)
            if (exists) {
                roomDao.updateFacilityEventResponseData(event, responses)
            }
        }
    }

    fun loadFacilityEvents(context: Context, code: String): FacilityEventData? {
        val userId = formatterClass.getSharedPref("username", context)
        if (userId != null) {
            return roomDao.loadFacilityEvents(code)
        }
        return null
    }

    fun getFacilityResponse(context: Context, org: String, code: String): String {
        val userId = formatterClass.getSharedPref("username", context)
        if (userId != null) {
            try {
                val event = roomDao.loadFacilityEvents(org)
                if (event != null) {
                    val gson = Gson()
                    val items = gson.fromJson(event.dataValues, Array<DataValueData>::class.java)
                    return items.find { it.dataElement == code }?.value ?: ""
                }
            } catch (e: Exception) {
                return ""
            }
            return ""
        }
        return ""
    }

    fun getAllPatientsData(context: Context): List<ElementResponse>? {
        val userId = formatterClass.getSharedPref("username", context)
        if (userId != null) {
            return roomDao.getAllPatientsData(true, "new")
        }
        return emptyList()
    }

    fun getPatientDetails(context: Context, eventId: String): Boolean {
        val userId = formatterClass.getSharedPref("username", context)
        if (userId != null) {
            return roomDao.getEventPatientDetails(eventId, true)
        }
        return false
    }

    fun competeEvent(context: Context, eventId: String): Boolean {
        val userId = formatterClass.getSharedPref("username", context)
        if (userId != null) {
            val data = roomDao.competeEvent(eventId, true)
            return true
        }
        return false
    }

    fun updatePatientEventResponse(context: Context, eventId: String, reference: String): Boolean {
        val userId = formatterClass.getSharedPref("username", context)
        if (userId != null) {
//            val ev = roomDao.updatePatientToEventResponse(eventId, reference)
            val data = roomDao.updatePatientEventResponse(eventId, reference, true)
            return true
        }
        return false
    }

    fun updateEventWithPatientId(context: Context, eventData: EventData, uuid: String): String {
        val userId = formatterClass.getSharedPref("username", context)
        if (userId != null) {
            val ev = roomDao.loadCurrentEvent(eventData.id.toString())
            if (ev != null) {
                return if (ev.patientId.isBlank()) {
                    val pb = roomDao.updatePatientToEventResponse(eventData.id.toString(), uuid)
                    uuid
                }else{
                    ev.patientId
                }
            }
            return uuid
        }
        return ""
    }

}
