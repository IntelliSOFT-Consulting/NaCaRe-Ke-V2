package com.intellisoft.hai.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.intellisoft.hai.R
import com.intellisoft.hai.helper_class.FormatterClass
import com.intellisoft.hai.room.EncounterData

class EncounterAdapter(
    private var patientList: List<EncounterData>,
    private val context: Context,
    private val click: (EncounterData) -> Unit
) : RecyclerView.Adapter<EncounterAdapter.Pager2ViewHolder>() {

    inner class Pager2ViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView), View.OnClickListener {

        val formatterClass = FormatterClass()

        val tvDate: TextView = itemView.findViewById(R.id.tv_date)

        init {
            tvDate.setOnClickListener(this)
        }

        override fun onClick(p0: View?) {
            val patient = patientList[adapterPosition]
            click(patient)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Pager2ViewHolder {
        return Pager2ViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_visits, parent, false)
        )
    }

    override fun onBindViewHolder(holder: Pager2ViewHolder, position: Int) {

        val patientId = patientList[position].patientId
        val userId = patientList[position].userId
        val date = patientList[position].date
        holder.tvDate.text = date
    }

    override fun getItemCount(): Int {
        return patientList.size
    }
}