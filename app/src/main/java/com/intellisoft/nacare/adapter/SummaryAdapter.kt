package com.intellisoft.nacare.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.intellisoft.nacare.helper_class.ProgramCategory
import com.nacare.capture.R


class SummaryAdapter(private val itemList: List<ProgramCategory>) :
    RecyclerView.Adapter<SummaryAdapter.ViewHolder>() {

    private var expandedPosition = -1

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val headerTextView: TextView = itemView.findViewById(R.id.headerTextView)
        val subItemRecyclerView: RecyclerView = itemView.findViewById(R.id.subItemRecyclerView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_summary, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = itemList[position]
        holder.headerTextView.text = item.name

        // Check if this item is expanded or not
        val isExpanded = position == expandedPosition
        holder.subItemRecyclerView.visibility = if (isExpanded) View.VISIBLE else View.GONE

        holder.headerTextView.setOnClickListener {
            // Toggle the expanded state
            expandedPosition = if (isExpanded) -1 else position
            notifyDataSetChanged()
        }

        // Bind sub-items using another RecyclerView (you can use any layout manager here)
        val subItemAdapter = SummaryItemAdapter(item.elements)
        holder.subItemRecyclerView.adapter = subItemAdapter
    }

    override fun getItemCount(): Int {
        return itemList.size
    }
}