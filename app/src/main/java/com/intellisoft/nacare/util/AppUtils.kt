package com.intellisoft.nacare.util

import android.app.DatePickerDialog
import android.content.Context
import android.net.ConnectivityManager
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.inputmethod.InputMethodManager
import android.widget.AutoCompleteTextView
import android.widget.CheckBox
import android.widget.DatePicker
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.intellisoft.nacare.helper_class.CountyUnit
import com.intellisoft.nacare.helper_class.OrgTreeNode
import com.nacare.ke.capture.R
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.UUID

object AppUtils {
    fun containsAnyKeyword(displayName: String, keywords: List<String>): Boolean {
        return keywords.any { keyword -> displayName.contains(keyword) }
    }

    fun showNoOrgUnits(context: Context) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle("No Organization Units")
            .setMessage("There are not organization units, pleas try again later!!")
            .setPositiveButton("Okay") { dialog, _ ->
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .show()

    }

    fun generateChild(children: List<CountyUnit>): List<OrgTreeNode> {
        val treeNodes = mutableListOf<OrgTreeNode>()
        for (ch in children) {
            val orgNode = OrgTreeNode(
                label = ch.name,
                code = ch.id,
                children = generateChild(ch.children)
            )
            treeNodes.add(orgNode)

        }

        return treeNodes.sortedBy { it.label }
    }

    fun noConnection(context: Context) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle("No Connection")
            .setMessage("You need active internet to perform global search")
            .setPositiveButton("Okay") { dd, _ ->
                dd.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    fun isOnline(context: Context): Boolean {
        var isOnline = false
        try {
            val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            if (cm != null) {
                val netInfo = cm.activeNetworkInfo
                //should check null because in airplane mode it will be null
                isOnline = netInfo != null && netInfo.isConnectedOrConnecting
            }
        } catch (ex: Exception) {

        }
        return isOnline
    }

    fun generateIcons(context: Context, iconName: String): Int {
        val iconDrawableMap = mapOf(
            "add" to R.drawable.add,
            "arrow_down" to R.drawable.arrowdown,
            "back" to R.drawable.back,
            "Cancer Information" to R.drawable.cancerinfo,
            "capture" to R.drawable.capture,
            "cleaner" to R.drawable.cleaner,
            "Comorbidities" to R.drawable.comorbidities,
            "completed_doc" to R.drawable.completeddoc,
            "dashb" to R.drawable.dashb,
            "Discrimination" to R.drawable.discrimination,
            "editdoc" to R.drawable.editdoc,
            "edit_form" to R.drawable.editform,
            "event_note_FILL0_wght400_GRAD0_opsz24" to R.drawable.event_note,
            "event_vis" to R.drawable.eventvis,
            "ev_viz_2" to R.drawable.evviz2,
            "expand" to R.drawable.expand,
            "facility" to R.drawable.facility,
            "facility_2" to R.drawable.facility2,
            "facility_cap" to R.drawable.facilitycap,
            "filter" to R.drawable.filter,
            "follow_up" to R.drawable.followup,
            "follow_upp" to R.drawable.followupp,
            "form" to R.drawable.form,
            "form_complete" to R.drawable.formcomplete,
            "helpdesk" to R.drawable.helpdesk,
            "info" to R.drawable.info,
            "interp" to R.drawable.interp,
            "key" to R.drawable.key,
            "maps" to R.drawable.maps,
            "menu" to R.drawable.menu,
            "menu_management" to R.drawable.menumanagement,
            "more" to R.drawable.more,
            "nci_form" to R.drawable.nciform,
            "next_next" to R.drawable.nextnext,
            "next_page" to R.drawable.nextpage,
            "Patient Details" to R.drawable.patientdetails,
            "Patient Status" to R.drawable.patientstatus,
            "Post-cancer Treatment Rehabilitation" to R.drawable.posttreatment,
            "remove_FILL0_wght400_GRAD0_opsz24" to R.drawable.remove_,
            "risk" to R.drawable.risk,
            "Risk Factors" to R.drawable.risk2,
            "save" to R.drawable.save,
            "search" to R.drawable.search,
            "settings" to R.drawable.settings,
            "star" to R.drawable.star,
            "Survivorship" to R.drawable.survivorship,
            "sync" to R.drawable.sync,
            "synccc" to R.drawable.synccc,
            "Treatment" to R.drawable.treatment
        )

        return iconDrawableMap[iconName]
            ?: throw IllegalArgumentException("Icon not found: $iconName")


    }

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
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                }

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

    fun controlSelectionData(
        child: AutoCompleteTextView,
        parent: TextInputLayout,
        error: String,
        hasMin: Boolean,
        hasMax: Boolean,
        min: Int,
        max: Int
    ) {

        child.addTextChangedListener(
            object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                }

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
//                    val selectedDate =                        String.format("%02d/%02d/%04d", dayOfMonth, monthOfYear + 1, year)
                    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    val selectedDate = dateFormat.format(Date(year - 1900, monthOfYear, dayOfMonth))

                    textInputEditText.setText(selectedDate)
                },
                year,
                month,
                day
            )

        // Set date picker dialog limits (optional)
        if (setMaxNow) {
            datePickerDialog.datePicker.maxDate = System.currentTimeMillis() + (1000 * 60 * 60 * 24)
        }
        if (setMinNow) {
            datePickerDialog.datePicker.minDate = System.currentTimeMillis() - 1000
        }

        datePickerDialog.show()
    }

    fun currentTimestamp(): String {
        val currentDate = Date()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH)
        return dateFormat.format(currentDate)
    }

    fun permissionError(
        layoutInflater: LayoutInflater,
        context: Context,
        status: String,
        message: String
    ) {
        val dialog: AlertDialog
        val dialogBuilder = AlertDialog.Builder(context)
        val dialogView = layoutInflater.inflate(R.layout.confirmation_dialog, null)
        dialogBuilder.setView(dialogView)

        val tvTitle: TextView = dialogView.findViewById(R.id.tv_title)
        val tvMessage: TextView = dialogView.findViewById(R.id.tv_message)
        val nextButton: MaterialButton = dialogView.findViewById(R.id.next_button)
        dialog = dialogBuilder.create()
        tvMessage.text = message
        nextButton.text = "Retry"
        nextButton.setOnClickListener {
            dialog.dismiss()

        }
        dialog.show()
    }
}
