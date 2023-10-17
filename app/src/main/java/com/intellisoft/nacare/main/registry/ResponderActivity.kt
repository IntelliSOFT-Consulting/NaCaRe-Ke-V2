package com.intellisoft.nacare.main.registry

import android.app.Application
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.Html
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.gson.Gson
import com.intellisoft.nacare.helper_class.DataElementItem
import com.intellisoft.nacare.helper_class.EntityAttributes
import com.intellisoft.nacare.helper_class.FormatterClass
import com.intellisoft.nacare.helper_class.ProgramStageSections
import com.intellisoft.nacare.room.EventData
import com.intellisoft.nacare.room.MainViewModel
import com.intellisoft.nacare.util.AppUtils
import com.intellisoft.nacare.util.AppUtils.containsAnyKeyword
import com.intellisoft.nacare.viewmodels.NetworkViewModel
import com.nacare.capture.R
import com.nacare.capture.databinding.ActivityResponderBinding

class ResponderActivity : AppCompatActivity() {
    private lateinit var binding: ActivityResponderBinding
    private lateinit var viewModel: MainViewModel
    private lateinit var networkViewModel: NetworkViewModel
    private lateinit var eventData: EventData
    private val formatterClass = FormatterClass()
    private val dataList: MutableList<DataElementItem> = mutableListOf()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityResponderBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        viewModel = MainViewModel((this.applicationContext as Application))
        networkViewModel = ViewModelProvider(this).get(NetworkViewModel::class.java)
        // Inside your ResponderActivity
        val receivedIntent = intent
        if (receivedIntent != null) {
            val dataBundle = receivedIntent.getBundleExtra("data")
            if (dataBundle != null) {
                val code = dataBundle.getString("code")
                val name = dataBundle.getString("name")
                val ev = dataBundle.getString("event")
                if (ev != null) {
                    eventData = Gson().fromJson(ev, EventData::class.java)
                }
                val programStageDataElements = dataBundle.getString("programStageDataElements")
                if (programStageDataElements != null) {

                    displayDataElements(programStageDataElements, eventData)
                }
                val attribute = dataBundle.getString("attribute")
                if (attribute != null) {
                    manipulateRetrievedAttribute(attribute, eventData)
                }
                supportActionBar?.apply {
                    title = name
                    setDisplayHomeAsUpEnabled(true)

                }

                binding.apply {
                    when (name) {

                        "Survivorship" -> {
                            nextButton.text = "Finish"
                            nextButton.setOnClickListener {
                                val intent = Intent(this@ResponderActivity, SummaryActivity::class.java)
                                startActivity(intent)
                                this@ResponderActivity.finish()
                            }
                        }
                        else -> {
                            prevButton.setOnClickListener {
                                this@ResponderActivity.finish()
                            }
                            nextButton.setOnClickListener {
                                this@ResponderActivity.finish()
                            }
                        }
                    }
                }

            }
        }
        binding.apply {
            val program = formatterClass.getSharedPref("program", this@ResponderActivity)
            val org = formatterClass.getSharedPref("name", this@ResponderActivity)

            val formattedText = "Saving to <b>$program</b> in <b>$org</b>"
            textView.text = Html.fromHtml(formattedText, Html.FROM_HTML_MODE_LEGACY)

        }
        networkViewModel.eventData.observe(this, Observer { newData ->
            // Update UI based on the new data
            if (newData != null) {
                updateUI(newData)
            }
        })
    }

    private fun updateUI(newData: EventData) {
        eventData = newData
    }

    private fun manipulateRetrievedAttribute(json: String, eventData: EventData) {
        val gson = Gson()
        val items = gson.fromJson(json, Array<EntityAttributes>::class.java)
        items.forEach {
            viewModel.addResponse(
                this@ResponderActivity,
                eventData,
                it.attribute,
                it.value
            )
        }
    }

    private fun displayDataElements(json: String, eventData: EventData) {
        Log.e("TAG", "Data Element Responder$eventData")
        val gson = Gson()
        val items = gson.fromJson(json, Array<ProgramStageSections>::class.java)
        dataList.clear()
        items.forEach {
            it.dataElements.forEach { t ->
                dataList.add(t)
            }
        }

        for (dataElement in dataList) {
            createInputField(dataElement, eventData)
        }

    }

    private fun createInputField(item: DataElementItem, eventData: EventData) {
        val valueType = item.valueType
        val label = item.displayName
        val inflater = LayoutInflater.from(this)
        when (valueType) {
            "TEXT" -> {
                if (item.optionSet == null) {
                    val itemView = inflater.inflate(
                        R.layout.item_edittext,
                        binding.lnParentView,
                        false
                    ) as LinearLayout

                    val tvName = itemView.findViewById<TextView>(R.id.tv_name)
                    val tvElement = itemView.findViewById<TextView>(R.id.tv_element)
                    val textInputLayout =
                        itemView.findViewById<TextInputLayout>(R.id.textInputLayout)
                    val editText = itemView.findViewById<TextInputEditText>(R.id.editText)
                    tvName.text = item.displayName
                    tvElement.text = item.id
                    val response = viewModel.getEventResponse(
                        this@ResponderActivity,
                        eventData,
                        item.id
                    )
                    if (response != null) {
                        editText.setText(response)
                    }

                    editText.apply {
                        addTextChangedListener(object : TextWatcher {
                            override fun beforeTextChanged(
                                s: CharSequence?,
                                start: Int,
                                count: Int,
                                after: Int
                            ) {
                                // This method is called before the text is changed.
                            }

                            override fun onTextChanged(
                                s: CharSequence?,
                                start: Int,
                                before: Int,
                                count: Int
                            ) {
                                if (s != null) {
                                    viewModel.addResponse(
                                        context,
                                        eventData,
                                        item.id,
                                        s.toString()
                                    )
                                }
                            }

                            override fun afterTextChanged(s: Editable?) {
                                // This method is called after the text has changed.
                                // You can perform actions here based on the updated text.
                            }
                        })
                    }
                    binding.lnParentView.addView(itemView)
                } else {
                    val itemView = inflater.inflate(
                        R.layout.item_autocomplete,
                        binding.lnParentView,
                        false
                    ) as LinearLayout

                    val optionsList: MutableList<String> = mutableListOf()
                    val adp = ArrayAdapter(
                        this@ResponderActivity,
                        android.R.layout.simple_list_item_1,
                        optionsList
                    )
                    val tvName = itemView.findViewById<TextView>(R.id.tv_name)
                    val tvElement = itemView.findViewById<TextView>(R.id.tv_element)
                    val textInputLayout =
                        itemView.findViewById<TextInputLayout>(R.id.textInputLayout)
                    val autoCompleteTextView =
                        itemView.findViewById<AutoCompleteTextView>(R.id.autoCompleteTextView)

                    tvElement.text = item.id
                    optionsList.clear()
                    item.optionSet?.options?.forEach {
                        optionsList.add(it.displayName)
                    }
                    tvName.text = item.displayName
                    autoCompleteTextView.setAdapter(adp)
                    adp.notifyDataSetChanged()

                    autoCompleteTextView.apply {

                        addTextChangedListener(object : TextWatcher {
                            override fun beforeTextChanged(
                                s: CharSequence?,
                                start: Int,
                                count: Int,
                                after: Int
                            ) {
                                // This method is called before the text is changed.
                            }

                            override fun onTextChanged(
                                s: CharSequence?,
                                start: Int,
                                before: Int,
                                count: Int
                            ) {
                                if (s != null) {
                                    viewModel.addResponse(
                                        context,
                                        eventData,
                                        item.id,
                                        s.toString()
                                    )
                                }
                            }

                            override fun afterTextChanged(s: Editable?) {
                                // This method is called after the text has changed.
                                // You can perform actions here based on the updated text.
                            }
                        })
                    }
                    binding.lnParentView.addView(itemView)
                    val response = viewModel.getEventResponse(
                        this@ResponderActivity,
                        eventData,
                        item.id
                    )
                    if (response != null) {
                        autoCompleteTextView.setText(response, false)
                    }
                }
            }

            "DATE" -> {

                val itemView = inflater.inflate(
                    R.layout.item_date_edittext,
                    binding.lnParentView,
                    false
                ) as LinearLayout

                val tvName = itemView.findViewById<TextView>(R.id.tv_name)
                val tvElement = itemView.findViewById<TextView>(R.id.tv_element)
                val textInputLayout =
                    itemView.findViewById<TextInputLayout>(R.id.textInputLayout)
                val editText = itemView.findViewById<TextInputEditText>(R.id.editText)
                tvName.text = item.displayName
                tvElement.text = item.id
                val response = viewModel.getEventResponse(
                    this@ResponderActivity,
                    eventData,
                    item.id
                )
                if (response != null) {
                    editText.setText(response)
                }
                val keywords = listOf("Birth", "Death")
                val max = containsAnyKeyword(item.displayName, keywords)
                AppUtils.disableTextInputEditText(editText)
                editText.apply {
                    setOnClickListener {
                        AppUtils.showDatePickerDialog(
                            context, editText, setMaxNow = max, setMinNow = false
                        )
                    }

                    addTextChangedListener(object : TextWatcher {
                        override fun beforeTextChanged(
                            s: CharSequence?,
                            start: Int,
                            count: Int,
                            after: Int
                        ) {
                            // This method is called before the text is changed.
                        }

                        override fun onTextChanged(
                            s: CharSequence?,
                            start: Int,
                            before: Int,
                            count: Int
                        ) {
                            if (s != null) {
                                viewModel.addResponse(
                                    context, eventData, item.id, s.toString()
                                )
                            }
                        }

                        override fun afterTextChanged(s: Editable?) {
                            // This method is called after the text has changed.
                            // You can perform actions here based on the updated text.
                        }
                    })
                }
                binding.lnParentView.addView(itemView)
            }

            "BOOLEAN" -> {
                val itemView = inflater.inflate(
                    R.layout.item_radio,
                    binding.lnParentView,
                    false
                ) as LinearLayout

                val tvName = itemView.findViewById<TextView>(R.id.tv_name)
                val radioButtonYes = itemView.findViewById<RadioButton>(R.id.radioButtonYes)
                val radioButtonNo = itemView.findViewById<RadioButton>(R.id.radioButtonNo)
                tvName.text = item.displayName

                val response = viewModel.getEventResponse(
                    this@ResponderActivity,
                    eventData,
                    item.id
                )
                if (response != null) {
                    if (response == "true") {
                        radioButtonYes.isChecked = true
                    } else if (response == "false") {
                        radioButtonNo.isChecked = true
                    }
                }

                radioButtonNo.apply {
                    setOnCheckedChangeListener { button, isChecked ->
                        if (isChecked) {
                            viewModel.addResponse(
                                this@ResponderActivity, eventData, item.id, "false"
                            )
                        }
                    }
                }
                radioButtonYes.apply {
                    setOnCheckedChangeListener { button, isChecked ->
                        if (isChecked) {
                            viewModel.addResponse(
                                this@ResponderActivity, eventData, item.id, "true"
                            )
                        }
                    }
                }
                binding.lnParentView.addView(itemView)
            }

            "LONG_TEXT" -> {

                val itemView = inflater.inflate(
                    R.layout.item_long_edittext,
                    binding.lnParentView,
                    false
                ) as LinearLayout

                val tvName = itemView.findViewById<TextView>(R.id.tv_name)
                val tvElement = itemView.findViewById<TextView>(R.id.tv_element)
                val textInputLayout =
                    itemView.findViewById<TextInputLayout>(R.id.textInputLayout)
                val editText = itemView.findViewById<TextInputEditText>(R.id.editText)
                tvName.text = item.displayName
                tvElement.text = item.id
                val response = viewModel.getEventResponse(
                    this@ResponderActivity,
                    eventData,
                    item.id
                )
                if (response != null) {
                    editText.setText(response)
                }

                editText.apply {
                    addTextChangedListener(object : TextWatcher {
                        override fun beforeTextChanged(
                            s: CharSequence?,
                            start: Int,
                            count: Int,
                            after: Int
                        ) {
                            // This method is called before the text is changed.
                        }

                        override fun onTextChanged(
                            s: CharSequence?,
                            start: Int,
                            before: Int,
                            count: Int
                        ) {
                            if (s != null) {
                                viewModel.addResponse(
                                    context, eventData,
                                    item.id,
                                    s.toString()
                                )
                            }
                        }

                        override fun afterTextChanged(s: Editable?) {
                            // This method is called after the text has changed.
                            // You can perform actions here based on the updated text.
                        }
                    })
                }
                binding.lnParentView.addView(itemView)
            }

            "NUMBER" -> {

                val itemView = inflater.inflate(
                    R.layout.item_number_edittext,
                    binding.lnParentView,
                    false
                ) as LinearLayout

                val tvName = itemView.findViewById<TextView>(R.id.tv_name)
                val tvElement = itemView.findViewById<TextView>(R.id.tv_element)
                val textInputLayout =
                    itemView.findViewById<TextInputLayout>(R.id.textInputLayout)
                val editText = itemView.findViewById<TextInputEditText>(R.id.editText)
                tvName.text = item.displayName
                tvElement.text = item.id
                val response = viewModel.getEventResponse(
                    this@ResponderActivity, eventData, item.id
                )
                if (response != null) {
                    editText.setText(response)
                }

                editText.apply {
                    addTextChangedListener(object : TextWatcher {
                        override fun beforeTextChanged(
                            s: CharSequence?,
                            start: Int,
                            count: Int,
                            after: Int
                        ) {
                            // This method is called before the text is changed.
                        }

                        override fun onTextChanged(
                            s: CharSequence?,
                            start: Int,
                            before: Int,
                            count: Int
                        ) {
                            if (s != null) {
                                viewModel.addResponse(
                                    context, eventData, item.id, s.toString()
                                )
                            }
                        }

                        override fun afterTextChanged(s: Editable?) {
                            // This method is called after the text has changed.
                            // You can perform actions here based on the updated text.
                        }
                    })
                }
                binding.lnParentView.addView(itemView)
            }

            "INTEGER_POSITIVE" -> {

                val itemView = inflater.inflate(
                    R.layout.item_number_edittext,
                    binding.lnParentView,
                    false
                ) as LinearLayout

                val tvName = itemView.findViewById<TextView>(R.id.tv_name)
                val tvElement = itemView.findViewById<TextView>(R.id.tv_element)
                val textInputLayout =
                    itemView.findViewById<TextInputLayout>(R.id.textInputLayout)
                val editText = itemView.findViewById<TextInputEditText>(R.id.editText)
                tvName.text = item.displayName
                tvElement.text = item.id
                val response = viewModel.getEventResponse(
                    this@ResponderActivity,
                    eventData,
                    item.id
                )
                if (response != null) {
                    editText.setText(response)
                }

                editText.apply {
                    addTextChangedListener(object : TextWatcher {
                        override fun beforeTextChanged(
                            s: CharSequence?,
                            start: Int,
                            count: Int,
                            after: Int
                        ) {
                            // This method is called before the text is changed.
                        }

                        override fun onTextChanged(
                            s: CharSequence?,
                            start: Int,
                            before: Int,
                            count: Int
                        ) {
                            if (s != null) {
                                viewModel.addResponse(
                                    context, eventData, item.id, s.toString()
                                )
                            }
                        }

                        override fun afterTextChanged(s: Editable?) {
                            // This method is called after the text has changed.
                            // You can perform actions here based on the updated text.
                        }
                    })
                }
                binding.lnParentView.addView(itemView)
            }

            "TRUE_ONLY" -> {
                val itemView = inflater.inflate(
                    R.layout.item_check_box,
                    binding.lnParentView,
                    false
                ) as LinearLayout

                val checkBox = itemView.findViewById<CheckBox>(R.id.checkBox)
                val tvName = itemView.findViewById<TextView>(R.id.tv_name)
                tvName.text = item.displayName
                val response = viewModel.getEventResponse(
                    this@ResponderActivity,
                    eventData,
                    item.id
                )
                if (response != null) {
                    if (response == "true") {
                        checkBox.isChecked = true
                    }
                }
                checkBox.setOnCheckedChangeListener { buttonView, isChecked ->
                    if (isChecked) {
                        viewModel.addResponse(
                            this@ResponderActivity,
                            this.eventData, item.id, "true"
                        )
                    } else {
                        viewModel.deleteResponse(
                            this@ResponderActivity, eventData.id.toString(), item.id,
                        )
                    }
                }
                binding.lnParentView.addView(itemView)
            }

            "ORGANISATION_UNIT" -> {

                val itemView = inflater.inflate(
                    R.layout.item_org_edittext,
                    binding.lnParentView,
                    false
                ) as LinearLayout

                val tvName = itemView.findViewById<TextView>(R.id.tv_name)
                val tvElement = itemView.findViewById<TextView>(R.id.tv_element)
                val textInputLayout =
                    itemView.findViewById<TextInputLayout>(R.id.textInputLayout)
                val editText = itemView.findViewById<TextInputEditText>(R.id.editText)
                tvName.text = item.displayName
                tvElement.text = item.id
                val response = viewModel.getEventResponse(
                    this@ResponderActivity, eventData, item.id
                )
                if (response != null) {
                    editText.setText(response)
                }

                editText.apply {
                    addTextChangedListener(object : TextWatcher {
                        override fun beforeTextChanged(
                            s: CharSequence?,
                            start: Int,
                            count: Int,
                            after: Int
                        ) {
                            // This method is called before the text is changed.
                        }

                        override fun onTextChanged(
                            s: CharSequence?,
                            start: Int,
                            before: Int,
                            count: Int
                        ) {
                            if (s != null) {
                                viewModel.addResponse(
                                    context, eventData, item.id, s.toString()
                                )
                            }
                        }

                        override fun afterTextChanged(s: Editable?) {
                            // This method is called after the text has changed.
                            // You can perform actions here based on the updated text.
                        }
                    })
                }
                binding.lnParentView.addView(itemView)
            }

            "PHONE_NUMBER" -> {

                val itemView = inflater.inflate(
                    R.layout.item_phone_number_edittext,
                    binding.lnParentView,
                    false
                ) as LinearLayout

                val tvName = itemView.findViewById<TextView>(R.id.tv_name)
                val tvElement = itemView.findViewById<TextView>(R.id.tv_element)
                val textInputLayout =
                    itemView.findViewById<TextInputLayout>(R.id.textInputLayout)
                val editText = itemView.findViewById<TextInputEditText>(R.id.editText)
                tvName.text = item.displayName
                tvElement.text = item.id
                val response = viewModel.getEventResponse(
                    this@ResponderActivity, eventData, item.id
                )
                if (response != null) {
                    editText.setText(response)
                }

                editText.apply {
                    addTextChangedListener(object : TextWatcher {
                        override fun beforeTextChanged(
                            s: CharSequence?,
                            start: Int,
                            count: Int,
                            after: Int
                        ) {
                            // This method is called before the text is changed.
                        }

                        override fun onTextChanged(
                            s: CharSequence?,
                            start: Int,
                            before: Int,
                            count: Int
                        ) {
                            if (s != null) {
                                viewModel.addResponse(
                                    context, eventData, item.id, s.toString()
                                )
                            }
                        }

                        override fun afterTextChanged(s: Editable?) {
                            // This method is called after the text has changed.
                            // You can perform actions here based on the updated text.
                        }
                    })
                }
                binding.lnParentView.addView(itemView)
            }
        }
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                // Handle the back button click (if needed)
                onBackPressed()
                return true
            }
            // Handle other menu item clicks if you have any
        }
        return super.onOptionsItemSelected(item)
    }
}