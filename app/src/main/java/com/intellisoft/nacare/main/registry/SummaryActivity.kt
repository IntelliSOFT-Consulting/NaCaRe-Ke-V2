package com.intellisoft.nacare.main.registry

import android.app.Application
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import com.intellisoft.nacare.adapter.SummaryAdapter
import com.intellisoft.nacare.helper_class.DataElementItem
import com.intellisoft.nacare.helper_class.ProgramCategory
import com.intellisoft.nacare.helper_class.ProgramSections
import com.intellisoft.nacare.helper_class.ProgramStageSections
import com.intellisoft.nacare.helper_class.ProgramStages
import com.intellisoft.nacare.room.MainViewModel
import com.nacare.capture.R
import com.nacare.capture.databinding.ActivitySummaryBinding

class SummaryActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySummaryBinding
    private lateinit var viewModel: MainViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySummaryBinding.inflate(layoutInflater)
        setContentView(binding.root)
        viewModel = MainViewModel((this.applicationContext as Application))
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            title = "Summary"
            setDisplayHomeAsUpEnabled(true)
        }

        binding.subItemRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@SummaryActivity)
            val itemList = createData()
            val expandableAdapter = SummaryAdapter(itemList)
            adapter = expandableAdapter
        }
    }

    private fun createData(): List<ProgramCategory> {
        val dataList= mutableListOf<ProgramCategory>()
        val data = viewModel.loadProgram(this, "notification")

        if (data != null) {

            val treeNodes = mutableListOf<ProgramStageSections>()
            val allTreeNodes = mutableListOf<ProgramStageSections>()

            val json = data.programStages
            val gson = Gson()
            val items = gson.fromJson(json, Array<ProgramStages>::class.java)

            val json1 = data.programTrackedEntityAttributes
            val items1 = gson.fromJson(json1, Array<ProgramSections>::class.java)
            items1.forEach {
                val combined = mutableListOf<DataElementItem>()
                val elements = mutableListOf<DataElementItem>()
                if (it.name == "SEARCH PATIENT") {
                    it.trackedEntityAttributes.forEach { k ->
                        val del = DataElementItem(
                            k.id,
                            k.displayName,
                            k.valueType,
                            optionSet = k.optionSet
                        )
                        combined.add(del)
                    }

                    val pd = ProgramStageSections(
                        id = it.name,
                        displayName = it.name,
                        dataElements = combined
                    )
                    allTreeNodes.add(pd)
                }
                it.trackedEntityAttributes.forEach { k ->
                    val del = DataElementItem(
                        k.id,
                        k.displayName,
                        k.valueType,
                        optionSet = k.optionSet
                    )
                    elements.add(del)
                }

                val pd = ProgramStageSections(
                    id = it.name,
                    displayName = it.name,
                    dataElements = elements
                )
                treeNodes.add(pd)
            }

            dataList.clear()

            val pr = ProgramCategory(
                iconResId = R.drawable.home,
                name = "Patient Details",
                id = "patient-detail",
                done = "0",
                total = "0",
                elements = allTreeNodes,
                position = "0",
                altElements = treeNodes
            )
            dataList.add(pr)

            items.forEachIndexed { index, it ->

                val pd = ProgramCategory(
                    iconResId = R.drawable.home,
                    name = it.name,
                    id = it.id,
                    done = "0",
                    total = "0",
                    elements = it.programStageSections,
                    position = index.toString(),
                    altElements = emptyList()

                )
                dataList.add(pd)
            }

        }

        return dataList
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