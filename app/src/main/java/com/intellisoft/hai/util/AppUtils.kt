package com.intellisoft.hai.util

import android.app.DatePickerDialog
import android.content.Context
import android.view.inputmethod.InputMethodManager
import android.widget.DatePicker
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import java.util.Calendar

object AppUtils {
  fun hideKeyboard(context: Context) {
    val inputMethodManager =
        context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    val rootView = (context as AppCompatActivity).window.decorView.rootView
    inputMethodManager.hideSoftInputFromWindow(rootView.windowToken, 0)
  }
  fun disableTextInputEditText(editText: TextInputEditText) {
    editText.isFocusable = false
    editText.isCursorVisible = false
    editText.keyListener = null
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
