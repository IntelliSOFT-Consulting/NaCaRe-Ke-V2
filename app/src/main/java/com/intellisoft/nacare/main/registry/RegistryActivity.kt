package com.intellisoft.nacare.main.registry

import android.app.Application
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.intellisoft.nacare.adapter.ProgramAdapter
import com.intellisoft.nacare.helper_class.FormatterClass
import com.intellisoft.nacare.helper_class.ProgramCategory
import com.intellisoft.nacare.helper_class.ProgramStages
import com.intellisoft.nacare.room.Converters
import com.intellisoft.nacare.room.MainViewModel
import com.intellisoft.nacare.room.ProgramData
import com.nacare.ke.capture.R
import com.nacare.ke.capture.databinding.ActivityRegistryBinding

class RegistryActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegistryBinding
    private lateinit var program: ProgramData
    private lateinit var viewModel: MainViewModel
    private val formatterClass = FormatterClass()
    private val dataList: MutableList<ProgramCategory> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegistryBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        viewModel = MainViewModel((this.applicationContext as Application))
        val data = viewModel.loadProgram(this)

        if (data != null) {
            program = data
            val org = formatterClass.getSharedPref("name", this)
            val date = formatterClass.getSharedPref("date", this)
            supportActionBar?.apply {
                title = program.name
                subtitle = "$date | $org"
                setDisplayHomeAsUpEnabled(true)
//            setHomeAsUpIndicator(R.drawable.ic_back_arrow)

            }
            loadProgramData(program)

        }

    }

    private fun loadProgramData(program: ProgramData) {
//        try {
        val json = program.programStages
        val gson = Gson()
        val items = gson.fromJson(json, Array<ProgramStages>::class.java)
        items.forEach {
            val pd = ProgramCategory(
                iconResId = R.drawable.home,
                name = it.name,
                id = it.id,
                done = "0",
                total = it.programStageDataElements.size.toString(),
                elements = it.programStageDataElements
            )
            dataList.add(pd)
        }
        val ad = ProgramAdapter(this, dataList, this::handleClick)

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(this@RegistryActivity)
            adapter = ad
        }


        /*  } catch (e: Exception) {
              e.printStackTrace()
          }*/

    }

    private fun handleClick(data: ProgramCategory) {

        val converters = Converters().toJsonElements(data.elements)
        val json = Gson().fromJson(converters, JsonArray::class.java)
        val bundle = Bundle()
        bundle.putString("code", data.id)
        bundle.putString("name", data.name)
        bundle.putString("programStageDataElements", json.toString())
        val intent = Intent(this@RegistryActivity, ResponderActivity::class.java)
        intent.putExtra("data", bundle)
        startActivity(intent)
    }

    override fun onBackPressed() {
        super.onBackPressed()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return super.onSupportNavigateUp()
    }

    // You can override onOptionsItemSelected to handle toolbar item clicks (e.g., back button)
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
