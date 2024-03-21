package com.capture.app.auth

import android.app.Application
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.capture.app.MainActivity
import com.capture.app.R
import com.capture.app.data.FormatterClass
import com.capture.app.network.RetrofitCalls
import com.capture.app.room.Converters
import com.capture.app.room.MainViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.Normalizer.Form

class SyncActivity : AppCompatActivity() {
    private lateinit var viewModel: MainViewModel
    private val retrofitCalls = RetrofitCalls()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sync)
        viewModel = MainViewModel(this.applicationContext as Application)
        viewModel.deletePrograms()
        CoroutineScope(Dispatchers.Main).launch {
            retrofitCalls.loadAllSites(this@SyncActivity)
            retrofitCalls.loadAllCategories(this@SyncActivity)
            retrofitCalls.loadAllEvents(this@SyncActivity)
            retrofitCalls.loadOrganization(this@SyncActivity)
            retrofitCalls.loadProgram(this@SyncActivity, "notification")
            retrofitCalls.loadProgram(this@SyncActivity, "facility")
            retrofitCalls.loadTopography(this@SyncActivity)
            retrofitCalls.loadAllFacilities(this@SyncActivity)
            retrofitCalls.loadTrackedEntities(this@SyncActivity)

            val userData = FormatterClass().getSharedPref("user_data", this@SyncActivity)
            if (userData != null) {
                val converters = Converters().fromJsonUser(userData)
                converters.organisationUnits.forEach {
                    FormatterClass().saveSharedPref("orgLevel", it.level, this@SyncActivity)
                    FormatterClass().saveSharedPref("orgCode", it.id, this@SyncActivity)
                }

            }


            delay(10000) // Delay for 3 seconds
            val intent = Intent(this@SyncActivity, MainActivity::class.java)
            startActivity(intent)
            finish()    // Finish the activity


        }
    }
}