package com.intellisoft.nacare.main.registry

import android.app.Application
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import com.intellisoft.nacare.helper_class.FormatterClass
import com.intellisoft.nacare.room.MainViewModel
import com.nacare.ke.capture.R
import com.nacare.ke.capture.databinding.ActivityRegistryBinding
import com.nacare.ke.capture.databinding.ActivityResponderBinding

class ResponderActivity : AppCompatActivity() {
    private lateinit var binding: ActivityResponderBinding
    private lateinit var viewModel: MainViewModel
    private val formatterClass = FormatterClass()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityResponderBinding.inflate(layoutInflater)
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

                supportActionBar?.apply {
                    title = name
//                    subtitle = "$date | $org"
                    setDisplayHomeAsUpEnabled(true)
//            setHomeAsUpIndicator(R.drawable.ic_back_arrow)

                }

            }
        }

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