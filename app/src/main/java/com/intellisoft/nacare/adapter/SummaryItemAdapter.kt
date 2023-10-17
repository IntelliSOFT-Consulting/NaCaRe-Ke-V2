package com.intellisoft.nacare.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.intellisoft.nacare.helper_class.ProgramStageSections
import com.nacare.capture.R


class SummaryItemAdapter(private val subItems: List<ProgramStageSections>) :
    RecyclerView.Adapter<SummaryItemAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val subItemTextView: TextView = itemView.findViewById(R.id.subItemTextView)
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_subitem, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val data = subItems[position]
        holder.subItemTextView.text = data.displayName
    }

    override fun getItemCount(): Int {
        return subItems.size
    }
}