package com.intellisoft.nacare.network_request

import com.intellisoft.nacare.helper_class.OrganizationResponse
import com.intellisoft.nacare.helper_class.ProgramResponse
import retrofit2.Response
import retrofit2.http.GET

interface Interface {
    @GET("/api/40/me.json?fields=id,username,surname,firstName,organisationUnits[name,id]")
    suspend fun loadOrganization(): Response<OrganizationResponse>
    @GET("/api/40/programs?filter=name:ilike:notification&fields=name,id,programTrackedEntityAttributes[id,name,valueType],programStages[id,name,programStageDataElements[dataElement[name,id,valueType,optionSet[options[id,name,code]]]]")
    suspend  fun loadPrograms(): Response<ProgramResponse>

//  @POST("data-entry/response/save")
//  suspend fun submitData(@Body dbSaveDataEntry: DbSaveDataEntry): Response<Any>
//  @PUT("data-entry/response/{id}")
//  suspend fun reSubmitData(
//    @Path("id") id: String,
//    @Body dbSaveDataEntry: DbSaveDataEntry
//  ): Response<Any>
//
//  @GET("national-template/published-indicators") suspend fun getDataEntry(): Response<DbDataEntry>
//  @GET("data-entry/response")
//  suspend fun getResponses(
//      @Query("dataEntryPersonId") dataEntryPersonId: String
//  ): Response<DbSubmissionEntry>
//  @GET("data-entry/response/{id}")
//  suspend fun getResponseDetails(@Path("id") id: String): Response<DbReportDetailsEntry>
//

//  @Multipart
//  @POST("file/upload")
//  suspend fun uploadImageFileData(@Part file: MultipartBody.Part): Response<ImageResponse>
}
