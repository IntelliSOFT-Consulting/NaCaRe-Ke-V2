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


    @GET("/api/40/programs?filter=name:ilike:notification&fields=name,id,programTrackedEntityAttributes[id,name,valueType],programStages[id,name,programStageDataElements[dataElement[name,id,valueType,optionSet[options[id,name,code]]]]")
    suspend fun loadPrograms(): Response<ProgramResponse>

}
