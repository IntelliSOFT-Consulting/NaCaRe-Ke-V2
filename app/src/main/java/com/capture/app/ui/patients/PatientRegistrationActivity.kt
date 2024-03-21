package com.capture.app.ui.patients

import android.app.Application
import android.app.DatePickerDialog
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
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.capture.app.R
import com.capture.app.data.Constants.AGE_MONTHS
import com.capture.app.data.Constants.AGE_YEARS
import com.capture.app.data.Constants.DATE_OF_BIRTH
import com.capture.app.data.Constants.DIAGNOSIS
import com.capture.app.data.Constants.DIAGNOSIS_CATEGORY
import com.capture.app.data.Constants.DIAGNOSIS_SITE
import com.capture.app.data.Constants.ICD_CODE
import com.capture.app.data.Constants.IDENTIFICATION_DOCUMENT
import com.capture.app.data.Constants.IDENTIFICATION_NUMBER
import com.capture.app.data.Constants.OPEN_FOR_EDITING
import com.capture.app.data.Constants.PATIENT_UNIQUE
import com.capture.app.data.Constants.SEX
import com.capture.app.data.Constants.UNDER_TREATMENT
import com.capture.app.data.FormatterClass
import com.capture.app.databinding.ActivityPatientRegistrationBinding
import com.capture.app.model.Attribute
import com.capture.app.model.AttributeValues
import com.capture.app.model.CodeValuePair
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
import org.w3c.dom.Document
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Date
import java.util.Locale


class PatientRegistrationActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPatientRegistrationBinding
    private lateinit var viewModel: MainViewModel
    private lateinit var liveData: ResponseViewModel
    private val searchList = ArrayList<TrackedEntityAttributes>()
    private val emptyList = ArrayList<TrackedEntityAttributes>()
    private val completeList = ArrayList<TrackedEntityAttributes>()
    private val attributeValueList = ArrayList<TrackedEntityInstanceAttributes>()
    private var searchParameters = ArrayList<CodeValuePair>()
    private val retrofitCalls = RetrofitCalls()
    private val formatter = FormatterClass()
    private var attributeList = ArrayList<ParentAttributeValues>()
    private var requiredFieldsString = ArrayList<String>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPatientRegistrationBinding.inflate(layoutInflater)
        setContentView(binding.root)
        viewModel = MainViewModel(this.applicationContext as Application)
        liveData = ViewModelProvider(this).get(ResponseViewModel::class.java)
        attributeList.clear()
        requiredFieldsString.clear()
        formatter.deleteSharedPref(
            "gender",
            this@PatientRegistrationActivity
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
                    generatePatientUniqueId()
                    if (allRequiredFieldsComplete()) {

                        if (noMatchingIdentification()) {
                            formatter.deleteSharedPref("new_case", this@PatientRegistrationActivity)
                            formatter.saveSharedPref(
                                "reload",
                                "true",
                                this@PatientRegistrationActivity
                            )
                            formatter.saveSharedPref(
                                "isRegistration",
                                "true",
                                this@PatientRegistrationActivity
                            )
                            try {
                                val isPatientUnderTreatment = confirmUserResponse(UNDER_TREATMENT)
                                if (isPatientUnderTreatment.isNotEmpty()) {
                                    if (isPatientUnderTreatment == "true") {
                                        formatter.saveSharedPref(
                                            "underTreatment",
                                            "true",
                                            this@PatientRegistrationActivity
                                        )
                                    } else {
                                        formatter.deleteSharedPref(
                                            "underTreatment",
                                            this@PatientRegistrationActivity
                                        )
                                    }
                                }
                                validateSearchData()
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        } else {
                            Toast.makeText(
                                this@PatientRegistrationActivity,
                                "Patient Identification Document Number already exists",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    } else {
                        Toast.makeText(
                            this@PatientRegistrationActivity,
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

    override fun onBackPressed() {
        super.onBackPressed()

    }

    private fun generatePatientUniqueId() {
        var firstname: String = extractCurrentValues("R1vaUuILrDy")
        var lastname: String = extractCurrentValues("hzVijy6tEUF")
        val dob: String = extractCurrentValues("mPpjmOxwsEZ")
        val month: String = extractDesiredValue(dob, "MM")
        val year: String = extractDesiredValue(dob, "yyyy")
        firstname = if (firstname.isNotEmpty() && firstname.length > 3) {
            firstname.substring(0, 3).uppercase(Locale.getDefault())
        } else {
            firstname!!.uppercase(Locale.getDefault())
        }
        lastname = if (lastname.isNotEmpty() && lastname.length > 3) {
            lastname.substring(0, 3).uppercase(Locale.getDefault())
        } else {
            lastname!!.uppercase(Locale.getDefault())
        }
        if (month != null && year != null) {
            val patient_identification = "$firstname-$lastname-$month-$year"
            saveValued(0, "AP13g7NcBOf", patient_identification)
            formatter.saveSharedPref(
                "patient_identification",
                patient_identification,
                this@PatientRegistrationActivity
            )
            val existingIndex = searchParameters.indexOfFirst { it.code == PATIENT_UNIQUE }
            if (existingIndex != -1) {
                // Update the existing entry if the code is found
                searchParameters[existingIndex] =
                    CodeValuePair(code = PATIENT_UNIQUE, value = patient_identification)
            } else {
                // Add a new entry if the code is not found
                val data =
                    CodeValuePair(code = PATIENT_UNIQUE, value = patient_identification)
                searchParameters.add(data)
            }
            formatter.saveSharedPref("current_data", Gson().toJson(searchParameters), this)

        }
    }

    private fun noMatchingIdentification(): Boolean {

        val similarIdentificationDocuments = arrayListOf<DocumentNumber>()
        val similarIdentificationNumbers = arrayListOf<String>()
        try {
            searchParameters = getSavedValues()
            val searchParameterCodes = searchParameters.map { it.code to it.value }.distinct()
            val allTracked = viewModel.loadAllSystemTrackedEntities()
            if (allTracked != null) {
                similarIdentificationDocuments.clear()
                allTracked.forEach {
                    if (it.attributes.isNotEmpty()) {
                        val attributes = Converters().fromJsonAttribute(it.attributes)
                        val existingDocumentType =
                            attributes.find { r -> r.attribute == IDENTIFICATION_DOCUMENT }
                        val existingDocumentNumber =
                            attributes.find { r -> r.attribute == IDENTIFICATION_NUMBER }
                        if (existingDocumentType != null && existingDocumentNumber != null) {
                            similarIdentificationDocuments.add(
                                DocumentNumber(
                                    type = existingDocumentType.value,
                                    number = existingDocumentNumber.value
                                )
                            )
                        }
                    }
                }
                if (similarIdentificationDocuments.isEmpty()) {
                    return true
                } else {
                    //current type
                    val currentType =
                        searchParameterCodes.first { it.first == IDENTIFICATION_DOCUMENT }.second
                    val currentNumber =
                        searchParameterCodes.first { it.first == IDENTIFICATION_NUMBER }.second

                    similarIdentificationNumbers.clear()
                    similarIdentificationDocuments.forEach {
                        if (it.type == currentType) {
                            similarIdentificationNumbers.add(it.number)
                        }
                    }
                    return !similarIdentificationNumbers.contains(currentNumber)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return false

    }

    private fun allRequiredFieldsComplete(): Boolean {
        try {
            searchParameters = getSavedValues()

            val searchParameterCodes = searchParameters.map { it.code }.distinct()
            // Check if all required field codes are present in searchParameterCodes
            val missingFields =
                requiredFieldsString.filter { !searchParameterCodes.contains(it) }

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
            converters.programs.forEach { it ->
                it.programSections.forEach {
                    if (it.name == "SEARCH PATIENT") {
                        val section = it.trackedEntityAttributes
                        searchList.addAll(section)

                    } else {
                        val filteredSections = it.trackedEntityAttributes.filter { section ->
                            section.id != OPEN_FOR_EDITING
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
                        this@PatientRegistrationActivity
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

    private fun confirmAndExtractProvidedAnswer(parent: String): String? {
        val single = searchParameters.singleOrNull { it.code == parent }

        if (single != null) {
            return single.value
        }
        return null
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

                    if (parts.size == 3) {
                        val part1 = parts[0] // this is the attribute to get it's answer
                        val part2 = parts[1] //comparator
                        val part3 = parts[2] // required answer
                        println("Part 1: $part1")
                        println("Part 2: $part2")
                        println("Part 3: $part3")

                        var previousAnswer = extractCurrentValues(part1)
                        if (previousAnswer.isNotEmpty()) {
                            Log.e("TAG", "Show me the Response to Compared $previousAnswer")
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
                            liveData.mutableListLiveData.observe(this@PatientRegistrationActivity) {
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
                            liveData.mutableListLiveData.observe(this@PatientRegistrationActivity) {
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
                                if (item.id == DIAGNOSIS) {
                                    var gender = ""
                                    val genders = searchParameters.find { it.code == SEX }
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
                                    this@PatientRegistrationActivity
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
                        liveData.mutableListLiveData.observe(this@PatientRegistrationActivity) {
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
            DATE_OF_BIRTH -> {
                val dateFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy")
                val birthDate = LocalDate.parse(value, dateFormatter)
                // Get the current date
                val currentDate = LocalDate.now()
                val (years, months) = formatter.calculateAge(birthDate, currentDate)

                for (i in 0 until lnParent.childCount) {
                    val child: View = lnParent.getChildAt(i)
                    if (child.tag == AGE_YEARS) {
                        if (child is ViewGroup) {
                            for (j in 0 until child.childCount) {
                                val view: View = child.getChildAt(j)
                                if (view is TextInputEditText) {
                                    view.setText(years)
                                }
                            }
                        }
                    }
                    if (child.tag == AGE_MONTHS) {
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

                saveValued(index, AGE_YEARS, "$years")
                saveValued(index, AGE_MONTHS, "$months")
            }

            DIAGNOSIS -> {
                val dataValue = item.optionSet?.let { getCodeFromText(value, it.options) }
                // load sites
                val site = viewModel.loadDataStore(this, "site")
                //load categories
                val category = viewModel.loadDataStore(this, "category")

                if (site != null && dataValue != null) {
                    val siteValue = formatter.generateRespectiveValue(site, dataValue)

                    if (siteValue.isNotEmpty()) {
                        saveValued(index, DIAGNOSIS_SITE, siteValue)
                    }
                }
                if (category != null && dataValue != null) {
                    val categoryValue = formatter.generateRespectiveValue(category, dataValue)

                    if (categoryValue.isNotEmpty()) {
                        saveValued(index, DIAGNOSIS_CATEGORY, categoryValue)
                    }
                }
                saveValued(index, ICD_CODE, "$dataValue")
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

        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            Log.e("TAG", "Error Generating the Unique ID ****" + e.message)
        }
    }

    private fun saveConfirmation() {
        val dialogBuilder = AlertDialog.Builder(this)
        val dialogView = layoutInflater.inflate(R.layout.item_submit_cancel, null)
        dialogBuilder.setView(dialogView)

        val tvTitle: TextView = dialogView.findViewById(R.id.tv_title)
        val tvMessage: TextView = dialogView.findViewById(R.id.tv_message)
        val yesButton: MaterialButton = dialogView.findViewById(R.id.yes_button)
        val noButton: MaterialButton = dialogView.findViewById(R.id.no_button)
        val dialog = dialogBuilder.create()
//        tvTitle.text = getString(R.string.search_results)
        tvMessage.text =
            getString(R.string.are_you_sure_you_wan_to_save_you_will_not_be_able_to_edit_this_patient_info_once_saved)

        yesButton.setOnClickListener {
            dialog.dismiss()
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
                val patientIdentification = formatter.getSharedPref(
                    "patient_identification",
                    this@PatientRegistrationActivity
                )
                if (patientIdentification != null) {
                    val data = TrackedEntityInstance(
                        trackedEntity = formatter.generateUUID(11),
                        enrollment = formatter.generateUUID(11),
                        enrollDate = formatter.formatCurrentDate(Date()),
                        orgUnit = orgCode,
                        attributes = attributeValueList,
                    )
                    viewModel.saveTrackedEntity(this, data, data.orgUnit, patientIdentification)
                    formatter.deleteSharedPref("index", this@PatientRegistrationActivity)
                    formatter.saveSharedPref(
                        "is_first_time",
                        "true",
                        this@PatientRegistrationActivity
                    )
                    startActivity(
                        Intent(
                            this@PatientRegistrationActivity,
                            PatientResponderActivity::class.java
                        )
                    )
                    this@PatientRegistrationActivity.finish()
                } else {
                    Toast.makeText(
                        this,
                        "Loading data, please try again to proceed",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } else {
                Toast.makeText(this, "Please Select Organization", Toast.LENGTH_SHORT).show()
            }

        }
        noButton.setOnClickListener {
            dialog.dismiss()

        }
        dialog.show()
    }
}