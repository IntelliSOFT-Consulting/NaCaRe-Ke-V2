package com.imeja.nacare_live.holders

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.imeja.nacare_live.R

class FacilityHolder (itemView: View) : RecyclerView.ViewHolder(itemView) {
    val dateTextView: TextView = itemView.findViewById(R.id.dateTextView)
    val statusTextView: TextView = itemView.findViewById(R.id.statusTextView)
    val actionTextView: TextView = itemView.findViewById(R.id.actionTextView)
}