package com.imeja.nacare_live.model


data class DbSignIn(
    val username: String,
    val password: String
)

data class ProgramData(
    val pager: Pager,
    val programs: List<ProgramDetails>
)

data class Pager(
    val page: String,
    val total: String,
    val pageSize: String,
    val pageCount: String
)

data class ProgramDetails(
    val id: String,
    val name: String,
    val programStages: List<ProgramStages>,
    val programSections: List<ProgramSections>,
    val trackedEntityType: TrackedEntityType?
)

data class TrackedEntityType(
    val id: String,
)

data class ProgramSections(
    val name: String,
    val trackedEntityAttributes: List<TrackedEntityAttributes>,
)

data class ProgramStages(
    val name: String,
    val id: String,
    val programStageSections: List<ProgramStageSections>
)

data class TrackedEntityAttributes(
    val id: String,
    val name: String,
    val valueType: String,
    val attributeValues: List<AttributeValues>,
    val optionSet: OptionSet?
)

data class OptionSet(
    val options: List<Option>,
)

data class Option(
    val id: String,
    val displayName: String,
    val code: String,
)

data class ProgramStageSections(
    val dataElements: List<DataElements>,
    val id: String,
    val displayName: String,
)

data class DataElements(
    val id: String,
    val displayName: String,
    val valueType: String,
    val optionSet: OptionSet?,
    val attributeValues: List<AttributeValues>,
)

data class AttributeValues(
    val value: String,
    val attribute: Attribute,
)

data class Attribute(
    val id: String,
    val name: String,
)

data class CodeValuePair(val code: String, val value: String)

data class TrackedEntityInstances(
    val trackedEntityType: String,
    val trackedEntityInstance: String,
    val attributes: List<Attributes>,
    val enrollments: List<Enrollments>
)

data class Attributes(
    val displayName: String,
    val attribute: String,
    val value: String,
)

data class Enrollments(
    val program: String,
    val orgUnit: String,
    val enrollment: String,
    val events: List<Events>

)

data class Events(
    val program: String,
    val orgUnit: String,
    val enrollment: String,
    val event: String,
    val programStage: String,
    val status: String,
)

data class SearchResult(
    val trackedEntityInstance: String,
    val enrollmentUid: String,
    val eventUid: String,
    val uniqueId: String,
    val hospitalNo: String,
    val patientName: String,
    val identification: String,
    val diagnosis: String
)

data class CountyUnit(
    val name: String,
    val id: String,
    val level: String,
    val children: List<CountyUnit>,
)

data class OrgTreeNode(
    val label: String,
    val code: String,
    val level: String,
    val children: List<OrgTreeNode> = emptyList(),
    var isExpanded: Boolean = false
)

/****
 * Creating a new tracked Entity
 */
data class TrackedEntityInstance(
    val trackedEntity: String,
    val orgUnit: String,
    val attributes: List<TrackedEntityInstanceAttributes>
)

data class TrackedEntityInstancePostData(
    val trackedEntity: String,
    val orgUnit: String,
    val attributes: List<TrackedEntityInstanceAttributes>,
    val trackedEntityType: String,
    val enrollments: List<EnrollmentPostData>,
)

data class EnrollmentPostData(
    val orgUnit: String,
    val program: String,
    val enrollmentDate: String,
    val incidentDate: String,

)
data class TrackedEntityInstanceAttributes(
    val attribute: String,
    val value: String,
)

data class MultipleTrackedEntityInstances(
    val trackedEntityInstances: List<TrackedEntityInstancePostData>
)