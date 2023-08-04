package com.intellisoft.hai.auth

import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.intellisoft.hai.R
import com.intellisoft.hai.SynchingPage
import com.intellisoft.hai.helper_class.FormatterClass
import com.intellisoft.hai.main.DashboardActivity
import com.intellisoft.hai.models.Constants.REFERENCE_URL
import com.intellisoft.hai.models.Constants.SERVER_URL
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

  @SuppressLint("CheckResult")
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_login)

    serverUrlEdittext = findViewById(R.id.serverUrlEdittext)
    usernameEdittext = findViewById(R.id.usernameEdittext)
    passwordEdittext = findViewById(R.id.passwordEdittext)

    populateInitial()

    findViewById<Button>(R.id.loginButton).setOnClickListener {
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
          !TextUtils.isEmpty(username)) {

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
                formatterClass.saveSharedPref("referenceSheetUrl", REFERENCE_URL, this@Login)
                formatterClass.saveSharedPref("username", username, this@Login)
                formatterClass.saveSharedPref("password", password, this@Login)
                formatterClass.saveSharedPref("isLoggedIn", "true", this@Login)

                val intent = Intent(this@Login, DashboardActivity::class.java)
                startActivity(intent)
              } else {
                Toast.makeText(this@Login, "Login Failed", Toast.LENGTH_SHORT).show()
              }
            }
          } catch (e: java.lang.Exception) {
            CoroutineScope(Dispatchers.Main).launch {
              progressDialog.dismiss()
              Toast.makeText(
                      this@Login,
                      "Couldn't establish the connection,Please try again later.",
                      Toast.LENGTH_SHORT)
                  .show()
            }
          }
        }
      } else {
        progressDialog.dismiss()
        if (TextUtils.isEmpty(serverUrl)) serverUrlEdittext.error = "Server cannot be found."
        if (TextUtils.isEmpty(username)) usernameEdittext.error = "Username cannot be found."
        if (TextUtils.isEmpty(password)) passwordEdittext.error = "Password cannot be found."
      }
    }
  }

  private fun populateInitial() {
    serverUrlEdittext.setText("http://pssnational.intellisoftkenya.com/")
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
