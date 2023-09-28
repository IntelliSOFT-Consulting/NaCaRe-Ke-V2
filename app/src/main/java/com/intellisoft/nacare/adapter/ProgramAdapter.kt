package com.intellisoft.nacare.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.intellisoft.nacare.helper_class.FormatterClass
import com.intellisoft.nacare.helper_class.ProgramCategory
import com.nacare.ke.capture.R


class ProgramAdapter(
    private val context: Context,
    private val dataList: List<ProgramCategory>, private val click: (ProgramCategory) -> Unit
) : RecyclerView.Adapter<ProgramAdapter.ProgramHolder>() {

    private val selectedItems = HashSet<Int>() // To keep track of selected item IDs

    inner class ProgramHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView), View.OnClickListener {
        val formatterClass = FormatterClass()
/*
        val syncIcon: ImageView = itemView.findViewById(R.id.sync_icon)
        val eventStatus: ImageView = itemView.findViewById(R.id.eventStatus)
        val eventDate: TextView = itemView.findViewById(R.id.event_date)*/
        val progressTextView: TextView = itemView.findViewById(R.id.progressTextView)
        val eventTextView: TextView = itemView.findViewById(R.id.eventTextView)
        private val materialCardView: MaterialCardView =
            itemView.findViewById(R.id.materialCardView)

        init {
            materialCardView.setOnClickListener(this)
        }

        override fun onClick(p0: View?) {
            val patient = dataList[adapterPosition]
            click(patient)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProgramHolder {
        val itemView =
            LayoutInflater.from(parent.context).inflate(R.layout.item_program, parent, false)
        return ProgramHolder(itemView)
    }

    override fun onBindViewHolder(holder: ProgramHolder, position: Int) {
        val item = dataList[position]
        holder.eventTextView.text = item.name
        holder.progressTextView.text = " ${item.done}/${item.total}"
    }

    override fun getItemCount(): Int {
        return dataList.size
    }
}