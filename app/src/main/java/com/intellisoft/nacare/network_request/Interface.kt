package com.intellisoft.nacare.network_request

import com.intellisoft.nacare.helper_class.OrganizationResponse
import com.intellisoft.nacare.helper_class.OrganizationUnitResponse
import com.intellisoft.nacare.helper_class.ProgramResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface Interface {
    @GET("/api/40/me.json?fields=id,username,surname,firstName,organisationUnits[name,id]")
    suspend fun loadOrganization(): Response<OrganizationResponse>

    @GET("/api/40/organisationUnits/{code}?fields=name,id,children[name,id,children[name,id,children[name,id,children[name,id,children]]]]")
    suspend fun loadChildUnits(@Path("code") code: String): Response<OrganizationUnitResponse>


    @GET("/api/40/programs?fields=id,name,trackedEntityType, programStages[id,name,programStageSections[id,displayName,dataElements[id,displayName,description,compulsory,valueType,optionSet[id,displayName,options[id,displayName,code]]]]],programSections[name,trackedEntityAttributes[description,id,displayName,valueType,optionSet[options[id,displayName, code]]]&filter=name:ilike:notification")
    suspend fun loadPrograms(): Response<ProgramResponse>

}
