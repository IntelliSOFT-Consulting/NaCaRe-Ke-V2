package com.intellisoft.hai.adapter

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.navigation.NavController
import androidx.recyclerview.widget.RecyclerView
import com.intellisoft.hai.R
import com.intellisoft.hai.helper_class.FormatterClass
import com.intellisoft.hai.room.Converters
import com.intellisoft.hai.room.PostOperativeData

class PostDataAdapter(
    private val context: Context,
    private val hostNavController: NavController,
    private val childItems: List<PostOperativeData>
) :
    RecyclerView.Adapter<PostDataAdapter.ViewHolder>() {
    val formatterClass = FormatterClass()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_child, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val childItem = childItems[position]
        holder.bind(childItem)
    }

    override fun getItemCount(): Int {
        return childItems.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvRisk = itemView.findViewById<TextView>(R.id.tv_risk)
        private val tvGlucose = itemView.findViewById<TextView>(R.id.tv_glucose)
        private val tvLevel = itemView.findViewById<TextView>(R.id.tv_level)
        private val tvIntervention = itemView.findViewById<TextView>(R.id.tv_intervention)
        private val tvAction = itemView.findViewById<TextView>(R.id.tvAction)

        init {
            tvAction.setOnClickListener {
                val data = childItems[adapterPosition]
                val converters = Converters()
                val jeff = converters.toPostJson(data)
                formatterClass.saveSharedPref("post", jeff, context)
                val caseId = formatterClass.getSharedPref("encounter", context)
                val bundle = Bundle()
                bundle.putString("caseId", caseId)
                hostNavController.navigate(R.id.postDateFragment, bundle)
            }
        }

        fun bind(childItem: PostOperativeData) {
            tvRisk.text = childItem.check_up_date
            tvGlucose.text = childItem.infection_signs
            tvLevel.text = childItem.check_up_date
            tvIntervention.text = childItem.infection_signs
            tvAction.text = "View"
        }
    }
}