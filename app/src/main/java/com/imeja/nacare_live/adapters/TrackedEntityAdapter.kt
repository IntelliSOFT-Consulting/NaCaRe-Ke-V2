package com.imeja.nacare_live.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.recyclerview.widget.RecyclerView
import com.imeja.nacare_live.R
import com.imeja.nacare_live.holders.TrackedEntityHolder
import com.imeja.nacare_live.model.EntityData


class TrackedEntityAdapter(
    private val dataList: List<EntityData>,
    private val context: Context,
    private val click: (EntityData) -> Unit
) : RecyclerView.Adapter<TrackedEntityHolder>() {
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
        holder.dateTextView.text = data.date
        holder.firstnameTextView.text = data.fName
        holder.lastnameTextView.text = data.lName
        holder.actionTextView.text = data.diagnosis

        holder.tv_place_of_notification.text = context.getString(R.string.place_of_notification)
        holder.tv_patient_name.text = context.getString(R.string.patient_name)
        holder.tv_phone_no.text = context.getString(R.string.phone_no)
        holder.tv_hospital_no.text = context.getString(R.string.hospital_no)
        holder.tv_id_doc_no.text = context.getString(R.string.id_doc_no)
        holder.tv_patient_id.text = context.getString(R.string.patient_id)

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

    override fun getItemCount(): Int {
        return dataList.size
    }
}