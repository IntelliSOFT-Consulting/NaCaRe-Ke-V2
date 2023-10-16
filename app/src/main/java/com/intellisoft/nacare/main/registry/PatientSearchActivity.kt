package com.intellisoft.nacare.main.registry

import android.app.Application
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.button.MaterialButton
import com.google.gson.Gson
import com.intellisoft.nacare.adapter.ElementAdapter
import com.intellisoft.nacare.helper_class.CodeValue
import com.intellisoft.nacare.helper_class.DataElementItem
import com.intellisoft.nacare.helper_class.FormatterClass
import com.intellisoft.nacare.helper_class.ProgramStageSections
import com.intellisoft.nacare.network_request.RetrofitCalls
import com.intellisoft.nacare.room.Converters
import com.intellisoft.nacare.room.EventData
import com.intellisoft.nacare.room.MainViewModel
import com.intellisoft.nacare.util.AppUtils.isOnline
import com.intellisoft.nacare.util.AppUtils.noConnection
import com.intellisoft.nacare.viewmodels.NetworkViewModel
import com.nacare.ke.capture.R
import com.nacare.ke.capture.databinding.ActivityPatientSearchBinding
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.gson.reflect.TypeToken
import com.intellisoft.nacare.models.Constants

class PatientSearchActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPatientSearchBinding
    private lateinit var viewModel: MainViewModel
    private lateinit var eventData: EventData
    private val formatterClass = FormatterClass()
    private val dataList: MutableList<DataElementItem> = mutableListOf()
    private lateinit var dialog: AlertDialog
    private val retrofitCalls = RetrofitCalls()
    private val collectedInputs = mutableListOf<CodeValue>()
    private lateinit var networkModel: NetworkViewModel
    private val inputFieldMap = mutableMapOf<String, View>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPatientSearchBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        viewModel = MainViewModel((this.applicationContext as Application))
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
                supportActionBar?.apply {
                    title = name
                    setDisplayHomeAsUpEnabled(true)

                }

            }
        }
        binding.apply {
            nextButton.setOnClickListener {
                collectedInputs.clear()
                for ((id, view) in inputFieldMap) {
                    when (view) {
                        is TextInputEditText -> {
                            val input = view.text.toString()
                            if (input.isNotEmpty()) {
                                val dt = CodeValue(
                                    id = id,
                                    value = input
                                )
                                collectedInputs.add(dt)
                            }
                        }
                        // Handle other view types if needed
                    }
                }

                if (collectedInputs.size > 0) {
                    performPatientSearch(collectedInputs, layoutInflater, eventData)
//                    Toast.makeText(
//                        this@PatientSearchActivity,
//                        "Found Inputs\nCode -> $collectedInputs",
//                        Toast.LENGTH_SHORT
//                    ).show()
                } else {
                    Toast.makeText(
                        this@PatientSearchActivity, "Please provide and input", Toast.LENGTH_SHORT
                    ).show()
                }
            }
            /*  nextButton.setOnClickListener {
                  collectedInputs.clear()
                  for (i in 0 until binding.recyclerView.adapter!!.itemCount) {
                      when (val viewHolder = recyclerView.findViewHolderForAdapterPosition(i)) {
                          is ElementAdapter.EditTextViewHolder -> {
                              val input =
                                  viewHolder.itemView.findViewById<TextInputEditText>(R.id.editText).text.toString()                           //editText.text.toString()
                              val code =
                                  viewHolder.itemView.findViewById<TextView>(R.id.tv_element).text.toString()
                              if (input.isNotEmpty()) {
                                  val dt = CodeValue(
                                      id = code, value = input
                                  )
                                  collectedInputs.add(dt)
                              }
                          }

                          is ElementAdapter.AutoCompleteViewHolder -> {
                              val input =
                                  viewHolder.itemView.findViewById<AutoCompleteTextView>(R.id.autoCompleteTextView).text.toString()
                              val code =
                                  viewHolder.itemView.findViewById<TextView>(R.id.tv_element).text.toString()
                              if (input.isNotEmpty()) {
                                  val dt = CodeValue(
                                      id = code, value = input
                                  )
                                  collectedInputs.add(dt)
                              }
                          }
                      }
                  }

                  if (collectedInputs.size > 0) {
                      performPatientSearch(collectedInputs, layoutInflater, eventData)
                  } else {
                      Toast.makeText(
                          this@PatientSearchActivity, "Please provide and input", Toast.LENGTH_SHORT
                      ).show()
                  }
              }*/
        }
        networkModel = ViewModelProvider(this).get(NetworkViewModel::class.java)

        // Observe boolean data changes
        networkModel.booleanData.observe(this, Observer { boolean ->
            if (boolean) {
                binding.progressBar.visibility = View.VISIBLE
            } else {
                binding.progressBar.visibility = View.GONE
            }
        })
    }

    private fun performPatientSearch(
        collectedInputs: MutableList<CodeValue>,
        layoutInflater: LayoutInflater,
        eventData: EventData
    ) {
        if (isOnline(this@PatientSearchActivity)) {
            val searchParametersString =
                collectedInputs.joinToString(separator = ",") { filterItem ->
                    "${filterItem.id}:ilike:${filterItem.value}"
                }
            binding.progressBar.visibility = View.VISIBLE
            networkModel.setBooleanValue(true)
            val programCode = formatterClass.getSharedPref(
                Constants.PROGRAM_TRACKED_ENTITY_TYPE, this@PatientSearchActivity
            )
            if (programCode != null) {
                retrofitCalls.performPatientSearch(
                    this@PatientSearchActivity,
                    this.eventData,
                    binding.progressBar,
                    searchParametersString,
                    networkModel,
                    layoutInflater,
                    eventData,
                    programCode
                )
            }
        } else {
            noConnection(this@PatientSearchActivity)
        }
    }

    private fun showPatientSearchWarning() {
        val dialogBuilder = AlertDialog.Builder(this@PatientSearchActivity)
        val inflater = layoutInflater
        val dialogView = inflater.inflate(R.layout.confirmation_dialog, null)
        dialogBuilder.setView(dialogView)

        val tvTitle: TextView = dialogView.findViewById(R.id.tv_title)
        val tvMessage: TextView = dialogView.findViewById(R.id.tv_message)
        val nextButton: MaterialButton = dialogView.findViewById(R.id.next_button)

        tvMessage.text = "No Record found of Patient Searched with those parameters"
        nextButton.text = "Search Again"
        nextButton.setOnClickListener {
            val bundle = Bundle()
            val cc = Converters().toJsonEvent(eventData)
            bundle.putString("event", cc)
            val intent = Intent(this@PatientSearchActivity, PatientListActivity::class.java)
            intent.putExtra("data", bundle)
            startActivity(intent)
            this@PatientSearchActivity.finish()
        }
        dialog = dialogBuilder.create()
        dialog.show()
    }

    private fun displayDataElements(json: String, eventData: EventData) {
        val gson = Gson()
        val items = gson.fromJson(json, Array<ProgramStageSections>::class.java)
        items.forEach {
            it.dataElements.forEach { t ->
                dataList.add(t)
            }
        }
        Log.e("TAG", "Search Data $dataList")
        for (dataElement in dataList) {
            createInputField(dataElement)
        }

    }

    private fun createInputField(item: DataElementItem) {
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
                        this@PatientSearchActivity,
                        eventData,
                        item.id
                    )
                    if (response != null) {
                        editText.setText(response)
                    }
                    inputFieldMap[item.id] = editText
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
                        this@PatientSearchActivity,
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
                    inputFieldMap[item.id] = autoCompleteTextView
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
                }
            }
        }
    }

    private fun displayDataElementsOld(json: String, eventData: EventData) {
        val gson = Gson()
        val items = gson.fromJson(json, Array<ProgramStageSections>::class.java)
        items.forEach {
            it.dataElements.forEach { t ->
                dataList.add(t)
            }
        }
      /*  val ad = ElementAdapter(
            this@PatientSearchActivity, layoutInflater, dataList, eventData
        )*/

//        binding.recyclerView.apply {
//            layoutManager = LinearLayoutManager(this@PatientSearchActivity)
//            adapter = ad
//        }

        /*  for (i in 0 until dataList.size) {
              val jsonObject = jsonArray.getJSONObject(i)
              createInputField(jsonObject)
          }*/
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