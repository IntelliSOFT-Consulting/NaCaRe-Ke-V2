package com.intellisoft.nacare.adapter

import android.app.Application
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.CheckBox
import android.widget.RadioButton
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.intellisoft.nacare.helper_class.DataElementItem
import com.intellisoft.nacare.room.MainViewModel
import com.nacare.ke.capture.R

class FacilityElementAdapter(
    private val context: Context,
    private val items: List<DataElementItem>,
    private val code: String
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

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = items[position]
        val viewModel = MainViewModel(context.applicationContext as Application)
        val answer = viewModel.getFacilityResponse(context, code,item.id)
        when (holder.itemViewType) {
            VIEW_TYPE_EDITTEXT -> {
                val editTextHolder = holder as EditTextViewHolder
                editTextHolder.bind(item, answer)
            }

            VIEW_TYPE_NUMBER -> {
                val numberTextHolder = holder as NumberEditTextViewHolder
                numberTextHolder.bind(item, answer)
            }


            VIEW_TYPE_PHONE_NUMBER -> {
                val numberTextHolder = holder as PhoneNumberEditTextViewHolder
                numberTextHolder.bind(item, answer)
            }

            VIEW_TYPE_RADIO -> {
                val radioViewHolder = holder as RadioViewHolder
                radioViewHolder.bind(item, answer)
            }

            VIEW_TYPE_AUTOCOMPLETE -> {
                val autoCompleteViewHolder = holder as AutoCompleteViewHolder
                autoCompleteViewHolder.bind(item, answer)
            }

            VIEW_TYPE_LONG_TEXT -> {
                val longHolder = holder as LongEditTextViewHolder
                longHolder.bind(item, answer)
            }

            VIEW_TYPE_DATE -> {
                val dateHolder = holder as DateEditTextViewHolder
                dateHolder.bind(item, answer)
            }

            VIEW_TYPE_TRUE_ONLY -> {
                val checkBoxHolder = holder as CheckBoxViewHolder
                checkBoxHolder.bind(item, answer)
            }

            VIEW_TYPE_ORGANIZATION -> {
                val orgHolder = holder as OrgUnitViewHolder
                orgHolder.bind(item, answer)
            }
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun getItemViewType(position: Int): Int {
        val item = items[position]
        return when (item.valueType) {
            "TEXT" -> VIEW_TYPE_EDITTEXT
            "DATE" -> VIEW_TYPE_DATE
            "BOOLEAN" -> VIEW_TYPE_RADIO
            "LONG_TEXT" -> VIEW_TYPE_LONG_TEXT
            "NUMBER" -> VIEW_TYPE_NUMBER
            "INTEGER_POSITIVE" -> VIEW_TYPE_NUMBER
            "TRUE_ONLY" -> VIEW_TYPE_TRUE_ONLY
            "ORGANISATION_UNIT" -> VIEW_TYPE_ORGANIZATION
            "PHONE_NUMBER" -> VIEW_TYPE_PHONE_NUMBER
            else -> VIEW_TYPE_EDITTEXT
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
        val tvName = itemView.findViewById<TextView>(R.id.tv_name)
        val textInputLayout = itemView.findViewById<TextInputLayout>(R.id.textInputLayout)
        val editText = itemView.findViewById<TextInputEditText>(R.id.editText)
        fun bind(item: DataElementItem, answer: String) {
            tvName.text = item.displayName
        }

        private fun containsAnyKeyword(displayName: String, keywords: List<String>): Boolean {
            return keywords.any { keyword -> displayName.contains(keyword) }
        }
    }

    val viewModel = MainViewModel(context.applicationContext as Application)

    inner class NumberEditTextViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val viewModel = MainViewModel(context.applicationContext as Application)
        val tvName = itemView.findViewById<TextView>(R.id.tv_name)
        val textInputLayout = itemView.findViewById<TextInputLayout>(R.id.textInputLayout)
        val editText = itemView.findViewById<TextInputEditText>(R.id.editText)
        fun bind(item: DataElementItem, answer: String) {
            tvName.text = item.displayName
            editText.isEnabled = false

        }
    }

    inner class PhoneNumberEditTextViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val viewModel = MainViewModel(context.applicationContext as Application)
        val tvName = itemView.findViewById<TextView>(R.id.tv_name)
        val textInputLayout = itemView.findViewById<TextInputLayout>(R.id.textInputLayout)
        val editText = itemView.findViewById<TextInputEditText>(R.id.editText)
        fun bind(item: DataElementItem, answer: String) {
            tvName.text = item.displayName
            editText.isEnabled = false
        }
    }

    inner class CheckBoxViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val viewModel = MainViewModel(context.applicationContext as Application)
        fun bind(item: DataElementItem, answer: String) {
            val checkBox = itemView.findViewById<CheckBox>(R.id.checkBox)
            val tvName = itemView.findViewById<TextView>(R.id.tv_name)
            tvName.text = item.displayName
            checkBox.isEnabled = false


        }
    }

    inner class EditTextViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val viewModel = MainViewModel(context.applicationContext as Application)
        val tvName = itemView.findViewById<TextView>(R.id.tv_name)
        val tvElement = itemView.findViewById<TextView>(R.id.tv_element)
        val textInputLayout = itemView.findViewById<TextInputLayout>(R.id.textInputLayout)
        val editText = itemView.findViewById<TextInputEditText>(R.id.editText)


        fun bind(item: DataElementItem, answer: String) {
            tvName.text = item.displayName
            tvElement.text = item.id
            editText.isEnabled = false
            editText.setText(answer)
        }
    }

    inner class OrgUnitViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val viewModel = MainViewModel(context.applicationContext as Application)
        val tvName = itemView.findViewById<TextView>(R.id.tv_name)
        val textInputLayout = itemView.findViewById<TextInputLayout>(R.id.textInputLayout)
        val editText = itemView.findViewById<TextInputEditText>(R.id.editText)
        private lateinit var dialog: AlertDialog


        fun bind(item: DataElementItem, answer: String) {
            tvName.text = item.displayName
        }
    }

    inner class LongEditTextViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val viewModel = MainViewModel(context.applicationContext as Application)
        val tvName = itemView.findViewById<TextView>(R.id.tv_name)
        val textInputLayout = itemView.findViewById<TextInputLayout>(R.id.textInputLayout)
        val editText = itemView.findViewById<TextInputEditText>(R.id.editText)
        fun bind(item: DataElementItem, answer: String) {
            tvName.text = item.displayName
            editText.isEnabled = false
        }
    }

    inner class RadioViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val viewModel = MainViewModel(context.applicationContext as Application)
        fun bind(item: DataElementItem, answer: String) {
            val tvName = itemView.findViewById<TextView>(R.id.tv_name)
            val radioButtonYes = itemView.findViewById<RadioButton>(R.id.radioButtonYes)
            val radioButtonNo = itemView.findViewById<RadioButton>(R.id.radioButtonNo)
            tvName.text = item.displayName
            radioButtonNo.isEnabled = false
            radioButtonYes.isEnabled = false
            when (answer) {
                "true" -> {
                    radioButtonYes.isChecked = true
                }
                "false" -> {
                    radioButtonNo.isChecked = true
                }
            }

        }
    }

    inner class AutoCompleteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val optionsList: MutableList<String> = mutableListOf()
        val adp = ArrayAdapter(context, android.R.layout.simple_list_item_1, optionsList)
        val tvName = itemView.findViewById<TextView>(R.id.tv_name)
        val tvElement = itemView.findViewById<TextView>(R.id.tv_element)
        val textInputLayout = itemView.findViewById<TextInputLayout>(R.id.textInputLayout)
        val autoCompleteTextView =
            itemView.findViewById<AutoCompleteTextView>(R.id.autoCompleteTextView)


        fun bind(item: DataElementItem, answer: String) {
            tvElement.text = item.id
            autoCompleteTextView.isEnabled = false


        }
    }
}

