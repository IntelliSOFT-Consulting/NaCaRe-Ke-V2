package com.nacare.capture.holders

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.nacare.capture.R

class FacilityHolder (itemView: View) : RecyclerView.ViewHolder(itemView) {
    val dateTextView: TextView = itemView.findViewById(R.id.dateTextView)
    val statusTextView: TextView = itemView.findViewById(R.id.statusTextView)
    val actionTextView: TextView = itemView.findViewById(R.id.actionTextView)
}