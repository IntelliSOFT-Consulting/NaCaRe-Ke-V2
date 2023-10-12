package com.intellisoft.nacare.main.facility

import android.app.Application
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Html
import android.view.MenuItem
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import com.intellisoft.nacare.adapter.ElementAdapter
import com.intellisoft.nacare.adapter.FacilityAdapter
import com.intellisoft.nacare.adapter.FacilityElementAdapter
import com.intellisoft.nacare.adapter.ProgramAdapter
import com.intellisoft.nacare.helper_class.DataElementItem
import com.intellisoft.nacare.helper_class.DataValueData
import com.intellisoft.nacare.helper_class.EntityAttributes
import com.intellisoft.nacare.helper_class.FacilityProgramCategory
import com.intellisoft.nacare.helper_class.FormatterClass
import com.intellisoft.nacare.helper_class.ProgramCategory
import com.intellisoft.nacare.helper_class.ProgramStageSections
import com.intellisoft.nacare.helper_class.ProgramStages
import com.intellisoft.nacare.room.EventData
import com.intellisoft.nacare.room.MainViewModel
import com.intellisoft.nacare.room.ProgramData
import com.nacare.ke.capture.R
import com.nacare.ke.capture.databinding.ActivityFacilityBinding
import com.nacare.ke.capture.databinding.ActivityResponderBinding

class FacilityActivity : AppCompatActivity() {
    private lateinit var binding: ActivityFacilityBinding
    private lateinit var viewModel: MainViewModel
    private val formatterClass = FormatterClass()
    private val dataList: MutableList<FacilityProgramCategory> = mutableListOf()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFacilityBinding.inflate(layoutInflater)
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
                val dataValues = dataBundle.getString("dataValues")
                if (dataValues != null) {
//                    displayDataElements(dataValues)
                }
                supportActionBar?.apply {
                    title = name
                    setDisplayHomeAsUpEnabled(true)
                }

            }

        }

        binding.apply {
            prevButton.setOnClickListener {
                this@FacilityActivity.finish()
            }
            nextButton.setOnClickListener {
                this@FacilityActivity.finish()
            }

            val formattedText = ""
            textView.text = Html.fromHtml(formattedText, Html.FROM_HTML_MODE_LEGACY)

        }
        loadInitialData()
    }

    private fun loadInitialData() {
        val data = viewModel.loadProgram(this, "facility")
        if (data != null) {
            val org = formatterClass.getSharedPref("name", this)
            val date = formatterClass.getSharedPref("date", this)
            supportActionBar?.apply {
                title = data.name
                subtitle = "$date | $org"
                setDisplayHomeAsUpEnabled(true)

            }
            loadProgramData(data)

        }
    }

    private fun loadProgramData(program: ProgramData) {
        val json = program.programStages
        val gson = Gson()
        val items = gson.fromJson(json, Array<ProgramStages>::class.java)
        dataList.clear()
        items.forEach {
            val dataListInner: MutableList<FacilityProgramCategory> = mutableListOf()
            it.programStageSections.forEachIndexed { index, k ->
                val pd =  FacilityProgramCategory(
                    iconResId = R.drawable.home,
                    name = k.displayName,
                    id = k.id,
                    done = "",
                    total = "",
                    elements = k.dataElements,
                    position = index.toString()
                )
                dataListInner.add(pd)
            }
            dataList.addAll(dataListInner)
        }

        val ad = FacilityAdapter(this, dataList)

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(this@FacilityActivity)
            adapter = ad

        }
        ad.notifyDataSetChanged()
    }

    override fun onResume() {
        loadInitialData()
        super.onResume()
    }

    /*  private fun displayDataElements(json: String) {
          val gson = Gson()
          val items = gson.fromJson(json, Array<DataValueData>::class.java)
          dataList.clear()
          items.forEach {
              dataList.add(it)
          }
          val ad = FacilityElementAdapter(
              this@FacilityActivity,
              dataList
          )
          binding.recyclerView.apply {
              layoutManager = LinearLayoutManager(this@FacilityActivity)
              adapter = ad
          }
      }
  */
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