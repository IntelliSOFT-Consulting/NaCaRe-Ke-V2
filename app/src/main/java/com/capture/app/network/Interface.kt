package com.capture.app.network

import com.capture.app.model.EnrollmentEventUploadData
import com.capture.app.model.EventUploadData
import com.capture.app.model.MultipleTrackedEntityInstances
import com.capture.app.model.TrackedEntityInstancePostData
import com.capture.app.response.DataStoreResponse
import com.capture.app.response.EnrollmentSingle
import com.capture.app.response.FacilityEventResponse
import com.capture.app.response.FacilityUploadResponse
import com.capture.app.response.GenderCaseResponse
import com.capture.app.response.OrganizationUnitResponse
import com.capture.app.response.PatientRegistrationResponse
import com.capture.app.response.ProgramResponse
import com.capture.app.response.SearchPatientResponse
import com.capture.app.response.TopographyResponse
import com.capture.app.response.UserLoginData

import retrofit2.Response
import retrofit2.http.*

interface Interface {
    @GET("/api/programs")
    suspend fun loadProgram(
        @Query("fields") fields: String = "id,name,trackedEntityType,programStages[id,name,programStageSections[id,displayName,dataElements[id,valueType,optionSet[id,displayName,options[id,displayName,code]],displayName,attributeValues[attribute[id,name],value]]]],programSections[name,trackedEntityAttributes[id,valueType,optionSet[id,displayName,options[id,displayName,code]],name,attributeValues[attribute[id,name],value]]]",
        @Query("filter") filter: String? = null
    ): Response<ProgramResponse>

    @GET("/api/me.json?fields=id,username,email,surname,firstName,organisationUnits[name,id,level]")
    suspend fun signIn(): Response<UserLoginData>

    @GET("/api/40/optionSets?filter=name:ilike:topography&fields=options[name,code]")
    suspend fun loadTopographies(): Response<TopographyResponse>

    @GET("/api/trackedEntityInstances.json")
    suspend fun searchPatient(
        @Query("program") program: String,
        @Query("ouMode") ouMode: String = "ALL",
        @Query("fields") fields: String = "trackedEntityInstance,trackedEntityType,attributes[attribute,displayName,value],enrollments[*]",
        @Query("filter") filter: String
    ): Response<SearchPatientResponse>

    @GET("/api/trackedEntityInstances.json")
    suspend fun loadTrackedEntities(
        @Query("program") program: String = "pZSnyiO9EF7",
        @Query("ouMode") ouMode: String = "ALL",
        @Query("skipPaging") skipPaging: Boolean = true,
        @Query("fields") fields: String = "trackedEntityInstance,trackedEntityType,attributes[attribute,displayName,value],enrollments[*]"
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

    @PUT("/api/events/{eventUid}")
    @Headers("Content-Type: application/json")
    suspend fun uploadKnownFacilityData(
        @Body payload: EventUploadData,
        @Path("eventUid") eventUid: String
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
        @Query("skipPaging") skipPaging: Boolean = true,
        @Query("program") program: String = "azS0XWMonUV",
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

    @POST("/api/events?strategy=CREATE_AND_UPDATE")
    @Headers("Content-Type: application/json")
    suspend fun uploadEnrollmentData(@Body payload: EnrollmentEventUploadData): Response<FacilityUploadResponse>

    @PUT("/api/events/{eventUid}")
    @Headers("Content-Type: application/json")
    suspend fun uploadEnrollmentDataUpdate(
        @Body payload: EnrollmentEventUploadData,
        @Path("eventUid") eventUid: String
    ): Response<FacilityUploadResponse>

    @GET("/api/40/dataStore/validations/{gender}")
    suspend fun loadCancerByGender(@Path("gender") gender: String): Response<GenderCaseResponse>

}