package com.imeja.nacare_live.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.imeja.nacare_live.R
import com.imeja.nacare_live.model.HomeData


class DashAdapter(
    private val context: Context,
    private val dataList: List<HomeData>,
    private val click: (HomeData) -> Unit
) :
    RecyclerView.Adapter<DashAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view: View =
            LayoutInflater.from(context).inflate(R.layout.item_dashboard, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val homeData = dataList[position]
        holder.bind(homeData)
        holder.itemView.setOnClickListener {
            click(homeData)
        }
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val idTextView: TextView
        private val nameTextView: TextView

        init {
            idTextView = itemView.findViewById(R.id.tv_title)
            nameTextView = itemView.findViewById(R.id.tv_description)

        }

        fun bind(data: HomeData) {
            idTextView.text = data.id
            nameTextView.text = data.name
        }

    }
}