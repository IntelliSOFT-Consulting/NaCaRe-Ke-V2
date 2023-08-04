package com.intellisoft.hai.main.workflows

import android.app.Application
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.intellisoft.hai.R
import com.intellisoft.hai.databinding.ActivityRegistrationBinding
import com.intellisoft.hai.helper_class.FormatterClass
import com.intellisoft.hai.room.MainViewModel
import com.intellisoft.hai.room.RegistrationData
import com.intellisoft.hai.util.AppUtils.disableTextInputEditText
import com.intellisoft.hai.util.AppUtils.showDatePickerDialog

class RegistrationActivity : AppCompatActivity() {
  private lateinit var binding: ActivityRegistrationBinding
  private lateinit var mainViewModel: MainViewModel
  private lateinit var formatterClass: FormatterClass
  private val procedure = HashSet<String>()
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    binding = ActivityRegistrationBinding.inflate(layoutInflater)
    setContentView(binding.root)

    mainViewModel = MainViewModel((this.applicationContext as Application))
    formatterClass = FormatterClass()
    setSupportActionBar(binding.toolbar)
    handleListeners()
    controlCheckBoxes()
    disableTextInputEditText(binding.edtDob)
    disableTextInputEditText(binding.edtAdm)
    disableTextInputEditText(binding.edtSurgery)

