package com.capture.app

import android.app.Application
import android.util.Log
import com.capture.app.data.FormatterClass
import com.capture.app.sync.SyncWorker

class App : Application() {
    private val formatter = FormatterClass()
    override fun onCreate() {
        super.onCreate()
        try {
            val login = formatter.getSharedPref("isLoggedIn", this)
            if (login != null) {
                if (login == "true") {
                    SyncWorker.scheduleSync(applicationContext)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e("App", "Service Failed to Start.....")
        }
    }
}