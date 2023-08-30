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
import com.intellisoft.hai.helper_class.HomeItem


class HomeAdapter(
    private var dataList: List<HomeItem>,
    private val context: Context,
    private val click: (HomeItem) -> Unit
) : RecyclerView.Adapter<HomeAdapter.Pager2ViewHolder>() {

    inner class Pager2ViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView), View.OnClickListener {
        val formatterClass = FormatterClass()
        val iconImageView: ImageView = itemView.findViewById(R.id.iconImageView)
        val titleTextView: TextView = itemView.findViewById(R.id.titleTextView)
        val lnParent: LinearLayout = itemView.findViewById(R.id.ln_parent)

        init {
            lnParent.setOnClickListener(this)
        }

        override fun onClick(p0: View?) {
            val patient = dataList[adapterPosition]
            click(patient)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Pager2ViewHolder {
        return Pager2ViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_home, parent, false)
        )
    }

    override fun onBindViewHolder(holder: Pager2ViewHolder, position: Int) {
        val icon = dataList[position].iconResId
        val text = dataList[position].text
        holder.titleTextView.text = text
        holder.iconImageView.setImageResource(icon)

    }

    override fun getItemCount(): Int {
        return dataList.size
    }
}
