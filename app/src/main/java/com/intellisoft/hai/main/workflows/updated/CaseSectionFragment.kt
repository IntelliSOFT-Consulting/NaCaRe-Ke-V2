package com.intellisoft.hai.main.workflows.updated

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentTransaction
import androidx.navigation.findNavController
import com.google.android.material.button.MaterialButton
import com.intellisoft.hai.R
import com.intellisoft.hai.databinding.FragmentCaseSectionBinding
import com.intellisoft.hai.main.workflows.peri.HandPreparationFragment
import com.intellisoft.hai.main.workflows.peri.PatientPreparationFragment
import com.intellisoft.hai.main.workflows.peri.PeriFragment
import com.intellisoft.hai.main.workflows.peri.PreFragment
import com.intellisoft.hai.main.workflows.peri.SkinPreparationFragment

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [CaseSectionFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class CaseSectionFragment : Fragment() {
    private lateinit var binding: FragmentCaseSectionBinding
    private var currentPage = 0
    private val fragmentIds =
        arrayOf(
            R.id.periFragment,
            R.id.patientPreparationFragment,
            R.id.skinPreparationFragment,
            R.id.handPreparationFragment,
            R.id.preFragment
        )

    private lateinit var prevButton: MaterialButton
    private lateinit var nextButton: MaterialButton
    private lateinit var saveButton: MaterialButton

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentCaseSectionBinding.inflate(inflater, container, false)
        val root: View = binding.root
        prevButton = binding.prevButton
        nextButton = binding.nextButton
        saveButton = binding.saveButton

        prevButton.setOnClickListener {
            showPreviousFragment()
        }

        nextButton.setOnClickListener {
            if (validateCurrentFragment()) {
                showNextFragment()
            }
        }
        saveButton.setOnClickListener {
            showConfirmFragment()
        }

        // Initially, show the first fragment
        showFragment(currentPage)

        return root
    }

    private fun validateCurrentFragment(): Boolean {

        return true // If the fragment doesn't implement validation, consider it as valid
    }

    private fun showConfirmFragment() {
        val hostNavController =
            requireActivity().findNavController(R.id.nav_host_fragment_content_dashboard)
        hostNavController.navigate(R.id.caseSummaryFragment)
    }

    private fun showFragment(position: Int) {
        if (position >= 0 && position < fragmentIds.size) {
            val transaction =
                childFragmentManager.beginTransaction()
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)

            for (i in fragmentIds.indices) {
                val fragmentInstance = when (i) {
                    0 -> PeriFragment()
                    1 -> PatientPreparationFragment()
                    2 -> SkinPreparationFragment()
                    3 -> HandPreparationFragment()
                    4 -> PreFragment()
                    else -> null
                }
                if (i == position) {

                    if (fragmentInstance != null) {
                        transaction.replace(R.id.fragment_container, fragmentInstance)
                    }

                }
            }
            transaction.commit()
            updateButtonsVisibility()
        }
    }

    private fun showPreviousFragment() {
        if (currentPage > 0) {
            currentPage--
            showFragment(currentPage)
        }
    }

    private fun showNextFragment() {

        if (currentPage < fragmentIds.size - 1) {
            // show me the current fragment first before moving next
            currentPage++
            showFragment(currentPage)
        }
    }

    private fun updateButtonsVisibility() {
        prevButton.visibility = if (currentPage == 0) View.INVISIBLE else View.VISIBLE
        nextButton.visibility =
            if (currentPage == fragmentIds.size - 1) View.GONE else View.VISIBLE
        saveButton.visibility =
            if (currentPage == fragmentIds.size - 1) View.VISIBLE else View.GONE
    }
}