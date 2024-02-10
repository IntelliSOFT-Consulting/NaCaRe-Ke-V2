package com.imeja.nacare_live.network

import com.imeja.nacare_live.response.ProgramResponse
import com.imeja.nacare_live.response.SearchPatientResponse
import com.imeja.nacare_live.response.UserLoginData

import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface Interface {
    @GET("/api/programs")
    suspend fun loadProgram(
        @Query("fields") fields: String = "id,name,trackedEntityType,programStages[id,name,programStageSections[id,displayName,dataElements[id,valueType,optionSet[id,displayName,options[id,displayName,code]],displayName,attributeValues[attribute[id,name],value]]]],programSections[name,trackedEntityAttributes[id,valueType,optionSet[id,displayName,options[id,displayName,code]],name,attributeValues[attribute[id,name],value]]]",
        @Query("filter") filter: String? = null
    ): Response<ProgramResponse>

    @GET("/api/me.json?fields=id,username,surname,firstName,organisationUnits[name,id]")
    suspend fun signIn(): Response<UserLoginData>

@GET("/api/40/trackedEntityInstances.json")
suspend fun searchPatient(
    @Query("program") program: String,
    @Query("ouMode") ouMode: String = "ALL",
    @Query("fields") fields: String = "trackedEntityInstance,trackedEntityType,attributes[attribute,displayName,value],enrollments[*]",
    @Query("filter") filter: String
): Response<SearchPatientResponse>

}