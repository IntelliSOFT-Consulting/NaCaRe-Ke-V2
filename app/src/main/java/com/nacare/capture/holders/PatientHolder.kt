package com.nacare.capture.holders

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.nacare.capture.R

class PatientHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val itemTitle: TextView = itemView.findViewById(R.id.itemTitle)
    val itemIcon: ImageView = itemView.findViewById(R.id.itemIcon)

}