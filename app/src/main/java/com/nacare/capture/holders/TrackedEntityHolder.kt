package com.nacare.capture.holders

import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.nacare.capture.R

class TrackedEntityHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val dateTextView: TextView = itemView.findViewById(R.id.dateTextView)
    val firstnameTextView: TextView = itemView.findViewById(R.id.firstnameTextView)
    val lastnameTextView: TextView = itemView.findViewById(R.id.lastnameTextView)
    val actionTextView: TextView = itemView.findViewById(R.id.actionTextView)
    val hiddenLayout: LinearLayout = itemView.findViewById(R.id.hiddenLayout)
    val ln_next_page: LinearLayout = itemView.findViewById(R.id.ln_next_page)
    val tv_place_of_notification: TextView = itemView.findViewById(R.id.tv_place_of_notification)
    val tv_patient_name: TextView = itemView.findViewById(R.id.tv_patient_name)
    val tv_phone_no: TextView = itemView.findViewById(R.id.tv_phone_no)
    val tv_hospital_no: TextView = itemView.findViewById(R.id.tv_hospital_no)
    val tv_id_doc_no: TextView = itemView.findViewById(R.id.tv_id_doc_no)
    val tv_patient_id: TextView = itemView.findViewById(R.id.tv_patient_id)
    val btnProceed: MaterialButton = itemView.findViewById(R.id.btn_proceed)

}