package com.imeja.nacare_live.holders

import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.imeja.nacare_live.R

class TrackedEntityHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val dateTextView: TextView = itemView.findViewById(R.id.dateTextView)
    val firstnameTextView: TextView = itemView.findViewById(R.id.firstnameTextView)
    val lastnameTextView: TextView = itemView.findViewById(R.id.lastnameTextView)
    val actionTextView: TextView = itemView.findViewById(R.id.actionTextView)
    val hiddenLayout: LinearLayout = itemView.findViewById(R.id.hiddenLayout)

}