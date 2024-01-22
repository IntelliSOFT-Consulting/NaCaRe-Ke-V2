package com.intellisoft.nacare

import android.app.Application
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.intellisoft.nacare.auth.Login
import com.intellisoft.nacare.core.Sdk
import com.intellisoft.nacare.helper_class.FormatterClass
import com.intellisoft.nacare.main.DashboardActivity
import com.intellisoft.nacare.network_request.RetrofitCalls
import com.intellisoft.nacare.room.MainViewModel
import com.nacare.capture.R
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.D2Manager.instantiateD2


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

    override fun onDestroy() {
        super.onDestroy()
    }

    private fun loadInitialData() {
        CoroutineScope(Dispatchers.IO).launch {
            retrofitCalls.loadOrganization(this@SynchingPage)
            retrofitCalls.loadPrograms(this@SynchingPage)
            retrofitCalls.loadFacilityTool(this@SynchingPage)
        }
    }

    override fun onStart() {
        super.onStart()
        CoroutineScope(Dispatchers.IO).launch { test() }
    }

    private suspend fun test() {
        coroutineScope {
            launch {
                delay(3000)
                CoroutineScope(Dispatchers.Main).launch {
                    val completed = formatterClass.getSharedPref(
                        "isLoggedIn",
                        this@SynchingPage
                    )
                    if (completed == "true") {
                        val intent = Intent(this@SynchingPage, DashboardActivity::class.java)
                        startActivity(intent)
                        this@SynchingPage.finish()
                    } else {
                        val intent = Intent(this@SynchingPage, Login::class.java)
                        startActivity(intent)
                    }
                    this@SynchingPage.finish()
                }
            }
        }
    }
}
