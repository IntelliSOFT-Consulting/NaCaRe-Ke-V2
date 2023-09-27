package com.intellisoft.nacare.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.intellisoft.nacare.helper_class.FormatterClass
import com.intellisoft.nacare.room.EventData
import com.nacare.ke.capture.R


class EventAdapter(
    private var dataList: List<EventData>,
    private val context: Context,
    private val click: (EventData) -> Unit
) : RecyclerView.Adapter<EventAdapter.Pager2ViewHolder>() {

    inner class Pager2ViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView), View.OnClickListener {
        val formatterClass = FormatterClass()
        val syncIcon: ImageView = itemView.findViewById(R.id.sync_icon)
        val eventStatus: ImageView = itemView.findViewById(R.id.eventStatus)
        val eventDate: TextView = itemView.findViewById(R.id.event_date)
        val organisationUnit: TextView = itemView.findViewById(R.id.organisationUnit)
        val eventInfo: TextView = itemView.findViewById(R.id.eventInfo)
        private val eventCard: MaterialCardView = itemView.findViewById(R.id.eventCard)

        init {
            eventCard.setOnClickListener(this)
        }

        override fun onClick(p0: View?) {
            val patient = dataList[adapterPosition]
            click(patient)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Pager2ViewHolder {
        return Pager2ViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_patient, parent, false)
        )
    }

    override fun onBindViewHolder(holder: Pager2ViewHolder, position: Int) {
        val date = dataList[position].date
        val org = dataList[position].orgUnitName
        holder.organisationUnit.text = org
        holder.eventDate.text = date
//        holder.iconImageView.setImageResource(icon)

    }

    override fun getItemCount(): Int {
        return dataList.size
    }
}