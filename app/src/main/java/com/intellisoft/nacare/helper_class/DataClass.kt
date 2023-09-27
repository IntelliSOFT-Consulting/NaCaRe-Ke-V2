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

data class HomeItem(val iconResId: Int, val text: String)

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

data class OrganizationResponse(
    val id: String,
    val username: String,
    val surname: String,
    val firstName: String,
    val organisationUnits: List<OrganisationUnit>
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