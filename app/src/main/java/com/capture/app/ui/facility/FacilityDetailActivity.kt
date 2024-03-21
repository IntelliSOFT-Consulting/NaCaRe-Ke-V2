package com.capture.app.ui.facility

import android.app.Application
import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.Html
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.DatePicker
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.capture.app.R
import com.capture.app.data.FormatterClass
import com.capture.app.databinding.ActivityFacilityDetailBinding
import com.capture.app.model.Attribute
import com.capture.app.model.AttributeValues
import com.capture.app.model.DataElements
import com.capture.app.model.DataValue
import com.capture.app.model.FormSection
import com.capture.app.model.Option
import com.capture.app.model.ParentAttributeValues
import com.capture.app.model.RefinedAttributeValues
import com.capture.app.room.Converters
import com.capture.app.room.EventData
import com.capture.app.room.MainViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Date


class FacilityDetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityFacilityDetailBinding
    private val formFieldsData = ArrayList<FormSection>()
    private lateinit var viewModel: MainViewModel
    private val formatter = FormatterClass()
    private var searchParameters = ArrayList<DataValue>()
    private var dataValueList = ArrayList<String>()
    private var attributeValueList = ArrayList<ParentAttributeValues>()
    private var requiredFieldsString = ArrayList<String>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFacilityDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        viewModel = MainViewModel(this.applicationContext as Application)
        searchParameters.clear()
        attributeValueList.clear()
        dataValueList.clear()
        requiredFieldsString.clear()
        val eventUid = formatter.getSharedPref("current_event_id", this)
        if (eventUid != null) {
            populateAvailableData(eventUid)
        }
        populateViews()
        binding.apply {
            setSupportActionBar(trackedEntityInstanceSearchToolbar)
            supportActionBar?.apply {
                title = getString(R.string.facility_details_capture_tool)
                setDisplayHomeAsUpEnabled(true)
            }
            trackedEntityInstanceSearchToolbar.setNavigationOnClickListener {
                // Handle back arrow click here
                onBackPressed() // Or implement your own logic
            }
            var saveData = true
            val canWrite =
                FormatterClass().getSharedPref("canWrite", this@FacilityDetailActivity)
            if (canWrite == null) {
                saveData = false
            }
            if (saveData) {
                btnSave.visibility = View.VISIBLE
                btnCancel.visibility = View.GONE
            } else {
                btnSave.visibility = View.GONE
                btnCancel.visibility = View.VISIBLE
            }
            btnCancel.apply {
                setOnClickListener {
                    this@FacilityDetailActivity.finish()
                }
            }
            btnSave.apply {

                setOnClickListener {

                    if (allRequiredFieldsComplete()) {
                        validateSearchData()
                    } else {
                        Toast.makeText(
                            this@FacilityDetailActivity,
                            "Please enter all required fields",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                }
            }

            val orgName = formatter.getSharedPref("orgName", this@FacilityDetailActivity)
            val program = formatter.getSharedPref("program", this@FacilityDetailActivity)
            if (!TextUtils.isEmpty(orgName)) {
                val formattedText = "Saving to <b>$program</b> in <b>$orgName</b>"
                binding.textViewNote.text = Html.fromHtml(formattedText)
            }

        }

    }

    private fun populateAvailableData(eventUid: String) {
        searchParameters.clear()
        val data = viewModel.loadEventById(eventUid, this@FacilityDetailActivity)
        if (data != null) {
            val attributes = Converters().fromJsonDataAttribute(data.dataValues)
            if (attributes.isNotEmpty()) {
                searchParameters.clear()
                attributes.forEachIndexed { index, attribute ->
                    saveValued(index, attribute.dataElement, attribute.value)
                }
            }
        }

    }

    private fun allRequiredFieldsComplete(): Boolean {
        try {
            searchParameters = getSavedValues()

            val searchParameterCodes = searchParameters.map { it.dataElement }
            // Check if all required field codes are present in searchParameterCodes
            val missingFields = requiredFieldsString.filter { !searchParameterCodes.contains(it) }

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

    private fun validateSearchData() {
        val orgCode = formatter.getSharedPref("orgCode", this)
        val programUid = formatter.getSharedPref("programUid", this)
        var eventUid = formatter.getSharedPref("current_event", this)
        val currentEventId = formatter.getSharedPref("current_event_id", this)
        var eventDate = formatter.getSharedPref("current_event_date", this)
        if (currentEventId != null) {
            if (orgCode != null) {
                val currentEvent =
                    viewModel.loadEventById(currentEventId, this@FacilityDetailActivity)
                if (currentEvent != null) {

                    if (eventUid == null) {
                        eventUid = formatter.generateUUID(11)
                    }
                    if (eventDate == null) {
                        eventDate = formatter.formatCurrentDate(Date())
                    }
                    val data = EventData(
                        uid = eventUid,
                        program = programUid.toString(),
                        orgUnit = orgCode,
                        eventDate = eventDate,
                        status = "ACTIVE",
                        isServerSide = currentEvent.isServerSide,
                        dataValues = Gson().toJson(searchParameters),
                        isSynced = false
                    )
                    viewModel.saveEventUpdated(this, data, currentEventId.toString())
                    this@FacilityDetailActivity.finish()
                } else {
                    Toast.makeText(this, "Invalid Event Search...", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Invalid Org Unit", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "Invalid Event", Toast.LENGTH_SHORT).show()
        }
    }

    private fun populateViews() {
        val data = viewModel.loadSingleProgram(this, "facility")
        if (data != null) {
            val converters = Converters().fromJson(data.jsonData)
            converters.programs.forEach { it ->
                it.programStages.forEach { q ->
                    q.programStageSections.forEach {
                        val section = FormSection(
                            displayName = it.displayName,
                            dataElements = it.dataElements
                        )
                        formFieldsData.add(section)
                    }
                }
            }
            binding.lnParent.removeAllViews()
            binding.lnParent.removeAllViewsInLayout()

            formFieldsData.forEach { parent ->
                var isFirst = false
                parent.dataElements.forEachIndexed { index, item ->
                    if (index == 0) {
                        isFirst = true
                    } else {
                        isFirst = false
                    }

                    attributeValueList.add(
                        ParentAttributeValues(
                            parentName = parent.displayName,
                            parent = item.id,
                            attributeValues = item.attributeValues
                        )
                    )
                    dataValueList.add(item.id)
                    populateSearchFields(
                        parent.displayName,
                        isFirst,
                        index,
                        item,
                        binding.lnParent,
                        extractCurrentValues(item.id)
                    )
                }
            }
        }
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

    private fun getCodeFromText(value: String, options: List<Option>): String {
        //loop though the options, if the value matches the name, returns the code
        for (option in options) {
            if (option.displayName == value) {
                return option.code
            }
        }
        return value
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

                            previousAnswer = previousAnswer.lowercase()
                            val part3Lower = parts[2].lowercase()

                            Log.e(
                                "TAG",
                                "Show me the Response to Compared Answer Above $previousAnswer Needed $part3Lower"
                            )

                            val result = when (part2) {
                                "eq" -> previousAnswer == part3Lower
                                "notin" -> previousAnswer != part3Lower
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
        parentName: String,
        isFirstItem: Boolean,
        index: Int,
        item: DataElements,
        lnParent: LinearLayout,
        currentValue: String
    ) {
        val valueType: String = item.valueType
        val label: String = item.displayName
        val inflater = LayoutInflater.from(this)
        val isHidden: Boolean = extractAttributeValue("Hidden", item.attributeValues)
        var isDisabled: Boolean = extractAttributeValue("Disabled", item.attributeValues)
        val isRequired: Boolean = extractAttributeValue("Required", item.attributeValues)
        val disableFutureDate: Boolean =
            extractAttributeValue("disableFutureDate", item.attributeValues)
        val showIf = showIfAttribute("showIf", item.attributeValues)

        if (isRequired) {
            requiredFieldsString.add(item.id)
        }
        val canWrite = FormatterClass().getSharedPref("canWrite", this)
        if (canWrite == null) {
            isDisabled = true
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
                    val name =
                        if (isRequired) generateRequiredField(item.displayName) else item.displayName

                    val lnViewParent = itemView.findViewById<LinearLayout>(R.id.ln_view_parent)
                    val tvParent = itemView.findViewById<TextView>(R.id.tv_parent)
                    tvParent.text = parentName
                    if (isFirstItem) {
                        lnViewParent.visibility = View.VISIBLE
                    }

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
                                saveValued(index, item.id, value)
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
                    tvElement.text = item.id
                    val lnViewParent = itemView.findViewById<LinearLayout>(R.id.ln_view_parent)
                    val tvParent = itemView.findViewById<TextView>(R.id.tv_parent)
                    tvParent.text = parentName
                    if (isFirstItem) {
                        lnViewParent.visibility = View.VISIBLE
                    }
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
                        autoCompleteTextView.setText(currentValue, false)
                    }
                    val name =
                        if (isRequired) generateRequiredField(item.displayName) else item.displayName
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
                val name =
                    if (isRequired) generateRequiredField(item.displayName) else item.displayName
                val lnViewParent = itemView.findViewById<LinearLayout>(R.id.ln_view_parent)
                val tvParent = itemView.findViewById<TextView>(R.id.tv_parent)
                tvParent.text = parentName
                if (isFirstItem) {
                    lnViewParent.visibility = View.VISIBLE
                }
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
                            val valueCurrent: String = formatter.getDate(year, month, day)
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
                val name =
                    if (isRequired) generateRequiredField(item.displayName) else item.displayName
                val lnViewParent = itemView.findViewById<LinearLayout>(R.id.ln_view_parent)
                val tvParent = itemView.findViewById<TextView>(R.id.tv_parent)
                tvParent.text = parentName
                if (isFirstItem) {
                    lnViewParent.visibility = View.VISIBLE
                }
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
                val name =
                    if (isRequired) generateRequiredField(item.displayName) else item.displayName
                val lnViewParent = itemView.findViewById<LinearLayout>(R.id.ln_view_parent)
                val tvParent = itemView.findViewById<TextView>(R.id.tv_parent)
                tvParent.text = parentName
                if (isFirstItem) {
                    lnViewParent.visibility = View.VISIBLE
                }
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
                val radioButtonYes = itemView.findViewById<RadioButton>(R.id.radioButtonYes)
                val radioButtonNo = itemView.findViewById<RadioButton>(R.id.radioButtonNo)
                val name =
                    if (isRequired) generateRequiredField(item.displayName) else item.displayName
                val lnViewParent = itemView.findViewById<LinearLayout>(R.id.ln_view_parent)
                val tvParent = itemView.findViewById<TextView>(R.id.tv_parent)
                tvParent.text = parentName
                if (isFirstItem) {
                    lnViewParent.visibility = View.VISIBLE
                }
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
//                                        requiredFieldsString.add(child.tag.toString())
                                    } else {
                                        child.visibility = View.GONE

//                                        requiredFieldsString.remove(child.tag.toString())
                                    }
                                } else {
                                    // If no match is found, leave the visibility unchanged
                                    if (child.visibility != View.VISIBLE) {
                                        child.visibility = View.GONE
                                    }
                                }
                            }


                            isProgrammaticChange = false
                        }
                    }
                }

                if (isHidden) {
                    itemView.visibility = View.GONE
                } else {
                    if (isDisabled) {
                        radioGroup.isEnabled = false
                        radioButtonYes.isEnabled = false
                        radioButtonNo.isEnabled = false
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
        attributeValueList.forEach { q ->
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

    private fun saveValued(index: Int, id: String, value: String) {
        val existingIndex = searchParameters.indexOfFirst { it.dataElement == id }
        if (existingIndex != -1) {
            // Update the existing entry if the code is found
            searchParameters[existingIndex] = DataValue(dataElement = id, value = value)
        } else {
            // Add a new entry if the code is not found
            val data = DataValue(dataElement = id, value = value)
            searchParameters.add(data)
        }
        formatter.saveSharedPref("index", "$index", this)
        formatter.saveSharedPref(
            "current_facility_data",
            Gson().toJson(searchParameters),
            this
        )
        Log.e("TAG", "Growing List $searchParameters")

    }

    private fun reloadActivity() {
        CoroutineScope(Dispatchers.Main).launch {
            delay(100)
            val intent =
                Intent(this@FacilityDetailActivity, FacilityDetailActivity::class.java)
            intent.flags =
                Intent.FLAG_ACTIVITY_CLEAR_TOP //or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
            finish()
            overridePendingTransition(0, 0) // Disable transition animation
        }
    }

    private fun extractCurrentValues(id: String): String {
        val response = formatter.getSharedPref("current_facility_data", this)
        if (response != null) {
            searchParameters = getSavedValues()
            Log.e("TAG", "Manipulated Data ***** $searchParameters")
            val foundItem = searchParameters.find { it.dataElement == id }
            return foundItem?.value ?: ""
        }
        return ""
    }

    private fun getSavedValues(): ArrayList<DataValue> {
        val savedData = formatter.getSharedPref("current_facility_data", this)
        if (savedData != null) {
            return if (savedData.isNotEmpty()) {
                Gson().fromJson(
                    savedData,
                    object : TypeToken<ArrayList<DataValue>>() {}.type
                )
            } else {
                ArrayList()
            }
        }
        return ArrayList()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle menu item clicks here
        return when (item.itemId) {
            R.id.action_settings -> {
                prepareFacilityEvent()
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun prepareFacilityEvent() {

    }


}