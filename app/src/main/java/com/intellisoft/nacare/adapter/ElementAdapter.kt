package com.intellisoft.nacare.adapter

import android.app.Application
import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.CheckBox
import android.widget.RadioButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.fasterxml.jackson.core.TreeNode
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.intellisoft.nacare.helper_class.DataElementItem
import com.intellisoft.nacare.helper_class.OrgTreeNode
import com.intellisoft.nacare.room.Converters
import com.intellisoft.nacare.room.EventData
import com.intellisoft.nacare.room.MainViewModel
import com.intellisoft.nacare.util.AppUtils
import com.intellisoft.nacare.util.AppUtils.showNoOrgUnits
import com.nacare.ke.capture.R


class ElementAdapter(
    private val context: Context,
    private val layoutInflater: LayoutInflater,
    private val items: List<DataElementItem>,
    private val event: EventData
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            VIEW_TYPE_EDITTEXT -> {
                val view = inflater.inflate(R.layout.item_edittext, parent, false)
                EditTextViewHolder(view)
            }

            VIEW_TYPE_ORGANIZATION -> {
                val view = inflater.inflate(R.layout.item_org_edittext, parent, false)
                OrgUnitViewHolder(view)
            }

            VIEW_TYPE_NUMBER -> {
                val view = inflater.inflate(R.layout.item_number_edittext, parent, false)
                NumberEditTextViewHolder(view)
            }

            VIEW_TYPE_PHONE_NUMBER -> {
                val view = inflater.inflate(R.layout.item_phone_number_edittext, parent, false)
                PhoneNumberEditTextViewHolder(view)
            }

            VIEW_TYPE_DATE -> {
                val view = inflater.inflate(R.layout.item_date_edittext, parent, false)
                DateEditTextViewHolder(view)
            }

            VIEW_TYPE_RADIO -> {
                val view = inflater.inflate(R.layout.item_radio, parent, false)
                RadioViewHolder(view)
            }

            VIEW_TYPE_AUTOCOMPLETE -> {
                val view = inflater.inflate(R.layout.item_autocomplete, parent, false)
                AutoCompleteViewHolder(view)
            }

            VIEW_TYPE_LONG_TEXT -> {
                val view = inflater.inflate(R.layout.item_long_edittext, parent, false)
                LongEditTextViewHolder(view)
            }

            VIEW_TYPE_TRUE_ONLY -> {
                val view = inflater.inflate(R.layout.item_check_box, parent, false)
                CheckBoxViewHolder(view)
            }

            else -> throw IllegalArgumentException("Invalid viewType")
        }
    }

    /*
        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            val item = items[position]
            when (holder.itemViewType) {
                VIEW_TYPE_EDITTEXT -> {
                    val editTextHolder = holder as EditTextViewHolder
                    editTextHolder.bind(item)
                }

                VIEW_TYPE_NUMBER -> {
                    val numberTextHolder = holder as NumberEditTextViewHolder
                    numberTextHolder.bind(item)
                }


                VIEW_TYPE_PHONE_NUMBER -> {
                    val numberTextHolder = holder as PhoneNumberEditTextViewHolder
                    numberTextHolder.bind(item)
                }

                VIEW_TYPE_RADIO -> {
                    val radioViewHolder = holder as RadioViewHolder
                    radioViewHolder.bind(item)
                }

                VIEW_TYPE_AUTOCOMPLETE -> {
                    val autoCompleteViewHolder = holder as AutoCompleteViewHolder
                    autoCompleteViewHolder.bind(item)
                }

                VIEW_TYPE_LONG_TEXT -> {
                    val longHolder = holder as LongEditTextViewHolder
                    longHolder.bind(item)
                }

                VIEW_TYPE_DATE -> {
                    val dateHolder = holder as DateEditTextViewHolder
                    dateHolder.bind(item)
                }

                VIEW_TYPE_TRUE_ONLY -> {
                    val checkBoxHolder = holder as CheckBoxViewHolder
                    checkBoxHolder.bind(item)
                }

                VIEW_TYPE_ORGANIZATION -> {
                    val orgHolder = holder as OrgUnitViewHolder
                    orgHolder.bind(item)
                }
            }
        }*/
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is OrgUnitViewHolder -> {
                val item = items[position]
                holder.bind(item)
            }

            is CheckBoxViewHolder -> {
                val item = items[position]
                holder.bind(item)
            }

            is DateEditTextViewHolder -> {
                val item = items[position]
                holder.bind(item)
            }

            is LongEditTextViewHolder -> {
                val item = items[position]
                holder.bind(item)
            }

            is AutoCompleteViewHolder -> {
                val item = items[position]
                holder.bind(item)
            }

            is RadioViewHolder -> {
                val item = items[position]
                holder.bind(item)
            }

            is PhoneNumberEditTextViewHolder -> {
                val item = items[position]
                holder.bind(item)
            }

            is NumberEditTextViewHolder -> {
                val item = items[position]
                holder.bind(item)
            }

            is EditTextViewHolder -> {
                val item = items[position]
                holder.bind(item)
            }
            // Handle other view types similarly
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun getItemViewType(position: Int): Int {
        val item = items[position]
        return when (item.valueType) {
            "TEXT" -> if (item.optionSet == null) {
                VIEW_TYPE_EDITTEXT
            } else {
                VIEW_TYPE_AUTOCOMPLETE
            }

            "DATE" -> VIEW_TYPE_DATE
            "BOOLEAN" -> VIEW_TYPE_RADIO
            "LONG_TEXT" -> VIEW_TYPE_LONG_TEXT
            "NUMBER" -> VIEW_TYPE_NUMBER
            "INTEGER_POSITIVE" -> VIEW_TYPE_NUMBER
            "TRUE_ONLY" -> VIEW_TYPE_TRUE_ONLY
            "ORGANISATION_UNIT" -> VIEW_TYPE_ORGANIZATION
            "PHONE_NUMBER" -> VIEW_TYPE_PHONE_NUMBER
            else -> throw IllegalArgumentException("Invalid data type")
        }
    }

    companion object {
        private const val VIEW_TYPE_EDITTEXT = 0
        private const val VIEW_TYPE_RADIO = 1
        private const val VIEW_TYPE_AUTOCOMPLETE = 2
        private const val VIEW_TYPE_LONG_TEXT = 3
        private const val VIEW_TYPE_DATE = 4
        private const val VIEW_TYPE_NUMBER = 5
        private const val VIEW_TYPE_TRUE_ONLY = 6
        private const val VIEW_TYPE_ORGANIZATION = 7
        private const val VIEW_TYPE_PHONE_NUMBER = 8
    }

    inner class DateEditTextViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val viewModel = MainViewModel(context.applicationContext as Application)

        fun bind(item: DataElementItem) {
            val tvName = itemView.findViewById<TextView>(R.id.tv_name)
            val textInputLayout = itemView.findViewById<TextInputLayout>(R.id.textInputLayout)
            val editText = itemView.findViewById<TextInputEditText>(R.id.editText)
            val keywords = listOf("Birth", "Death")
            tvName.text = item.displayName
//            editText.text = null
            val response = viewModel.getEventResponse(context, event, item.id)
            if (response != null) {
                editText.setText(response)
            }
            val max = containsAnyKeyword(item.displayName, keywords)

            AppUtils.disableTextInputEditText(editText)
            editText.apply {

                setOnClickListener {
                    AppUtils.showDatePickerDialog(
                        context, editText, setMaxNow = max, setMinNow = false
                    )

                }

                addTextChangedListener(object : TextWatcher {
                    override fun beforeTextChanged(
                        s: CharSequence?,
                        start: Int,
                        count: Int,
                        after: Int
                    ) {
                        // This method is called before the text is changed.
                    }

                    override fun onTextChanged(
                        s: CharSequence?,
                        start: Int,
                        before: Int,
                        count: Int
                    ) {
                        // This method is called when the text is changing.

                        if (s != null) {
                           /* viewModel.addResponse(
                                context,
                                event, item.id, s.toString()
                            )*/
                        }
                    }

                    override fun afterTextChanged(s: Editable?) {
                        // This method is called after the text has changed.
                        // You can perform actions here based on the updated text.
                    }
                })
            }
        }

        private fun containsAnyKeyword(displayName: String, keywords: List<String>): Boolean {
            return keywords.any { keyword -> displayName.contains(keyword) }
        }
    }

    inner class NumberEditTextViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bind(item: DataElementItem) {
            val viewModel = MainViewModel(context.applicationContext as Application)
            val tvName = itemView.findViewById<TextView>(R.id.tv_name)
            val textInputLayout = itemView.findViewById<TextInputLayout>(R.id.textInputLayout)
            val editText = itemView.findViewById<TextInputEditText>(R.id.editText)
            tvName.text = item.displayName
//            editText.text = null
            val response = viewModel.getEventResponse(context, event, item.id)
            if (response != null) {
                editText.setText(response)
            }
            editText.apply {

                addTextChangedListener(object : TextWatcher {
                    override fun beforeTextChanged(
                        s: CharSequence?,
                        start: Int,
                        count: Int,
                        after: Int
                    ) {
                        // This method is called before the text is changed.
                    }

                    override fun onTextChanged(
                        s: CharSequence?,
                        start: Int,
                        before: Int,
                        count: Int
                    ) {
                        // This method is called when the text is changing.

                        if (s != null) {
                           /* viewModel.addResponse(
                                context,
                                event, item.id, s.toString()
                            )*/
                        }
                    }

                    override fun afterTextChanged(s: Editable?) {
                        // This method is called after the text has changed.
                        // You can perform actions here based on the updated text.
                    }
                })
            }
        }
    }

    inner class PhoneNumberEditTextViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bind(item: DataElementItem) {
            val viewModel = MainViewModel(context.applicationContext as Application)
            val tvName = itemView.findViewById<TextView>(R.id.tv_name)
            val textInputLayout = itemView.findViewById<TextInputLayout>(R.id.textInputLayout)
            val editText = itemView.findViewById<TextInputEditText>(R.id.editText)
            tvName.text = item.displayName
//            editText.text = null
            val response = viewModel.getEventResponse(context, event, item.id)
            if (response != null) {
                editText.setText(response)
            }
            editText.apply {

                addTextChangedListener(object : TextWatcher {
                    override fun beforeTextChanged(
                        s: CharSequence?,
                        start: Int,
                        count: Int,
                        after: Int
                    ) {
                        // This method is called before the text is changed.
                    }

                    override fun onTextChanged(
                        s: CharSequence?,
                        start: Int,
                        before: Int,
                        count: Int
                    ) {
                        // This method is called when the text is changing.

                        if (s != null) {
                           /* viewModel.addResponse(
                                context,
                                event, item.id, s.toString()
                            )*/
                        }
                    }

                    override fun afterTextChanged(s: Editable?) {
                        // This method is called after the text has changed.
                        // You can perform actions here based on the updated text.
                    }
                })
            }
        }
    }

    inner class CheckBoxViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bind(item: DataElementItem) {
            val viewModel = MainViewModel(context.applicationContext as Application)
            val checkBox = itemView.findViewById<CheckBox>(R.id.checkBox)
            val tvName = itemView.findViewById<TextView>(R.id.tv_name)
            tvName.text = item.displayName
            val response = viewModel.getEventResponse(context, event, item.id)
            if (response != null) {
                if (response == "true") {
                    checkBox.isChecked = true
                }
            }
            checkBox.setOnCheckedChangeListener { buttonView, isChecked ->
                if (isChecked) {
                   /* viewModel.addResponse(
                        context,
                        event, item.id, "true"
                    )*/
                } else {
//                    viewModel.deleteResponse(
//                        context,
//                        event, item.id,
//                    )
                }
            }
        }
    }

    inner class EditTextViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {


        fun bind(item: DataElementItem) {
            val viewModel = MainViewModel(context.applicationContext as Application)
            val tvName = itemView.findViewById<TextView>(R.id.tv_name)
            val tvElement = itemView.findViewById<TextView>(R.id.tv_element)
            val textInputLayout = itemView.findViewById<TextInputLayout>(R.id.textInputLayout)
            val editText = itemView.findViewById<TextInputEditText>(R.id.editText)

            tvName.text = item.displayName
            tvElement.text = item.id
//            editText.text = null
            val response = viewModel.getEventResponse(context, event, item.id)
            if (response != null) {
                editText.setText(response)
            }
            editText.apply {

                addTextChangedListener(object : TextWatcher {
                    override fun beforeTextChanged(
                        s: CharSequence?,
                        start: Int,
                        count: Int,
                        after: Int
                    ) {
                        // This method is called before the text is changed.
                    }

                    override fun onTextChanged(
                        s: CharSequence?,
                        start: Int,
                        before: Int,
                        count: Int
                    ) {
                        if (s != null) {
                         /*   viewModel.addResponse(
                                context,
                                event,
                                items[adapterPosition].id,
                                s.toString()
                            )*/
                        }
                    }

                    override fun afterTextChanged(s: Editable?) {
                        // This method is called after the text has changed.
                        // You can perform actions here based on the updated text.
                    }
                })
            }

        }
    }

    inner class OrgUnitViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bind(item: DataElementItem) {
            val viewModel = MainViewModel(context.applicationContext as Application)
            val tvName = itemView.findViewById<TextView>(R.id.tv_name)
            val textInputLayout = itemView.findViewById<TextInputLayout>(R.id.textInputLayout)
            val editText = itemView.findViewById<TextInputEditText>(R.id.editText)
            var dialog: AlertDialog

            tvName.text = item.displayName
//            editText.text = null
            val response = viewModel.getEventResponse(context, event, item.id)
            if (response != null) {
                editText.setText(response)
            }

            editText.apply {

                addTextChangedListener(object : TextWatcher {
                    override fun beforeTextChanged(
                        s: CharSequence?,
                        start: Int,
                        count: Int,
                        after: Int
                    ) {
                        // This method is called before the text is changed.
                    }

                    override fun onTextChanged(
                        s: CharSequence?,
                        start: Int,
                        before: Int,
                        count: Int
                    ) {
                        if (s != null) {
                         /*   viewModel.addResponse(
                                context,
                                event,
                                items[adapterPosition].id,
                                s.toString()
                            )*/
                        }
                    }

                    override fun afterTextChanged(s: Editable?) {
                        // This method is called after the text has changed.
                        // You can perform actions here based on the updated text.
                    }
                })
                setOnClickListener {
                    val org = viewModel.loadOrganizations(context)
                    if (!org.isNullOrEmpty()) {
                        val or = org.firstOrNull()
                        if (or != null) {
                            val treeNodes = mutableListOf<OrgTreeNode>()
                            try {
                                val converters = Converters().fromJsonOrgUnit(or.children)
                                val orgNode = OrgTreeNode(
                                    label = converters.name,
                                    code = converters.id,
                                    children = AppUtils.generateChild(converters.children)
                                )
                                treeNodes.add(orgNode)
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }

                            val dialogBuilder = AlertDialog.Builder(context)
                            val inflater = layoutInflater
                            val dialogView = inflater.inflate(R.layout.dialog_tree, null)
                            dialogBuilder.setView(dialogView)
                            dialog = dialogBuilder.create()
                            val recyclerView: RecyclerView =
                                dialogView.findViewById(R.id.recyclerView)

                            val adapter = TreeAdapter(
                                context,
                                treeNodes,
                                click = {
                                    editText.setText(it.label)
                                    dialog.dismiss()
                                }
                            )
                            recyclerView.adapter = adapter
                            recyclerView.layoutManager = LinearLayoutManager(context)
                            dialogBuilder.setPositiveButton("OK") { dialog, which ->
                                // Handle positive button click if needed
                            }

                            dialog.show()
                        }
                    } else {
                        showNoOrgUnits(context)
                    }
                }
            }
        }
    }

    inner class LongEditTextViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(item: DataElementItem) {
            val viewModel = MainViewModel(context.applicationContext as Application)
            val tvName = itemView.findViewById<TextView>(R.id.tv_name)
            val textInputLayout = itemView.findViewById<TextInputLayout>(R.id.textInputLayout)
            val editText = itemView.findViewById<TextInputEditText>(R.id.editText)
            tvName.text = item.displayName
//            editText.text = null
            val response = viewModel.getEventResponse(context, event, item.id)
            if (response != null) {
                editText.setText(response)
            }
            editText.apply {

                addTextChangedListener(object : TextWatcher {
                    override fun beforeTextChanged(
                        s: CharSequence?,
                        start: Int,
                        count: Int,
                        after: Int
                    ) {
                        // This method is called before the text is changed.
                    }

                    override fun onTextChanged(
                        s: CharSequence?,
                        start: Int,
                        before: Int,
                        count: Int
                    ) {
                        // This method is called when the text is changing.

                        if (s != null) {
                           /* viewModel.addResponse(
                                context,
                                event, item.id, s.toString()
                            )*/
                        }
                    }

                    override fun afterTextChanged(s: Editable?) {
                        // This method is called after the text has changed.
                        // You can perform actions here based on the updated text.
                    }
                })
            }

        }
    }

    inner class RadioViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bind(item: DataElementItem) {
            val viewModel = MainViewModel(context.applicationContext as Application)
            val tvName = itemView.findViewById<TextView>(R.id.tv_name)
            val radioButtonYes = itemView.findViewById<RadioButton>(R.id.radioButtonYes)
            val radioButtonNo = itemView.findViewById<RadioButton>(R.id.radioButtonNo)
            tvName.text = item.displayName

            val response = viewModel.getEventResponse(context, event, item.id)
            if (response != null) {
                if (response == "true") {
                    radioButtonYes.isChecked = true
                } else if (response == "false") {
                    radioButtonNo.isChecked = true
                }
            }

            radioButtonNo.apply {
                setOnCheckedChangeListener { button, isChecked ->
                    if (isChecked) {
                      /*  viewModel.addResponse(
                            context,
                            event, item.id, "false"
                        )*/
                    }
                }
            }
            radioButtonYes.apply {
                setOnCheckedChangeListener { button, isChecked ->
                    if (isChecked) {
                      /*  viewModel.addResponse(
                            context,
                            event, item.id, "true"
                        )*/
                    }
                }
            }
        }
    }

    inner class AutoCompleteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {


        fun bind(item: DataElementItem) {

            val viewModel = MainViewModel(context.applicationContext as Application)
            val optionsList: MutableList<String> = mutableListOf()
            val adp = ArrayAdapter(context, android.R.layout.simple_list_item_1, optionsList)
            val tvName = itemView.findViewById<TextView>(R.id.tv_name)
            val tvElement = itemView.findViewById<TextView>(R.id.tv_element)
            val textInputLayout = itemView.findViewById<TextInputLayout>(R.id.textInputLayout)
            val autoCompleteTextView =
                itemView.findViewById<AutoCompleteTextView>(R.id.autoCompleteTextView)

            tvElement.text = item.id
//            autoCompleteTextView.text = null
            optionsList.clear()
            item.optionSet?.options?.forEach {
                optionsList.add(it.displayName)
            }
            tvName.text = item.displayName
            val response = viewModel.getEventResponse(context, event, item.id)
            if (response != null) {
                autoCompleteTextView.setText(response, false)
            }
            autoCompleteTextView.setAdapter(adp)
            adp.notifyDataSetChanged()
            autoCompleteTextView.apply {

                addTextChangedListener(object : TextWatcher {
                    override fun beforeTextChanged(
                        s: CharSequence?,
                        start: Int,
                        count: Int,
                        after: Int
                    ) {
                        // This method is called before the text is changed.
                    }

                    override fun onTextChanged(
                        s: CharSequence?,
                        start: Int,
                        before: Int,
                        count: Int
                    ) {
                        if (s != null) {
                         /*   viewModel.addResponse(
                                context,
                                event,
                                items[adapterPosition].id,
                                s.toString()
                            )*/
                        }
                    }

                    override fun afterTextChanged(s: Editable?) {
                        // This method is called after the text has changed.
                        // You can perform actions here based on the updated text.
                    }
                })
            }

        }
    }
}

