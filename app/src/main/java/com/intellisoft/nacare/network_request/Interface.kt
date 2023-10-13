package com.intellisoft.nacare.network_request

import com.intellisoft.nacare.helper_class.EventDataResponse
import com.intellisoft.nacare.helper_class.EventResponse
import com.intellisoft.nacare.helper_class.FacilityProgramResponse
import com.intellisoft.nacare.helper_class.OrganizationResponse
import com.intellisoft.nacare.helper_class.OrganizationUnitResponse
import com.intellisoft.nacare.helper_class.PatientEnrollmentResponse
import com.intellisoft.nacare.helper_class.PatientPayload
import com.intellisoft.nacare.helper_class.ProgramResponse
import com.intellisoft.nacare.helper_class.SearchPatientResponse
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface Interface {
    @GET("/api/40/me.json?fields=id,username,surname,firstName,organisationUnits[name,id]")
    suspend fun loadOrganization(): Response<OrganizationResponse>

    @GET("/api/40/organisationUnits/{code}?fields=name,id,children[name,id,children[name,id,children[name,id,children[name,id,children]]]]")
    suspend fun loadChildUnits(@Path("code") code: String): Response<OrganizationUnitResponse>

    @GET("/api/40/programs")
    suspend fun loadPrograms(
        @Query("fields") fields: String = "id,name,trackedEntityType,programStages[id,name,programStageSections[id,displayName,dataElements[id,displayName,description,compulsory,valueType,optionSet[id,displayName,options[id,displayName,code]]]]],programSections[name,trackedEntityAttributes[description,id,displayName,valueType,optionSet[options[id,displayName,code]]]",
        @Query("filter") filter: String = "name:ilike:notification"
    ): Response<ProgramResponse>

    @GET("/api/40/programs")
    suspend fun loadFacility(
        @Query("fields") fields: String = "id,name,trackedEntityType,programStages[id,name,programStageSections[id,displayName,dataElements[id,displayName,description,compulsory,valueType,optionSet[id,displayName,options[id,displayName,code]]]]],programSections[name,trackedEntityAttributes[description,id,displayName,valueType,optionSet[options[id,displayName,code]]]",
        @Query("filter") filter: String = "name:ilike:facility"

    ): Response<FacilityProgramResponse>

    @GET("/api/40/tracker/events")
    suspend fun loadEvents(
        @Query("program") program: String,
        @Query("paging") paging: Boolean = false,
        @Query("fields") fields: String = "event,programStage,orgUnit,status,occurredAt,completedDate,updatedAt,createdAt,dataValues[createdAt,updatedAt,dataElement,value]"
    ): Response<EventResponse>

    @GET("/api/40/trackedEntityInstances.json")
    suspend fun searchPatient(
        @Query("program") program: String,
        @Query("ouMode") ouMode: String = "ALL",
        @Query("fields") fields: String = "trackedEntityInstance,trackedEntityType,attributes[attribute,displayName,value],enrollments[*]",
        @Query("filter") filter: String
    ): Response<SearchPatientResponse>

    @GET("/api/40/tracker/events/{eventId}")
    suspend fun loadEventData(
        @Path("eventId") eventId: String,
    ): Response<EventDataResponse>

    @POST("/api/40/trackedEntityInstances.json")
    @Headers("Content-Type: application/json")
    suspend fun registerPatient(@Body payload: PatientPayload): Response<PatientEnrollmentResponse>

}
