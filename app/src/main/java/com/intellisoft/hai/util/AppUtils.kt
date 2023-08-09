package com.intellisoft.hai.util

import android.app.DatePickerDialog
import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.view.inputmethod.InputMethodManager
import android.widget.CheckBox
import android.widget.DatePicker
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import java.util.Calendar
import java.util.UUID

object AppUtils {
  fun hideKeyboard(context: Context) {
    val inputMethodManager =
        context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    val rootView = (context as AppCompatActivity).window.decorView.rootView
    inputMethodManager.hideSoftInputFromWindow(rootView.windowToken, 0)
  }
    fun checkBoxHide(
    check: CheckBox,
    view: TextInputLayout,
    input: TextInputEditText
  ) {
    check.apply {
      setOnCheckedChangeListener { _, isChecked ->
        view.isVisible = isChecked
        if (!isChecked) {
          input.text?.clear()
        }
      }
    }

  }
  fun disableTextInputEditText(editText: TextInputEditText) {
    editText.isFocusable = false
    editText.isCursorVisible = false
    editText.keyListener = null
  }

    fun generateUuid(): String {
    return UUID.randomUUID().toString()
  }
    fun controlData(
    child: TextInputEditText,
    parent: TextInputLayout,
    error: String,
    hasMin: Boolean,
    hasMax: Boolean,
    min: Int,
    max: Int
  ) {

    child.addTextChangedListener(
      object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
          if (hasMin && s?.length ?: 0 < min) {
            parent.error = "Minimum of $min characters required"
          } else if (hasMax && s?.length ?: 0 > max) {
            parent.error = "Maximum of $max characters allowed"
          } else if (s.isNullOrEmpty()) {
            parent.error = error
          } else {
            parent.error = null
          }
        }

        override fun afterTextChanged(s: Editable?) {}
      })
  }
  fun showDatePickerDialog(
      context: Context,
      textInputEditText: TextInputEditText,
      setMaxNow: Boolean,
      setMinNow: Boolean
  ) {
    val calendar = Calendar.getInstance()
    val year = calendar.get(Calendar.YEAR)
    val month = calendar.get(Calendar.MONTH)
    val day = calendar.get(Calendar.DAY_OF_MONTH)

    val datePickerDialog =
        DatePickerDialog(
            context,
            { _: DatePicker, year: Int, monthOfYear: Int, dayOfMonth: Int ->
              val selectedDate = String.format("%02d/%02d/%04d", dayOfMonth, monthOfYear + 1, year)
              textInputEditText.setText(selectedDate)
            },
            year,
            month,
            day)

    // Set date picker dialog limits (optional)
    if (setMaxNow) {
      datePickerDialog.datePicker.maxDate = System.currentTimeMillis() + (1000 * 60 * 60 * 24)
    }
    if (setMinNow) {
      datePickerDialog.datePicker.minDate = System.currentTimeMillis() - 1000
    }

    datePickerDialog.show()
  }
}
