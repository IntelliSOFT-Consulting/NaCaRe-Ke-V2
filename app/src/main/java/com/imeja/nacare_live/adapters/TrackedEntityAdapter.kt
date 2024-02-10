package com.imeja.nacare_live.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
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
//        holder.uniqueTextView.setTextColor(context.resources.getColor(R.color.black))
//        holder.hospitalNo.setTextColor(context.resources.getColor(R.color.black))
//        holder.patientName.setTextColor(context.resources.getColor(R.color.black))
//        holder.identificationDoc.setTextColor(context.resources.getColor(R.color.black))
//        holder.diagnosis.setTextColor(context.resources.getColor(R.color.black))

        holder.itemView.apply {
            setOnClickListener {
                click(data)
            }
        }

    }

    override fun getItemCount(): Int {
        return dataList.size
    }
}