package com.capture.app.holders

import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.capture.app.R
import com.capture.app.model.ExpandableItem

class PersonViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    fun bind(expandableItem: ExpandableItem, isSelected: Boolean) {
        itemView.isSelected = isSelected
    }

    val textViewName: TextView = itemView.findViewById(R.id.textViewName)
    val linearLayout: LinearLayout = itemView.findViewById(R.id.linearLayout)
    val lnLinearLayout: LinearLayout = itemView.findViewById(R.id.lnLinearLayout)
    val rotationImageView: ImageView = itemView.findViewById(R.id.rotationImageView)
}