package com.intellisoft.hai.adapter

import android.app.Application
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.intellisoft.hai.R
import com.intellisoft.hai.helper_class.FormatterClass
import com.intellisoft.hai.room.MainViewModel
import com.intellisoft.hai.room.PatientData
import com.intellisoft.hai.room.PeriData
import com.intellisoft.hai.room.RegistrationData

class PatientAdapter(
    private var patientList: List<PatientData>,
    private val context: Context,
    private val click: (PatientData) -> Unit
) : RecyclerView.Adapter<PatientAdapter.Pager2ViewHolder>(), Filterable {

    inner class Pager2ViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView), View.OnClickListener {

        val formatterClass = FormatterClass()

        val tvPatientId: TextView = itemView.findViewById(R.id.tvPatientId)
        val tvSurgeryId: TextView = itemView.findViewById(R.id.tvSurgeryId)
        val tvSurgeryDate: TextView = itemView.findViewById(R.id.tvSurgeryDate)
        val tvSurgeryStatus: TextView = itemView.findViewById(R.id.tvSurgeryStatus)
        val layoutExpanded: LinearLayout = itemView.findViewById(R.id.ln_parent)
        val cardView: MaterialCardView = itemView.findViewById(R.id.card_parent)
        val tvAction: TextView = itemView.findViewById(R.id.tvAction)

        init {
            tvAction.setOnClickListener(this)
        }

        override fun onClick(p0: View?) {
            val patient = patientList[adapterPosition]
            click(patient)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Pager2ViewHolder {
        return Pager2ViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_patient, parent, false)
        )
    }

    override fun onBindViewHolder(holder: Pager2ViewHolder, position: Int) {
        val viewModel = MainViewModel(context.applicationContext as Application)
        val id = patientList[position].id
        val dos = viewModel.getCaseDetails(context, id.toString())

        val patientId = patientList[position].patientId
        val secondaryId = patientList[position].secondaryId
        holder.tvPatientId.text = patientId
        holder.tvSurgeryDate.text = dos.date_of_surgery
        holder.tvSurgeryStatus.text = "Ongoing"
        holder.tvAction.text = "View"
        holder.tvSurgeryId.text = secondaryId

//        // it's a possible divisible by 2 then background gray
//        if (position % 2 == 0) {
//            // Set the background color to gray for even positions
//            holder.itemView.setBackgroundColor(ContextCompat.getColor(holder.itemView.context, R.color.gray))
//        } else {
//            // Reset the background color for odd positions
//            holder.itemView.setBackgroundColor(ContextCompat.getColor(holder.itemView.context, android.R.color.transparent))
//        }

    }

    override fun getItemCount(): Int {
        return patientList.size
    }



    // Implement the Filterable interface
    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val query = constraint.toString().trim()
                val filteredList = if (query.isEmpty()) {
                    patientList // Return the original list when the query is empty
                } else {
                    patientList.filter { item ->
                        item.patientId.contains(query, ignoreCase = true)
                    }
                }

                val results = FilterResults()
                results.values = filteredList
                return results
            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                patientList = results?.values as List<PatientData>// as List<PatientData>
                notifyDataSetChanged()
            }
        }
    }

}
