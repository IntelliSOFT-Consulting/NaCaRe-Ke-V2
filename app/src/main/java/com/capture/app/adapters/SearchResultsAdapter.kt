package com.capture.app.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.capture.app.R
import com.capture.app.holders.SearchResultHolder
import com.capture.app.model.SearchResult


class SearchResultsAdapter(
    private val dataList: List<SearchResult>,
    private val context: Context,
    private val click: (SearchResult) -> Unit
) : RecyclerView.Adapter<SearchResultHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchResultHolder {
        return SearchResultHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.item_search_data_child,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: SearchResultHolder, position: Int) {
        val data = dataList[position]
        holder.uniqueTextView.text = data.uniqueId
        holder.hospitalNo.text = data.hospitalNo
        holder.patientName.text = data.patientName
        holder.identificationDoc.text = data.identification
        holder.diagnosis.text = data.diagnosis

        holder.uniqueTextView.setTextColor(context.resources.getColor(R.color.black))
        holder.hospitalNo.setTextColor(context.resources.getColor(R.color.black))
        holder.patientName.setTextColor(context.resources.getColor(R.color.black))
        holder.identificationDoc.setTextColor(context.resources.getColor(R.color.black))
        holder.diagnosis.setTextColor(context.resources.getColor(R.color.black))

        holder.itemView.apply {
            setOnClickListener {
                click(data)
            }
        }

    }

    override fun getItemCount(): Int {
        return dataList.size
    }
}
