package com.intellisoft.nacare.auth

import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.intellisoft.nacare.SynchingPage
import com.intellisoft.nacare.helper_class.FormatterClass
import com.intellisoft.nacare.main.DashboardActivity
import com.intellisoft.nacare.models.Constants.SERVER_URL
import com.intellisoft.nacare.util.AppUtils.controlData
import com.nacare.ke.capture.R
import com.nacare.ke.capture.databinding.ActivityLoginBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.hisp.dhis.Dhis2
import org.hisp.dhis.Dhis2Config

class Login : AppCompatActivity() {

    private lateinit var serverUrlEdittext: EditText
    private lateinit var usernameEdittext: EditText
    private lateinit var passwordEdittext: EditText
    private var formatterClass = FormatterClass()
    private lateinit var binding: ActivityLoginBinding

    @SuppressLint("CheckResult")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        serverUrlEdittext = findViewById(R.id.serverUrlEdittext)
        usernameEdittext = findViewById(R.id.usernameEdittext)
        passwordEdittext = findViewById(R.id.passwordEdittext)

        populateInitial()

        findViewById<Button>(R.id.loginButton).setOnClickListener {
            handleSubmission()
        }

        inputController()
    }

    private fun inputController() {
        controlData(
            binding.usernameEdittext,
            binding.usernameLayout,
            "Please provide username",
            hasMin = false,
            hasMax = false,
            min = 0,
            max = 0
        )
        controlData(
            binding.passwordEdittext,
            binding.passwordLayout,
            "Please provide password",
            hasMin = false,
            hasMax = false,
            min = 0,
            max = 0
        )
    }

    private fun handleSubmission() {
        var progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Please wait..")
        progressDialog.setMessage("Authentication in progress..")
        progressDialog.setCanceledOnTouchOutside(false)
        progressDialog.show()

        val serverUrl = serverUrlEdittext.text.toString()
        val password = passwordEdittext.text.toString()
        val username = usernameEdittext.text.toString()

        if (!TextUtils.isEmpty(serverUrl) &&
            !TextUtils.isEmpty(password) &&
            !TextUtils.isEmpty(username)
        ) {

            val config = Dhis2Config(serverUrl, username, password)

            val dhis2 = Dhis2(config)

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val isLoggedIn = dhis2.status.is2xxSuccessful


                    CoroutineScope(Dispatchers.Main).launch {
                        progressDialog.dismiss()
                        if (isLoggedIn) {

                            formatterClass.saveSharedPref("serverUrl1", serverUrl, this@Login)
                            formatterClass.saveSharedPref("serverUrl", SERVER_URL, this@Login)

                            formatterClass.saveSharedPref("username", username, this@Login)
                            formatterClass.saveSharedPref("password", password, this@Login)
                            formatterClass.saveSharedPref("isLoggedIn", "true", this@Login)

                            val intent = Intent(this@Login, SynchingPage::class.java)
                            startActivity(intent)
                        } else {
                            Toast.makeText(this@Login, "Login Failed", Toast.LENGTH_SHORT)
                                .show()
                        }
                    }
                } catch (e: java.lang.Exception) {
                    CoroutineScope(Dispatchers.Main).launch {
                        progressDialog.dismiss()
                        Toast.makeText(
                            this@Login,
                            "Couldn't establish the connection,Please try again later.",
                            Toast.LENGTH_SHORT
                        )
                            .show()
                    }
                }
            }
        } else {
            progressDialog.dismiss()
            if (TextUtils.isEmpty(serverUrl)) serverUrlEdittext.error =
                "Server cannot be found."
            if (TextUtils.isEmpty(username)) binding.usernameLayout.error =
                "Username cannot be found."
            if (TextUtils.isEmpty(password)) binding.passwordLayout.error =
                "Password cannot be found."
        }
    }

    private fun populateInitial() {
        serverUrlEdittext.setText("http://45.79.116.38:8080/")
    }

    override fun onStart() {
        super.onStart()

        getSavedData()
    }

    private fun getSavedData() {

        val isLoggedIn = formatterClass.getSharedPref("isLoggedIn", this)
        if (isLoggedIn != null) {
            if (isLoggedIn == "true") {
                val intent = Intent(this@Login, DashboardActivity::class.java)
                startActivity(intent)
                finish()
            }
        }
        val url = formatterClass.getSharedPref("serverUrl1", this)
        if (url != null) {
            serverUrlEdittext.setText(url)
        }
    }
}
