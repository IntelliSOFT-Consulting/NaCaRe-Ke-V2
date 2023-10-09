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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.fasterxml.jackson.core.TreeNode
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.intellisoft.nacare.helper_class.DataElement
import com.intellisoft.nacare.helper_class.OrgTreeNode
import com.intellisoft.nacare.room.Converters
import com.intellisoft.nacare.room.MainViewModel
import com.intellisoft.nacare.util.AppUtils
import com.intellisoft.nacare.util.AppUtils.showNoOrgUnits
import com.nacare.ke.capture.R


class ElementAdapter(
    private val context: Context,
    private val layoutInflater: LayoutInflater,
    private val items: List<DataElement>,
    private val event: String
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
    }

    inner class DateEditTextViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val viewModel = MainViewModel(context.applicationContext as Application)
        fun bind(item: DataElement) {
            val tvName = itemView.findViewById<TextView>(R.id.tv_name)
            val textInputLayout = itemView.findViewById<TextInputLayout>(R.id.textInputLayout)
            val editText = itemView.findViewById<TextInputEditText>(R.id.editText)
            tvName.text = item.name
            val response = viewModel.getEventResponse(context, event, item.id)
            if (response != null) {
                editText.setText(response)
            }
            AppUtils.disableTextInputEditText(editText)
            editText.apply {
                setOnClickListener {
                    setOnClickListener {
                        AppUtils.showDatePickerDialog(
                            context, editText, setMaxNow = false, setMinNow = false
                        )
                    }
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
                            viewModel.addResponse(
                                context,
                                event, item.id, s.toString()
                            )
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

    val viewModel = MainViewModel(context.applicationContext as Application)

    inner class NumberEditTextViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val viewModel = MainViewModel(context.applicationContext as Application)
        fun bind(item: DataElement) {
            val tvName = itemView.findViewById<TextView>(R.id.tv_name)
            val textInputLayout = itemView.findViewById<TextInputLayout>(R.id.textInputLayout)
            val editText = itemView.findViewById<TextInputEditText>(R.id.editText)
            tvName.text = item.name
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
                            viewModel.addResponse(
                                context,
                                event, item.id, s.toString()
                            )
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
        val viewModel = MainViewModel(context.applicationContext as Application)
        fun bind(item: DataElement) {
            val checkBox = itemView.findViewById<CheckBox>(R.id.checkBox)
            val tvName = itemView.findViewById<TextView>(R.id.tv_name)
            tvName.text = item.name
            val response = viewModel.getEventResponse(context, event, item.id)
            if (response != null) {
                if (response == "Yes") {
                    checkBox.isChecked = true
                }
            }
            checkBox.setOnCheckedChangeListener { buttonView, isChecked ->
                if (isChecked) {
                    viewModel.addResponse(
                        context,
                        event, item.id, "Yes"
                    )
                } else {
                    viewModel.deleteResponse(
                        context,
                        event, item.id,
                    )
                }
            }
        }
    }

    inner class EditTextViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val viewModel = MainViewModel(context.applicationContext as Application)
        val tvName = itemView.findViewById<TextView>(R.id.tv_name)
        val textInputLayout = itemView.findViewById<TextInputLayout>(R.id.textInputLayout)
        val editText = itemView.findViewById<TextInputEditText>(R.id.editText)

        init {
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
                            viewModel.addResponse(
                                context,
                                event,
                                items[adapterPosition].id,
                                s.toString()
                            )
                        }
                    }

                    override fun afterTextChanged(s: Editable?) {
                        // This method is called after the text has changed.
                        // You can perform actions here based on the updated text.
                    }
                })
            }
        }

        fun bind(item: DataElement) {
            tvName.text = item.name
            val response = viewModel.getEventResponse(context, event, item.id)
            if (response != null) {
                editText.setText(response)
            }


        }
    }

    inner class OrgUnitViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val viewModel = MainViewModel(context.applicationContext as Application)
        val tvName = itemView.findViewById<TextView>(R.id.tv_name)
        val textInputLayout = itemView.findViewById<TextInputLayout>(R.id.textInputLayout)
        val editText = itemView.findViewById<TextInputEditText>(R.id.editText)
        private lateinit var dialog: AlertDialog
        init {
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
                              viewModel.addResponse(
                                  context,
                                  event,
                                  items[adapterPosition].id,
                                  s.toString()
                              )
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
                            dialog = dialogBuilder.create()
                            dialog.show()
                        }
                    } else {
                        showNoOrgUnits(context)
                    }
                }
            }
        }

        fun bind(item: DataElement) {
            tvName.text = item.name
            val response = viewModel.getEventResponse(context, event, item.id)
            if (response != null) {
                editText.setText(response)
            }


        }
    }

    inner class LongEditTextViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val viewModel = MainViewModel(context.applicationContext as Application)
        fun bind(item: DataElement) {
            val tvName = itemView.findViewById<TextView>(R.id.tv_name)
            val textInputLayout = itemView.findViewById<TextInputLayout>(R.id.textInputLayout)
            val editText = itemView.findViewById<TextInputEditText>(R.id.editText)
            tvName.text = item.name
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
                            viewModel.addResponse(
                                context,
                                event, item.id, s.toString()
                            )
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
        val viewModel = MainViewModel(context.applicationContext as Application)
        fun bind(item: DataElement) {
            val tvName = itemView.findViewById<TextView>(R.id.tv_name)
            val radioButtonYes = itemView.findViewById<RadioButton>(R.id.radioButtonYes)
            val radioButtonNo = itemView.findViewById<RadioButton>(R.id.radioButtonNo)
            tvName.text = item.name
            val response = viewModel.getEventResponse(context, event, item.id)
            if (response != null) {
                if (response == "Yes") {
                    radioButtonYes.isChecked = true
                } else if (response == "No") {
                    radioButtonNo.isChecked = true
                }
            }

            radioButtonNo.apply {
                setOnCheckedChangeListener { button, isChecked ->
                    if (isChecked) {
                        viewModel.addResponse(
                            context,
                            event, item.id, "No"
                        )
                    }
                }
            }
            radioButtonYes.apply {
                setOnCheckedChangeListener { button, isChecked ->
                    if (isChecked) {
                        viewModel.addResponse(
                            context,
                            event, item.id, "Yes"
                        )
                    }
                }
            }
        }
    }

    inner class AutoCompleteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val optionsList: MutableList<String> = mutableListOf()
        val adp = ArrayAdapter(context, android.R.layout.simple_list_item_1, optionsList)
        val tvName = itemView.findViewById<TextView>(R.id.tv_name)
        val textInputLayout = itemView.findViewById<TextInputLayout>(R.id.textInputLayout)
        val autoCompleteTextView =
            itemView.findViewById<AutoCompleteTextView>(R.id.autoCompleteTextView)

        init {
            autoCompleteTextView.setAdapter(adp)
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
                            viewModel.addResponse(
                                context,
                                event,
                                items[adapterPosition].id,
                                s.toString()
                            )
                        }
                    }

                    override fun afterTextChanged(s: Editable?) {
                        // This method is called after the text has changed.
                        // You can perform actions here based on the updated text.
                    }
                })
            }
        }

        fun bind(item: DataElement) {
            optionsList.clear()
            item.optionSet?.options?.forEach {
                optionsList.add(it.name)
            }
            tvName.text = item.name
            val response = viewModel.getEventResponse(context, event, item.id)
            if (response != null) {
                autoCompleteTextView.setText(response, false)
            }

        }
    }
}

