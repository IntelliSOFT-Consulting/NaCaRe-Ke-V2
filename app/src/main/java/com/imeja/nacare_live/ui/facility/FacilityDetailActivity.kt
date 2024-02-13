package com.imeja.nacare_live.ui.facility

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
import android.widget.RadioGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.work.impl.Schedulers
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.imeja.nacare_live.R
import com.imeja.nacare_live.data.FormatterClass
import com.imeja.nacare_live.databinding.ActivityFacilityDetailBinding
import com.imeja.nacare_live.model.Attribute
import com.imeja.nacare_live.model.AttributeValues
import com.imeja.nacare_live.model.CodeValueEventPair
import com.imeja.nacare_live.model.CodeValuePair
import com.imeja.nacare_live.model.DataElements
import com.imeja.nacare_live.model.DataValue
import com.imeja.nacare_live.model.Option
import com.imeja.nacare_live.room.Converters
import com.imeja.nacare_live.room.EventData
import com.imeja.nacare_live.room.MainViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.lang.Throwable
import java.util.Calendar
import java.util.Date
import kotlin.Boolean
import kotlin.CharSequence
import kotlin.Exception
import kotlin.Int
import kotlin.String
import kotlin.apply
import kotlin.toString


class FacilityDetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityFacilityDetailBinding
    private val formFieldsData = ArrayList<DataElements>()
    private lateinit var viewModel: MainViewModel
    private val formatter = FormatterClass()
    private var searchParameters = ArrayList<DataValue>()
    private var dataValueList = ArrayList<DataValue>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFacilityDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        viewModel = MainViewModel(this.applicationContext as Application)
        searchParameters.clear()
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
            btnSave.apply {
                setOnClickListener {
                    formatter.saveSharedPref("reload", "true", this@FacilityDetailActivity)
                    validateSearchData()
                }
            }

            val orgName = formatter.getSharedPref("orgName", this@FacilityDetailActivity)
            val program = formatter.getSharedPref("program", this@FacilityDetailActivity)
            if (!TextUtils.isEmpty(orgName)) {
                val formattedText = "Saving to <b>$program</b> in <b>$orgName</b>"
                binding.textViewNote.text = Html.fromHtml(formattedText)
            }

        }

        val index = formatter.getSharedPref("index", this@FacilityDetailActivity)
        if (index != null) {
            val viewToFocus: View? = binding.lnParent.getChildAt(index.toInt())
//            viewToFocus?.requestFocus()
            binding.scrollView.post {
                binding.scrollView.smoothScrollTo(0, viewToFocus?.top ?: 0)
            }
        }
    }

    private fun validateSearchData() {
        val orgCode = formatter.getSharedPref("orgCode", this)
        val programUid = formatter.getSharedPref("programUid", this)
        var eventUid = formatter.getSharedPref("current_event", this)
        var eventDate = formatter.getSharedPref("current_event_date", this)
        if (orgCode != null) {
            dataValueList.clear()
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
                dataValues = Gson().toJson(searchParameters)
            )
            viewModel.saveEvent(this, data)
            this@FacilityDetailActivity.finish()
        }
    }

    private fun populateViews() {
        val data = viewModel.loadSingleProgram(this, "facility")
        if (data != null) {
            Log.e("TAG", "Program Data Retrieved $data")
            val converters = Converters().fromJson(data.jsonData)
            converters.programs.forEach { it ->
                it.programStages.forEach { q ->
                    q.programStageSections.forEach {
                        val section = it.dataElements
                        formFieldsData.addAll(section)
                    }
                }
            }
            binding.lnParent.removeAllViews()
            binding.lnParent.removeAllViewsInLayout()
            formFieldsData.forEachIndexed { index, item ->
                populateSearchFields(index,item, binding.lnParent, extractCurrentValues(item.id))
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
        index:Int,
        item: DataElements,
        lnParent: LinearLayout,
        currentValue: String
    ) {
        val valueType: String = item.valueType
        val label: String = item.displayName
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
                    val name =
                        if (isRequired) generateRequiredField(item.displayName) else item.displayName
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
                            saveValued(index,item.id, editText.text.toString())
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
                                saveValued(index,item.id, dataValue)
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
                            saveValued(index,item.id, value)
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
                            saveValued(index,item.id, value)
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
                            saveValued(index,item.id, value)
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
                val name =
                    if (isRequired) generateRequiredField(item.displayName) else item.displayName
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
                            saveValued(index,item.id, dataValue)
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

    private fun saveValued(index: Int,id: String, value: String) {
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
        formatter.saveSharedPref("current_facility_data", Gson().toJson(searchParameters), this)
        Log.e("TAG", "Growing List $searchParameters")
        val reloadPage = formatter.getSharedPref("reload", this@FacilityDetailActivity)
        if (reloadPage == null) {
            reloadActivity()
        }
    }

    private fun reloadActivity() {
        CoroutineScope(Dispatchers.Main).launch {
            delay(100)
            val intent =
                Intent(this@FacilityDetailActivity, FacilityDetailActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP //or Intent.FLAG_ACTIVITY_SINGLE_TOP
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
                Gson().fromJson(savedData, object : TypeToken<ArrayList<DataValue>>() {}.type)
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