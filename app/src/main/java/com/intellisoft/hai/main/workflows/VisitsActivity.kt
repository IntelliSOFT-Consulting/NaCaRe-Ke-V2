package com.intellisoft.hai.main.workflows

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import com.intellisoft.hai.R
import com.intellisoft.hai.adapter.ViewPagerAdapter
import com.intellisoft.hai.databinding.ActivityVisitsBinding
import com.intellisoft.hai.main.workflows.visits.AntiobiticsDataFragment
import com.intellisoft.hai.main.workflows.visits.HandDataFragment
import com.intellisoft.hai.main.workflows.visits.InfectionDataFragment
import com.intellisoft.hai.main.workflows.visits.OutcomeDataFragment
import com.intellisoft.hai.main.workflows.visits.PeriDataFragment
import com.intellisoft.hai.main.workflows.visits.PostDataFragment
import com.intellisoft.hai.main.workflows.visits.PreparationDataFragment
import com.intellisoft.hai.main.workflows.visits.SkinDataFragment

class VisitsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityVisitsBinding
    private lateinit var tabViewpager: ViewPager
    private lateinit var tabTabLayout: TabLayout
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVisitsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        tabViewpager = binding.tabViewpager
        tabTabLayout = binding.tabTabLayout
        setupViewPager(tabViewpager)
        tabTabLayout.setupWithViewPager(tabViewpager)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayShowHomeEnabled(true)
            setDisplayHomeAsUpEnabled(true)
            setHomeAsUpIndicator(R.drawable.baseline_clear_24)
            title = ""
        }

    }
    override fun onSupportNavigateUp(): Boolean {
        this.finish()
        return super.onSupportNavigateUp()
    }

    override fun onBackPressed() {
        super.onBackPressed()
    }
    private fun setupViewPager(viewpager: ViewPager) {
        val adapter = ViewPagerAdapter(supportFragmentManager)
        adapter.addFragment(PeriDataFragment(), "PERI-OPERATIVE")
        adapter.addFragment(PreparationDataFragment(), "Patient Preparation")
        adapter.addFragment(SkinDataFragment(), "Skin Preparation")
        adapter.addFragment(HandDataFragment(), "Hand Preparation")
        adapter.addFragment(AntiobiticsDataFragment(), "Operative Antibiotics")
        adapter.addFragment(PostDataFragment(), "POST-OPERATIVE DATA")
        adapter.addFragment(InfectionDataFragment(), "INFECTION PATHOGEN")
        adapter.addFragment(OutcomeDataFragment(), "OUTCOME")

//        viewpager.offscreenPageLimit = 3
        viewpager.adapter = adapter
//        viewpager.currentItem = 3
    }
}