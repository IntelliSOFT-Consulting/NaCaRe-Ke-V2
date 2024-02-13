package com.nacare.capture.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.nacare.capture.R
import com.nacare.capture.data.FormatterClass
import com.nacare.capture.holders.TrackedEntityHolder
import com.nacare.capture.model.EntityData
import com.nacare.capture.room.Converters


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
        holder.dateTextView.text = data.date
        holder.firstnameTextView.text = data.fName
        holder.lastnameTextView.text = data.lName
        holder.actionTextView.text = data.diagnosis

        val name = "${data.fName} ${data.lName}"

        holder.tv_place_of_notification.text =
            "${context.getString(R.string.place_of_notification)}" + org
        holder.tv_patient_name.text =
            context.getString(R.string.patient_name) + name
        holder.tv_phone_no.text =
            context.getString(R.string.phone_no) + extractValueFromAttributes(
                "fZB1WuCDlHt",
                data.attributes
            )
        holder.tv_hospital_no.text =
            context.getString(R.string.hospital_no) + extractValueFromAttributes(
                "MiXrdHDZ6Hw",
                data.attributes
            )
        holder.tv_id_doc_no.text =
            context.getString(R.string.id_doc_no) + extractValueFromAttributes("eFbT7iTnljR", data.attributes)
        holder.tv_patient_id.text =
            context.getString(R.string.patient_id) + extractValueFromAttributes(
                "AP13g7NcBOf",
                data.attributes
            )

        val upDrawable = ContextCompat.getDrawable(context, R.drawable.resized_icon)
        val downDrawable = ContextCompat.getDrawable(context, R.drawable.resized_icon_down)


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

        holder.ln_next_page.apply {
            setOnClickListener {
                click(data)
            }
        }

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