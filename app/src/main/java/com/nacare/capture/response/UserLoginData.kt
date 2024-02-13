package com.nacare.capture.response

import com.nacare.capture.model.CountyUnit
import com.nacare.capture.model.EventInstances
import com.nacare.capture.model.FacilityUpload
import com.nacare.capture.model.Pager
import com.nacare.capture.model.ProgramDetails
import com.nacare.capture.model.RegistrationResponse
import com.nacare.capture.model.TrackedEntityInstances

data class UserLoginData(
    val id: String,
    val username: String,
    val surname: String,
    val email: String,
    val organisationUnits: List<OrgUnits>
)

data class OrgUnits(
    val id: String,
    val name: String
)

data class ProgramResponse(
    val pager: Pager,
    val programs: List<ProgramDetails>
)

data class SearchPatientResponse(
    val trackedEntityInstances: List<TrackedEntityInstances>
)

data class OrganizationUnitResponse(
    val name: String,
    val id: String,
    val level: String,
    val children: List<CountyUnit>,
)

data class FacilityEventResponse(
    val page: String,
    val pageSize: String,
    val instances: List<EventInstances>
)

data class DataStoreResponse(
    val name: String,
    val codes: List<String>
)

data class PatientRegistrationResponse(
    val httpStatus: String,
    val httpStatusCode: String,
    val status: String,
    val message: String,
    val response: RegistrationResponse
)

data class EnrollmentSingle(
    val trackedEntityInstances: List<TrackedEntityInstances>
)

data class FacilityUploadResponse(
    val httpStatus: String,
    val httpStatusCode: String,
    val status: String,
    val message: String,
    val response: FacilityUpload
)