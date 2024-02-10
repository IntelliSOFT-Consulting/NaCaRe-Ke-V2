package com.imeja.nacare_live.response

import com.imeja.nacare_live.model.CountyUnit
import com.imeja.nacare_live.model.Pager
import com.imeja.nacare_live.model.ProgramDetails
import com.imeja.nacare_live.model.TrackedEntityInstances

data class UserLoginData(
    val id: String,
    val username: String,
    val surname: String,
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