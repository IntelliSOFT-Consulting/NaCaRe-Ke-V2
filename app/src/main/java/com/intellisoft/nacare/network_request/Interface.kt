package com.intellisoft.nacare.network_request

import com.intellisoft.nacare.helper_class.OrganizationResponse
import com.intellisoft.nacare.helper_class.OrganizationUnitResponse
import com.intellisoft.nacare.helper_class.ProgramResponse
import com.intellisoft.nacare.helper_class.SearchPatientResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface Interface {
    @GET("/api/40/me.json?fields=id,username,surname,firstName,organisationUnits[name,id]")
    suspend fun loadOrganization(): Response<OrganizationResponse>

    @GET("/api/40/organisationUnits/{code}?fields=name,id,children[name,id,children[name,id,children[name,id,children[name,id,children]]]]")
    suspend fun loadChildUnits(@Path("code") code: String): Response<OrganizationUnitResponse>

    @GET("/api/40/programs?fields=id,name,trackedEntityType, programStages[id,name,programStageSections[id,displayName,dataElements[id,displayName,description,compulsory,valueType,optionSet[id,displayName,options[id,displayName,code]]]]],programSections[name,trackedEntityAttributes[description,id,displayName,valueType,optionSet[options[id,displayName, code]]]&filter=name:ilike:notification")
    suspend fun loadPrograms(): Response<ProgramResponse>


    @GET("/api/40/trackedEntityInstances.json")
    suspend fun searchPatient(
        @Query("program") program: String = "pZSnyiO9EF7",
        @Query("ouMode") ouMode: String = "ALL",
        @Query("fields") fields: String = "trackedEntityInstance,trackedEntityType,attributes[attribute,displayName,value],enrollments[*]",
        @Query("filter") filter: String
    ): Response<SearchPatientResponse>

}
