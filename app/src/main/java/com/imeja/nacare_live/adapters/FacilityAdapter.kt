package com.imeja.nacare_live.adapters

import android.content.Context
import android.text.Html
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.imeja.nacare_live.R
import com.imeja.nacare_live.data.FormatterClass
import com.imeja.nacare_live.holders.FacilityHolder
import com.imeja.nacare_live.model.FacilitySummary

class FacilityAdapter(
    private val dataList: List<FacilitySummary>,
    private val context: Context,
    private val click: (FacilitySummary) -> Unit
) : RecyclerView.Adapter<FacilityHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FacilityHolder {
        return FacilityHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.item_facility_data,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: FacilityHolder, position: Int) {
        val data = dataList[position]
        val formatter = FormatterClass()
        val date = formatter.convertDateFormat(data.date)//convertDateFormat("hhhh")
        holder.dateTextView.text = date
        holder.statusTextView.text = data.status
        val htmlText = "<a href=\"\">Edit</a>"
        holder.actionTextView.text = Html.fromHtml(htmlText)
        holder.itemView.setOnClickListener {
            click(data)
        }

    }

    override fun getItemCount(): Int {
        return dataList.size
    }
}