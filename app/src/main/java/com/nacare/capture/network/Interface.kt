package com.nacare.capture.network

import com.nacare.capture.model.EnrollmentEventUploadData
import com.nacare.capture.model.EventUploadData
import com.nacare.capture.model.MultipleTrackedEntityInstances
import com.nacare.capture.model.TrackedEntityInstancePostData
import com.nacare.capture.response.DataStoreResponse
import com.nacare.capture.response.EnrollmentSingle
import com.nacare.capture.response.FacilityEventResponse
import com.nacare.capture.response.FacilityUploadResponse
import com.nacare.capture.response.OrganizationUnitResponse
import com.nacare.capture.response.PatientRegistrationResponse
import com.nacare.capture.response.ProgramResponse
import com.nacare.capture.response.SearchPatientResponse
import com.nacare.capture.response.UserLoginData

import retrofit2.Response
import retrofit2.http.*

interface Interface {
    @GET("/api/programs")
    suspend fun loadProgram(
        @Query("fields") fields: String = "id,name,trackedEntityType,programStages[id,name,programStageSections[id,displayName,dataElements[id,valueType,optionSet[id,displayName,options[id,displayName,code]],displayName,attributeValues[attribute[id,name],value]]]],programSections[name,trackedEntityAttributes[id,valueType,optionSet[id,displayName,options[id,displayName,code]],name,attributeValues[attribute[id,name],value]]]",
        @Query("filter") filter: String? = null
    ): Response<ProgramResponse>

    @GET("/api/me.json?fields=id,username,email,surname,firstName,organisationUnits[name,id]")
    suspend fun signIn(): Response<UserLoginData>

    @GET("/api/trackedEntityInstances.json")
    suspend fun searchPatient(
        @Query("program") program: String,
        @Query("ouMode") ouMode: String = "ALL",
        @Query("fields") fields: String = "trackedEntityInstance,trackedEntityType,attributes[attribute,displayName,value],enrollments[*]",
        @Query("filter") filter: String
    ): Response<SearchPatientResponse>

    @GET("/api/organisationUnits/{code}?fields=name,id,level,children[name,id,level,children[name,id,level,children[name,id,level,children[name,id,level,children]]]]")
    suspend fun loadChildUnits(@Path("code") code: String): Response<OrganizationUnitResponse>


    @POST("/api/trackedEntityInstances?strategy=CREATE_AND_UPDATE")
    @Headers("Content-Type: application/json")
    suspend fun uploadTrackedEntity(
        @Body payload: TrackedEntityInstancePostData
    ): Response<PatientRegistrationResponse>

    @POST("/api/trackedEntityInstances?strategy=CREATE_AND_UPDATE")
    @Headers("Content-Type: application/json")
    suspend fun uploadMultipleTrackedEntity(@Body payload: MultipleTrackedEntityInstances): Response<Any>

    @POST("/api/events")
    @Headers("Content-Type: application/json")
    suspend fun uploadFacilityData(
        @Body payload: EventUploadData
    ): Response<FacilityUploadResponse>

    @GET("api/tracker/events")
    suspend fun loadFacilityEvents(
        @Query("program") program: String,
        @Query("orgUnit") orgUnit: String,
        @Query("paging") paging: Boolean = false,
        @Query("fields") fields: String = "event,program,programStage,orgUnit,status,occurredAt,completedDate,createdAt,dataValues[createdAt,updatedAt,dataElement, value]",
    ): Response<FacilityEventResponse>

    @GET("api/tracker/events")
    suspend fun loadAllFacilityEvents(
        @Query("paging") paging: Boolean = false,
        @Query("fields") fields: String = "event,program,programStage,orgUnit,status,occurredAt,completedDate,createdAt,dataValues[createdAt,updatedAt,dataElement, value]",
    ): Response<FacilityEventResponse>

    @GET("api/dataStore/cancer_sites/sites")
    suspend fun loadAllSites(): Response<List<DataStoreResponse>>

    @GET("api/dataStore/cancer_categories/categories")
    suspend fun loadAllCategories(): Response<List<DataStoreResponse>>

    @POST("/api/trackedEntityInstances?strategy=CREATE_AND_UPDATE")
    @Headers("Content-Type: application/json")
    suspend fun uploadTrackedEntityRetry(@Body payload: TrackedEntityInstancePostData): Response<Any>

    @GET("api/trackedEntityInstances")
    suspend fun getTrackedEntiry(
        @Query("program") program: String,
        @Query("ouMode") ouMode: String = "ALL",
        @Query("fields") fields: String = "trackedEntityInstance,trackedEntityType,enrollments[*]",
        @Query("trackedEntityInstance") reference: String
    ): Response<EnrollmentSingle>

    @POST("/api/events")

    @Headers("Content-Type: application/json")
    suspend fun uploadEnrollmentData(@Body payload: EnrollmentEventUploadData): Response<FacilityUploadResponse>

    @PUT("/api/events/{eventUid}")
    @Headers("Content-Type: application/json")
    suspend fun uploadEnrollmentDataUpdate(
        @Body payload: EnrollmentEventUploadData,
        @Path("eventUid") eventUid: String
    ): Response<FacilityUploadResponse>

}