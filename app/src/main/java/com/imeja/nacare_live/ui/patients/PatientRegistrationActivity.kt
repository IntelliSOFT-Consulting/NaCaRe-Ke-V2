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
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.imeja.nacare_live.R
import com.imeja.nacare_live.data.Constants.AGE_MONTHS
import com.imeja.nacare_live.data.Constants.AGE_YEARS
import com.imeja.nacare_live.data.Constants.DATE_OF_BIRTH
import com.imeja.nacare_live.data.Constants.DIAGNOSIS
import com.imeja.nacare_live.data.Constants.ICD_CODE
import com.imeja.nacare_live.data.FormatterClass
import com.imeja.nacare_live.databinding.ActivityPatientRegistrationBinding
import com.imeja.nacare_live.model.Attribute
import com.imeja.nacare_live.model.AttributeValues
import com.imeja.nacare_live.model.CodeValuePair
import com.imeja.nacare_live.model.Option
import com.imeja.nacare_live.model.TrackedEntityAttributes
import com.imeja.nacare_live.model.TrackedEntityInstance
import com.imeja.nacare_live.model.TrackedEntityInstanceAttributes
import com.imeja.nacare_live.network.RetrofitCalls
import com.imeja.nacare_live.room.Converters
import com.imeja.nacare_live.room.MainViewModel
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
                                calculateRelevant(item, value)
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
                            calculateRelevant(item, value)
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

    private fun getDisplayNameFromCode(options: List<Option>, value: String): String {
        for (option in options) {
            if (option.code == value) {
                return option.displayName
            }
        }
        return value
    }


    fun calculateAge(birthDate: LocalDate, currentDate: LocalDate): Pair<Int, Int> {
        val period = Period.between(birthDate, currentDate)
        val years = period.years
        val months = period.months
        return Pair(years, months)
    }

    private fun calculateRelevant(item: TrackedEntityAttributes, value: String) {

        if (item.id == DATE_OF_BIRTH) {
            val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
            val birthDate = LocalDate.parse(value, dateFormatter)
            // Get the current date
            val currentDate = LocalDate.now()
            val (years, months) = calculateAge(birthDate, currentDate)


            Log.e("TAG", "Age: $years years and $months months")
            saveValued(AGE_YEARS, "$years")
            saveValued(AGE_MONTHS, "$months")
        } else if (item.id == DIAGNOSIS) {
            val dataValue = item.optionSet?.let { getCodeFromText(value, it.options) }
            saveValued(ICD_CODE, "$dataValue")
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
        val reloadPage = formatter.getSharedPref("reload", this@PatientRegistrationActivity)
        if (reloadPage == null) {
            reloadActivity()
        }
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
                saveValued("AP13g7NcBOf", patient_identification)
                val existingIndex = searchParameters.indexOfFirst { it.code == "AP13g7NcBOf" }
                if (existingIndex != -1) {
                    // Update the existing entry if the code is found
                    searchParameters[existingIndex] =
                        CodeValuePair(code = "AP13g7NcBOf", value = patient_identification)
                } else {
                    // Add a new entry if the code is not found
                    val data = CodeValuePair(code = "AP13g7NcBOf", value = patient_identification)
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
                viewModel.saveTrackedEntity(this, data)

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