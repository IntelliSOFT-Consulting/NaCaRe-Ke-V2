package com.intellisoft.nacare.main.registry

import android.app.Application
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import com.intellisoft.nacare.adapter.ElementAdapter
import com.intellisoft.nacare.helper_class.DataElement
import com.intellisoft.nacare.helper_class.FormatterClass
import com.intellisoft.nacare.helper_class.ProgramStageDataElements
import com.intellisoft.nacare.room.EventData
import com.intellisoft.nacare.room.MainViewModel
import com.nacare.ke.capture.databinding.ActivityResponderBinding

class ResponderActivity : AppCompatActivity() {
    private lateinit var binding: ActivityResponderBinding
    private lateinit var viewModel: MainViewModel
    private lateinit var eventData: EventData
    private val formatterClass = FormatterClass()

    private val dataList: MutableList<DataElement> = mutableListOf()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityResponderBinding.inflate(layoutInflater)
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
            prevButton.setOnClickListener {
                this@ResponderActivity.finish()
            }
            nextButton.setOnClickListener {
                this@ResponderActivity.finish()
            }

        }
    }

    private fun displayDataElements(json: String, eventData: EventData) {
        val gson = Gson()
        val items = gson.fromJson(json, Array<ProgramStageDataElements>::class.java)
        items.forEach {
            dataList.add(it.dataElement)
        }
        val ad = ElementAdapter(this@ResponderActivity,layoutInflater, dataList, eventData.id.toString())
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(this@ResponderActivity)
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