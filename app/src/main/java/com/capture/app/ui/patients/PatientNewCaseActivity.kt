package com.capture.app.ui.patients

import android.app.Application
import android.app.DatePickerDialog
import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.Html
import android.text.InputType
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.DatePicker
import android.widget.LinearLayout
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import com.capture.app.R
import com.capture.app.data.Constants
import com.capture.app.data.FormatterClass
import com.capture.app.data.Mappings
import com.capture.app.databinding.ActivityPatientNewCaseBinding
import com.capture.app.databinding.ActivityPatientRegistrationBinding
import com.capture.app.model.Attribute
import com.capture.app.model.AttributeValues
import com.capture.app.model.CodeValuePair
import com.capture.app.model.DataElements
import com.capture.app.model.DataValue
import com.capture.app.model.DocumentNumber
import com.capture.app.model.Option
import com.capture.app.model.ParentAttributeValues
import com.capture.app.model.RefinedAttributeValues
import com.capture.app.model.TrackedEntityAttributes
import com.capture.app.model.TrackedEntityInstance
import com.capture.app.model.TrackedEntityInstanceAttributes
import com.capture.app.network.RetrofitCalls
import com.capture.app.room.Converters
import com.capture.app.room.MainViewModel
import com.capture.app.ui.viewmodel.ResponseViewModel
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Calendar
import java.util.Date
import java.util.Locale

class PatientNewCaseActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPatientNewCaseBinding
    private lateinit var viewModel: MainViewModel
    private lateinit var liveData: ResponseViewModel
    private val searchList = ArrayList<TrackedEntityAttributes>()
    private val emptyList = ArrayList<TrackedEntityAttributes>()
    private val completeList = ArrayList<TrackedEntityAttributes>()
    private val allTrackedElements = ArrayList<DataElements>()
    private val attributeValueList = ArrayList<TrackedEntityInstanceAttributes>()
    private var searchParameters = ArrayList<CodeValuePair>()
    private val newCaseResponses = ArrayList<TrackedEntityInstanceAttributes>()
    private val formatter = FormatterClass()
    private var attributeList = ArrayList<ParentAttributeValues>()
    private var requiredFieldsString = ArrayList<String>()
    private lateinit var progressDialog: ProgressDialog
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPatientNewCaseBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = MainViewModel(this.applicationContext as Application)
        liveData = ViewModelProvider(this).get(ResponseViewModel::class.java)
        progressDialog = ProgressDialog(this)
        progressDialog.setMessage("Please wait...") // Set your message
        progressDialog.setCancelable(true)
        val currentPatient = formatter.getSharedPref("current_patient_id", this)
        if (currentPatient != null) {
            populateAvailableData(currentPatient)
        }
        attributeList.clear()
        requiredFieldsString.clear()
        formatter.deleteSharedPref(
            "gender",
            this@PatientNewCaseActivity
        )
        loadSearchParameters()

        binding.apply {
            setSupportActionBar(trackedEntityInstanceSearchToolbar)
            supportActionBar?.apply {
                title = getString(R.string.cancer_notification_tool)
                setDisplayHomeAsUpEnabled(true)
            }
            trackedEntityInstanceSearchToolbar.setNavigationOnClickListener {
                // Handle back arrow click here
                onBackPressed() // Or implement your own logic
            }
            btnSave.apply {
                setOnClickListener {
                    if (allRequiredFieldsComplete()) {
                        formatter.deleteSharedPref("new_case", this@PatientNewCaseActivity)
                        formatter.saveSharedPref(
                            "reload",
                            "true",
                            this@PatientNewCaseActivity
                        )
                        formatter.saveSharedPref(
                            "isRegistration",
                            "true",
                            this@PatientNewCaseActivity
                        )
                        val turnAround = calculateDurationOfTreatment(
                            Constants.TREATMENT_DATE,
                            Constants.DATE_OF_REPORTING, Constants.DIAGNOSIS_TURNAROUND, true
                        )
                        if (turnAround != null) {
                            searchParameters.add(turnAround)
                        }
                        try {
                            val isPatientUnderTreatment =
                                confirmUserResponse(Constants.UNDER_TREATMENT)
                            if (isPatientUnderTreatment.isNotEmpty()) {
                                if (isPatientUnderTreatment == "true") {

                                    val duration = calculateDurationOfTreatment(
                                        Constants.TREATMENT_DATE,
                                        Constants.DATE_OF_REPORTING,
                                        Constants.DURATION_OF_DIAGNOSIS, false
                                    )
                                    if (duration != null) {
                                        searchParameters.add(duration)
                                    }

                                    formatter.saveSharedPref(
                                        "underTreatment",
                                        "true",
                                        this@PatientNewCaseActivity
                                    )
                                } else {
                                    formatter.deleteSharedPref(
                                        "underTreatment",
                                        this@PatientNewCaseActivity
                                    )
                                }
                            }
                            validateSearchData()
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }

                    } else {
                        Toast.makeText(
                            this@PatientNewCaseActivity,
                            "Please enter all required fields",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
            btnCancel.apply {
                setOnClickListener {
                    onBackPressed()
                }
            }
        }

    }

    private fun getDateToday(): String {

        val formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy")

        // Get the current date
        val currentDate = LocalDate.now()

        // Format the current date using the formatter
        return currentDate.format(formatter)
    }

    private fun calculateDurationOfTreatment(
        treatmentDate: String,
        dateOfReporting: String,
        calculatedValue: String, isToday: Boolean
    ): CodeValuePair? {
        try {
            val formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy")
            val startDateResponse =
                if (isToday) getDateToday() else confirmUserResponse(treatmentDate)
            val endDateResponse = confirmUserResponse(dateOfReporting)

            if (startDateResponse.isNotEmpty() && endDateResponse.isNotEmpty()) {
                // Parse the date strings into LocalDate objects using the formatter
                val date1 = LocalDate.parse(startDateResponse, formatter)
                val date2 = LocalDate.parse(endDateResponse, formatter)

                // Calculate the difference in days between the two dates
                val differenceInDays = ChronoUnit.DAYS.between(date2, date1)

                return CodeValuePair(calculatedValue, "$differenceInDays")
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }

        return null

    }

    private fun populateAvailableData(currentPatient: String) {
        CoroutineScope(Dispatchers.IO).launch {
            val data = viewModel.loadTrackedEntity(currentPatient)
            if (data != null) {
                liveData.updatePatientDetails(data.isSubmitted)
                formatter.saveSharedPref(
                    "isSubmitted",
                    "${data.isSubmitted}",
                    this@PatientNewCaseActivity
                )
                formatter.saveSharedPref(
                    "isDead",
                    "${data.isDead}",
                    this@PatientNewCaseActivity
                )
                val attributes = Converters().fromJsonAttribute(data.attributes)
                attributes.forEachIndexed { index, attribute ->
                    if (attribute.attribute == Constants.DATE_OF_BIRTH) {
                        try {
                            if (attribute.value.isNotEmpty()) {
                                val dateFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy")
                                val date = formatter.convertDateFormat(attribute.value)
                                val birthDate = LocalDate.parse(date, dateFormatter)
                                // Get the current date
                                val currentDate = LocalDate.now()
                                val (years, months) = formatter.calculateAge(birthDate, currentDate)
                                saveValued(index, Constants.AGE_YEARS, "$years")
                                saveValued(index, Constants.AGE_MONTHS, "$months")
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                    if (attribute.attribute == Constants.DIAGNOSIS) {
                        try {
                            // load sites
                            val site =
                                viewModel.loadDataStore(this@PatientNewCaseActivity, "site")
                            //load categories
                            val category =
                                viewModel.loadDataStore(this@PatientNewCaseActivity, "category")

                            if (site != null && attribute.value.isNotEmpty()) {
                                val siteValue =
                                    formatter.generateRespectiveValue(site, attribute.value)
                                if (siteValue.isNotEmpty()) {
                                    saveValued(index, Constants.DIAGNOSIS_SITE, siteValue)
                                }
                            }
                            if (category != null && attribute.value.isNotEmpty()) {
                                val categoryValue =
                                    formatter.generateRespectiveValue(category, attribute.value)
                                if (categoryValue.isNotEmpty()) {
                                    saveValued(
                                        index, Constants.DIAGNOSIS_CATEGORY, categoryValue
                                    )
                                }
                            }
                            saveValued(index, Constants.ICD_CODE, attribute.value)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }

                    }
                    saveValued(index, attribute.attribute, attribute.value)
                }

                val eventUid = formatter.getSharedPref("eventUid", this@PatientNewCaseActivity)
                if (eventUid != null) {
                    val dataEnrollment =
                        viewModel.loadEnrollment(this@PatientNewCaseActivity, eventUid)
                    if (dataEnrollment != null) {

                        if (dataEnrollment.dataValues.isNotEmpty()) {
                            val elementAttributes =
                                Converters().fromJsonDataAttribute(dataEnrollment.dataValues)
                            elementAttributes.forEachIndexed { index, attribute ->
                                saveValued(index, attribute.dataElement, attribute.value)
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()

    }

    private fun allRequiredFieldsComplete(): Boolean {
        try {
            searchParameters = getSavedValues()

            val searchParameterCodes = searchParameters.map { it.code }.distinct()
            if (FormatterClass().responsesNonOther(searchParameters)) {
                requiredFieldsString.remove(Constants.OTHER_FACILITY)

            }
            val uniqueRequiredFields = requiredFieldsString.toSet()

            val missingFields =
                uniqueRequiredFields.filter { !searchParameterCodes.contains(it) }

            return if (missingFields.isEmpty()) {
                println("No fields are missing.")
                true
            } else {
                println("Missing fields: $missingFields")
                false
            }

        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }

    }

    private fun confirmUserResponse(id: String): String {
        val foundItem = searchParameters.find { it.code == id }
        return foundItem?.value ?: ""
    }


    private fun loadSearchParameters() {
        val data = viewModel.loadSingleProgram(this, "notification")
        if (data != null) {
            val converters = Converters().fromJson(data.jsonData)
            searchList.clear()
            emptyList.clear()
            allTrackedElements.clear()
            converters.programs.forEach { it ->
                it.programStages.forEach { stage ->
                    stage.programStageSections.forEach {
                        allTrackedElements.addAll(it.dataElements)
                    }

                }
                it.programSections.forEach {

                    if (it.name == "SEARCH PATIENT") {
                        val section = it.trackedEntityAttributes
                        searchList.addAll(section)

                    } else {
                        val filteredSections = it.trackedEntityAttributes.filter { section ->
                            section.id != Constants.OPEN_FOR_EDITING
                        }
                        emptyList.addAll(filteredSections)

                    }
                }
            }
            completeList.addAll(searchList)
            completeList.addAll(emptyList)
            binding.lnParent.removeAllViews()
            binding.lnParent.removeAllViewsInLayout()


            completeList.forEachIndexed { index, item ->
                attributeList.add(
                    ParentAttributeValues(
                        item.name,
                        item.id,
                        item.attributeValues
                    )
                )
                populateSearchFields(
                    index,
                    item,
                    binding.lnParent,
                    extractCurrentValues(item.id)
                )

            }

        }
    }

    private fun extractCurrentValues(id: String): String {
        val response = formatter.getSharedPref("current_data", this)
        if (response != null) {
            searchParameters = getSavedValues()
            val foundItem = searchParameters.find { it.code == id }
            return foundItem?.value ?: ""
        }
        return ""
    }

    private fun getSavedValues(): ArrayList<CodeValuePair> {
        val savedData = formatter.getSharedPref("current_data", this)
        if (savedData != null) {
            return if (savedData.isNotEmpty()) {
                Gson().fromJson(
                    savedData,
                    object : TypeToken<ArrayList<CodeValuePair>>() {}.type
                )
            } else {
                ArrayList()
            }
        }
        return ArrayList()
    }

    private fun generateRequiredField(text: String): String? {
        var data: String? = null
        data = try {
            "$text <font color='red'>*</font>"
        } catch (e: Exception) {
            text
        }
        return data
    }

    private fun hasValidatorAndPasses(
        target: String,
        itemValue: String,
        attributeValues: List<AttributeValues>,
    ): Boolean {
        var status = false
        if (attributeValues.isEmpty()) {
            status = true
        } else {
            val attribute = attributeValues.singleOrNull { it.attribute.name == target }
            if (attribute != null) {
                val parts = attribute.value.split(':')
                val part1 = parts[0]
                val part2 = parts[1]
                var parentValue = extractCurrentValues(part2.trim())
                val parentLabel = attributeList.singleOrNull { it.parent == part2.trim() }
                if (parentLabel != null) {
                    formatter.saveSharedPref(
                        "parent_name",
                        parentLabel.parentName,
                        this@PatientNewCaseActivity
                    )
                }
                if (parentValue.isNotEmpty()) {
                    val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH)
                    val formattedCurrent = dateFormat.parse(itemValue)
                    val formattedParent = dateFormat.parse(parentValue)
                    val result = when (part1) {
                        "eq" -> formattedCurrent == formattedParent
                        "ne" -> formattedCurrent != formattedParent
                        "gt" -> formattedCurrent!! > formattedParent
                        "ge" -> formattedCurrent!! >= formattedParent
                        "lt" -> formattedCurrent!! < formattedParent
                        "le" -> formattedCurrent!! <= formattedParent
                        "like" -> formattedCurrent == formattedParent
                        "null" -> false
                        "notnull" -> true
                        else -> false
                    }
                    status = result

                } else {
                    status = false
                }

            } else {
                status = true
            }
        }

        return status
    }


    private fun extractAttributeValue(
        target: String,
        attributeValues: List<AttributeValues>,
    ): Boolean {
        var status = false
        if (attributeValues.isEmpty()) status = false else {
            for (hey in attributeValues) {
                val data: Attribute = hey.attribute
                if (data.name == target) {
                    status = hey.value == "true"
                }
            }
        }
        return status
    }

    private fun extractValidatorAttributeValue(
        target: String,
        attributeValues: List<AttributeValues>,
    ): Boolean {
        var status = false
        if (attributeValues.isEmpty()) status = false else {
            for (hey in attributeValues) {
                val data: Attribute = hey.attribute
                if (data.name == target) {
                    status = true//hey.value == "true"
                }
            }
        }
        return status
    }

    private fun showIfAttribute(
        target: String,
        attributeValueList: List<AttributeValues>
    ): Boolean {
        var isHidden = false
        if (attributeValueList.isEmpty()) isHidden = false else {
            for (patr in attributeValueList) {
                val data: Attribute = patr.attribute
                if (data.name == target) {
                    isHidden = true
                    break
                }
            }
        }
        return isHidden
    }

    private fun showIfRespondedAttribute(
        attributeValueList: List<AttributeValues>
    ): Boolean {
        var isHidden = false
        if (attributeValueList.isEmpty()) isHidden = false else {
            for (patr in attributeValueList) {
                val data: Attribute = patr.attribute
                if (data.name == "showIf") {
                    val currentValidator = patr.value
                    val parts = currentValidator.split(':')

                    if (parts.size >= 3) {
                        val part1 = parts[0] // this is the attribute to get it's answer
                        val part2 = parts[1] //comparator
                        val part3 = parts[2] // required answer

                        var previousAnswer = extractCurrentValues(part1)
                        if (previousAnswer.isNotEmpty()) {
                            previousAnswer = previousAnswer.lowercase()
                            val part3Lower = parts[2].lowercase()


                            val result = when (part2) {
                                "eq" -> previousAnswer == part3Lower
                                "ne" -> previousAnswer != part3Lower
                                "gt" -> previousAnswer > part3Lower
                                "ge" -> previousAnswer >= part3Lower
                                "lt" -> previousAnswer < part3Lower
                                "le" -> previousAnswer <= part3Lower
                                "null" -> false
                                "notnull" -> true
                                else -> false
                            }
                            isHidden = !result

                        } else {
                            isHidden = true
                            break
                        }


                    } else {
                        println("The input string does not have three parts separated by a colon.")
                        isHidden = true
                        break
                    }
                }
            }
        }
        return isHidden
    }

    private fun isPartOfBasicInformation(uid: String, excludeHiddenFields: List<String>): Boolean {
        return excludeHiddenFields.any { it == uid }
    }

    private fun populateSearchFields(
        index: Int,
        item: TrackedEntityAttributes,
        lnParent: LinearLayout,
        currentValue: String
    ) {
        val valueType: String = item.valueType
        val label: String = item.name
        val inflater = LayoutInflater.from(this)
        val isHidden: Boolean = extractAttributeValue("Hidden", item.attributeValues)
        val isDisabled: Boolean = extractAttributeValue("Disabled", item.attributeValues)
        val isRequired: Boolean = extractAttributeValue("Required", item.attributeValues)
        val hasValidator: Boolean =
            extractValidatorAttributeValue("Validator", item.attributeValues)
        val disableFutureDate: Boolean =
            extractAttributeValue("disableFutureDate", item.attributeValues)
        val showIf = showIfAttribute("showIf", item.attributeValues)

        val basicHiddenFields = isPartOfBasicInformation(item.id, formatter.excludeHiddenFields())
        if (isRequired) {

            requiredFieldsString.add(item.id)
        }
        when (valueType) {
            "TEXT" -> {
                if (item.optionSet == null) {

                    val itemView = inflater.inflate(
                        R.layout.item_edittext,
                        findViewById(R.id.lnParent),
                        false
                    ) as LinearLayout
                    val tvName = itemView.findViewById<TextView>(R.id.tv_name)
                    val tvElement = itemView.findViewById<TextView>(R.id.tv_element)
                    val textInputLayout =
                        itemView.findViewById<TextInputLayout>(R.id.textInputLayout)
                    val editText = itemView.findViewById<TextInputEditText>(R.id.editText)
                    val name = if (isRequired) generateRequiredField(item.name) else item.name
                    tvName.text = Html.fromHtml(name)
                    tvElement.text = item.id
                    itemView.tag = item.id
                    lnParent.addView(itemView)
                    val onlyLetters = formatter.onlyAcceptLetters(item.id)
                    if (onlyLetters) {
                        formatter.setLettersOnly(editText)
                    }

                    if (currentValue.isNotEmpty()) {
                        editText.setText(currentValue)
                    }
                    if (isHidden) {
                        itemView.visibility = View.GONE
                    } else {

                        if (isDisabled) {
                            editText.keyListener = null
                            editText.isCursorVisible = false
                            editText.isFocusable = false
                            editText.isEnabled = false
                            textInputLayout.setBackgroundColor(
                                ContextCompat.getColor(
                                    this,
                                    R.color.greyColor
                                )
                            )
                            textInputLayout.isEnabled = false
                            liveData.mutableListLiveData.observe(this@PatientNewCaseActivity) {
                                val valueObtained = it.find { it.code == item.id }
                                if (valueObtained != null) {
                                    editText.setText(valueObtained.value)
                                }
                            }
                        }
                        if (showIf) {
                            val showNow = showIfRespondedAttribute(item.attributeValues)
                            if (showNow) {
                                itemView.visibility = View.GONE
                            } else {
                                itemView.visibility = View.VISIBLE
                            }
                        }
                    }

                    editText.addTextChangedListener(object : TextWatcher {
                        override fun beforeTextChanged(
                            s: CharSequence?,
                            start: Int,
                            count: Int,
                            after: Int
                        ) {
                        }

                        override fun onTextChanged(
                            s: CharSequence?,
                            start: Int,
                            before: Int,
                            count: Int
                        ) {

                        }

                        override fun afterTextChanged(s: Editable?) {
                            val value = s.toString()
                            if (value.isNotEmpty()) {
                                saveValued(index, item.id, editText.text.toString())
                            }
                        }
                    })
                    if (basicHiddenFields) {
                        itemView.visibility = View.GONE
                    }
                } else {
                    val itemView = inflater.inflate(
                        R.layout.item_autocomplete,
                        findViewById(R.id.lnParent),
                        false
                    ) as LinearLayout
                    val tvName = itemView.findViewById<TextView>(R.id.tv_name)
                    val tvElement = itemView.findViewById<TextView>(R.id.tv_element)
                    val autoCompleteTextView =
                        itemView.findViewById<AutoCompleteTextView>(R.id.autoCompleteTextView)
                    val textInputLayout =
                        itemView.findViewById<TextInputLayout>(R.id.textInputLayout)
                    tvElement.text = item.id
                    val optionsStringList: MutableList<String> = ArrayList()
                    val isAllowedToSearch = formatter.retrieveAllowedToTypeItem(item.id)
                    if (isAllowedToSearch) {
                        autoCompleteTextView.inputType = InputType.TYPE_CLASS_TEXT
                        autoCompleteTextView.setHint("Type here to Search")
                    }
                    item.optionSet.options.forEach {
                        optionsStringList.add(it.displayName)
                    }
                    val adp = ArrayAdapter(
                        this,
                        android.R.layout.simple_list_item_1,
                        optionsStringList
                    )
                    if (currentValue.isNotEmpty()) {
                        val answer =
                            getDisplayNameFromCode(item.optionSet.options, currentValue)
                        autoCompleteTextView.setText(answer, false)
                    }
                    val name = if (isRequired) generateRequiredField(item.name) else item.name
                    tvName.text = Html.fromHtml(name)
                    autoCompleteTextView.setAdapter(adp)
                    adp.notifyDataSetChanged()
                    itemView.tag = item.id
                    lnParent.addView(itemView)
                    if (isHidden) {
                        itemView.visibility = View.GONE
                    } else {
                        if (isDisabled) {
                            autoCompleteTextView.keyListener = null
                            autoCompleteTextView.isCursorVisible = false
                            autoCompleteTextView.isFocusable = false
                            autoCompleteTextView.isEnabled = false
                            autoCompleteTextView.setAdapter(null)
                            textInputLayout.setBackgroundColor(
                                ContextCompat.getColor(
                                    this,
                                    R.color.greyColor
                                )
                            )
                            textInputLayout.isEnabled = false
                            liveData.mutableListLiveData.observe(this@PatientNewCaseActivity) {
                                val valueObtained = it.find { it.code == item.id }
                                if (valueObtained != null) {
                                    autoCompleteTextView.setText(valueObtained.value, false)
                                }
                            }
                        }
                        if (showIf) {
                            val showNow = showIfRespondedAttribute(item.attributeValues)
                            if (showNow) {
                                itemView.visibility = View.GONE

                            } else {
                                itemView.visibility = View.VISIBLE
                            }
                        }

                    }
                    autoCompleteTextView.addTextChangedListener(object : TextWatcher {
                        override fun beforeTextChanged(
                            s: CharSequence?,
                            start: Int,
                            count: Int,
                            after: Int
                        ) {
                        }

                        override fun onTextChanged(
                            s: CharSequence?,
                            start: Int,
                            before: Int,
                            count: Int
                        ) {

                        }

                        override fun afterTextChanged(s: Editable?) {
                            val value = s.toString()
                            if (value.isNotEmpty()) {
                                val dataValue = getCodeFromText(value, item.optionSet.options)
                                if (item.id == Constants.DIAGNOSIS) {
                                    var gender = ""
                                    val genders = searchParameters.find { it.code == Constants.SEX }
                                    if (genders != null) {
                                        gender = genders.value
                                    }

                                    var rejectedCancerList: List<String> = emptyList()
                                    if (gender.isNotEmpty()) {
                                        rejectedCancerList = if (gender == "Male") {
                                            formatter.femaleCancers()
                                        } else if (gender == "Female") {
                                            formatter.maleCancers()
                                        } else {
                                            emptyList()
                                        }
                                    }

                                    try {
                                        val parts = dataValue.split(".")
                                        val firstPart = parts[0]  // "C"
                                        val secondPart = parts[1] // "61"

                                        if (rejectedCancerList.contains(firstPart)) {
                                            val opposite = if (gender == "Male") {
                                                "Female"
                                            } else {
                                                "Male"
                                            }
                                            textInputLayout.error =
                                                "$opposite Diagnosis is not applicable for $gender patient"
                                        } else {
                                            textInputLayout.error = null
                                            calculateRelevant(
                                                lnParent,
                                                index,
                                                item,
                                                value
                                            )
                                            saveValued(index, item.id, dataValue)

                                        }
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                    }
                                } else {
                                    calculateRelevant(lnParent, index, item, value)
                                    saveValued(index, item.id, dataValue)
                                }
                                val list = checkIfParentHasChildren(item.id)
                                for (i in 0 until lnParent.childCount) {
                                    val child: View = lnParent.getChildAt(i)
                                    // Check if any inner data of the list matches the child's tag
                                    val matchFound = list.any { innerData ->
                                        // Replace the condition below with the appropriate comparison between innerData and child's tag
                                        innerData.parent == child.tag
                                    }
                                    if (matchFound) {
                                        val validAnswer =
                                            checkProvidedAnswer(
                                                child.tag.toString(),
                                                list,
                                                dataValue
                                            )
                                        if (validAnswer) {

                                            val attributeValues =
                                                attributeList.find { it.parent == child.tag.toString() }
                                            if (attributeValues != null) {
                                                val isInnerRequired: Boolean =
                                                    extractAttributeValue(
                                                        "Required",
                                                        attributeValues.attributeValues
                                                    )
                                                if (isInnerRequired) {
                                                    requiredFieldsString.add(child.tag.toString())
                                                } else {

                                                    requiredFieldsString.remove(child.tag.toString())
                                                }
                                            }
                                            child.visibility = View.VISIBLE

                                        } else {
                                            child.visibility = View.GONE
                                            requiredFieldsString.remove(child.tag.toString())
                                        }
                                    } else {
                                        // If no match is found, leave the visibility unchanged
                                        if (child.visibility != View.VISIBLE) {
                                            child.visibility = View.GONE
                                        }
                                    }
                                }
                            }
                        }
                    })
                    if (basicHiddenFields) {
                        itemView.visibility = View.GONE
                    }
                }

            }

            "DATE" -> {
                val itemView = inflater.inflate(
                    R.layout.item_edittext_date,
                    findViewById(R.id.lnParent),
                    false
                ) as LinearLayout
                val tvName = itemView.findViewById<TextView>(R.id.tv_name)
                val tvElement = itemView.findViewById<TextView>(R.id.tv_element)
                val textInputLayout =
                    itemView.findViewById<TextInputLayout>(R.id.textInputLayout)
                val editText = itemView.findViewById<TextInputEditText>(R.id.editText)
                val name = if (isRequired) generateRequiredField(item.name) else item.name
                tvName.text = Html.fromHtml(name)
                tvElement.text = item.id
                editText.setKeyListener(null)
                editText.isCursorVisible = false
                editText.isFocusable = false
                if (currentValue.isNotEmpty()) {
                    editText.setText(currentValue)
                }
                itemView.tag = item.id
                lnParent.addView(itemView)
                if (isHidden) {
                    itemView.visibility = View.GONE
                } else {
                    if (isDisabled) {
                        editText.isEnabled = false
                    }
                    if (showIf) {
                        val showNow = showIfRespondedAttribute(item.attributeValues)
                        if (showNow) {
                            itemView.visibility = View.GONE
                        } else {
                            itemView.visibility = View.VISIBLE
                        }
                    }

                }
                editText.setOnClickListener { v ->
                    val calendar: Calendar = Calendar.getInstance()
                    val datePickerDialog = DatePickerDialog(
                        this,
                        { datePicker: DatePicker?, year: Int, month: Int, day: Int ->
                            val valueCurrent: String = getDate(year, month, day)
                            editText.setText(valueCurrent)
                        },
                        calendar.get(Calendar.YEAR),
                        calendar.get(Calendar.MONTH),
                        calendar.get(Calendar.DAY_OF_MONTH)
                    )
                    if (disableFutureDate) {
                        datePickerDialog.datePicker.maxDate = calendar.getTimeInMillis()
                    }
                    datePickerDialog.show()
                }
                editText.addTextChangedListener(object : TextWatcher {
                    override fun beforeTextChanged(
                        s: CharSequence?,
                        start: Int,
                        count: Int,
                        after: Int
                    ) {
                    }

                    override fun onTextChanged(
                        s: CharSequence?,
                        start: Int,
                        before: Int,
                        count: Int
                    ) {

                    }

                    override fun afterTextChanged(s: Editable?) {
                        val value = s.toString()
                        if (value.isNotEmpty()) {
                            //check if it is date of birth, calculate relevant
                            if (hasValidator) {

                                val passes = hasValidatorAndPasses(
                                    "Validator",
                                    value,
                                    item.attributeValues
                                )

                                val parentName = formatter.getSharedPref(
                                    "parent_name",
                                    this@PatientNewCaseActivity
                                )
                                if (passes) {
                                    textInputLayout.error = null
                                    calculateRelevant(lnParent, index, item, value)
                                    saveValued(index, item.id, value)
                                } else {
                                    textInputLayout.error =
                                        "${item.name} cannot come before $parentName"
                                }
                            } else {
                                calculateRelevant(lnParent, index, item, value)
                                saveValued(index, item.id, value)
                            }
                        }
                    }
                })
                if (basicHiddenFields) {
                    itemView.visibility = View.GONE
                }
            }

            "INTEGER" -> {
                val itemView = inflater.inflate(
                    R.layout.item_edittext_number,
                    findViewById(R.id.lnParent),
                    false
                ) as LinearLayout
                val tvName = itemView.findViewById<TextView>(R.id.tv_name)
                val tvElement = itemView.findViewById<TextView>(R.id.tv_element)
                val textInputLayout =
                    itemView.findViewById<TextInputLayout>(R.id.textInputLayout)
                val editText = itemView.findViewById<TextInputEditText>(R.id.editText)
                val name = if (isRequired) generateRequiredField(item.name) else item.name
                tvName.text = Html.fromHtml(name)
                tvElement.text = item.id
                if (currentValue.isNotEmpty()) {
                    editText.setText(currentValue)
                }
                itemView.tag = item.id
                lnParent.addView(itemView)
                if (isHidden) {
                    itemView.visibility = View.GONE
                } else {
                    if (isDisabled) {
                        editText.keyListener = null
                        editText.isCursorVisible = false
                        editText.isFocusable = false
                        editText.isEnabled = false
                        textInputLayout.setBackgroundColor(
                            ContextCompat.getColor(
                                this,
                                R.color.greyColor
                            )
                        )
                        textInputLayout.isEnabled = false
                        liveData.mutableListLiveData.observe(this@PatientNewCaseActivity) {
                            val valueObtained = it.find { it.code == item.id }
                            if (valueObtained != null) {
                                editText.setText(valueObtained.value)
                            }
                        }
                    }
                    if (showIf) {
                        val showNow = showIfRespondedAttribute(item.attributeValues)
                        if (showNow) {
                            itemView.visibility = View.GONE
                        } else {
                            itemView.visibility = View.VISIBLE
                        }
                    }

                }
                editText.addTextChangedListener(object : TextWatcher {
                    override fun beforeTextChanged(
                        s: CharSequence?,
                        start: Int,
                        count: Int,
                        after: Int
                    ) {
                    }

                    override fun onTextChanged(
                        s: CharSequence?,
                        start: Int,
                        before: Int,
                        count: Int
                    ) {
                        val value = s.toString()
                        if (value.isNotEmpty()) {
                            saveValued(index, item.id, value)
                        }
                    }

                    override fun afterTextChanged(s: Editable?) {
                    }
                })
                if (basicHiddenFields) {
                    itemView.visibility = View.GONE
                }
            }

            "NUMBER" -> {
                val itemView = inflater.inflate(
                    R.layout.item_edittext_number,
                    findViewById(R.id.lnParent),
                    false
                ) as LinearLayout
                val tvName = itemView.findViewById<TextView>(R.id.tv_name)
                val tvElement = itemView.findViewById<TextView>(R.id.tv_element)
                val textInputLayout =
                    itemView.findViewById<TextInputLayout>(R.id.textInputLayout)
                val editText = itemView.findViewById<TextInputEditText>(R.id.editText)
                val name = if (isRequired) generateRequiredField(item.name) else item.name
                tvName.text = Html.fromHtml(name)
                tvElement.text = item.id
                if (currentValue.isNotEmpty()) {
                    editText.setText(currentValue)
                }
                itemView.tag = item.id
                lnParent.addView(itemView)
                if (isHidden) {
                    itemView.visibility = View.GONE
                } else {
                    if (isDisabled) {
                        editText.keyListener = null
                        editText.isCursorVisible = false
                        editText.isFocusable = false
                        editText.isEnabled = false
                        textInputLayout.setBackgroundColor(
                            ContextCompat.getColor(
                                this,
                                R.color.greyColor
                            )
                        )
                        textInputLayout.isEnabled = false
                    }
                    if (showIf) {
                        val showNow = showIfRespondedAttribute(item.attributeValues)
                        if (showNow) {
                            itemView.visibility = View.GONE
                        } else {
                            itemView.visibility = View.VISIBLE
                        }
                    }

                }
                editText.addTextChangedListener(object : TextWatcher {
                    override fun beforeTextChanged(
                        s: CharSequence?,
                        start: Int,
                        count: Int,
                        after: Int
                    ) {
                    }

                    override fun onTextChanged(
                        s: CharSequence?,
                        start: Int,
                        before: Int,
                        count: Int
                    ) {
                        val value = s.toString()
                        if (value.isNotEmpty()) {
                            saveValued(index, item.id, value)
                        }
                    }

                    override fun afterTextChanged(s: Editable?) {
                    }
                })
                if (basicHiddenFields) {
                    itemView.visibility = View.GONE
                }
            }

            "PHONE_NUMBER" -> {
                val itemView = inflater.inflate(
                    R.layout.item_edittext_phone,
                    findViewById(R.id.lnParent),
                    false
                ) as LinearLayout
                val tvName = itemView.findViewById<TextView>(R.id.tv_name)
                val tvElement = itemView.findViewById<TextView>(R.id.tv_element)
                val textInputLayout =
                    itemView.findViewById<TextInputLayout>(R.id.textInputLayout)
                val editText = itemView.findViewById<TextInputEditText>(R.id.editText)
                val name = if (isRequired) generateRequiredField(item.name) else item.name
                tvName.text = Html.fromHtml(name)
                tvElement.text = item.id
                if (currentValue.isNotEmpty()) {
                    editText.setText(currentValue)
                }
                itemView.tag = item.id
                lnParent.addView(itemView)
                if (isHidden) {
                    itemView.visibility = View.GONE
                } else {
                    if (isDisabled) {
                        editText.keyListener = null;
                        editText.isCursorVisible = false;
                        editText.isFocusable = false;
                        editText.isEnabled = false;
                    }
                    if (showIf) {
                        val showNow = showIfRespondedAttribute(item.attributeValues)
                        if (showNow) {
                            itemView.visibility = View.GONE
                        } else {
                            itemView.visibility = View.VISIBLE
                        }
                    }

                }
                editText.addTextChangedListener(object : TextWatcher {
                    override fun beforeTextChanged(
                        s: CharSequence?,
                        start: Int,
                        count: Int,
                        after: Int
                    ) {
                    }

                    override fun onTextChanged(
                        s: CharSequence?,
                        start: Int,
                        before: Int,
                        count: Int
                    ) {
                        val value = s.toString()
                        if (value.isNotEmpty()) {
                            saveValued(index, item.id, value)
                        }
                    }

                    override fun afterTextChanged(s: Editable?) {
                    }
                })
                if (basicHiddenFields) {
                    itemView.visibility = View.GONE
                }
            }

            "BOOLEAN" -> {
                val itemView = inflater.inflate(
                    R.layout.item_boolean_field,
                    findViewById(R.id.lnParent),
                    false
                ) as LinearLayout
                val tvName = itemView.findViewById<TextView>(R.id.tv_name)
                val tvElement = itemView.findViewById<TextView>(R.id.tv_element)
                val radioGroup = itemView.findViewById<RadioGroup>(R.id.radioGroup)
                val name = if (isRequired) generateRequiredField(item.name) else item.name
                tvName.text = Html.fromHtml(name)
                tvElement.text = item.id
                itemView.tag = item.id
                lnParent.addView(itemView)
                var isProgrammaticChange = false
                radioGroup.setOnCheckedChangeListener(null)
                when (currentValue) {
                    "true" -> {
                        radioGroup.check(R.id.radioButtonYes);
                    }

                    "false" -> {
                        radioGroup.check(R.id.radioButtonNo);
                    }

                    else -> {
                        radioGroup.clearCheck();
                    }
                }
                radioGroup.setOnCheckedChangeListener { group, checkedId ->
                    if (!isProgrammaticChange && checkedId != -1) {
                        var dataValue: String? = null
                        dataValue = when (checkedId) {
                            R.id.radioButtonYes -> "true"
                            R.id.radioButtonNo -> "false"
                            else -> null
                        }
                        if (dataValue != null) {
                            isProgrammaticChange = true
                            saveValued(index, item.id, dataValue)
                            isProgrammaticChange = false
                            val list = checkIfParentHasChildren(item.id)
                            for (i in 0 until lnParent.childCount) {
                                val child: View = lnParent.getChildAt(i)
                                // Check if any inner data of the list matches the child's tag
                                val matchFound = list.any { innerData ->
                                    // Replace the condition below with the appropriate comparison between innerData and child's tag
                                    innerData.parent == child.tag
                                }
                                if (matchFound) {
                                    val validAnswer =
                                        checkProvidedAnswer(
                                            child.tag.toString(),
                                            list,
                                            dataValue
                                        )
                                    if (validAnswer) {
                                        child.visibility = View.VISIBLE
                                        val attributeValues =
                                            attributeList.find { it.parent == child.tag.toString() }
                                        if (attributeValues != null) {
                                            val isInnerRequired: Boolean =
                                                extractAttributeValue(
                                                    "Required",
                                                    attributeValues.attributeValues
                                                )
                                            if (isInnerRequired) {
                                                requiredFieldsString.add(child.tag.toString())
                                            } else {

                                                requiredFieldsString.remove(child.tag.toString())
                                            }
                                        }
                                    } else {
                                        child.visibility = View.GONE
                                        requiredFieldsString.remove(child.tag.toString())
                                    }
                                } else {
                                    // If no match is found, leave the visibility unchanged
                                    if (child.visibility != View.VISIBLE) {
                                        child.visibility = View.GONE
                                    }
                                }
                            }
                        }
                    }
                }

                if (isHidden) {
                    itemView.visibility = View.GONE
                } else {
                    if (isDisabled) {
                        radioGroup.isEnabled = false
                    }
                    if (showIf) {
                        val showNow = showIfRespondedAttribute(item.attributeValues)
                        if (showNow) {
                            itemView.visibility = View.GONE
                        } else {
                            itemView.visibility = View.VISIBLE
                        }
                    }

                }
                if (basicHiddenFields) {
                    itemView.visibility = View.GONE
                }
            }
        }
    }

    private fun checkProvidedAnswer(
        parent: String,
        list: List<RefinedAttributeValues>,
        dataValue: String
    ): Boolean {
        var resultResponse = false
        try {
            val single = list.singleOrNull { it.parent == parent }

            val lowercaseAnswer = dataValue.lowercase()
            if (single != null) {
                val parts = single.value.split(':')
                if (parts.size == 3) {
                    val part1 = parts[0]
                    val part2 = parts[1]
                    val part3 = parts[2]
                    val part3Lower = parts[2].lowercase()
                    val result = when (part2) {
                        "eq" -> lowercaseAnswer == part3Lower
                        "ne" -> lowercaseAnswer != part3Lower
                        "notin" -> lowercaseAnswer != part3Lower
                        "gt" -> lowercaseAnswer > part3Lower
                        "ge" -> lowercaseAnswer >= part3Lower
                        "lt" -> lowercaseAnswer < part3Lower
                        "le" -> lowercaseAnswer <= part3Lower
                        "like" -> lowercaseAnswer == part3Lower
                        "in" -> lowercaseAnswer == part3Lower
                        "null" -> false
                        "notnull" -> true
                        else -> false
                    }
                    resultResponse = result
                } else {
                    resultResponse = false
                }
            }
        } catch (e: Exception) {
            resultResponse = false
        }

        return resultResponse

    }

    private fun checkIfParentHasChildren(id: String): List<RefinedAttributeValues> {
        val childItem = mutableListOf<RefinedAttributeValues>()
        attributeList.forEach { q ->
            q.attributeValues.forEach {
                if (it.attribute.name == "showIf") {
                    try {
                        val currentValidator = it.value
                        val parts = currentValidator.split(':')
                        if (parts.size == 3) {
                            val part1 = parts[0]
                            if (part1 == id) {
                                childItem.add(
                                    RefinedAttributeValues(
                                        q.parent,
                                        currentValidator
                                    )
                                )
                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }
        return childItem
    }

    private fun getDisplayNameFromCode(options: List<Option>, value: String): String {
        for (option in options) {
            if (option.code == value) {
                return option.displayName
            }
        }
        return value
    }


    private fun calculateRelevant(
        lnParent: LinearLayout,
        index: Int,
        item: TrackedEntityAttributes,
        value: String
    ) {

        when (item.id) {
            Constants.DATE_OF_BIRTH -> {
                val dateFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy")
                val birthDate = LocalDate.parse(value, dateFormatter)
                // Get the current date
                val currentDate = LocalDate.now()
                val (years, months) = formatter.calculateAge(birthDate, currentDate)

                for (i in 0 until lnParent.childCount) {
                    val child: View = lnParent.getChildAt(i)
                    if (child.tag == Constants.AGE_YEARS) {
                        if (child is ViewGroup) {
                            for (j in 0 until child.childCount) {
                                val view: View = child.getChildAt(j)
                                if (view is TextInputEditText) {
                                    view.setText(years)
                                }
                            }
                        }
                    }
                    if (child.tag == Constants.AGE_MONTHS) {
                        if (child is ViewGroup) {
                            for (j in 0 until child.childCount) {
                                val view: View = child.getChildAt(j)
                                if (view is TextInputEditText) {
                                    view.setText(years)
                                }
                            }
                        }
                    }
                }

                saveValued(index, Constants.AGE_YEARS, "$years")
                saveValued(index, Constants.AGE_MONTHS, "$months")
            }

            Constants.DIAGNOSIS -> {
                val dataValue = item.optionSet?.let { getCodeFromText(value, it.options) }
                // load sites
                val site = viewModel.loadDataStore(this, "site")
                //load categories
                val category = viewModel.loadDataStore(this, "category")

                if (site != null && dataValue != null) {
                    val siteValue = formatter.generateRespectiveValue(site, dataValue)

                    if (siteValue.isNotEmpty()) {
                        saveValued(index, Constants.DIAGNOSIS_SITE, siteValue)
                    }
                }
                if (category != null && dataValue != null) {
                    val categoryValue = formatter.generateRespectiveValue(category, dataValue)

                    if (categoryValue.isNotEmpty()) {
                        saveValued(index, Constants.DIAGNOSIS_CATEGORY, categoryValue)
                    }
                }
                saveValued(index, Constants.ICD_CODE, "$dataValue")
            }

            Constants.HISTOLOGY -> {
                val dataValue = item.optionSet?.let { getCodeFromText(value, it.options) }
                saveValued(index, Constants.MORPHOLOGY_CODE, "$dataValue")
            }
        }
    }


    private fun getDate(year: Int, month: Int, day: Int): String {
        val calendar = Calendar.getInstance()
        calendar[year, month] = day
        val date: Date = calendar.time
        return FormatterClass().formatSimpleDate(date)
    }

    private fun getCodeFromText(value: String, options: List<Option>): String {
        //loop though the options, if the value matches the name, returns the code
        for (option in options) {
            if (option.displayName == value) {
                return option.code
            }
        }
        return value
    }

    private fun saveValued(index: Int, id: String, value: String) {
        val existingIndex = searchParameters.indexOfFirst { it.code == id }
        if (existingIndex != -1) {
            // Update the existing entry if the code is found
            searchParameters[existingIndex] = CodeValuePair(code = id, value = value)
        } else {
            // Add a new entry if the code is not found
            val data = CodeValuePair(code = id, value = value)
            searchParameters.add(data)
        }
        formatter.saveSharedPref("current_data", Gson().toJson(searchParameters), this)
        formatter.saveSharedPref("index", "$index", this)
        liveData.populateRelevantData(searchParameters)

    }


    private fun extractDesiredValue(dateString: String, format: String): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
        val data = try {
            val date: Date = dateFormat.parse(dateString)
            val desireFormat = SimpleDateFormat(format)
            desireFormat.format(date)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            dateString
        }
        return data
    }

    private fun validateSearchData() {
        try {
            saveConfirmation()

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun saveConfirmation() {

        val dialogBuilder = AlertDialog.Builder(this)
        val dialogView = layoutInflater.inflate(R.layout.item_submit_cancel, null)
        dialogBuilder.setView(dialogView)

        val tvTitle: TextView = dialogView.findViewById(R.id.tv_title)
        val tvMessage: TextView = dialogView.findViewById(R.id.tv_message)
        val yesButton: MaterialButton = dialogView.findViewById(R.id.yes_button)
        val dialog = dialogBuilder.create()
        val cancelButton: MaterialButton =
            dialogView.findViewById(R.id.no_button)
        cancelButton.apply {
            setOnClickListener { dialog.dismiss() }
        }
        tvMessage.text =
            getString(R.string.are_you_sure_you_wan_to_save_you_will_not_be_able_to_edit_this_patient_info_once_saved)

        yesButton.setOnClickListener {
            dialog.dismiss()
            val patientUid = formatter.getSharedPref(
                "current_patient_id", this@PatientNewCaseActivity
            )
            val orgCode = formatter.getSharedPref("orgCode", this)
            if (orgCode != null) {
                attributeValueList.clear()
                searchParameters.forEach {
                    val attr = TrackedEntityInstanceAttributes(
                        attribute = it.code,
                        value = it.value
                    )
                    attributeValueList.add(attr)
                }
//                val patientIdentification = formatter.getSharedPref(
//                    "patient_identification",
//                    this@PatientNewCaseActivity
//                )
//                Log.e("TAG","patientIdentification***** $patientIdentification")
//                if (patientIdentification != null) {
                val data = TrackedEntityInstance(
                    trackedEntity = formatter.generateUUID(11),
                    enrollment = formatter.generateUUID(11),
                    enrollDate = formatter.formatCurrentDate(Date()),
                    orgUnit = orgCode,
                    attributes = attributeValueList,
                )
                var dataValues = "[]"
                val isPatientUnderTreatment = confirmUserResponse(Constants.UNDER_TREATMENT)
                if (isPatientUnderTreatment.isNotEmpty()) {
                    if (isPatientUnderTreatment == "true") {
                        dataValues = defaultTreatmentData()
                    }
                }
                attributeValueList.clear()
                newCaseResponses.clear()
                searchParameters.forEach {
                    newCaseResponses.add(
                        TrackedEntityInstanceAttributes(
                            attribute = it.code, value = it.value
                        )
                    )

                }

                viewModel.updateTrackedAttributesWithDataValues(
                    Gson().toJson(newCaseResponses), patientUid.toString(), dataValues
                )

                formatter.deleteSharedPref("index", this@PatientNewCaseActivity)
                formatter.saveSharedPref(
                    "is_first_time",
                    "true",
                    this@PatientNewCaseActivity
                )

                CoroutineScope(Dispatchers.Main).launch {
                    progressDialog.show()
                    delay(3000)
                    if (progressDialog.isShowing) {
                        progressDialog.dismiss()
                    }
                    startActivity(
                        Intent(
                            this@PatientNewCaseActivity,
                            PatientResponderActivity::class.java
                        )
                    )
                    this@PatientNewCaseActivity.finish()
                }
//                }
//                else {
//                    Toast.makeText(
//                        this,
//                        "Loading data, please try again to proceed",
//                        Toast.LENGTH_SHORT
//                    ).show()
//                }
            } else {
                Toast.makeText(this, "Please Select Organization", Toast.LENGTH_SHORT).show()
            }

        }
        dialog.show()
    }

    private fun defaultTreatmentData(): String {
        var dataValue = "[]"
        val selectedTreatment = confirmUserResponse(Constants.RECEIVED_TREATMENT)
        val selectedTreatmentDate = confirmUserResponse(Constants.TREATMENT_DATE)

        if (selectedTreatment.isNotEmpty()) {
            val starterDataValues = mutableListOf<DataValue>()
            val isTherapy = Mappings().systemicTherapies().contains(selectedTreatment)
            if (isTherapy) {
                val parent = DataValue(dataElement = Constants.SYSTEMIC_THERAPY, value = "true")
                starterDataValues.add(parent)
            }
            val isRadio = Mappings().radioTherapies().contains(selectedTreatment)
            if (isRadio) {
                val parent = DataValue(dataElement = Constants.RADIO_THERAPY, value = "true")
                starterDataValues.add(parent)
            }
            val data = Mappings().getTreatmentMapping().get(selectedTreatment)
            if (data != null) {
                val treatment = data["treatment"]
                val date = data["date"]
                val value = data["value"]
                val child =
                    DataValue(dataElement = treatment.toString(), value = value.toString())
                val childDate =
                    DataValue(dataElement = date.toString(), value = selectedTreatmentDate)

                //if the data element has parent show true for the parent

                starterDataValues.add(child)
                starterDataValues.add(childDate)

                val parents = confirmParentElements(treatment.toString())
                if (parents.isNotEmpty()) {
                    starterDataValues.addAll(parents)
                }

            }
            dataValue = Gson().toJson(starterDataValues)

        }

        return dataValue
    }

    private fun confirmParentElements(dataElement: String): List<DataValue> {
        val starterDataValues = mutableListOf<DataValue>()

        val parent = allTrackedElements.find { it.id == dataElement }
        if (parent != null) {
            parent.attributeValues.forEach {
                val data: Attribute = it.attribute
                if (data.name == "showIf") {
                    val currentValidator = it.value
                    val parts = currentValidator.split(':')
                    if (parts.size >= 3) {
                        val part1 = parts[0]
                        val childDate =
                            DataValue(dataElement = part1, value = "true")
                        starterDataValues.add(childDate)
                        val parentData = confirmParentResponse(part1)
                        if (parentData.isNotEmpty()) {
                            starterDataValues.addAll(parentData)
                        }
                    }

                }
            }
        }


        return starterDataValues
    }

    private fun confirmParentResponse(dataElement: String): List<DataValue> {
        val starterDataValues = mutableListOf<DataValue>()
        val parent = allTrackedElements.find { it.id == dataElement }
        if (parent != null) {
            parent.attributeValues.forEach {
                val data: Attribute = it.attribute
                if (data.name == "showIf") {
                    val currentValidator = it.value
                    val parts = currentValidator.split(':')
                    if (parts.size >= 3) {
                        val part1 = parts[0]
                        val childDate =
                            DataValue(dataElement = part1, value = "true")
                        starterDataValues.add(childDate)
                        confirmParentResponse(part1)
                    }

                }
            }
        }
        return starterDataValues
    }
}