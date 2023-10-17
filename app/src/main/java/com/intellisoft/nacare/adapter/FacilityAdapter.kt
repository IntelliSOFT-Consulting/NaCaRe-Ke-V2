package com.intellisoft.nacare.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.intellisoft.nacare.helper_class.FacilityProgramCategory
import com.intellisoft.nacare.helper_class.FormatterClass
import com.intellisoft.nacare.util.AppUtils
import com.nacare.capture.R


class FacilityAdapter(
    private val context: Context,
    private val dataList: List<FacilityProgramCategory>,
    private val code: String
) : RecyclerView.Adapter<FacilityAdapter.ProgramHolder>() {

    inner class ProgramHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView), View.OnClickListener {
        val formatterClass = FormatterClass()
        val progressTextView: TextView = itemView.findViewById(R.id.progressTextView)
        val eventTextView: TextView = itemView.findViewById(R.id.eventTextView)
        val leftIconImageView: ImageView = itemView.findViewById(R.id.leftIconImageView)
        val recyclerView: RecyclerView = itemView.findViewById(R.id.recyclerView)


        override fun onClick(p0: View?) {
            val patient = dataList[adapterPosition]

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProgramHolder {
        val itemView =
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_facility_program, parent, false)
        return ProgramHolder(itemView)
    }

    override fun onBindViewHolder(holder: ProgramHolder, position: Int) {
        val item = dataList[position]
        holder.eventTextView.text = item.name
        holder.progressTextView.text = " ${item.done}/${item.total}"

//        try {

        val ad = FacilityElementAdapter(context, item.elements,code)
        holder.recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = ad

        }
//        } catch (e: Exception) {
//
//        }
    }

    override fun getItemCount(): Int {
        return dataList.size
    }
}