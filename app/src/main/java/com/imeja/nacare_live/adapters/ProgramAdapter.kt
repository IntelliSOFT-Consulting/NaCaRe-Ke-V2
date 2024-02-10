package com.imeja.nacare_live.adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.NavHostFragment
import androidx.recyclerview.widget.RecyclerView
import com.imeja.nacare_live.R
import com.imeja.nacare_live.data.FormatterClass
import com.imeja.nacare_live.holders.PatientHolder
import com.imeja.nacare_live.model.ProgramDetails

class ProgramAdapter(
    private val fragment: Fragment,
    private val programList: List<ProgramDetails>,
    private val context: Context
) : RecyclerView.Adapter<PatientHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PatientHolder {
        return PatientHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.list_item_with_style,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: PatientHolder, position: Int) {
        val data = programList[position]
        holder.itemTitle.text = data.name
        if (data.name.contains("Registry")) {
            holder.itemIcon.setImageResource(R.drawable.patient);
        }
        holder.itemView.setOnClickListener {
            val formatter = FormatterClass()
            formatter.saveSharedPref("programUid", data.id, context)
            if (data.name.contains("Registry")) {
                data.trackedEntityType?.let { it1 ->
                    formatter.saveSharedPref(
                        "trackedEntity",
                        it1.id,
                        context
                    )
                }
                NavHostFragment.findNavController(fragment)
                    .navigate(R.id.patientListFragment)
            } else {
                NavHostFragment.findNavController(fragment)
                    .navigate(R.id.facilityListFragment)
            }
        }

    }

    override fun getItemCount(): Int {
        return programList.size
    }
}
