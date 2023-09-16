package com.intellisoft.hai.adapter

import android.app.Application
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.navigation.NavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.intellisoft.hai.R
import com.intellisoft.hai.helper_class.ParentItem
import com.intellisoft.hai.room.MainViewModel

class ParentAdapter(
    private val context: Context,
    private val hostNavController: NavController,
    private val click: (ParentItem) -> Unit,
    private val parentItems: List<ParentItem>,
    private val caseId: String,

    ) :
    RecyclerView.Adapter<ParentAdapter.ViewHolder>() {

    private var expandedPosition = RecyclerView.NO_POSITION


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_parent, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val parentItem = parentItems[position]
        holder.bind(parentItem)

    }

    override fun getItemCount(): Int {
        return parentItems.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val parentTextView = itemView.findViewById<TextView>(R.id.parentTextView)
        private val childRecyclerView = itemView.findViewById<RecyclerView>(R.id.childRecyclerView)
        private val ln_child = itemView.findViewById<LinearLayout>(R.id.ln_child)
        private val btnAdd = itemView.findViewById<MaterialButton>(R.id.btn_add)
        private val ln_child_items = itemView.findViewById<LinearLayout>(R.id.ln_child_items)
        private val expandIcon = itemView.findViewById<ImageView>(R.id.expandIcon)
        val viewModel = MainViewModel(context.applicationContext as Application)

        init {
            parentTextView.setOnClickListener {
                toggleExpandCollapse()
            }
            expandIcon.setOnClickListener {
                toggleExpandCollapse()
            }
            btnAdd.setOnClickListener {
                //show positions
                val patient = parentItems[adapterPosition]
                click(patient)

            }
        }


        private fun toggleExpandCollapse() {
            val position = adapterPosition
            if (position != RecyclerView.NO_POSITION) {
                // Check if the clicked item is already expanded
                val isExpanded = position == expandedPosition

                // Collapse the previously expanded item
                val previousExpandedPosition = expandedPosition
                expandedPosition = if (isExpanded) RecyclerView.NO_POSITION else position

                // Rotate the expandIcon based on the expansion state
                val rotationDegrees = if (isExpanded) 0f else 180f
                expandIcon.animate().rotation(rotationDegrees).start()

                // Notify items to update their UI
                if (previousExpandedPosition != RecyclerView.NO_POSITION) {
                    notifyItemChanged(previousExpandedPosition)
                }
                notifyItemChanged(position)
            }
        }

        fun bind(parentItem: ParentItem) {
            parentTextView.text = parentItem.name
            val isExpanded = adapterPosition == expandedPosition
            ln_child.visibility = if (isExpanded) View.VISIBLE else View.GONE
            // Create an adapter for child items and set it to childRecyclerView
            if (parentItem.pos == "0") {
                val data = viewModel.loadPeriData(context, caseId)
                if (data != null) {
                    if (data.isNotEmpty()) {

                        btnAdd.visibility = View.GONE
                        ln_child_items.visibility = View.VISIBLE
                        val childAdapter = ChildAdapter(context,hostNavController, data)
                        childRecyclerView.layoutManager = LinearLayoutManager(itemView.context)
                        childRecyclerView.adapter = childAdapter
                    }
                }
            }
        }
    }
}
