package com.intellisoft.hai.adapter

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.intellisoft.hai.R
import com.intellisoft.hai.helper_class.FormatterClass
import com.intellisoft.hai.room.HandPreparationData

class HandAdapter(
    private val context: Context,
    private val childItems: List<HandPreparationData>
) :
    RecyclerView.Adapter<HandAdapter.ViewHolder>() {
    val formatterClass = FormatterClass()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.hand_patient, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val childItem = childItems[position]
        holder.bind(childItem)
    }

    override fun getItemCount(): Int {
        return childItems.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvTime = itemView.findViewById<TextView>(R.id.tv_time)
        private val tvPlain = itemView.findViewById<TextView>(R.id.tv_plain)
        private val tvAntimicrobial = itemView.findViewById<TextView>(R.id.tv_antimicrobial)
        private val tvRole = itemView.findViewById<TextView>(R.id.tv_role)
        private val tvAlcohol = itemView.findViewById<TextView>(R.id.tv_alcohol)

        fun bind(data: HandPreparationData) {
            tvTime.text = data.time_spent
            tvPlain.text = data.plain_soap_water
            tvAntimicrobial.text = data.antimicrobial_soap_water
            tvRole.text = data.practitioner
            tvAlcohol.text = data.hand_rub
        }
    }
}