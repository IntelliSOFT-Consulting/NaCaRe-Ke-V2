package com.imeja.nacare_live.room

import android.content.Context
import com.google.gson.Gson
import com.imeja.nacare_live.data.FormatterClass
import com.imeja.nacare_live.model.TrackedEntityInstance

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

    fun createUpdateOrg(context: Context, orgUid: String, json: String) {
        val exists = roomDao.checkOrganizationExist(orgUid)
        if (exists) {
            roomDao.updateOrganization(json, orgUid)
        } else {
            val data = OrganizationData(parentUid = orgUid, jsonData = json)
            roomDao.createOrganization(data)
        }
    }

    fun loadOrganization(context: Context): List<OrganizationData>? {
        return roomDao.loadOrganization()
    }

    fun saveTrackedEntity(context: Context, data: TrackedEntityInstance) {
        val exists = roomDao.checkTrackedEntity(data.orgUnit, data.trackedEntity)
        if (exists) {
            roomDao.updateTrackedEntity(
                data.orgUnit,
                data.trackedEntity,
                Gson().toJson(data.attributes)
            )
        } else {
            val save = TrackedEntityInstanceData(
                trackedEntity = data.trackedEntity,
                orgUnit = data.orgUnit,
                enrollment = data.enrollment,
                enrollDate = data.enrollDate,
                attributes = Gson().toJson(data.attributes)

            )
            roomDao.saveTrackedEntity(save)
        }
    }

    fun loadTrackedEntities(context: Context): List<TrackedEntityInstanceData>? {
        return roomDao.loadTrackedEntities(false)
    }

    fun wipeData(context: Context) {
        roomDao.wipeData()
    }

    fun loadAllTrackedEntities(orgUnit: String): List<TrackedEntityInstanceData>? {
        return roomDao.loadAllTrackedEntities(orgUnit)
    }

    fun saveEvent(data: EventData) {
        val exists = roomDao.checkEvent(data.program, data.orgUnit)
        if (exists) {
            roomDao.updateEvent(data.dataValues, data.program, data.orgUnit)
        } else {
            roomDao.saveEvent(data)
        }
    }

    fun loadEvents(orgUnit: String): List<EventData>? {
        return roomDao.loadEvents(orgUnit)

    }

    fun countEntities(): String {
        val data = roomDao.countEntities()
        return "$data"
    }

    fun loadEvent(uid: String) : EventData? {
        return roomDao.loadEvent(uid)
    }

    fun addDataStore(data: DataStoreData) {
        val exists = roomDao.checkDataStore(data.uid,)
        if (exists) {
            roomDao.updateDataStore(data.dataValues, data.uid)
        } else {
            roomDao.addDataStore(data)
        }
    }

    fun loadDataStore(uid: String):DataStoreData?{
        return roomDao.loadDataStore(uid)
    }

}
