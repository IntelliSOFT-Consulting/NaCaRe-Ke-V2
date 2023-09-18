package com.intellisoft.hai.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface RoomDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun addPatient(patientData: RegistrationData)

    @Query("SELECT EXISTS (SELECT 1 FROM registration WHERE patientId =:id)")
    fun checkPatientExists(id: String): Boolean

    @Query("SELECT EXISTS (SELECT 1 FROM patients WHERE patientId =:id)")
    fun checkPatient(id: String): Boolean

    @Query("SELECT * FROM registration WHERE userId =:userId")
    fun getPatients(userId: String): List<RegistrationData>?

    @Query("SELECT * FROM patients WHERE userId =:userId ORDER BY id DESC")
    fun getPatientsData(userId: String): List<PatientData>?

    @Query("SELECT EXISTS (SELECT 1 FROM patient_preparation WHERE userId =:userId AND patientId =:patientId AND encounterId =:encounterId)")
    fun checkExistsPatientPreparation(
        userId: String,
        patientId: String,
        encounterId: String
    ): Boolean

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun addPreparationData(data: PreparationData)

    @Query(
        "UPDATE patient_preparation SET encounterId =:encounterId,pre_bath =:pre_bath,soap_used =:soap_used," +
                "hair_removal =:hair_removal,date_of_removal =:date_of_removal WHERE userId =:userId AND patientId =:patientId AND encounterId =:encounterId"
    )
    fun updatePreparationData(
        userId: String,
        patientId: String,
        encounterId: String,
        pre_bath: String,
        soap_used: String,
        hair_removal: String,
        date_of_removal: String?
    )

    @Query("SELECT * FROM patient_preparation  WHERE userId =:userId AND patientId =:patientId AND encounterId =:encounterId  ORDER BY id DESC LIMIT 1")
    fun getLatestPreparation(
        userId: String,
        patientId: String,
        encounterId: String
    ): PreparationData?


    @Query("SELECT * FROM skin_preparation  WHERE userId =:userId AND patientId =:patientId AND encounterId =:encounterId  ORDER BY id DESC LIMIT 1")
    fun loadSkinPreparationData(
        userId: String,
        patientId: String,
        encounterId: String
    ): SkinPreparationData?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun addOutcomeData(data: OutcomeData)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun addSurgicalSiteData(data: SurgicalSiteData)

    @Query("SELECT EXISTS (SELECT 1 FROM peri_data WHERE userId =:user AND patientId =:id)")
    fun checkExistsPeri(user: String, id: String): Boolean

    @Query("SELECT EXISTS (SELECT 1 FROM skin_preparation WHERE userId =:user AND patientId =:patientId AND encounterId =:encounterId)")
    fun checkExistsSkinData(user: String, patientId: String, encounterId: String): Boolean

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun addPeriData(data: PeriData)

    @Query("UPDATE peri_data SET encounterId =:encounterId,risk_factors =:risk,glucose_measured =:measured,glucose_level =:level,intervention =:intervention WHERE userId =:userId AND patientId =:patientId")
    fun updatePeriData(
        risk: String,
        measured: String,
        level: String,
        intervention: String,
        userId: String,
        patientId: String,
        encounterId: String
    )

    @Query(
        "UPDATE skin_preparation SET encounterId =:encounterId,chlorhexidine_alcohol =:chlorhexidine_alcohol,iodine_alcohol =:iodine_alcohol," +
                "chlorhexidine_aq =:chlorhexidine_aq,iodine_aq =:iodine_aq,skin_fully_dry =:skin_fully_dry WHERE userId =:userId AND patientId =:patientId AND encounterId =:encounterId"
    )
    fun updateSkinPreparationData(
        userId: String,
        patientId: String,
        encounterId: String,
        chlorhexidine_alcohol: String,
        iodine_alcohol: String,
        chlorhexidine_aq: String,
        iodine_aq: String,
        skin_fully_dry: String
    )

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun addSkinPreparationData(data: SkinPreparationData)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun addHandPreparationData(data: HandPreparationData)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun addPrePostOperativeData(data: PrePostOperativeData)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun addPostOperativeData(data: PostOperativeData)

    @Query("SELECT * FROM encounter WHERE patientId =:patientId")
    fun getEncounters(patientId: String): List<EncounterData>?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun addEncounterData(enc: EncounterData)

    @Query("SELECT * FROM outcome WHERE patientId =:patientId AND encounterId =:encounterId")
    fun getOutcomes(patientId: String, encounterId: String): List<OutcomeData>?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun addNewPatient(data: PatientData)

    @Query("SELECT * FROM registration WHERE id =:caseId AND userId =:userId")
    fun getCaseDetails(userId: String, caseId: String): RegistrationData

    @Query("SELECT * FROM peri_data WHERE id =:caseId AND userId =:userId")
    fun loadPeriData(userId: String, caseId: String): List<PeriData>?

    @Query("SELECT EXISTS (SELECT 1 FROM hand_preparation WHERE userId =:userId AND patientId =:patientId AND encounterId =:encounterId)")
    fun checkExistsHandData(userId: String, patientId: String, encounterId: String): Boolean

    @Query(
        "UPDATE hand_preparation SET encounterId =:encounterId,practitioner =:practitioner,time_spent =:time_spent," +
                "plain_soap_water =:plain_soap_water,antimicrobial_soap_water =:antimicrobial_soap_water,hand_rub =:hand_rub WHERE userId =:userId AND patientId =:patientId AND encounterId =:encounterId"
    )
    fun updateHandPreparationData(
        userId: String,
        patientId: String,
        encounterId: String,
        practitioner: String,
        time_spent: String,
        plain_soap_water: String,
        antimicrobial_soap_water: String,
        hand_rub: String
    )

    @Query("SELECT * FROM hand_preparation  WHERE userId =:userId AND patientId =:patientId AND encounterId =:encounterId  ORDER BY id DESC LIMIT 1")
    fun loadHandPreparationData(
        userId: String,
        patientId: String,
        encounterId: String
    ): HandPreparationData?

    @Query("SELECT EXISTS (SELECT 1 FROM pre_post_operative WHERE userId =:userId AND patientId =:patientId AND encounterId =:encounterId)")
    fun checkExistsPrePostOperativeData(
        userId: String,
        patientId: String,
        encounterId: String
    ): Boolean

    @Query(
        "UPDATE pre_post_operative SET encounterId =:encounterId,pre_antibiotic_prophylaxis =:pre_antibiotic_prophylaxis,pre_antibiotic_prophylaxis_other =:pre_antibiotic_prophylaxis_other," +
                "pre_other_antibiotic_given =:pre_other_antibiotic_given,antibiotics_ceased =:antibiotics_ceased,post_antibiotic_prophylaxis =:post_antibiotic_prophylaxis," +
                "post_antibiotic_prophylaxis_other =:post_antibiotic_prophylaxis_other,post_other_antibiotic_given =:post_other_antibiotic_given,post_reason =:post_reason, " +
                "post_reason_other =:post_reason_other,drain_inserted =:drain_inserted,drain_location =:drain_location, " +
                "drain_antibiotic =:drain_antibiotic,implant_used =:implant_used,implant_other =:implant_other " +
                "WHERE userId =:userId AND patientId =:patientId AND encounterId =:encounterId"
    )
    fun updatePrePostOperativeData(
        userId: String,
        patientId: String,
        encounterId: String,
        pre_antibiotic_prophylaxis: String,
        pre_antibiotic_prophylaxis_other: String,
        pre_other_antibiotic_given: String,
        antibiotics_ceased: String,
        post_antibiotic_prophylaxis: String,
        post_antibiotic_prophylaxis_other: String,
        post_other_antibiotic_given: String,
        post_reason: String,
        post_reason_other: String,
        drain_inserted: String,
        drain_location: String,
        drain_antibiotic: String,
        implant_used: String,
        implant_other: String
    )

    @Query("SELECT * FROM pre_post_operative  WHERE userId =:userId AND patientId =:patientId AND encounterId =:encounterId  ORDER BY id DESC LIMIT 1")
    fun loadPrePostPreparationData(
        userId: String,
        patientId: String,
        encounterId: String
    ): PrePostOperativeData?
    @Query("SELECT * FROM post_operative  WHERE encounterId =:encounterId  ORDER BY id DESC LIMIT 1")
    fun loadCurrentPostData(
        encounterId: String
    ): PostOperativeData?

    @Query("SELECT * FROM post_operative WHERE caseId =:caseId AND userId =:userId ORDER BY id ")
    fun loadPostData(userId: String, caseId: String): List<PostOperativeData>?

    @Query("SELECT EXISTS (SELECT 1 FROM post_operative WHERE encounterId =:encounterId)")
    fun checkExistsPostOperativeData(encounterId: String): Boolean

    @Query("UPDATE post_operative SET check_up_date =:checkUpDate,infection_signs =:infectionSigns WHERE encounterId =:encounterId")
    fun updateInitialOperative(checkUpDate: String, infectionSigns: String, encounterId: String)
    @Query("UPDATE post_operative SET event_date =:eventDate,infection_surgery_time =:infectionSurgeryTime,ssi =:ssi WHERE encounterId =:encounterId")
    fun updateInfectionData(eventDate: String, infectionSurgeryTime: String, ssi: String, encounterId: String)
    @Query("UPDATE post_operative SET drainage =:drainage,pain =:pain,erythema =:erythema ," +
            "heat =:heat ,fever =:fever ,incision_opened =:incisionOpened ,wound_dehisces =:woundDehisces ," +
            "abscess =:abscess ,sinus =:sinus ,hypothermia =:hypothermia ,apnea =:apnea ,bradycardia =:bradycardia," +
            "lethargy =:lethargy,cough =:cough,nausea =:nausea,vomiting =:vomiting,symptom_other =:symptomOther ,samples_sent =:samplesSent WHERE encounterId =:encounterId")
    fun updateSymptomsData(
        drainage: String,
        pain: String,
        erythema: String,
        heat: String,
        fever: String,
        incisionOpened: String,
        woundDehisces: String,
        abscess: String,
        sinus: String,
        hypothermia: String,
        apnea: String,
        bradycardia: String,
        lethargy: String,
        cough: String,
        nausea: String,
        vomiting: String,
        symptomOther: String,
        samplesSent: String,
        encounterId: String
    )

}
