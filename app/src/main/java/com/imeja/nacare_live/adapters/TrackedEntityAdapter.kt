package com.imeja.nacare_live.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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

        val upDrawable = context.resources.getDrawable(R.drawable.arrow_up)
        val downDrawable = context.resources.getDrawable(R.drawable.arrow_down)
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