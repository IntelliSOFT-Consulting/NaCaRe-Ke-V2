package com.capture.app.ui.patients

import android.app.Application
import android.app.DatePickerDialog
import android.content.DialogInterface
import android.os.Bundle
import android.text.Editable
import android.text.Html
import android.text.InputType
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.CheckBox
import android.widget.DatePicker
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.hbb20.CountryCodePicker
import com.capture.app.R
import com.capture.app.data.Constants
import com.capture.app.data.Constants.AGE_MONTHS
import com.capture.app.data.Constants.AGE_YEARS
import com.capture.app.data.Constants.DATE_OF_BIRTH
import com.capture.app.data.Constants.DIAGNOSIS
import com.capture.app.data.Constants.DIAGNOSIS_CATEGORY
import com.capture.app.data.Constants.DIAGNOSIS_SITE
import com.capture.app.data.Constants.ICD_CODE
import com.capture.app.data.Constants.OPEN_FOR_EDITING
import com.capture.app.data.FormatterClass
import com.capture.app.databinding.ActivityPatientResponderBinding
import com.capture.app.model.Attribute
import com.capture.app.model.AttributeValues
import com.capture.app.model.CodeValuePairPatient
import com.capture.app.model.DataElements
import com.capture.app.model.DataValue

import com.capture.app.model.ExpandableItem
import com.capture.app.model.Option
import com.capture.app.model.ParentAttributeValues
import com.capture.app.model.ProgramStageSections
import com.capture.app.model.RefinedAttributeValues
import com.capture.app.model.TrackedEntityAttributes
import com.capture.app.model.TrackedEntityInstanceAttributes
import com.capture.app.room.Converters
import com.capture.app.room.EnrollmentEventData
import com.capture.app.room.MainViewModel
import com.capture.app.ui.viewmodel.ResponseViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.lang.reflect.Type
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Date


class PatientResponderActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPatientResponderBinding
    private var searchParameters = ArrayList<CodeValuePairPatient>()
    private lateinit var viewModel: MainViewModel
    private lateinit var liveData: ResponseViewModel
    private val formatter = FormatterClass()
    private val emptyList = ArrayList<TrackedEntityAttributes>()
    private val elementList = ArrayList<ProgramStageSections>()
    private val completeList = ArrayList<TrackedEntityAttributes>()
    private val searchList = ArrayList<TrackedEntityAttributes>()
    private val expandableList = ArrayList<ExpandableItem>()
    private val attributeValueList = ArrayList<DataValue>()
    private val newCaseResponses = ArrayList<TrackedEntityInstanceAttributes>()
    private var linearLayouts: Array<LinearLayout?>? = null
    private var attributeList = ArrayList<ParentAttributeValues>()
    private var requiredFieldsString = ArrayList<String>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPatientResponderBinding.inflate(layoutInflater)
        setContentView(binding.root)
        viewModel = MainViewModel(this.applicationContext as Application)
        liveData = ViewModelProvider(this).get(ResponseViewModel::class.java)
        attributeList.clear()
        requiredFieldsString.clear()
        val current_patient = formatter.getSharedPref("current_patient_id", this)
        if (current_patient != null) {
            populateAvailableData(current_patient)
        }
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
        }
        loadProgramDetails()
    }


    override fun onBackPressed() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Exit")
        builder.setMessage("Are you sure you want to exit?\nYou changes will not be saved")
        builder.setPositiveButton("Yes") { dialogInterface: DialogInterface, _: Int ->
            // Finish the activity if "Yes" is clicked
            dialogInterface.dismiss()
            super.onBackPressed()
        }
        builder.setNegativeButton("No") { dialogInterface: DialogInterface, _: Int ->
            // Dismiss the dialog if "No" is clicked
            dialogInterface.dismiss()
        }
        val dialog = builder.create()
        dialog.show()
    }

    private fun populateAvailableData(currentPatient: String) {
        CoroutineScope(Dispatchers.IO).launch {
            val data = viewModel.loadTrackedEntity(currentPatient)
            if (data != null) {
                liveData.updatePatientDetails(data.isSubmitted)
                formatter.saveSharedPref(
                    "isSubmitted",
                    "${data.isSubmitted}",
                    this@PatientResponderActivity
                )
                val attributes = Converters().fromJsonAttribute(data.attributes)
                attributes.forEachIndexed { index, attribute ->
                    if (attribute.attribute == DATE_OF_BIRTH) {
                        try {
                            if (attribute.value.isNotEmpty()) {
                                val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
                                val date = formatter.convertDateFormat(attribute.value)
                                val birthDate = LocalDate.parse(date, dateFormatter)
                                // Get the current date
                                val currentDate = LocalDate.now()
                                val (years, months) = formatter.calculateAge(birthDate, currentDate)
                                saveValued(index, AGE_YEARS, "$years", false)
                                saveValued(index, AGE_MONTHS, "$months", false)
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                    if (attribute.attribute == DIAGNOSIS) {
                        try {
                            // load sites
                            val site =
                                viewModel.loadDataStore(this@PatientResponderActivity, "site")
                            //load categories
                            val category =
                                viewModel.loadDataStore(this@PatientResponderActivity, "category")

                            if (site != null && attribute.value.isNotEmpty()) {
                                val siteValue =
                                    formatter.generateRespectiveValue(site, attribute.value)
                                if (siteValue.isNotEmpty()) {
                                    saveValued(index, DIAGNOSIS_SITE, siteValue, false)
                                }
                            }
                            if (category != null && attribute.value.isNotEmpty()) {
                                val categoryValue =
                                    formatter.generateRespectiveValue(category, attribute.value)
                                if (categoryValue.isNotEmpty()) {
                                    saveValued(
                                        index, DIAGNOSIS_CATEGORY, categoryValue, false
                                    )
                                }
                            }
                            saveValued(index, ICD_CODE, attribute.value, false)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }

                    }
                    saveValued(index, attribute.attribute, attribute.value, false)
                }

                val eventUid = formatter.getSharedPref("eventUid", this@PatientResponderActivity)
                if (eventUid != null) {
                    val dataEnrollment =
                        viewModel.loadEnrollment(this@PatientResponderActivity, eventUid)
                    if (dataEnrollment != null) {

                        if (dataEnrollment.dataValues.isNotEmpty()) {
                            val elementAttributes =
                                Converters().fromJsonDataAttribute(dataEnrollment.dataValues)
                            elementAttributes.forEachIndexed { index, attribute ->

                                saveValued(index, attribute.dataElement, attribute.value, true)
                            }
                        }
                    }
                }
            }
        }
    }

    private fun loadProgramDetails() {
        CoroutineScope(Dispatchers.Main).launch {
            val patientUid =
                formatter.getSharedPref("current_patient_id", this@PatientResponderActivity)
            if (patientUid != null) {
                val data =
                    viewModel.loadSingleProgram(this@PatientResponderActivity, "notification")
                if (data != null) {
                    val converters = Converters().fromJson(data.jsonData)
                    searchList.clear()
                    emptyList.clear()
                    completeList.clear()
                    converters.programs.forEach { it ->
                        it.programSections.forEach {
                            if (it.name == "SEARCH PATIENT") {
                                val section = it.trackedEntityAttributes
                                searchList.addAll(section)

                            } else {
                                val filteredSections =
                                    it.trackedEntityAttributes.filter { section ->
                                        section.id != OPEN_FOR_EDITING
                                    }
                                emptyList.addAll(filteredSections)


                            }
                        }
                        elementList.clear()
                        it.programStages.forEach { q ->
                            q.programStageSections.forEach {
                                elementList.add(it)
                            }

                        }
                    }
                    completeList.addAll(searchList)
                    completeList.addAll(emptyList)

                    expandableList.add(
                        ExpandableItem(
                            groupName = "Patient Details and Cancer Information",
                            dataElements = Gson().toJson(completeList),
                            programUid = formatter.getSharedPref(
                                "programUid", this@PatientResponderActivity
                            ).toString(),
                            programStageUid = formatter.getSharedPref(
                                "programUid", this@PatientResponderActivity
                            ).toString(),
                            selectedOrgUnit = formatter.getSharedPref(
                                "orgCode", this@PatientResponderActivity
                            ).toString(),
                            selectedTei = patientUid,
                            isExpanded = false,
                            isProgram = false
                        )
                    )

                    elementList.forEach {
                        expandableList.add(
                            ExpandableItem(
                                groupName = it.displayName,
                                dataElements = Gson().toJson(it.dataElements),
                                programUid = formatter.getSharedPref(
                                    "programUid", this@PatientResponderActivity
                                ).toString(),
                                programStageUid = formatter.getSharedPref(
                                    "programStage", this@PatientResponderActivity
                                ).toString(),
                                selectedOrgUnit = formatter.getSharedPref(
                                    "orgCode", this@PatientResponderActivity
                                ).toString(),
                                selectedTei = patientUid,
                                isExpanded = false,
                                isProgram = true
                            )
                        )
                    }
                    binding.syncProgressBar.visibility = View.GONE
                    expandableList.forEachIndexed { index, item ->
                        createFormField(index, item, expandableList.size)
                    }
                }
            } else {
                Toast.makeText(
                    this@PatientResponderActivity,
                    "Please select Patient to proceed",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun createFormField(index: Int, data: ExpandableItem, size: Int) {
        binding.apply {
            val inflater = LayoutInflater.from(this@PatientResponderActivity)
            val itemView = inflater.inflate(R.layout.list_layout_tracked, null) as LinearLayout

            // Find the TextView inside the custom layout

            val smallTextView: TextView = itemView.findViewById(R.id.smallTextView)
            val textViewName: TextView = itemView.findViewById(R.id.textViewName)
            val linearLayout: LinearLayout = itemView.findViewById(R.id.linearLayout)
            val lnLinearLayout: LinearLayout = itemView.findViewById(R.id.lnLinearLayout)
            val ln_with_buttons: LinearLayout = itemView.findViewById(R.id.ln_with_buttons)
            val rotationImageView: ImageView = itemView.findViewById(R.id.rotationImageView)
            val yes_button: MaterialButton = itemView.findViewById(R.id.yes_button)
            val no_button: MaterialButton = itemView.findViewById(R.id.no_button)
            // Set text and other properties if needed
            textViewName.text = data.groupName
            val isRegistration =
                formatter.getSharedPref("isRegistration", this@PatientResponderActivity)
            val underTreatment =
                formatter.getSharedPref("underTreatment", this@PatientResponderActivity)
            if (isRegistration != null) {
                if (underTreatment != null) {
                    if (underTreatment == "true") {
                        if (index == 1) {
                            ln_with_buttons.visibility = View.VISIBLE
                        }
                    } else {
                        if (index == 2) {
                            ln_with_buttons.visibility = View.VISIBLE
                        }
                    }
                } else {
                    if (index == 2) {
                        ln_with_buttons.visibility = View.VISIBLE
                    }
                }
            } else {
                if (index == 0) {
                    ln_with_buttons.visibility = View.VISIBLE
                }
            }
            if (data.isProgram) {
                no_button.visibility = View.GONE
            }

            itemView.setOnClickListener {

                for (i in 0 until lnParent.childCount) {
                    val childView = lnParent.getChildAt(i)
                    val lnWithButtons = childView.findViewById<LinearLayout>(R.id.ln_with_buttons)

                    if (childView == itemView) { // For clicked item
                        // Toggle visibility and appearance of ln_with_buttons
                        ln_with_buttons.visibility =
                            if (ln_with_buttons.isVisible) View.GONE else View.VISIBLE
                        val bgColor =
                            if (ln_with_buttons.isVisible) R.color.selected else R.color.unselected
                        lnLinearLayout.setBackgroundColor(
                            ContextCompat.getColor(
                                this@PatientResponderActivity, bgColor
                            )
                        )
                        rotationImageView.rotation = if (ln_with_buttons.isVisible) 0f else 180f
                    } else { // For other items
                        // Hide ln_with_buttons
                        lnWithButtons.visibility = View.GONE
                        lnLinearLayout.setBackgroundColor(
                            ContextCompat.getColor(
                                this@PatientResponderActivity, R.color.unselected
                            )
                        )
                        rotationImageView.rotation = 180f
                    }
                }
            }

            if (!data.isProgram) {

                val isSubmitted = formatter.getSharedPref(
                    "isSubmitted",
                    this@PatientResponderActivity
                )
                if (isSubmitted != null) {
                    if (isSubmitted == "true") {
                        liveData.updatePatientDetails(true)
                    } else {
                        liveData.updatePatientDetails(false)
                    }
                }
                val gson = Gson()
                val listType: Type = object : TypeToken<List<TrackedEntityAttributes>>() {}.type
                val dataElements: List<TrackedEntityAttributes> =
                    gson.fromJson(data.dataElements, listType)
                smallTextView.text = "0/${dataElements.count()}"
                linearLayout.removeAllViews()
                for (element in dataElements) {

                    attributeList.add(
                        ParentAttributeValues(
                            element.name, element.id, element.attributeValues
                        )
                    )
                    createFormFieldsAttribute(
                        index, element, linearLayout, extractCurrentValues(element.id), false
                    )
                }
            }
            if (data.isProgram) {
                GlobalScope.launch(Dispatchers.IO) {
                    val gson = Gson()
                    val listType: Type = object : TypeToken<List<DataElements>>() {}.type
                    val dataElements: List<DataElements> =
                        gson.fromJson(data.dataElements, listType)
                    launch(Dispatchers.Main) {
                        smallTextView.text = "0/${dataElements.count()}"
                        linearLayout.removeAllViews()
                        for (element in dataElements) {

                            attributeList.add(
                                ParentAttributeValues(
                                    element.displayName, element.id, element.attributeValues
                                )
                            )
                            createFormFields(
                                index,
                                element,
                                linearLayout,
                                extractCurrentValues(element.id),
                                false
                            )
                        }
                    }
                }
            }
            yes_button.apply {
                setOnClickListener {
                    val patientUid = formatter.getSharedPref(
                        "current_patient_id", this@PatientResponderActivity
                    )
                    searchParameters = getSavedValues()
                    //check if program, ensure all fields are entered
                    if (!data.isProgram) {
                        if (!allRequiredFieldsComplete()) {
                            Toast.makeText(
                                this@PatientResponderActivity,
                                "Please enter all required fields",
                                Toast.LENGTH_SHORT
                            ).show()
                            return@setOnClickListener
                        }
                    }
                    if (patientUid != null) {
                        newCaseResponses.clear()
                        searchParameters.forEach {
                            if (!it.isProgram) {
                                newCaseResponses.add(
                                    TrackedEntityInstanceAttributes(
                                        attribute = it.code, value = it.value
                                    )
                                )
                            }
                        }
                        val isSimilarCase = confirmIfPatientHasAnotherCase(
                            newCaseResponses, patientUid, DIAGNOSIS
                        )

                        if (!isSimilarCase) {

                            val dialog: AlertDialog
                            val dialogBuilder = AlertDialog.Builder(this@PatientResponderActivity)
                            val dialogView =
                                layoutInflater.inflate(R.layout.item_submit_cancel, null)
                            dialogBuilder.setView(dialogView)

                            val tvTitle: TextView = dialogView.findViewById(R.id.tv_title)
                            val tvMessage: TextView = dialogView.findViewById(R.id.tv_message)
                            val nextButton: MaterialButton =
                                dialogView.findViewById(R.id.yes_button)
                            dialog = dialogBuilder.create()
                            tvTitle.text = context.getString(R.string.search_results)
                            tvMessage.text = context.getString(R.string.save_and_continue)
                            nextButton.setOnClickListener {
                                dialog.dismiss()
                                attributeValueList.clear()
                                newCaseResponses.clear()
                                searchParameters.forEach {
                                    if (it.isProgram) {
                                        val attr = DataValue(
                                            dataElement = it.code, value = it.value
                                        )
                                        attributeValueList.add(attr)
                                    } else {
                                        newCaseResponses.add(
                                            TrackedEntityInstanceAttributes(
                                                attribute = it.code, value = it.value
                                            )
                                        )
                                    }
                                }

                                if (data.isProgram) {


                                    val payload = EnrollmentEventData(
                                        dataValues = Gson().toJson(attributeValueList),
                                        uid = formatter.generateUUID(11),
                                        eventUid = formatter.getSharedPref(
                                            "eventUid", this@PatientResponderActivity
                                        ).toString(),
                                        program = data.programUid,
                                        programStage = data.programStageUid,
                                        orgUnit = formatter.getSharedPref(
                                            "orgCode", this@PatientResponderActivity
                                        ).toString(),
                                        eventDate = formatter.formatCurrentDate(Date()),
                                        status = "ACTIVE",
                                        trackedEntity = formatter.getSharedPref(
                                            "current_patient_id", this@PatientResponderActivity
                                        ).toString()
                                    )
                                    viewModel.addProgramStage(
                                        this@PatientResponderActivity, payload
                                    )
                                } else {
                                    viewModel.updateTrackedAttributes(
                                        Gson().toJson(newCaseResponses), patientUid
                                    )
                                }
                                try {
                                    val nextPage = index + 1
                                    if (nextPage == size) {
                                        this@PatientResponderActivity.finish()
                                    } else {

                                        // get the items at this index

                                        val currentChildView = lnParent.getChildAt(index)
                                        val childView = lnParent.getChildAt(nextPage)
                                        val currentLnWithButtons =
                                            currentChildView.findViewById<LinearLayout>(R.id.ln_with_buttons)
                                        val currentRotationImageView: ImageView =
                                            currentChildView.findViewById(R.id.rotationImageView)
                                        val lnWithButtons =
                                            childView.findViewById<LinearLayout>(R.id.ln_with_buttons)
                                        val nextRotationImageView: ImageView =
                                            childView.findViewById(R.id.rotationImageView)
                                        currentLnWithButtons.visibility = View.GONE
                                        lnWithButtons.visibility = View.VISIBLE

                                        currentRotationImageView.rotation = 180f

                                        nextRotationImageView.rotation = 0f
                                    }
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                    Log.e("TAG", "Navigation Error ${e.message}")
                                }

                            }
                            dialog.show()

                        } else {

                            val similarId = formatter.getSharedPref(
                                "found_exiting_case", this@PatientResponderActivity
                            )

                            if (similarId == patientUid) {
                                val dialog: AlertDialog
                                val dialogBuilder =
                                    AlertDialog.Builder(this@PatientResponderActivity)
                                val dialogView =
                                    layoutInflater.inflate(R.layout.item_submit_cancel, null)
                                dialogBuilder.setView(dialogView)

                                val tvTitle: TextView = dialogView.findViewById(R.id.tv_title)
                                val tvMessage: TextView = dialogView.findViewById(R.id.tv_message)
                                val nextButton: MaterialButton =
                                    dialogView.findViewById(R.id.yes_button)
                                dialog = dialogBuilder.create()
                                tvTitle.text = context.getString(R.string.search_results)
                                tvMessage.text = context.getString(R.string.save_and_continue)
                                nextButton.setOnClickListener {
                                    dialog.dismiss()
                                    attributeValueList.clear()
                                    newCaseResponses.clear()
                                    searchParameters.forEach {
                                        if (it.isProgram) {
                                            val attr = DataValue(
                                                dataElement = it.code, value = it.value
                                            )
                                            attributeValueList.add(attr)
                                        } else {
                                            newCaseResponses.add(
                                                TrackedEntityInstanceAttributes(
                                                    attribute = it.code, value = it.value
                                                )
                                            )
                                        }
                                    }

                                    if (data.isProgram) {


                                        val payload = EnrollmentEventData(
                                            dataValues = Gson().toJson(attributeValueList),
                                            uid = formatter.generateUUID(11),
                                            eventUid = formatter.getSharedPref(
                                                "eventUid", this@PatientResponderActivity
                                            ).toString(),
                                            program = data.programUid,
                                            programStage = data.programStageUid,
                                            orgUnit = formatter.getSharedPref(
                                                "orgCode", this@PatientResponderActivity
                                            ).toString(),
                                            eventDate = formatter.formatCurrentDate(Date()),
                                            status = "ACTIVE",
                                            trackedEntity = formatter.getSharedPref(
                                                "current_patient_id", this@PatientResponderActivity
                                            ).toString()
                                        )
                                        viewModel.addProgramStage(
                                            this@PatientResponderActivity, payload
                                        )
                                    } else {
                                        liveData.updatePatientDetails(true)
                                        viewModel.updateTrackedAttributes(
                                            Gson().toJson(newCaseResponses), patientUid
                                        )
                                    }

                                    try {
                                        val nextPage = index + 1
                                        if (nextPage == size) {
                                            this@PatientResponderActivity.finish()
                                        } else {

                                            val currentChildView = lnParent.getChildAt(index)
                                            val childView = lnParent.getChildAt(nextPage)

                                            val currentLnWithButtons =
                                                currentChildView.findViewById<LinearLayout>(R.id.ln_with_buttons)
                                            val currentRotationImageView: ImageView =
                                                currentChildView.findViewById(R.id.rotationImageView)
                                            val lnWithButtons =
                                                childView.findViewById<LinearLayout>(R.id.ln_with_buttons)
                                            val nextRotationImageView: ImageView =
                                                childView.findViewById(R.id.rotationImageView)
                                            currentLnWithButtons.visibility = View.GONE
                                            lnWithButtons.visibility = View.VISIBLE

                                            currentRotationImageView.rotation = 180f

                                            nextRotationImageView.rotation = 0f

                                        }
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                        Log.e("TAG", "Navigation Error ${e.message}")
                                    }

                                }
                                dialog.show()
                            } else {
                                val dialog: AlertDialog
                                val dialogBuilder =
                                    AlertDialog.Builder(this@PatientResponderActivity)
                                val dialogView =
                                    layoutInflater.inflate(R.layout.item_submit_cancel, null)
                                dialogBuilder.setView(dialogView)

                                val tvTitle: TextView = dialogView.findViewById(R.id.tv_title)
                                val tvMessage: TextView = dialogView.findViewById(R.id.tv_message)
                                val cancelButton: MaterialButton =
                                    dialogView.findViewById(R.id.no_button)
                                val nextButton: MaterialButton =
                                    dialogView.findViewById(R.id.yes_button)
                                dialog = dialogBuilder.create()
                                tvTitle.text = context.getString(R.string.existing_case)
                                tvMessage.text =
                                    context.getString(R.string.existing_case_description)
                                tvMessage.text =
                                    context.getString(R.string.existing_case_description)
                                nextButton.text = context.getString(R.string.view_existing_case)
                                nextButton.setOnClickListener {
                                    dialog.dismiss()

                                    if (similarId != null) {
                                        formatter.saveSharedPref(
                                            "current_patient_id",
                                            similarId,
                                            this@PatientResponderActivity
                                        )
                                        viewModel.deleteCurrentSimilarCase(
                                            this@PatientResponderActivity, patientUid
                                        )
                                        Log.e("TAG", "Entity Data Here **** $similarId")
                                        val singleRecord = viewModel.getLatestEnrollment(
                                            this@PatientResponderActivity, similarId
                                        )
                                        if (singleRecord != null) {
                                            Log.e(
                                                "TAG",
                                                "Retrieved Event *** ${singleRecord.eventUid} Patient ${singleRecord.id}"
                                            )
                                            formatter.saveSharedPref(
                                                "eventUid",
                                                singleRecord.eventUid,
                                                this@PatientResponderActivity
                                            )
                                            formatter.saveSharedPref(
                                                "reopen_form", "true", this@PatientResponderActivity
                                            )

//
//                                            startActivity(
//                                                Intent(
//                                                    this@PatientResponderActivity,
//                                                    PatientResponderActivity::class.java
//                                                )
//                                            )
                                            finish()
                                        }
                                        //means delete current patient and related data
                                    }

                                }
                                cancelButton.text = context.getString(R.string.change_diagnosis)
                                cancelButton.setOnClickListener {
                                    dialog.dismiss()
                                }
                                dialog.show()
                            }
                        }
                    }
                }
            }

            lnParent.addView(itemView)

        }
    }

    private fun allRequiredFieldsComplete(): Boolean {
        try {
            searchParameters = getSavedValues()

            val searchParameterCodes = searchParameters.map { it.code }
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

    private fun confirmIfPatientHasAnotherCase(
        newCaseResponses: ArrayList<TrackedEntityInstanceAttributes>,
        patientUid: String,
        diagnosis: String
    ): Boolean {

        Log.e("TAG", "Patient has existing Similar Case Responses $patientUid")
        var hasSimilarCase = true
        if (newCaseResponses.isEmpty()) {
            hasSimilarCase = false
        } else {
            //get all cases for the patient in question
            val existing = viewModel.loadPatientById(this@PatientResponderActivity, patientUid)
            if (existing != null) {
                val existingCases = viewModel.getPatientExistingCases(
                    this@PatientResponderActivity, existing.trackedUnique
                )
                if (existingCases != null) {
                    if (existingCases.isEmpty()) {
                        hasSimilarCase = false
                    } else {
                        // get diagnosis for the existing data
                        val caseData = newCaseResponses.find { q -> q.attribute == diagnosis }
                        if (caseData != null) {
                            existingCases.forEach {
                                val attributes = Converters().fromJsonAttribute(it.attributes)
                                val existingCaseData =
                                    attributes.find { r -> r.attribute == diagnosis }
                                if (existingCaseData != null) {

                                    if (existingCaseData.value == caseData.value) {
                                        hasSimilarCase = true
                                        val similarId = it.id
                                        formatter.saveSharedPref(
                                            "found_exiting_case",
                                            "$similarId",
                                            this@PatientResponderActivity
                                        )

                                        return true
                                    } else {
                                        hasSimilarCase = false
                                    }

                                } else {
                                    hasSimilarCase = false
                                }
                            }
                        } else {
                            hasSimilarCase = false
                        }
                    }
                } else {
                    hasSimilarCase = false
                }

            } else {
                hasSimilarCase = false
            }
        }

        return hasSimilarCase
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
                        "parent_name", parentLabel.parentName, this@PatientResponderActivity
                    )
                }
                if (parentValue.isNotEmpty()) {
                    val refinedParent = formatter.convertDateFormat(parentValue)

                    if (refinedParent != null) {
                        parentValue = refinedParent
                        val result = when (part1) {
                            "eq" -> itemValue == parentValue
                            "ne" -> itemValue != parentValue
                            "gt" -> itemValue > parentValue
                            "ge" -> itemValue >= parentValue
                            "lt" -> itemValue < parentValue
                            "le" -> itemValue <= parentValue
                            "like" -> itemValue == parentValue
                            "null" -> false
                            "notnull" -> true
                            else -> false
                        }
                        status = result

                    } else {
                        status = false
                    }

                } else {
                    status = false
                }

            } else {
                status = true
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

    private fun createFormFieldsAttribute(
        index: Int,
        item: TrackedEntityAttributes,
        lnParent: LinearLayout,
        currentValue: String,
        isProgram: Boolean
    ) {
        val valueType: String = item.valueType
        val inflater = LayoutInflater.from(this)
        val isHidden: Boolean = extractAttributeValue("Hidden", item.attributeValues)
        var isDisabled: Boolean = extractAttributeValue("Disabled", item.attributeValues)
        val isRequired: Boolean = extractAttributeValue("Required", item.attributeValues)
        val disableFutureDate: Boolean =
            extractAttributeValue("disableFutureDate", item.attributeValues)
        val showIf = showIfAttribute("showIf", item.attributeValues)
        val basicHiddenFields = isPartOfBasicInformation(item.id, formatter.excludeHiddenFields())
        val hasValidator: Boolean =
            extractValidatorAttributeValue("Validator", item.attributeValues)

        if (isRequired) {
            requiredFieldsString.add(item.id)
        }
        liveData.additionalInformationSaved.observe(this@PatientResponderActivity) {
            Log.e("TAG", "Updated Live Data **** Later $it")
            if (it) {
                isDisabled = it
            }
        }
        when (valueType) {
            "TEXT" -> {
                if (item.optionSet == null) {

                    val itemView = inflater.inflate(
                        R.layout.item_edittext, lnParent, false
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
                            editText.keyListener = null
                            editText.isCursorVisible = false
                            editText.isFocusable = false
                            editText.isEnabled = false

                            liveData.mutableListLiveDataPatient.observe(this@PatientResponderActivity) {
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
                            s: CharSequence?, start: Int, count: Int, after: Int
                        ) {
                        }

                        override fun onTextChanged(
                            s: CharSequence?, start: Int, before: Int, count: Int
                        ) {

                        }

                        override fun afterTextChanged(s: Editable?) {
                            val value = s.toString()
                            if (value.isNotEmpty()) {
                                saveValued(index, item.id, value, isProgram)
                            }
                        }
                    })


                    if (basicHiddenFields) {
                        itemView.visibility = View.GONE
                    }

                } else {
                    val itemView = inflater.inflate(
                        R.layout.item_autocomplete, lnParent, false
                    ) as LinearLayout
                    val tvName = itemView.findViewById<TextView>(R.id.tv_name)
                    val tvElement = itemView.findViewById<TextView>(R.id.tv_element)
                    val textInputLayout =
                        itemView.findViewById<TextInputLayout>(R.id.textInputLayout)
                    val autoCompleteTextView =
                        itemView.findViewById<AutoCompleteTextView>(R.id.autoCompleteTextView)
                    tvElement.text = item.id
                    val optionsStringList: MutableList<String> = ArrayList()
                    val isAllowedToSearch = formatter.retrieveAllowedToTypeItem(item.id)
                    if (isAllowedToSearch) {
                        autoCompleteTextView.inputType = InputType.TYPE_CLASS_TEXT
                    }
                    item.optionSet.options.forEach {
                        optionsStringList.add(it.displayName)
                    }
                    val adp = ArrayAdapter(
                        this, android.R.layout.simple_list_item_1, optionsStringList
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

                            liveData.mutableListLiveDataPatient.observe(this@PatientResponderActivity) {
                                val valueObtained = it.find { it.code == item.id }
                                if (valueObtained != null) {
                                    val answer = getDisplayNameFromCode(
                                        item.optionSet.options, valueObtained.value
                                    )
                                    autoCompleteTextView.setText(answer, false)
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
                            s: CharSequence?, start: Int, count: Int, after: Int
                        ) {
                        }

                        override fun onTextChanged(
                            s: CharSequence?, start: Int, before: Int, count: Int
                        ) {

                        }

                        override fun afterTextChanged(s: Editable?) {
                            val value = s.toString()
                            if (value.isNotEmpty()) {
                                val dataValue = getCodeFromText(value, item.optionSet.options)
                                if (dataValue.isNotEmpty()) {

                                    if (item.id == DIAGNOSIS) {
                                        val gender = formatter.getSharedPref(
                                            "gender",
                                            this@PatientResponderActivity
                                        )

                                        var rejectedCancerList: List<String> = emptyList()

                                        if (gender != null) {
                                            rejectedCancerList = if (gender == "Male") {
                                                formatter.femaleCancers()
                                            } else {
                                                formatter.maleCancers()
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
                                                    "$opposite Diagnosis is not application for $gender patient"
                                            } else {
                                                textInputLayout.error = null
                                                calculateRelevant(
                                                    lnParent,
                                                    index,
                                                    item,
                                                    value,
                                                    isProgram
                                                )
                                                saveValued(index, item.id, dataValue, isProgram)

                                            }
                                        } catch (e: Exception) {
                                            e.printStackTrace()
                                        }
                                    } else {

                                        calculateRelevant(lnParent, index, item, value, isProgram)
                                        saveValued(index, item.id, dataValue, isProgram)

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
                                            val validAnswer = checkProvidedAnswer(
                                                child.tag.toString(), list, dataValue
                                            )
                                            if (validAnswer) {
//                                            if (isRequired) {
//                                                requiredFieldsString.add(child.tag.toString())
//                                            }
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
                        }
                    })

                    if (basicHiddenFields) {
                        itemView.visibility = View.GONE
                    }
                }
            }

            "DATE" -> {
                val itemView = inflater.inflate(
                    R.layout.item_edittext_date, findViewById(R.id.lnParent), false
                ) as LinearLayout
                val tvName = itemView.findViewById<TextView>(R.id.tv_name)
                val tvElement = itemView.findViewById<TextView>(R.id.tv_element)
                val textInputLayout = itemView.findViewById<TextInputLayout>(R.id.textInputLayout)
                val editText = itemView.findViewById<TextInputEditText>(R.id.editText)
                val name = if (isRequired) generateRequiredField(item.name) else item.name
                tvName.text = Html.fromHtml(name)
                tvElement.text = item.id
                editText.setKeyListener(null)
                editText.isCursorVisible = false
                editText.isFocusable = false
                if (currentValue.isNotEmpty()) {
                    val refinedDate = formatter.convertDateFormat(currentValue)
                    if (refinedDate != null) {
                        editText.setText(currentValue)
                    }
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
                        s: CharSequence?, start: Int, count: Int, after: Int
                    ) {
                    }

                    override fun onTextChanged(
                        s: CharSequence?, start: Int, before: Int, count: Int
                    ) {

                    }

                    override fun afterTextChanged(s: Editable?) {
                        val value = s.toString()
                        if (value.isNotEmpty()) {
                            //check if it is date of birth, calculate relevant
                            if (hasValidator) {
                                val passes = hasValidatorAndPasses(
                                    "Validator", value, item.attributeValues
                                )

                                val parentName = formatter.getSharedPref(
                                    "parent_name", this@PatientResponderActivity
                                )
                                if (passes) {
                                    textInputLayout.error = null
                                    calculateRelevant(lnParent, index, item, value, isProgram)
                                    saveValued(index, item.id, value, isProgram)
                                } else {
                                    textInputLayout.error =
                                        "${item.name} cannot come before $parentName"
                                }
                            } else {
                                calculateRelevant(lnParent, index, item, value, isProgram)
                                saveValued(index, item.id, value, isProgram)
                            }
                        }
                    }
                })
                if (basicHiddenFields) {
                    itemView.visibility = View.GONE
                }
            }

            "PHONE_NUMBER" -> {
                val itemView = inflater.inflate(
                    R.layout.item_edittext_phone, findViewById(R.id.lnParent), false
                ) as LinearLayout
                val tvName = itemView.findViewById<TextView>(R.id.tv_name)
                val tvElement = itemView.findViewById<TextView>(R.id.tv_element)
                val textInputLayout = itemView.findViewById<TextInputLayout>(R.id.textInputLayout)
                val editText = itemView.findViewById<TextInputEditText>(R.id.editText)
                val countryCodePicker =
                    itemView.findViewById<CountryCodePicker>(R.id.countyCodePicker)

                val name = if (isRequired) generateRequiredField(item.name) else item.name
                tvName.text = Html.fromHtml(name)
                tvElement.text = item.id
                if (currentValue.isNotEmpty()) {
                    // check if length is 12, split into 2 parts, the first 3 then remainder
                    if (currentValue.length == 12) { // Check if length is 12
                        val firstPart =
                            currentValue.substring(0, 3) // Extract the first 3 characters
                        val secondPart =
                            currentValue.substring(3) // Extract the remainder of the string
                        countryCodePicker.setCountryForPhoneCode(firstPart.toInt())//setselectedCountryCode = firstPart
                        editText.setText(secondPart) // Set the text of the editText to the formatted value
                    } else {
                        editText.setText(currentValue) // If length is not 12, set the text as it is
                    }
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
                        s: CharSequence?, start: Int, count: Int, after: Int
                    ) {
                    }

                    override fun onTextChanged(
                        s: CharSequence?, start: Int, before: Int, count: Int
                    ) {

                    }

                    override fun afterTextChanged(s: Editable?) {
                        val value = s.toString()
                        if (value.isNotEmpty()) {
                            val countryCode = countryCodePicker.selectedCountryCode
                            val completeCode = "$countryCode$value"
                            saveValued(index, item.id, completeCode, isProgram)
                        }
                    }
                })
                if (basicHiddenFields) {
                    itemView.visibility = View.GONE
                }
            }

            "INTEGER" -> {
                val itemView = inflater.inflate(
                    R.layout.item_edittext_number, lnParent, false
                ) as LinearLayout
                val tvName = itemView.findViewById<TextView>(R.id.tv_name)
                val tvElement = itemView.findViewById<TextView>(R.id.tv_element)
                val textInputLayout = itemView.findViewById<TextInputLayout>(R.id.textInputLayout)
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
                        liveData.mutableListLiveDataPatient.observe(this@PatientResponderActivity) {
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
                        s: CharSequence?, start: Int, count: Int, after: Int
                    ) {
                    }

                    override fun onTextChanged(
                        s: CharSequence?, start: Int, before: Int, count: Int
                    ) {

                    }

                    override fun afterTextChanged(s: Editable?) {
                        val value = s.toString()
                        if (value.isNotEmpty()) {
                            saveValued(index, item.id, value, isProgram)
                        }
                    }
                })

                if (basicHiddenFields) {
                    itemView.visibility = View.GONE
                }
            }

            "NUMBER" -> {
                val itemView = inflater.inflate(
                    R.layout.item_edittext_number, lnParent, false
                ) as LinearLayout
                val tvName = itemView.findViewById<TextView>(R.id.tv_name)
                val tvElement = itemView.findViewById<TextView>(R.id.tv_element)
                val textInputLayout = itemView.findViewById<TextInputLayout>(R.id.textInputLayout)
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
                        liveData.mutableListLiveDataPatient.observe(this@PatientResponderActivity) {
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
                        s: CharSequence?, start: Int, count: Int, after: Int
                    ) {
                    }

                    override fun onTextChanged(
                        s: CharSequence?, start: Int, before: Int, count: Int
                    ) {

                    }

                    override fun afterTextChanged(s: Editable?) {
                        val value = s.toString()
                        if (value.isNotEmpty()) {
                            saveValued(index, item.id, value, isProgram)
                        }
                    }
                })

                if (basicHiddenFields) {
                    itemView.visibility = View.GONE
                }
            }

            // patient Data

            "BOOLEAN" -> {
                val itemView = inflater.inflate(
                    R.layout.item_boolean_field, findViewById(R.id.lnParent), false
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
                            saveValued(index, item.id, dataValue, isProgram)
                            val list = checkIfParentHasChildren(item.id)
                            Log.e("TAG", "Selected Radio Button $dataValue List of Children $list")
                            for (i in 0 until lnParent.childCount) {
                                val child: View = lnParent.getChildAt(i)
                                // Check if any inner data of the list matches the child's tag
                                val matchFound = list.any { innerData ->
                                    // Replace the condition below with the appropriate comparison between innerData and child's tag
                                    innerData.parent == child.tag
                                }
                                if (matchFound) {
                                    val validAnswer = checkProvidedAnswer(
                                        child.tag.toString(), list, dataValue
                                    )
                                    if (validAnswer) {
                                        child.visibility = View.VISIBLE
                                        if (isRequired) {
                                            requiredFieldsString.add(child.tag.toString())
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

    private fun getDate(year: Int, month: Int, day: Int): String {
        val calendar = Calendar.getInstance()
        calendar[year, month] = day
        val date: Date = calendar.time
        return FormatterClass().formatCurrentDate(date)
    }

    private fun calculateRelevant(
        lnParent: LinearLayout,
        index: Int,
        item: TrackedEntityAttributes,
        value: String,
        isProgram: Boolean
    ) {

        when (item.id) {

            DIAGNOSIS -> {
                val dataValue = item.optionSet?.let { getCodeFromText(value, it.options) }
                // load sites
                val site = viewModel.loadDataStore(this, "site")
                //load categories
                val category = viewModel.loadDataStore(this, "category")

                if (site != null && dataValue != null) {
                    val siteValue = formatter.generateRespectiveValue(site, dataValue)

                    if (siteValue.isNotEmpty()) {
                        saveValued(index, DIAGNOSIS_SITE, siteValue, isProgram)
                    }
                }
                if (category != null && dataValue != null) {
                    val categoryValue = formatter.generateRespectiveValue(category, dataValue)
                    Log.e("TAG", "Match found: $categoryValue")
                    if (categoryValue.isNotEmpty()) {
                        saveValued(index, DIAGNOSIS_CATEGORY, categoryValue, isProgram)
                    }
                }
                saveValued(index, ICD_CODE, "$dataValue", isProgram)
            }
        }
    }

    private fun isPartOfBasicInformation(uid: String, excludeHiddenFields: List<String>): Boolean {
        return excludeHiddenFields.any { it == uid }
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


                        var previousAnswer = extractCurrentValues(part1)
                        if (previousAnswer.isNotEmpty()) {
                            previousAnswer = previousAnswer.lowercase()
                            val part3Lower = parts[2].lowercase()
                            var lowercaseAnswer = previousAnswer.lowercase()
                            if (lowercaseAnswer.contains("other", ignoreCase = true)) {
                                lowercaseAnswer = "other"
                            }
                            Log.e(
                                "TAG",
                                "Show me the Response to Compared Answer Above $previousAnswer Needed $part3Lower"
                            )
                            val result = when (part2) {
                                "eq" -> lowercaseAnswer == part3Lower
                                "ne" -> lowercaseAnswer != part3Lower
                                "notin" -> lowercaseAnswer != part3Lower
                                "gt" -> lowercaseAnswer > part3Lower
                                "ge" -> lowercaseAnswer >= part3Lower
                                "lt" -> lowercaseAnswer < part3Lower
                                "le" -> lowercaseAnswer <= part3Lower
                                "like" -> lowercaseAnswer == part3Lower
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
        target: String, attributeValueList: List<AttributeValues>
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

    private fun extractCurrentValues(id: String): String {
        val response = formatter.getSharedPref("current_data", this)

        Log.e("TAG", "Retrieving Current Data **** $response")
        if (response != null) {
            searchParameters = getSavedValues()
            val foundItem = searchParameters.find { it.code == id }
            return foundItem?.value ?: ""
        }
        return ""
    }

    private fun getDisplayNameFromCode(options: List<Option>, value: String): String {
        for (option in options) {
            if (option.code == value) {
                return option.displayName
            }
        }
        return value
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

    private fun saveValued(index: Int, id: String, value: String, isProgram: Boolean) {
        val existingIndex = searchParameters.indexOfFirst { it.code == id }
        if (existingIndex != -1) {
            // Update the existing entry if the code is found
            searchParameters[existingIndex] =
                CodeValuePairPatient(code = id, value = value, isProgram = isProgram)
        } else {
            // Add a new entry if the code is not found
            val data = CodeValuePairPatient(code = id, value = value, isProgram = isProgram)
            searchParameters.add(data)
        }
        formatter.saveSharedPref("current_data", Gson().toJson(searchParameters), this)

        if (!isProgram) {
            liveData.populateRelevantPatientData(searchParameters)
        }

    }

    private fun removeSavedItems(index: Int, id: String, isProgram: Boolean) {
        val existingIndex = searchParameters.indexOfFirst { it.code == id }
        if (existingIndex != -1) {
            searchParameters.removeAt(existingIndex)
        }
        formatter.saveSharedPref("current_data", Gson().toJson(searchParameters), this)
    }

    private fun getSavedValues(): ArrayList<CodeValuePairPatient> {
        val savedData = formatter.getSharedPref("current_data", this)
        if (savedData != null) {
            return if (savedData.isNotEmpty()) {
                Gson().fromJson(
                    savedData, object : TypeToken<ArrayList<CodeValuePairPatient>>() {}.type
                )
            } else {
                ArrayList()
            }
        }
        return ArrayList()
    }

    private fun createFormFields(
        index: Int,
        item: DataElements,
        lnParent: LinearLayout,
        currentValue: String,
        isProgram: Boolean
    ) {
        val valueType: String = item.valueType
        val inflater = LayoutInflater.from(this)
        Log.e("TAG", "Data Populated $currentValue")
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
                        R.layout.item_edittext, lnParent, false
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
                            editText.keyListener = null
                            editText.isCursorVisible = false
                            editText.isFocusable = false
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
                    editText.addTextChangedListener(object : TextWatcher {
                        override fun beforeTextChanged(
                            s: CharSequence?, start: Int, count: Int, after: Int
                        ) {
                        }

                        override fun onTextChanged(
                            s: CharSequence?, start: Int, before: Int, count: Int
                        ) {

                        }

                        override fun afterTextChanged(s: Editable?) {
                            val value = s.toString()
                            if (value.isNotEmpty()) {
                                saveValued(index, item.id, value, isProgram)
                            }
                        }
                    })

                } else {
                    val itemView = inflater.inflate(
                        R.layout.item_autocomplete, lnParent, false
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
                        this, android.R.layout.simple_list_item_1, optionsStringList
                    )
                    if (currentValue.isNotEmpty()) {
                        val answer = getDisplayNameFromCode(item.optionSet.options, currentValue)
                        autoCompleteTextView.setText(answer, false)
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
                            s: CharSequence?, start: Int, count: Int, after: Int
                        ) {
                        }

                        override fun onTextChanged(
                            s: CharSequence?, start: Int, before: Int, count: Int
                        ) {

                        }

                        override fun afterTextChanged(s: Editable?) {
                            val value = s.toString()
                            if (value.isNotEmpty()) {
                                val dataValue = getCodeFromText(value, item.optionSet.options)
                                saveValued(index, item.id, dataValue, isProgram)
                                val list = checkIfParentHasChildren(item.id)
                                for (i in 0 until lnParent.childCount) {
                                    val child: View = lnParent.getChildAt(i)
                                    // Check if any inner data of the list matches the child's tag
                                    val matchFound = list.any { innerData ->
                                        // Replace the condition below with the appropriate comparison between innerData and child's tag
                                        innerData.parent == child.tag
                                    }
                                    if (matchFound) {
                                        val validAnswer = checkProvidedAnswer(
                                            child.tag.toString(), list, dataValue
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
                    R.layout.item_edittext_date, lnParent, false
                ) as LinearLayout
                val tvName = itemView.findViewById<TextView>(R.id.tv_name)
                val tvElement = itemView.findViewById<TextView>(R.id.tv_element)
                val textInputLayout = itemView.findViewById<TextInputLayout>(R.id.textInputLayout)
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
                        s: CharSequence?, start: Int, count: Int, after: Int
                    ) {
                    }

                    override fun onTextChanged(
                        s: CharSequence?, start: Int, before: Int, count: Int
                    ) {

                    }

                    override fun afterTextChanged(s: Editable?) {
                        val value = s.toString()
                        if (value.isNotEmpty()) {
                            //check if it is date of birth, calculate relevant
//                            calculateRelevant(item, value)
                            saveValued(index, item.id, value, isProgram)
                        }
                    }
                })
            }

            "PHONE_NUMBER" -> {
                val itemView = inflater.inflate(
                    R.layout.item_edittext_phone, findViewById(R.id.lnParent), false
                ) as LinearLayout
                val tvName = itemView.findViewById<TextView>(R.id.tv_name)
                val tvElement = itemView.findViewById<TextView>(R.id.tv_element)
                val textInputLayout = itemView.findViewById<TextInputLayout>(R.id.textInputLayout)
                val editText = itemView.findViewById<TextInputEditText>(R.id.editText)
                val countryCodePicker =
                    itemView.findViewById<CountryCodePicker>(R.id.countyCodePicker)

                val name =
                    if (isRequired) generateRequiredField(item.displayName) else item.displayName
                tvName.text = Html.fromHtml(name)
                tvElement.text = item.id
                if (currentValue.isNotEmpty()) {
                    if (currentValue.isNotEmpty()) {
                        // check if length is 12, split into 2 parts, the first 3 then remainder
                        if (currentValue.length == 12) { // Check if length is 12
                            val firstPart =
                                currentValue.substring(0, 3) // Extract the first 3 characters
                            val secondPart =
                                currentValue.substring(3) // Extract the remainder of the string
                            countryCodePicker.setCountryForPhoneCode(firstPart.toInt())
                            editText.setText(secondPart) // Set the text of the editText to the formatted value
                        } else {
                            editText.setText(currentValue) // If length is not 12, set the text as it is
                        }
                    }
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
                        s: CharSequence?, start: Int, count: Int, after: Int
                    ) {
                    }

                    override fun onTextChanged(
                        s: CharSequence?, start: Int, before: Int, count: Int
                    ) {

                    }

                    override fun afterTextChanged(s: Editable?) {
                        val value = s.toString()
                        if (value.isNotEmpty()) {
                            val countryCode = countryCodePicker.selectedCountryCode
                            val completeCode = "$countryCode$value"
                            saveValued(index, item.id, completeCode, isProgram)
                        }
                    }
                })
            }

            "INTEGER" -> {
                val itemView = inflater.inflate(
                    R.layout.item_edittext_number, lnParent, false
                ) as LinearLayout
                val tvName = itemView.findViewById<TextView>(R.id.tv_name)
                val tvElement = itemView.findViewById<TextView>(R.id.tv_element)
                val textInputLayout = itemView.findViewById<TextInputLayout>(R.id.textInputLayout)
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
                        s: CharSequence?, start: Int, count: Int, after: Int
                    ) {
                    }

                    override fun onTextChanged(
                        s: CharSequence?, start: Int, before: Int, count: Int
                    ) {

                    }

                    override fun afterTextChanged(s: Editable?) {
                        val value = s.toString()
                        if (value.isNotEmpty()) {
                            saveValued(index, item.id, value, isProgram)
                        }
                    }
                })
            }

            "NUMBER" -> {
                val itemView = inflater.inflate(
                    R.layout.item_edittext_number, lnParent, false
                ) as LinearLayout
                val tvName = itemView.findViewById<TextView>(R.id.tv_name)
                val tvElement = itemView.findViewById<TextView>(R.id.tv_element)
                val textInputLayout = itemView.findViewById<TextInputLayout>(R.id.textInputLayout)
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
                        editText.keyListener = null
                        editText.isCursorVisible = false
                        editText.isFocusable = false
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
                editText.addTextChangedListener(object : TextWatcher {
                    override fun beforeTextChanged(
                        s: CharSequence?, start: Int, count: Int, after: Int
                    ) {
                    }

                    override fun onTextChanged(
                        s: CharSequence?, start: Int, before: Int, count: Int
                    ) {

                    }

                    override fun afterTextChanged(s: Editable?) {
                        val value = s.toString()
                        if (value.isNotEmpty()) {
                            saveValued(index, item.id, value, isProgram)
                        }
                    }
                })
            }

            "BOOLEAN" -> {
                val itemView = inflater.inflate(
                    R.layout.item_boolean_field, lnParent, false
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
                            saveValued(index, item.id, dataValue, isProgram)
                            val list = checkIfParentHasChildren(item.id)
                            for (i in 0 until lnParent.childCount) {
                                val child: View = lnParent.getChildAt(i)
                                // Check if any inner data of the list matches the child's tag
                                val matchFound = list.any { innerData ->
                                    // Replace the condition below with the appropriate comparison between innerData and child's tag
                                    innerData.parent == child.tag
                                }
                                if (matchFound) {
                                    val validAnswer = checkProvidedAnswer(
                                        child.tag.toString(), list, dataValue
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

            "TRUE_ONLY" -> {
                val itemView = inflater.inflate(
                    R.layout.item_checkbox_field, lnParent, false
                ) as LinearLayout
                val tvName = itemView.findViewById<TextView>(R.id.tv_name)
                val tvElement = itemView.findViewById<TextView>(R.id.tv_element)
                val checkBox = itemView.findViewById<CheckBox>(R.id.checkBox)
                val name =
                    if (isRequired) generateRequiredField(item.displayName) else item.displayName
                tvName.text = Html.fromHtml(name)
                tvElement.text = item.id
                itemView.tag = item.id
                lnParent.addView(itemView)
                var isProgrammaticChange = false
                checkBox.setOnCheckedChangeListener(null)
                when (currentValue) {
                    "true" -> {
                        checkBox.isChecked = true
                    }

                    "false" -> {
                        checkBox.isChecked = false
                    }

                    else -> {
                        checkBox.isChecked = false
                    }
                }
                checkBox.setOnCheckedChangeListener { buttonView, isChecked ->
                    if (isChecked) {
                        saveValued(index, item.id, "true", isProgram)
                    } else {
                        removeSavedItems(index, item.id, isProgram)
                    }
                }

                if (isHidden) {
                    itemView.visibility = View.GONE
                } else {
                    if (isDisabled) {
                        checkBox.isEnabled = false
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
        parent: String, list: List<RefinedAttributeValues>, dataValue: String
    ): Boolean {
        var resultResponse = false
        try {
            val single = list.singleOrNull { it.parent == parent }


            var lowercaseAnswer = dataValue.lowercase()
            if (lowercaseAnswer.contains("other", ignoreCase = true)) {
                lowercaseAnswer = "other"
            }
            Log.e(
                "TAG",
                "We are looking the answer here **** $parent value need is $dataValue \nfinal value $lowercaseAnswer and Comparator $single"
            )
            if (single != null) {
                val parts = single.value.split(':')
                if (parts.size == 3) {
                    val part1 = parts[0]
                    val part2 = parts[1]
                    val part3 = parts[2]
                    val part3Lower = parts[2].lowercase()
                    val result = when (part2) {
                        "eq" -> lowercaseAnswer == part3Lower
                        "in" -> lowercaseAnswer == part3Lower
                        "ne" -> lowercaseAnswer != part3Lower
                        "notin" -> lowercaseAnswer != part3Lower
                        "gt" -> lowercaseAnswer > part3Lower
                        "ge" -> lowercaseAnswer >= part3Lower
                        "lt" -> lowercaseAnswer < part3Lower
                        "le" -> lowercaseAnswer <= part3Lower
                        "like" -> lowercaseAnswer == part3Lower
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
                                        q.parent, currentValidator
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

}