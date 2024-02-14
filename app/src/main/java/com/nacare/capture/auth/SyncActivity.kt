package com.nacare.capture.auth

import android.app.Application
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.nacare.capture.MainActivity
import com.nacare.capture.R
import com.nacare.capture.network.RetrofitCalls
import com.nacare.capture.room.MainViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

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
            delay(10000) // Delay for 3 seconds
            val intent = Intent(this@SyncActivity, MainActivity::class.java)
            startActivity(intent)
            finish()    // Finish the activity


        }
    }
}