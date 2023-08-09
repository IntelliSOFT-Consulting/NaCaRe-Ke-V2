package com.intellisoft.hai.room

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "registration")
data class RegistrationData(
    var userId: String,
    val patientId: String,
    val secondaryId: String,
    val gender: String,
    val date_of_birth: String,
    val date_of_admission: String,
    val date_of_surgery: String,
    @ColumnInfo(name = "procedure") val procedure: String,
    val procedure_other: String?,
    val scheduling: String,
    val location: String,
) : Serializable {
  @PrimaryKey(autoGenerate = true) var id: Int? = null
}

@Entity(tableName = "encounter")
data class EncounterData(
    var userId: String,
    val patientId: String,
    val date: String,
    val type: String,
) : Serializable {
  @PrimaryKey(autoGenerate = true) var id: Int? = null
}

@Entity(tableName = "peri_data")
data class PeriData(
    var userId: String,
    val patientId: String,
    val encounterId: String,
    @ColumnInfo(name = "risk_factors") val risk_factors: String,
    val glucose_measured: String,
    val glucose_level: String,
    val intervention: String,
) : Serializable {
  @PrimaryKey(autoGenerate = true) var id: Int? = null
}

@Entity(tableName = "patient_preparation")
data class PreparationData(
    var userId: String,
    val patientId: String,
    val encounterId: String,
    val pre_bath: String,
    val soap_used: String,
    val hair_removal: String,
    val date_of_removal: String?
) : Serializable {
  @PrimaryKey(autoGenerate = true) var id: Int? = null
}

@Entity(tableName = "skin_preparation")
data class SkinPreparationData(
    var userId: String,
    val patientId: String,
    val encounterId: String,
    val chlorhexidine_alcohol: String,
    val iodine_alcohol: String,
    val chlorhexidine_aq: String,
    val iodine_aq: String,
    val skin_fully_dry: String
) : Serializable {
  @PrimaryKey(autoGenerate = true) var id: Int? = null
}

@Entity(tableName = "hand_preparation")
data class HandPreparationData(
    var userId: String,
    val patientId: String,
    val encounterId: String,
    val practitioner: String,
    val time_spent: String,
    val plain_soap_water: String,
    val antimicrobial_soap_water: String,
    val hand_rub: String,
) : Serializable {
  @PrimaryKey(autoGenerate = true) var id: Int? = null
}

@Entity(tableName = "pre_post_operative")
data class PrePostOperativeData(
    var userId: String,
    val patientId: String,
    val encounterId: String,
    val pre_antibiotic_prophylaxis: String,
    val pre_antibiotic_prophylaxis_other: String,
    val pre_other_antibiotic_given: String,
    val antibiotics_ceased: String,
    val post_antibiotic_prophylaxis: String,
    val post_antibiotic_prophylaxis_other: String,
    val post_other_antibiotic_given: String,
    val post_reason: String,
    val post_reason_other: String,
    val drain_inserted: String,
    val drain_location: String,
    val drain_antibiotic: String,
    val implant_used: String,
    val implant_other: String,
) : Serializable {
  @PrimaryKey(autoGenerate = true) var id: Int? = null
}

@Entity(tableName = "post_operative")
data class PostOperativeData(
    var userId: String,
    val patientId: String,
    val encounterId: String,
    val check_up_date: String,
    val infection_signs: String,
    val event_date: String,
    val ssi: String,
    val infection_surgery_time: String,
    val drainage: String,
    val pain: String,
    val erythema: String,
    val heat: String,
    val fever: String,
    val incision_opened: String,
    val wound_dehisces: String,
    val abscess: String,
    val sinus: String,
    val hypothermia: String,
    val apnea: String,
    val bradycardia: String,
    val lethargy: String,
    val cough: String,
    val nausea: String,
    val vomiting: String,
    val symptom_other: String,
    val samples_sent: String,
) : Serializable {
  @PrimaryKey(autoGenerate = true) var id: Int? = null
}

@Entity(tableName = "surgical_site")
data class SurgicalSiteData(
    var userId: String,
    val patientId: String,
    val encounterId: String,
    val lab_type: String,
    val specimen: String,
    val specimen_other: String,
    val sample_collection_date: String,
    val sample_reception_date: String,
    val sample_processing_date: String,
    val culture_finding_date: String,
    val culture_finding: String,
    val organism_isolated: String,
    val organism_other: String,
    val acinetobacter_species: String,
    val entero_bacter: String,
    val other_pathogen_species: String,
    val amoxicillin: String,
    val Amikacin: String,
    val ampicillin: String,
    val cloxacillin: String,
    val cotrimoxazole: String,
    val cephalexin: String,
    val ciprofloxacin: String,
    val colistin_sulphate: String,
    val cefotaxime: String,
    val erythromycin: String,
    val gentamycin: String,
    val nalidixic_acid: String,
    val norfloxacin: String,
    val penicillin: String,
    val tobramycin: String,
    val vancomycin: String,
    val ceftazidime: String,
    val ceftriaxone: String,
) : Serializable {
  @PrimaryKey(autoGenerate = true) var id: Int? = null
}

@Entity(tableName = "outcome")
data class OutcomeData(
    var userId: String,
    val patientId: String,
    val encounterId: String,
    val date: String,
    val status: String,
) : Serializable {
  @PrimaryKey(autoGenerate = true) var id: Int? = null
}
