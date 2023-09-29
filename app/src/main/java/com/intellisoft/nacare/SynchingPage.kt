package com.intellisoft.nacare

import android.app.Application
import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.intellisoft.nacare.helper_class.FormatterClass
import com.intellisoft.nacare.helper_class.PinLockStatus
import com.intellisoft.nacare.main.DashboardActivity
import com.intellisoft.nacare.network_request.RetrofitCalls
import com.intellisoft.nacare.room.MainViewModel
import com.nacare.ke.capture.R
import kotlin.random.Random
import kotlinx.coroutines.*

class SynchingPage : AppCompatActivity() {
    private val retrofitCalls = RetrofitCalls()
    private val formatterClass = FormatterClass()
    private lateinit var viewModel: MainViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_synching_page)
        viewModel = MainViewModel((this.applicationContext as Application))
        loadInitialData()
    }

    private fun loadInitialData() {
        CoroutineScope(Dispatchers.IO).launch {
            retrofitCalls.loadOrganization(this@SynchingPage)
            retrofitCalls.loadPrograms(this@SynchingPage)
        }
    }

    override fun onStart() {
        super.onStart()
//        CoroutineScope(Dispatchers.IO).launch { test() }
    }

    private suspend fun test() {
        coroutineScope {
            launch {
                delay(3000)
                CoroutineScope(Dispatchers.Main).launch {
                    val completed = formatterClass.getSharedPref(
                        PinLockStatus.CONFIRMED.name,
                        this@SynchingPage
                    )
                    /*if (completed == null) {*/
                    val intent = Intent(this@SynchingPage, DashboardActivity::class.java)
                    startActivity(intent)
                    this@SynchingPage.finish()
//          }else{
//            val intent = Intent(this@SynchingPage, PinActivity::class.java)
//            startActivity(intent)
//          }
//          this@SynchingPage.finish()
                }
            }
        }
    }
}
