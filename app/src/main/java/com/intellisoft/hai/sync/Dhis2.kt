package com.intellisoft.hai.sync

import android.app.Service
import android.content.Intent
import android.os.Handler
import android.os.IBinder
import android.util.Log

class Dhis2 : Service() {

  private var handler: Handler? = null
  private var runnable: Runnable? = null

  override fun onBind(intent: Intent): IBinder? {
    return null
  }

  override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
    handler = Handler()
    runnable =
        object : Runnable {
          override fun run() {
            val broadcastIntent = Intent("dhis2")
            sendBroadcast(broadcastIntent) // Send a broadcast message
            Log.e("App","Service Started.....")
            handler?.postDelayed(this, 30000) // Run this runnable again in 30 seconds
          }
        }
    handler?.post(runnable!!)
    return START_STICKY
  }

  override fun onDestroy() {
    super.onDestroy()
    handler?.removeCallbacks(runnable!!)
  }
}
