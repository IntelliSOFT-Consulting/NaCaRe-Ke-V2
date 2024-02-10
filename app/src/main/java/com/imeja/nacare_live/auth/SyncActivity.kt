package com.imeja.nacare_live.auth

import android.app.Application
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.imeja.nacare_live.MainActivity
import com.imeja.nacare_live.R
import com.imeja.nacare_live.room.MainViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SyncActivity : AppCompatActivity() {
    private lateinit var viewModel: MainViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sync)
        viewModel = MainViewModel(this.applicationContext as Application)
        viewModel.deletePrograms()
        CoroutineScope(Dispatchers.Main).launch {
            delay(3000) // Delay for 3 seconds

            val intent = Intent(this@SyncActivity, MainActivity::class.java)
            startActivity(intent)
            finish()    // Finish the activity
        }
    }
}