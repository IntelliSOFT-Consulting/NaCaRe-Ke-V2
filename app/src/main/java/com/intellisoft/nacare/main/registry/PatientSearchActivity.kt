package com.intellisoft.nacare.main.registry

import android.app.Application
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.gson.Gson
import com.intellisoft.nacare.adapter.ElementAdapter
import com.intellisoft.nacare.adapter.TreeAdapter
import com.intellisoft.nacare.helper_class.DataElementItem
import com.intellisoft.nacare.helper_class.FormatterClass
import com.intellisoft.nacare.helper_class.ProgramStageSections
import com.intellisoft.nacare.room.Converters
import com.intellisoft.nacare.room.EventData
import com.intellisoft.nacare.room.MainViewModel
import com.nacare.ke.capture.R
import com.nacare.ke.capture.databinding.ActivityPatientSearchBinding
import com.nacare.ke.capture.databinding.ActivityResponderBinding

class PatientSearchActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPatientSearchBinding
    private lateinit var viewModel: MainViewModel
    private lateinit var eventData: EventData
    private val formatterClass = FormatterClass()
    private val dataList: MutableList<DataElementItem> = mutableListOf()
    private lateinit var dialog: AlertDialog
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
//                    subtitle = "$date | $org"
                    setDisplayHomeAsUpEnabled(true)
//            setHomeAsUpIndicator(R.drawable.ic_back_arrow)

                }

            }
        }
        binding.apply {

            nextButton.setOnClickListener {
//                this@ResponderActivity.finish()
                showPatientSearchWarning()
            }
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
        val ad = ElementAdapter(
            this@PatientSearchActivity, layoutInflater, dataList, eventData.id.toString()
        )
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(this@PatientSearchActivity)
            adapter = ad
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