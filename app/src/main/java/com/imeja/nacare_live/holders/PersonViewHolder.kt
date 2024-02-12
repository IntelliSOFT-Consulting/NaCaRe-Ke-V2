package com.imeja.nacare_live.holders

import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.imeja.nacare_live.R

class PersonViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    val textViewName: TextView = itemView.findViewById(R.id.textViewName)
    val linearLayout: LinearLayout = itemView.findViewById(R.id.linearLayout)
    val lnLinearLayout: LinearLayout = itemView.findViewById(R.id.lnLinearLayout)
    val rotationImageView: ImageView = itemView.findViewById(R.id.rotationImageView)
}