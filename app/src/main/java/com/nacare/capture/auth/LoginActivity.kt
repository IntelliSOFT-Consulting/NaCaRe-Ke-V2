package com.nacare.capture.auth

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.text.Html
import android.text.SpannableString
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.ForegroundColorSpan
import android.text.style.UnderlineSpan
import android.util.Base64
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.nacare.capture.MainActivity
import com.nacare.capture.R
import com.nacare.capture.data.FormatterClass
import com.nacare.capture.databinding.ActivityLoginBinding
import com.nacare.capture.network.RetrofitCalls

class LoginActivity : AppCompatActivity() {
    private val retrofitCalls = RetrofitCalls()
    private val formatter = FormatterClass()
    private lateinit var binding: ActivityLoginBinding
    private lateinit var progressDialog: ProgressDialog
    override fun onStart() {
        super.onStart()
        val login = formatter.getSharedPref("isLoggedIn", this@LoginActivity)
        if (login != null) {
            if (login == "true") {
                val intent = Intent(this@LoginActivity, MainActivity::class.java)
                startActivity(intent)
                finish()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        progressDialog = ProgressDialog(this)
        progressDialog.setMessage("Please wait...") // Set your message
        progressDialog.setCancelable(true)
        binding.apply {

            val recover =
                "<a href=\"https://nacareke.on.spiceworks.com/portal/registrations\"><u>Account Recovery</u></a>"
            val mail =
                "For assistance on the National Cancer <br>Registry of Kenya System, click here or send an email to<br><br> <a href=\"mailto:help@nacare.on.spiceworks.com\">help@nacare.on.spiceworks.com</a>"
            val span = SpannableString(Html.fromHtml(recover))

            val start_span = span.toString().indexOf("Account Recovery")
            val end_span = start_span + "Account Recovery".length

            span.setSpan(
                ForegroundColorSpan(resources.getColor(R.color.white)),
                start_span,
                end_span,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            span.setSpan(UnderlineSpan(), start_span, end_span, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

            tvRecovery.apply {
                text = span
                movementMethod = LinkMovementMethod.getInstance()
            }

            // Use Html.fromHtml() to interpret the HTML formatting
            // Use Html.fromHtml() to interpret the HTML formatting
            val spannableString = SpannableString(Html.fromHtml(mail))

            // Customize the appearance of the link (white color and underline)

            // Customize the appearance of the link (white color and underline)
            val start = spannableString.toString().indexOf("help@nacare.on.spiceworks.com")
            val end = start + "help@nacare.on.spiceworks.com".length

            spannableString.setSpan(
                ForegroundColorSpan(resources.getColor(R.color.white)),
                start,
                end,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            spannableString.setSpan(UnderlineSpan(), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            tvMailer.apply {
                text = spannableString
                movementMethod = LinkMovementMethod.getInstance()
            }
            loginButton.apply {
                setOnClickListener {
                    validateData()
                }
            }
        }

//        retrofitCalls.signIn(this, "admin", "district")


    }

    private fun validateData() {
        val username = binding.usernameEdittext.text.toString()
        val password = binding.passwordEdittext.text.toString()
        if (username.isEmpty()) {
            Toast.makeText(this, "Enter Username", Toast.LENGTH_SHORT).show()
            binding.usernameEdittext.requestFocus()
            return
        }
        if (password.isEmpty()) {
            Toast.makeText(this, "Enter Password", Toast.LENGTH_SHORT).show()
            binding.passwordEdittext.requestFocus()
            return
        }

        val credentials = "${username.trim()}:${password.trim()}"

        val encodedString = Base64.encodeToString(credentials.toByteArray(), Base64.NO_WRAP)
        formatter.saveSharedPref("encodedString", encodedString, this@LoginActivity)
        retrofitCalls.signIn(this@LoginActivity, progressDialog)


    }
}