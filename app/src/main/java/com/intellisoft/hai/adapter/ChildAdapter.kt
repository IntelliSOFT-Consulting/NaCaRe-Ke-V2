package com.intellisoft.hai.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.intellisoft.hai.R

class ChildAdapter(private val childItems: List<String>) :
    RecyclerView.Adapter<ChildAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_child, parent, false)
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
        private val childTextView = itemView.findViewById<TextView>(R.id.childTextView)

        fun bind(childItem: String) {
            childTextView.text = childItem
        }
    }
}
