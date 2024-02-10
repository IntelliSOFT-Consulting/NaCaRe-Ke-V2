package com.imeja.nacare_live.holders

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.imeja.nacare_live.R


class SearchResultHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val uniqueTextView: TextView = itemView.findViewById(R.id.uniqueTextView)
    val hospitalNo: TextView = itemView.findViewById(R.id.hospitalNo)
    val patientName: TextView = itemView.findViewById(R.id.patientName)
    val identificationDoc: TextView = itemView.findViewById(R.id.identificationDoc)
    val diagnosis: TextView = itemView.findViewById(R.id.diagnosis)

}