    supportActionBar?.apply {
      setDisplayShowHomeEnabled(true)
      setDisplayHomeAsUpEnabled(true)
      setHomeAsUpIndicator(R.drawable.baseline_cancel_24)
      title = ""
    }
    binding.checkBoxOther.apply {
      setOnCheckedChangeListener { _, isChecked ->
        binding.textInputOther.isVisible = isChecked
        if (!isChecked) {
          binding.edtSurgeryOther.text?.clear()
        }
      }
    }
    controlClicks()
    binding.btnSubmit.apply {
      setOnClickListener {
        if (validate()) {
          // Proceed with submitting the form
          val user = formatterClass.getSharedPref("username", this@RegistrationActivity)
          Log.e("TAG", "addPatient $user")
          if (user != null) {
            val gender = if (binding.rbMale.isChecked) "Male" else "Female"
            val scheduling = if (binding.radioElective.isChecked) "Elective" else "Emergency"
            val location =
                if (binding.radioOrthopedicWard.isChecked) "Orthopedic ward"
                else "General surgery ward"
            val selectedLabels = procedure.toTypedArray()
            val commaSeparatedString = selectedLabels.joinToString(", ")
            val patientData =
                RegistrationData(
                    user,
                    binding.edtPatientId.text.toString(),
                    binding.edtSecondaryId.text.toString(),
                    gender,
                    binding.edtDob.text.toString(),
                    binding.edtAdm.text.toString(),
                    binding.edtSurgery.text.toString(),
                    commaSeparatedString,
                    binding.edtSurgeryOther.text.toString(),
                    scheduling,
                    location,
                )
            val added = mainViewModel.addPatient(patientData)
            if (added) {
              Toast.makeText(
                      this@RegistrationActivity,
                      "Patient Successfully registered",
                      Toast.LENGTH_SHORT)
                  .show()
              this@RegistrationActivity.finish()
            } else {
              Toast.makeText(
                      this@RegistrationActivity,
                      "Encountered problems registering patient",
                      Toast.LENGTH_SHORT)
                  .show()
            }
          } else {
            Toast.makeText(
                    this@RegistrationActivity, "Please check user account", Toast.LENGTH_SHORT)
                .show()
          }
        }
      }
    }
  }

  private fun controlCheckBoxes() {
    binding.checkBoxHipReplacement.setOnCheckedChangeListener { _, isChecked ->
      updateSelectedCheckboxes(getString(R.string.hip_replacement), isChecked)
    }
    binding.checkBoxKneeReplacement.setOnCheckedChangeListener { _, isChecked ->
      updateSelectedCheckboxes(getString(R.string.knee_replacement), isChecked)
    }
    binding.checkBoxACLReconstruction.setOnCheckedChangeListener { _, isChecked ->
      updateSelectedCheckboxes(getString(R.string.acl_reconstruction_surgery), isChecked)
    }
    binding.checkBoxORIF.setOnCheckedChangeListener { _, isChecked ->
      updateSelectedCheckboxes(getString(R.string.orif), isChecked)
    }
    binding.checkBoxShoulderReplacement.setOnCheckedChangeListener { _, isChecked ->
      updateSelectedCheckboxes(getString(R.string.shoulder_replacement_surgery), isChecked)
    }
    binding.checkBoxJointArthroscopy.setOnCheckedChangeListener { _, isChecked ->
      updateSelectedCheckboxes(getString(R.string.joint_arthroscopy), isChecked)
    }
    binding.checkBoxAnkleRepair.setOnCheckedChangeListener { _, isChecked ->
      updateSelectedCheckboxes(getString(R.string.ankle_repair), isChecked)
    }
    binding.checkBoxJointFusion.setOnCheckedChangeListener { _, isChecked ->
      updateSelectedCheckboxes(getString(R.string.joint_fusion_surgery), isChecked)
    }
    binding.checkBoxSpinalSurgery.setOnCheckedChangeListener { _, isChecked ->
      updateSelectedCheckboxes(getString(R.string.spinal_surgery), isChecked)
    }
    binding.checkBoxOther.setOnCheckedChangeListener { _, isChecked ->
      updateSelectedCheckboxes(getString(R.string.other_specify), isChecked)
    }
  }
  private fun updateSelectedCheckboxes(checkboxName: String, isChecked: Boolean) {
    if (isChecked) {
      procedure.add(checkboxName)
    } else {
      procedure.remove(checkboxName)
    }
  }
  private fun controlClicks() {
    binding.edtDob.apply {
      setOnClickListener {
        showDatePickerDialog(
            this@RegistrationActivity, binding.edtDob, setMaxNow = true, setMinNow = false)
      }
    }
    binding.edtAdm.apply {
      setOnClickListener {
        showDatePickerDialog(
            this@RegistrationActivity, binding.edtAdm, setMaxNow = true, setMinNow = false)
      }
    }
    binding.edtSurgery.apply {
      setOnClickListener {
        showDatePickerDialog(
            this@RegistrationActivity, binding.edtSurgery, setMaxNow = false, setMinNow = true)
      }
    }
  }

  private fun handleListeners() {
    controlData(
        binding.edtPatientId,
        binding.patientHolder,
        "Please provide patient ID",
        hasMin = false,
        hasMax = false,
        min = 0,
        max = 0)
    controlData(
        binding.edtSecondaryId,
        binding.secondaryHolder,
        "Please provide secondary ID",
        hasMin = false,
        hasMax = false,
        min = 0,
        max = 0)
    controlData(
        binding.edtDob,
        binding.dobHolder,
        "Please provide dob",
        hasMin = false,
        hasMax = false,
        min = 0,
        max = 0)
    controlData(
        binding.edtAdm,
        binding.admHolder,
        "Please provide admission date",
        hasMin = false,
        hasMax = false,
        min = 0,
        max = 0)
    controlData(
        binding.edtSurgery,
        binding.sgrHolder,
        "Please provide surgery date",
        hasMin = false,
        hasMax = false,
        min = 0,
        max = 0)
  }

  private fun controlData(
      child: TextInputEditText,
      parent: TextInputLayout,
      error: String,
      hasMin: Boolean,
      hasMax: Boolean,
      min: Int,
      max: Int
  ) {

    child.addTextChangedListener(
        object : TextWatcher {
          override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

          override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            if (hasMin && s?.length ?: 0 < min) {
              parent.error = "Minimum of $min characters required"
            } else if (hasMax && s?.length ?: 0 > max) {
              parent.error = "Maximum of $max characters allowed"
            } else if (s.isNullOrEmpty()) {
              parent.error = error
            } else {
              parent.error = null
            }
          }

          override fun afterTextChanged(s: Editable?) {}
        })
  }

  private fun validate(): Boolean {
    // Implement your validation logic here
    // Return true if validation passes, false otherwise
    val patientIdInput = binding.edtPatientId.text?.toString()
    val secondaryIdInput = binding.edtSecondaryId.text?.toString()
    val dob = binding.edtDob.text?.toString()
    val adm = binding.edtAdm.text?.toString()
    val sgr = binding.edtSurgery.text?.toString()
    if (patientIdInput.isNullOrEmpty()) {
      // Validation failed, show an error message or handle as needed
      binding.patientHolder.error = "Please provide patient ID"
      binding.edtPatientId.requestFocus()
      return false
    }
    if (secondaryIdInput.isNullOrEmpty()) {
      // Validation failed, show an error message or handle as needed
      binding.secondaryHolder.error = "Please provide secondary ID"
      binding.edtSecondaryId.requestFocus()
      return false
    }
    if (!binding.rbFemale.isChecked && !binding.rbMale.isChecked) {
      Toast.makeText(this@RegistrationActivity, "please select gender", Toast.LENGTH_SHORT).show()
      return false
    }
    if (dob.isNullOrEmpty()) {
      binding.dobHolder.error = "Please provide dob"
      binding.edtDob.requestFocus()
      return false
    }
    if (adm.isNullOrEmpty()) {
      binding.admHolder.error = "Please provide admission date"
      binding.edtAdm.requestFocus()
      return false
    }
    if (sgr.isNullOrEmpty()) {
      binding.sgrHolder.error = "Please provide surgery date"
      binding.edtSurgery.requestFocus()
      return false
    }
    val selectedLabels = procedure.toTypedArray()
    if (selectedLabels.isEmpty()) {
      Toast.makeText(this@RegistrationActivity, "please select a procedure", Toast.LENGTH_SHORT)
          .show()
      return false
    }
    if (binding.checkBoxOther.isChecked) {
      val other = binding.edtSurgeryOther.text?.toString()
      if (other.isNullOrEmpty()) {
        binding.textInputOther.error = "Please provide surgery date"
        binding.edtSurgeryOther.requestFocus()
        return false
      }
    }
    if (!binding.radioElective.isChecked && !binding.radioEmergency.isChecked) {
      Toast.makeText(this@RegistrationActivity, "please select scheduling", Toast.LENGTH_SHORT)
          .show()
      return false
    }
    if (!binding.radioOrthopedicWard.isChecked && !binding.radioGeneralSurgeryWard.isChecked) {
      Toast.makeText(
              this@RegistrationActivity, "please select surgery location", Toast.LENGTH_SHORT)
          .show()
      return false
    }
    return true
  }
  override fun onSupportNavigateUp(): Boolean {
    onBackPressed()
    return super.onSupportNavigateUp()
  }

  override fun onBackPressed() {
    super.onBackPressed()
  }
}
