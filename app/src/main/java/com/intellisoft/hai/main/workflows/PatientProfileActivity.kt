package com.intellisoft.hai.main.workflows

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.intellisoft.hai.R
import com.intellisoft.hai.databinding.ActivityPatientProfileBinding

class PatientProfileActivity : AppCompatActivity() {
  private lateinit var binding: ActivityPatientProfileBinding

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    binding = ActivityPatientProfileBinding.inflate(layoutInflater)
    setContentView(binding.root)

    setSupportActionBar(binding.toolbar)
    supportActionBar?.apply {
      setDisplayShowHomeEnabled(true)
      setDisplayHomeAsUpEnabled(true)
      setHomeAsUpIndicator(R.drawable.ic_cancel)
      title = ""
    }

    if (savedInstanceState == null) {
      val fragment = PatientFragment()
      supportFragmentManager.beginTransaction().replace(R.id.fragment_container, fragment).commit()
    }
  }
  override fun onCreateOptionsMenu(menu: Menu): Boolean {
    // Inflate the menu; this adds items to the action bar if it is present.
    menuInflater.inflate(R.menu.patient, menu)
    return true
  }
  override fun onOptionsItemSelected(item: MenuItem): Boolean {
    return when (item.itemId) {
      R.id.action_peri -> {
        replaceFragment(PeriFragment())
        true
      }
      R.id.action_patient_preparation -> {
        replaceFragment(PatientPreparationFragment())
        true
      }
      R.id.surgical_skin_preparation -> {
        replaceFragment(SkinPreparationFragment())
        true
      }
      R.id.action_surgical_hand_preparation -> {
        replaceFragment(HandPreparationFragment())
        true
      }
      R.id.action_pre_and_post_operative_antibiotics -> {
        replaceFragment(PreFragment())
        true
      }
      R.id.action_post_operative_data -> {
        replaceFragment(PostFragment())
        true
      }
      R.id.action_surgical_site_infection_pathogen_information -> {
        replaceFragment(SurgicalFragment())
        true
      }
      R.id.action_microbiology_findings_and_ast_testing_results -> {
        replaceFragment(MicrobiologyFragment())
        true
      }
      R.id.action_antimicrobial_susceptibility_testing -> {
        replaceFragment(AntimicrobialFragment())
        true
      }
      R.id.action_outcome -> {
        replaceFragment(OutcomeFragment())
        true
      }
      else -> super.onOptionsItemSelected(item)
    }
  }

  private fun replaceFragment(fragment: Fragment) {
    supportFragmentManager
        .beginTransaction()
        .replace(R.id.fragment_container, fragment)
        .addToBackStack(null)
        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
        .commit()
  }
  override fun onSupportNavigateUp(): Boolean {
    this.finish()
    return super.onSupportNavigateUp()
  }

  override fun onBackPressed() {
    super.onBackPressed()
  }
}