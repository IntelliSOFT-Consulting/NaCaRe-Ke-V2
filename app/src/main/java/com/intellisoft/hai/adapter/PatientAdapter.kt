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
import com.intellisoft.hai.room.RegistrationData

class PatientAdapter(
    private var patientList: List<RegistrationData>,
    private val context: Context
) : RecyclerView.Adapter<PatientAdapter.Pager2ViewHolder>() {

  inner class Pager2ViewHolder(itemView: View) :
      RecyclerView.ViewHolder(itemView), View.OnClickListener {

    val formatterClass = FormatterClass()

    val tvPatientId: TextView = itemView.findViewById(R.id.tvPatientId)
    val tvSecondaryId: TextView = itemView.findViewById(R.id.tvSecondaryId)
    val tvGender: TextView = itemView.findViewById(R.id.tvGender)
    val layoutExpanded: LinearLayout = itemView.findViewById(R.id.layoutExpanded)
    val imgViewMore: ImageView = itemView.findViewById(R.id.imgViewMore)
    val tvDateOfBirth: TextView = itemView.findViewById(R.id.tvDateOfBirth)
    val tvDateOfAdmission: TextView = itemView.findViewById(R.id.tvDateOfAdmission)
    val tvDateOfSurgery: TextView = itemView.findViewById(R.id.tvDateOfSurgery)
    val tvProcedure: TextView = itemView.findViewById(R.id.tvProcedure)
    val tvProcedureOther: TextView = itemView.findViewById(R.id.tvProcedureOther)
    val tvScheduling: TextView = itemView.findViewById(R.id.tvScheduling)
    val tvLocation: TextView = itemView.findViewById(R.id.tvLocation)

    init {
      layoutExpanded.setOnClickListener(this)
    }

    override fun onClick(p0: View?) {
      val submissionId = patientList[adapterPosition].id
    }
  }

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Pager2ViewHolder {
    return Pager2ViewHolder(
        LayoutInflater.from(parent.context).inflate(R.layout.item_patient, parent, false))
  }

  override fun onBindViewHolder(holder: Pager2ViewHolder, position: Int) {

    val patientId = patientList[position].patientId
    val secondaryId = patientList[position].secondaryId
    val gender = patientList[position].gender
    val date_of_birth = patientList[position].date_of_birth
    val date_of_admission = patientList[position].date_of_admission
    val date_of_surgery = patientList[position].date_of_surgery
    val procedure = patientList[position].procedure
    val procedure_other = patientList[position].procedure_other
    val scheduling = patientList[position].scheduling
    val location = patientList[position].location
    val newlineSeparatedString = procedure.replace(", ", "          \n")
    holder.tvPatientId.text = "Patient ID: $patientId"
    holder.tvSecondaryId.text = "Secondary ID: $secondaryId"
    holder.tvGender.text = "Gender: $gender"
    holder.tvDateOfBirth.text = "Date of Birth: $date_of_birth"
    holder.tvDateOfAdmission.text = "Date of Admission: $date_of_admission"
    holder.tvDateOfSurgery.text = "Date of Surgery: $date_of_surgery"
    holder.tvProcedure.text = "Procedure: $newlineSeparatedString"
    holder.tvProcedureOther.text = "Procedure Other:: $procedure_other"
    holder.tvScheduling.text = "Scheduling: $scheduling"
    holder.tvLocation.text = "Location: $location"
    holder.imgViewMore.setOnClickListener {
      holder.layoutExpanded.visibility =
          if (holder.layoutExpanded.visibility == View.VISIBLE) {
            View.GONE
          } else {
            View.VISIBLE
          }
      if (holder.layoutExpanded.visibility == View.VISIBLE) {
        holder.imgViewMore.rotation = 180F
      } else {
        holder.imgViewMore.rotation = 0F
      }
    }
  }

  override fun getItemCount(): Int {
    return patientList.size
  }
}
