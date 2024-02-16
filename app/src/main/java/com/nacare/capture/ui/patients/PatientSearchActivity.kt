package com.nacare.capture.ui.patients

import android.app.Application
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.gson.Gson
import com.nacare.capture.R
import com.nacare.capture.data.FormatterClass
import com.nacare.capture.databinding.ActivityPatientSearchBinding
import com.nacare.capture.model.CodeValuePair
import com.nacare.capture.model.Option
import com.nacare.capture.model.TrackedEntityAttributes
import com.nacare.capture.network.RetrofitCalls
import com.nacare.capture.room.Converters
import com.nacare.capture.room.MainViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class PatientSearchActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPatientSearchBinding
    private lateinit var viewModel: MainViewModel
    private val searchList = ArrayList<TrackedEntityAttributes>()
    private val searchParameters = ArrayList<CodeValuePair>()
    private val retrofitCalls = RetrofitCalls()
    private val formatter = FormatterClass()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPatientSearchBinding.inflate(layoutInflater)
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
            btnProceed.apply {
                setOnClickListener {
                    try {
                        if (formatter.isNetworkAvailable(this@PatientSearchActivity)) {
                            validateSearchData()
                        } else {
                            formatter.showInternetConnectionRequiredDialog(context)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }
    }

    private fun validateSearchData() {
        if (searchParameters.isEmpty()) {
            Toast.makeText(this, "Please Enter at least one search parameter", Toast.LENGTH_SHORT)
                .show()
            return
        }
        val searchParametersString =
            searchParameters.joinToString(separator = ",") { filterItem ->
                "${filterItem.code}:ilike:${filterItem.value}"
            }
        val programUid = formatter.getSharedPref("programUid", this)
        val trackedEntity = formatter.getSharedPref("trackedEntity", this)

        if (programUid == null) {
            Toast.makeText(this, "Please select program", Toast.LENGTH_SHORT)
                .show()
            return
        }
        if (trackedEntity == null) {
            Toast.makeText(this, "Please select tracked entity", Toast.LENGTH_SHORT)
                .show()
            return
        }
        val inflater = LayoutInflater.from(this)
        formatter.deleteSharedPref("initial_data", this)
        formatter.deleteSharedPref("reload", this@PatientSearchActivity)
        retrofitCalls.performPatientSearch(
            this,
            programUid,
            trackedEntity,
            searchParametersString,
            inflater
        )
    }

    private fun loadSearchParameters() {
        val data = viewModel.loadSingleProgram(this, "notification")
        if (data != null) {
            Log.e("TAG", "Program Data Retrieved $data")
            val converters = Converters().fromJson(data.jsonData)
            Log.e("TAG", "Program Data Retrieved $converters")
            searchList.clear()
            converters.programs.forEach { it ->
                it.programSections.forEach {
                    if (it.name == "SEARCH PATIENT") {
                        val section = it.trackedEntityAttributes
                        Log.e("TAG", "Program Data Retrieved $section")
                        searchList.addAll(section)

                    }
                }
            }
            val excludes: List<String> = listOf("hn8hJsBAKrh", "hzVijy6tEUF")
            val orderedSearchList = searchList.sortedBy {
                if (it.id == "R1vaUuILrDy") 0 else 1
            }
            orderedSearchList.forEach {
                //order the above to start with  R1vaUuILrDy
                if (!excludes.contains(it.id)) {
                    populateSearchFields(it, binding.lnParent)
                }
            }
        }
    }

    private fun populateSearchFields(item: TrackedEntityAttributes, lnParent: LinearLayout) {
        val valueType: String = item.valueType
        val label: String = item.name
        val inflater = LayoutInflater.from(this)
        Log.e("TAG", "Data Populated $valueType")

        if ("TEXT" == valueType) {
            if (item.optionSet == null) {
                if (item.id == "AP13g7NcBOf") {
                    val itemView = inflater.inflate(
                        R.layout.item_edittext_custom,
                        findViewById(R.id.lnParent),
                        false
                    ) as LinearLayout
                    val tvName = itemView.findViewById<TextView>(R.id.tv_name)
                    val tvElement = itemView.findViewById<TextView>(R.id.tv_element)
                    val editTextOne = itemView.findViewById<TextInputEditText>(R.id.editTextOne)
                    val editTextTwo = itemView.findViewById<TextInputEditText>(R.id.editTextTwo)
                    val editTextThree = itemView.findViewById<TextInputEditText>(R.id.editTextThree)
                    val editTextFour = itemView.findViewById<TextInputEditText>(R.id.editTextFour)
                    val editText = itemView.findViewById<TextInputEditText>(R.id.editText)
                    val textInputLayoutOne =
                        itemView.findViewById<TextInputLayout>(R.id.textInputLayoutOne)
                    val textInputLayoutTwo =
                        itemView.findViewById<TextInputLayout>(R.id.textInputLayoutTwo)
                    val textInputLayoutThree =
                        itemView.findViewById<TextInputLayout>(R.id.textInputLayoutThree)
                    val textInputLayoutFour =
                        itemView.findViewById<TextInputLayout>(R.id.textInputLayoutFour)
                    setupEditText(textInputLayoutOne, editTextOne, editTextTwo, 3, false, 0)
                    setupEditText(textInputLayoutTwo, editTextTwo, editTextThree, 3, false, 0)
                    setupEditText(textInputLayoutThree, editTextThree, editTextFour, 2, true, 12)
                    handleFinalTextView(editTextFour, textInputLayoutFour, 4)
                    tvName.text = modifiedLabelName(item.name, item.id)
                    tvElement.text = item.id
                    lnParent.addView(itemView)
                } else {
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
                    tvName.text = modifiedLabelName(item.name, item.id)
                    tvElement.text = item.id
                    lnParent.addView(itemView)

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
                tvName.text = item.name
                autoCompleteTextView.setAdapter(adp)
                adp.notifyDataSetChanged()
                lnParent.addView(itemView)
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
                        val value = s.toString()
                        if (value.isNotEmpty()) {
                            val dataValue = getCodeFromText(value, item.optionSet.options)
                            saveValued(item.id, dataValue)
                        }
                    }

                    override fun afterTextChanged(s: Editable?) {
                    }
                })
            }
        }

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

        formatter.deleteSharedPref("current_data", this)

    }

    private fun modifiedLabelName(s: String, uid: String): String? {
        return if (uid == "R1vaUuILrDy") {
            "Patient Name"
        } else {
            s
        }
    }

    private fun setupEditText(
        textInputLayout: TextInputLayout, currentEditText: EditText,
        nextEditText: EditText, maxLength: Int, isNumber: Boolean, maxValue: Int
    ) {
        currentEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(
                charSequence: CharSequence,
                start: Int,
                count: Int,
                after: Int
            ) {
            }

            override fun onTextChanged(
                charSequence: CharSequence,
                start: Int,
                before: Int,
                count: Int
            ) {
                if (charSequence.length == maxLength) {
                    // Move the cursor to the next EditText when maxLength is reached
                    if (isNumber) {
                        if (!charSequence.toString().isEmpty() && charSequence.toString()
                                .toInt() > maxValue
                        ) {

                            currentEditText.setSelection(currentEditText.text.length) // Move the cursor to the end
                            textInputLayout.error = "."
                        } else {
                            nextEditText.requestFocus()
                            if (nextEditText is TextInputEditText) {
                                nextEditText.setSelection(0)
                            }
                            textInputLayout.error = null
                        }
                    } else {
                        nextEditText.requestFocus()
                        if (nextEditText is TextInputEditText) {
                            nextEditText.setSelection(0)
                        }
                    }
                }
            }

            override fun afterTextChanged(editable: Editable) {}
        })
    }

    private fun handleFinalTextView(
        currentEditText: TextInputEditText,
        textInputLayout: TextInputLayout,
        maxLength: Int
    ) {
        val currentDate = Date()
        val yearFormat = SimpleDateFormat("yyyy", Locale.ENGLISH)
        val formattedYear: String = yearFormat.format(currentDate)
        println("Current Year: $formattedYear")
        val maxValue = Integer.valueOf(formattedYear)
        currentEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(
                charSequence: CharSequence,
                start: Int,
                count: Int,
                after: Int
            ) {
            }

            override fun onTextChanged(
                charSequence: CharSequence,
                start: Int,
                before: Int,
                count: Int
            ) {
                if (charSequence.length == maxLength) {
                    if (!charSequence.toString().isEmpty() && charSequence.toString()
                            .toInt() > maxValue
                    ) {
                        currentEditText.setSelection(currentEditText.text!!.length) // Move the cursor to the end
                        textInputLayout.error = "."
                    } else {
                        textInputLayout.error = null
                    }
                }
            }

            override fun afterTextChanged(editable: Editable) {}
        })
    }
}