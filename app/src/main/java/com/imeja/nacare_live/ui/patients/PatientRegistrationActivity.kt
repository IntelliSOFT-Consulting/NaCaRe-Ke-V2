package com.imeja.nacare_live.ui.patients

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
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.DatePicker
import android.widget.LinearLayout
import android.widget.RadioGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.imeja.nacare_live.R
import com.imeja.nacare_live.data.FormatterClass
import com.imeja.nacare_live.databinding.ActivityPatientRegistrationBinding
import com.imeja.nacare_live.model.Attribute
import com.imeja.nacare_live.model.AttributeValues
import com.imeja.nacare_live.model.CodeValuePair
import com.imeja.nacare_live.model.Option
import com.imeja.nacare_live.model.TrackedEntityAttributes
import com.imeja.nacare_live.network.RetrofitCalls
import com.imeja.nacare_live.room.Converters
import com.imeja.nacare_live.room.MainViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Date


class PatientRegistrationActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPatientRegistrationBinding
    private lateinit var viewModel: MainViewModel
    private val searchList = ArrayList<TrackedEntityAttributes>()
    private val emptyList = ArrayList<TrackedEntityAttributes>()
    private val completeList = ArrayList<TrackedEntityAttributes>()
    private var searchParameters = ArrayList<CodeValuePair>()
    private val retrofitCalls = RetrofitCalls()
    private val formatter = FormatterClass()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPatientRegistrationBinding.inflate(layoutInflater)
        setContentView(binding.root)
        viewModel = MainViewModel(this.applicationContext as Application)

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
                    validateSearchData()
                }
            }
            btnCancel.apply {
                setOnClickListener {
                    onBeginBatchEdit()
                }
            }
        }

    }

    private fun scrollToTheLast() {
        binding.apply {
            val response = formatter.getSharedPref("current_data", this@PatientRegistrationActivity)
            if (response != null) {
                searchParameters = getSavedValues()
                val lastAnswerUID = searchParameters.lastOrNull()?.code

                Log.e("TAG", "LAst UID  $lastAnswerUID")
                if (lastAnswerUID != null) {
                    // Iterate through the child views of the LinearLayout
                    for (i in 0 until lnParent.childCount) {
                        val view = lnParent.getChildAt(i)
                        // Check if the view's tag matches the last answer UID
                        if (view.tag == lastAnswerUID) {
                            // Get the Y-coordinate of the target view
                            val y = view.y.toInt()
                            // Scroll to the target view
                            scrollView.post {
                                scrollView.smoothScrollTo(0, y)
                            }
                            break // Exit the loop after finding the target view
                        }
                    }
                }

            }

        }
    }


    private fun loadSearchParameters() {
        val data = viewModel.loadSingleProgram(this, "notification")
        if (data != null) {
            Log.e("TAG", "Program Data Retrieved $data")
            val converters = Converters().fromJson(data.jsonData)
            Log.e("TAG", "Program Data Retrieved $converters")
            searchList.clear()
            emptyList.clear()
            converters.programs.forEach { it ->
                it.programSections.forEach {
                    if (it.name == "SEARCH PATIENT") {
                        val section = it.trackedEntityAttributes
                        Log.e("TAG", "Program Data Retrieved $section")
                        searchList.addAll(section)

                    } else {
                        val section = it.trackedEntityAttributes
                        Log.e("TAG", "Program Data Retrieved Other  $section")
                        emptyList.addAll(section)

                    }
                }
            }
            completeList.addAll(searchList)
            completeList.addAll(emptyList)
            binding.lnParent.removeAllViews()
            binding.lnParent.removeAllViewsInLayout()
            completeList.forEach {
                populateSearchFields(it, binding.lnParent, extractCurrentValues(it.id))

            }
            scrollToTheLast()
        }
    }

    private fun extractCurrentValues(id: String): String {
        val response = formatter.getSharedPref("current_data", this)
        if (response != null) {
            searchParameters = getSavedValues()
            Log.e("TAG", "Manipulated Data ***** $searchParameters")
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

                            Log.e("TAG", "Show me the Response to Compared Answer Above $previousAnswer Needed $part3Lower")
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
        item: TrackedEntityAttributes,
        lnParent: LinearLayout,
        currentValue: String
    ) {
        val valueType: String = item.valueType
        val label: String = item.name
        val inflater = LayoutInflater.from(this)
        Log.e("TAG", "Data Populated $valueType")
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
                            saveValued(item.id, editText.text.toString())
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
                        autoCompleteTextView.setText(currentValue, false)
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
                            itemView.visibility = View.GONE
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
                                saveValued(item.id, dataValue)
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
                        itemView.visibility = View.GONE
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
                            saveValued(item.id, value)
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
                        itemView.visibility = View.GONE
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
                            saveValued(item.id, value)
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
                        itemView.visibility = View.GONE
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
                            saveValued(item.id, value)
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
                            saveValued(item.id, dataValue)
                            isProgrammaticChange = false
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
                        itemView.visibility = View.GONE
                    }

                }
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

    private fun saveValued(id: String, value: String) {
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
        Log.e("TAG", "Growing List $searchParameters")
        reloadActivity()

    }

    private fun reloadActivity() {
        CoroutineScope(Dispatchers.Main).launch {
            delay(100)
            val intent =
                Intent(this@PatientRegistrationActivity, PatientRegistrationActivity::class.java)
            startActivity(intent)
            finish()
            overridePendingTransition(0, 0) // Disable transition animation
        }
    }

    private fun validateSearchData() {

    }
}