package com.capture.app.adapters

import android.app.Application
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.capture.app.R
import com.capture.app.data.FormatterClass
import com.capture.app.holders.TrackedEntityHolder
import com.capture.app.model.EntityData
import com.capture.app.room.Converters
import com.capture.app.room.MainViewModel


class TrackedEntityAdapter(
    private val dataList: List<EntityData>,
    private val context: Context,
    private val click: (EntityData) -> Unit
) : RecyclerView.Adapter<TrackedEntityHolder>() {
    private val formatter = FormatterClass()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TrackedEntityHolder {
        return TrackedEntityHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.list_item_tracked,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: TrackedEntityHolder, position: Int) {
        val data = dataList[position]
        val org = formatter.getSharedPref("orgName", context)
        holder.dateTextView.text = "  ${data.date}"
        holder.firstnameTextView.text = data.fName
        holder.lastnameTextView.text = data.lName
        holder.actionTextView.text = data.diagnosis

//        holder.dateTextView.setBackgroundResource(R.drawable.top_left_border_layoured)


        val name = "${data.fName} ${data.lName}"

        holder.tv_place_of_notification.text = org
        holder.tv_patient_name.text = name
        holder.tv_phone_no.text = extractValueFromDataValues(
            data.id,
            "fZB1WuCDlHt",
        )
        holder.tv_hospital_no.text = extractValueFromAttributes(
            "MiXrdHDZ6Hw",
            data.attributes
        )
        holder.tv_id_doc_no.text = extractValueFromAttributes(
            "eFbT7iTnljR",
            data.attributes
        )
        holder.tv_patient_id.text = extractValueFromAttributes(
            "AP13g7NcBOf",
            data.attributes
        )

        val upDrawable = ContextCompat.getDrawable(context, R.drawable.resized_icon_medium)
        val downDrawable = ContextCompat.getDrawable(context, R.drawable.resized_icon_down_medium)


        holder.dateTextView.setCompoundDrawablesRelativeWithIntrinsicBounds(
            downDrawable,
            null,
            null,
            null
        )


        holder.dateTextView.setOnClickListener { v ->
            if (holder.hiddenLayout.visibility === View.VISIBLE) {
                holder.hiddenLayout.visibility = View.GONE
                holder.dateTextView.setCompoundDrawablesRelativeWithIntrinsicBounds(
                    downDrawable,
                    null,
                    null,
                    null
                )
            } else {
                holder.hiddenLayout.visibility = View.VISIBLE
                holder.dateTextView.setCompoundDrawablesRelativeWithIntrinsicBounds(
                    upDrawable,
                    null,
                    null,
                    null
                )
            }
        }

        holder.btnProceed.apply {
            setOnClickListener {
                click(data)
            }
        }

    }

    private fun extractValueFromDataValues(patient: String, uid: String): String {
        var data = ""
        val viewModel = MainViewModel(context.applicationContext as Application)
        val single = viewModel.getLatestEnrollment(context, patient)
        if (single != null) {
            val dtValues = Converters().fromJsonDataAttribute(single.dataValues)
            val found = dtValues.find { it.dataElement == uid }
            if (found != null) {
                data = found.value
            }

        }
        return data
    }


    private fun extractValueFromAttributes(s: String, attributes: String): String {
        var data = ""
        val converters = Converters().fromJsonAttribute(attributes)
        val single = converters.find { it.attribute == s }
        if (single != null) {
            data = single.value
        }

        return data

    }


    override fun getItemCount(): Int {
        return dataList.size
    }
}