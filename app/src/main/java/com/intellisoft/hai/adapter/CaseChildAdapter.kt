package com.intellisoft.hai.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.TextInputLayout
import com.intellisoft.hai.R
import com.intellisoft.hai.helper_class.DataElements


class CaseChildAdapter(
    private val context: Context,
    private val items: List<DataElements>
) :
    RecyclerView.Adapter<CaseChildAdapter.ItemViewHolder>() {

    inner class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nameTextView: TextView = itemView.findViewById(R.id.tv_name)
        val autoHolder: TextInputLayout = itemView.findViewById(R.id.auto_holder)
        val numberHolder: TextInputLayout = itemView.findViewById(R.id.number_holder)
        val textHolder: TextInputLayout = itemView.findViewById(R.id.text_holder)
        val actInput: AutoCompleteTextView = itemView.findViewById(R.id.act_input)
        val lnRadioSection: LinearLayout = itemView.findViewById(R.id.ln_radio_section)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val itemView =
            LayoutInflater.from(parent.context).inflate(R.layout.inner_item_layout, parent, false)
        return ItemViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val currentItem = items[position]
        holder.nameTextView.text = currentItem.quiz
        when (currentItem.type) {
            "dropdown" -> {
                holder.autoHolder.visibility = View.VISIBLE
                holder.lnRadioSection.visibility = View.GONE
                holder.numberHolder.visibility = View.GONE
                holder.textHolder.visibility = View.GONE
                val adapter =
                    ArrayAdapter(context, android.R.layout.simple_list_item_1, currentItem.options)
                holder.actInput.setAdapter(adapter);
            }

            "radio" -> {
                holder.autoHolder.visibility = View.GONE
                holder.lnRadioSection.visibility = View.VISIBLE
                holder.numberHolder.visibility = View.GONE
                holder.textHolder.visibility = View.GONE
            }

            "number" -> {
                holder.autoHolder.visibility = View.GONE
                holder.lnRadioSection.visibility = View.GONE
                holder.numberHolder.visibility = View.VISIBLE
                holder.textHolder.visibility = View.GONE
            }

            "string" -> {
                holder.autoHolder.visibility = View.GONE
                holder.lnRadioSection.visibility = View.GONE
                holder.numberHolder.visibility = View.GONE
                holder.textHolder.visibility = View.VISIBLE
            }
            else -> {
                holder.autoHolder.visibility = View.GONE
            }
        }
        
    }

    override fun getItemCount(): Int {
        return items.size
    }
}