package com.nacare.capture.ui.patients

import android.app.Application
import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.Html
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
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.nacare.capture.R
import com.nacare.capture.data.Constants.AGE_MONTHS
import com.nacare.capture.data.Constants.AGE_YEARS
import com.nacare.capture.data.Constants.DATE_OF_BIRTH
import com.nacare.capture.data.Constants.DIAGNOSIS
import com.nacare.capture.data.Constants.DIAGNOSIS_CATEGORY
import com.nacare.capture.data.Constants.DIAGNOSIS_SITE
import com.nacare.capture.data.Constants.ICD_CODE
import com.nacare.capture.data.FormatterClass
import com.nacare.capture.databinding.ActivityPatientRegistrationBinding
import com.nacare.capture.model.Attribute
import com.nacare.capture.model.AttributeValues
import com.nacare.capture.model.CodeValuePair
import com.nacare.capture.model.Option
import com.nacare.capture.model.ParentAttributeValues
import com.nacare.capture.model.RefinedAttributeValues
import com.nacare.capture.model.TrackedEntityAttributes
import com.nacare.capture.model.TrackedEntityInstance
import com.nacare.capture.model.TrackedEntityInstanceAttributes
import com.nacare.capture.network.RetrofitCalls
import com.nacare.capture.response.DataStoreResponse
import com.nacare.capture.room.Converters
import com.nacare.capture.room.DataStoreData
import com.nacare.capture.room.MainViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.Period
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Date
import java.util.Locale


class PatientRegistrationActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPatientRegistrationBinding
    private lateinit var viewModel: MainViewModel
    private val searchList = ArrayList<TrackedEntityAttributes>()
    private val emptyList = ArrayList<TrackedEntityAttributes>()
    private val completeList = ArrayList<TrackedEntityAttributes>()
    private val attributeValueList = ArrayList<TrackedEntityInstanceAttributes>()
    private var searchParameters = ArrayList<CodeValuePair>()
    private val retrofitCalls = RetrofitCalls()
    private val formatter = FormatterClass()
    private var attributeList = ArrayList<ParentAttributeValues>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPatientRegistrationBinding.inflate(layoutInflater)
        setContentView(binding.root)
        viewModel = MainViewModel(this.applicationContext as Application)
        attributeList.clear()
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
                    formatter.saveSharedPref("reload", "true", this@PatientRegistrationActivity)
                    validateSearchData()
                }
            }
            btnCancel.apply {
                setOnClickListener {
                    onBackPressed()
                }
            }
        }

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
                        val section = it.trackedEntityAttributes
                        emptyList.addAll(section)

                    }
                }
            }
            completeList.addAll(searchList)
            completeList.addAll(emptyList)
            binding.lnParent.removeAllViews()
            binding.lnParent.removeAllViewsInLayout()
            completeList.forEachIndexed { index, item ->
                attributeList.add(ParentAttributeValues(item.id, item.attributeValues))
                populateSearchFields(index, item, binding.lnParent, extractCurrentValues(item.id))

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
                Gson().fromJson(savedData, object : TypeToken<ArrayList<CodeValuePair>>() {}.type)
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
                    Log.e("TAG", "Show me the Response to Compared ${patr.value}")
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

                            Log.e(
                                "TAG",
                                "Show me the Response to Compared Answer Above $previousAnswer Needed $part3Lower"
                            )
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
        val disableFutureDate: Boolean =
            extractAttributeValue("disableFutureDate", item.attributeValues)
        val showIf = showIfAttribute("showIf", item.attributeValues)
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

                    if (currentValue.isNotEmpty()) {
                        editText.setText(currentValue)
                    }
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
                    editText.onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
                        if (!hasFocus) {                            // Save the data when the EditText loses focus
                            saveValued(index, item.id, editText.text.toString())
                        }
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
                    tvElement.text = item.id
                    val optionsStringList: MutableList<String> = ArrayList()
                    item.optionSet.options.forEach {
                        optionsStringList.add(it.displayName)
                    }
                    val adp = ArrayAdapter(
                        this,
                        android.R.layout.simple_list_item_1,
                        optionsStringList
                    )
                    if (currentValue.isNotEmpty()) {
                        val answer = getDisplayNameFromCode(item.optionSet.options, currentValue)
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
                                calculateRelevant(lnParent, index, item, value)
                                saveValued(index, item.id, dataValue)
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
                                        } else {
                                            child.visibility = View.GONE
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
                            calculateRelevant(lnParent, index, item, value)
                            saveValued(index, item.id, value)
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

            "PHONE_NUMBER" -> {
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
                                        checkProvidedAnswer(child.tag.toString(), list, dataValue)
                                    if (validAnswer) {
                                        child.visibility = View.VISIBLE
                                    } else {
                                        child.visibility = View.GONE
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
            Log.e(
                "TAG",
                "We are looking the answer here **** $parent value need is $dataValue $single"
            )
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
                        "gt" -> lowercaseAnswer > part3Lower
                        "ge" -> lowercaseAnswer >= part3Lower
                        "lt" -> lowercaseAnswer < part3Lower
                        "le" -> lowercaseAnswer <= part3Lower
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
                    Log.e("TAG", "Available Attribute **** ${it.attribute} ${it.value}")
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
                val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
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

                Log.e("TAG", "Age: $years years and $months months")
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
                    Log.e("TAG", "Match found: $siteValue")
                    if (siteValue.isNotEmpty()) {
                        saveValued(index, DIAGNOSIS_SITE, siteValue)
                    }
                }
                if (category != null && dataValue != null) {
                    val categoryValue = formatter.generateRespectiveValue(category, dataValue)
                    Log.e("TAG", "Match found: $categoryValue")
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
        return FormatterClass().formatCurrentDate(date)
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
        Log.e("TAG", "Growing List $searchParameters")

    }

    private fun reloadActivity() {
        CoroutineScope(Dispatchers.Main).launch {
            delay(100)
            val intent =
                Intent(
                    this@PatientRegistrationActivity,
                    PatientRegistrationActivity::class.java
                )
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP //or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
            finish()
            overridePendingTransition(0, 0) // Disable transition animation
        }
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
            var firstname: String = extractCurrentValues("R1vaUuILrDy")
            var lastname: String = extractCurrentValues("hzVijy6tEUF")
            val dob: String = extractCurrentValues("mPpjmOxwsEZ")
            val month: String = extractDesiredValue(dob, "MM")
            val year: String = extractDesiredValue(dob, "yyyy")
            firstname = if (firstname != null && firstname.length > 3) {
                firstname.substring(0, 3).uppercase(Locale.getDefault())
            } else {
                firstname!!.uppercase(Locale.getDefault())
            }
            lastname = if (lastname != null && lastname.length > 3) {
                lastname.substring(0, 3).uppercase(Locale.getDefault())
            } else {
                lastname!!.uppercase(Locale.getDefault())
            }
            if (month != null && year != null) {
                val patient_identification = "$firstname-$lastname-$month-$year"
                saveValued(0, "AP13g7NcBOf", patient_identification)
                val existingIndex = searchParameters.indexOfFirst { it.code == "AP13g7NcBOf" }
                if (existingIndex != -1) {
                    // Update the existing entry if the code is found
                    searchParameters[existingIndex] =
                        CodeValuePair(code = "AP13g7NcBOf", value = patient_identification)
                } else {
                    // Add a new entry if the code is not found
                    val data =
                        CodeValuePair(code = "AP13g7NcBOf", value = patient_identification)
                    searchParameters.add(data)
                }
                formatter.saveSharedPref("current_data", Gson().toJson(searchParameters), this)
                Log.e("TAG", "Growing List $searchParameters")
                saveConfirmation()

            }
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
        tvTitle.text = getString(R.string.search_results)
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
                val data = TrackedEntityInstance(
                    trackedEntity = formatter.generateUUID(11),
                    enrollment = formatter.generateUUID(11),
                    enrollDate = formatter.formatCurrentDate(Date()),
                    orgUnit = orgCode,
                    attributes = attributeValueList,
                )
                viewModel.saveTrackedEntity(this, data, data.orgUnit)
                formatter.deleteSharedPref("index", this@PatientRegistrationActivity)
                startActivity(
                    Intent(
                        this@PatientRegistrationActivity,
                        PatientResponderActivity::class.java
                    )
                )
                this@PatientRegistrationActivity.finish()
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