package com.intellisoft.nacare.helper_class

enum class NavigationValues {
    NAVIGATION,
    HOME,
    SUBMISSION,
    DATA_ENTRY
}

enum class SubmissionsStatus {
    SUBMITTED,
    DRAFT,
    REJECTED,
    PUBLISHED
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
    val elements: List<ProgramStageDataElements>
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

data class ProgramResponse(
    val pager: Pager,
    val programs: List<ProgramData>
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
    val programStages: List<ProgramStages>,
    val programTrackedEntityAttributes: List<ProgramTrackedEntityAttributes>

)

data class ProgramStages(
    val id: String,
    val name: String,
    val programStageDataElements: List<ProgramStageDataElements>
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
    val options: List<Options>
)

data class Options(
    val code: String,
    val name: String,
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
    val children: List<CountyUnit>,
    val id: String
)

data class CountyUnit(
    val name: String,
    val children: List<SubCountyUnit>,
    val id: String
)

data class SubCountyUnit(
    val name: String,
    val children: List<WardUnit>,
    val id: String
)

data class WardUnit(
    val name: String,
    val children: List<FacilityUnit>,
    val id: String
)

data class FacilityUnit(
    val name: String,
    val children: List<OtherUnit>,
    val id: String
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