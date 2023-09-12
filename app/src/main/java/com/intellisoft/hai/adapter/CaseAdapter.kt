package com.intellisoft.hai.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.intellisoft.hai.R
import com.intellisoft.hai.helper_class.DataItems


class CaseAdapter(
    private val context: Context,
    private val items: List<DataItems>
) :
    RecyclerView.Adapter<CaseAdapter.ItemViewHolder>() {

    inner class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nameTextView: TextView = itemView.findViewById(R.id.tv_name)
        val tvCount: TextView = itemView.findViewById(R.id.tv_count)
        val recyclerView: RecyclerView = itemView.findViewById(R.id.recyclerView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val itemView =
            LayoutInflater.from(parent.context).inflate(R.layout.item_layout, parent, false)
        return ItemViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val currentItem = items[position]
        val count = position + 1
        holder.tvCount.text = count.toString()
        holder.nameTextView.text = currentItem.name
        val dataEntryAdapter = CaseChildAdapter(context,currentItem.elements)
        holder.recyclerView.layoutManager = LinearLayoutManager(context)
        holder.recyclerView.adapter = dataEntryAdapter
    }

    override fun getItemCount(): Int {
        return items.size
    }
}