package com.intellisoft.nacare.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface RoomDao {
    @Query("SELECT EXISTS (SELECT 1 FROM organizations WHERE code =:code)")
    fun checkOrganizationExists(code: String): Boolean

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun addOrganization(data: OrganizationData)

    @Query("UPDATE organizations SET name =:name WHERE code =:code")
    fun updateOrganization(name: String, code: String)

    @Query("SELECT * FROM organizations ORDER BY id DESC")
    fun loadOrganizations(): List<OrganizationData>?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun addEvent(data: EventData)
    @Query("SELECT * FROM events ORDER BY id DESC")
    fun loadEvents(): List<EventData>?

}
