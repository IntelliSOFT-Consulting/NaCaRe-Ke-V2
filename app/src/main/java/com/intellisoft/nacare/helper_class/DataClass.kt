package com.intellisoft.nacare.helper_class

enum class NavigationValues {
    NAVIGATION,
    HOME,
    SUBMISSION,
    DATA_ENTRY
}

enum class SubmissionsStatus {
    COMPLETED,
    DRAFT,
    SYNCED,
    NOT_SYNCED,
    DUPLICATED
}

enum class PositionStatus {
    CURRENT
}

enum class PinLockStatus {
    INITIAL,
    CONFIRMED,
    LOCK
}

enum class SubmissionQueue {
    INITIATED,
    RESPONSE,
    COMPLETED
}

enum class SettingsQueue {
    SYNC,
    CONFIGURATION,
    RESERVED
}

enum class FileUpload {
    USER,
    INDICATOR,
    SUBMISSION
}

enum class Information {
    ABOUT,
    CONTACT
}

data class ProgramCategory(
    val iconResId: Int?,
    val name: String,
    val id: String,
    val done: String?,
    val total: String?,
    val elements: List<ProgramStageSections>,
    val altElements: List<ProgramStageSections>?,
    val position: String
)

data class FacilityProgramCategory(
    val iconResId: Int?,
    val name: String,
    val id: String,
    val done: String?,
    val total: String?,
    val elements: List<DataElementItem>,
    val position: String
)

data class SettingItem(
    val title: String,
    val innerList: SettingItemChild,
    var expandable: Boolean = false,
    var count: Int,
    var icon: Int,
    val options: List<String>?,
    var selector: Boolean = false,
)

data class SettingItemChild(
    val title: String,
    val subTitle: String,
    val showEdittext: Boolean,
    val buttonName: String,
)

data class EventResponse(
    val page: String,
    val pageSizeval: String,
    val instances: List<InstanceData>
)

data class ProgramEnrollment(
    val trackedEntityInstance: String,
    val orgUnit: String,
    val program: String,
    val enrollmentDate: String,
    val incidentDate: String,

    )

data class PatientEnrollmentResponse(
    val httpStatus: String,
    val httpStatusCode: String,
    val status: String,
    val message: String,
    val response: PatientResponse,
)

data class PatientResponse(
    val responseType: String,
    val status: String,
    val importSummaries: List<ImportSummaries>
)

data class ImportSummaries(
    val status: String,
    val reference: String,
    val href: String,
)

data class EventDataResponse(
    val event: String,
    val status: String,
    val program: String,
    val programStage: String,
    val enrollment: String,
    val orgUnit: String,
    val orgUnitName: String,
    val dataValues: List<DataValueData>

)

data class InstanceData(
    val event: String,
    val status: String,
    val programStage: String,
    val orgUnit: String,
    val occurredAt: String,
    val createdAt: String,
    val updatedAt: String,
    val dataValues: List<DataValueData>
)

data class DataValueData(
    val createdAt: String,
    val updatedAt: String,
    val dataElement: String,
    val value: String,
    val providedElsewhere: Boolean = false
)

data class ProgramResponse(
    val pager: Pager,
    val programs: List<ProgramData>
)

data class FacilityProgramResponse(
    val pager: Pager,
    val programs: List<FacilityProgramData>
)

data class FacilityProgramData(
    val name: String,
    val id: String,
    val programStages: List<ProgramStages>
)

data class PatientPayload(
    val trackedEntityType: String,
    val orgUnit: String,
    val attributes: List<EntityAttributes>,
    val enrollments: List<EntityEnrollments>,
)

data class SearchPatientResponse(
    val trackedEntityInstances: List<TrackedEntityInstances>
)

data class ErrorResponse(
    val httpStatus: String,
    val httpStatusCode: String,
    val status: String,
    val message: String,
)

data class TrackedEntityInstances(
    val trackedEntityType: String,
    val trackedEntityInstance: String,
    val attributes: List<EntityAttributes>,
    val enrollments: List<EntityEnrollments>
)

data class EntityEnrollments(
    val storedBy: String,
    val createdAtClient: String,
    val program: String
)

data class EntityAttributes(
    val displayName: String,
    val attribute: String,
    val value: String
)


data class Pager(
    val page: Int,
    val total: Int,
    val pageSize: Int,
    val pageCount: Int
)

data class ProgramData(
    val id: String,
    val name: String,
    val trackedEntityType: TrackedEntityType,
    val programStages: List<ProgramStages>,
    val programSections: List<ProgramSections>

)

data class TrackedEntityType(
    val id: String,
)

data class ProgramSections(
    val name: String,
    val trackedEntityAttributes: List<TrackedEntityAttributes>,
)

data class TrackedEntityAttributes(
    val valueType: String,
    val id: String,
    val displayName: String,
    val optionSet: OptionSet?
)

data class OrgTreeNode(
    val label: String,
    val code: String,
    val level:String,
    val children: List<OrgTreeNode> = emptyList(),
    var isExpanded: Boolean = false
)


data class ProgramStages(
    val id: String,
    val name: String,
    val programStageSections: List<ProgramStageSections>
)

data class ProgramStageSections(
    val id: String,
    val displayName: String,
    val dataElements: List<DataElementItem>
)

data class DataElementItem(
    val id: String,
    val displayName: String,
    val valueType: String,
    val optionSet: OptionSet?
)

data class CodeValue(
    val id: String,
    val value: String,
)

data class ProgramStageDataElements(
    val dataElement: DataElement
)

data class DataElement(
    val name: String,
    val valueType: String,
    val id: String,
    val optionSet: OptionSet?
)

data class OptionSet(
    val id: String?,
    val displayName: String?,
    val options: List<Options>
)

data class Person(
    val patientId: String,
    val trackedEntityInstance: String,
    val firstName: String,
    val middleName: String,
    val lastName: String,
    val document: String,
    val attribute: List<EntityAttributes>
)

data class Options(
    val code: String,
    val displayName: String,
    val id: String,
)

data class ProgramTrackedEntityAttributes(
    val name: String,
    val valueType: String,
    val id: String
)

data class OrganizationResponse(

    val id: String,
    val username: String,
    val surname: String,
    val firstName: String,
    val organisationUnits: List<OrganisationUnit>
)

data class OrganizationUnitResponse(
    val name: String,
    val id: String,
    val level: String,
    val children: List<CountyUnit>,

)

data class CountyUnit(
    val name: String,
    val id: String,
    val level: String,
    val children: List<CountyUnit>,

)


data class OtherUnit(
    val name: String,
    val id: String
)

data class OrganisationUnit(val id: String, val name: String)

data class DataItems(
    val name: String,
    val elements: List<DataElements>,
)

data class DataElements(
    val code: String,
    val quiz: String,
    val type: String,
    val options: List<String>
)

data class EventPayload(
    val program: String,
    val orgUnit: String,
    val eventDate: String,
    val status: String = "COMPLETED",
    val programStage: String,
    val trackedEntityInstance: String,
    val dataValues: List<PayloadDataValues>
)
data class PayloadDataValues(
    val dataElement: String,
    val value: String,

)

data class MultipleEvents(
    val events: List<EventPayload>
)