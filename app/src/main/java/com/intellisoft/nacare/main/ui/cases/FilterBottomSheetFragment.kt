package com.intellisoft.nacare.main.ui.cases

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.nacare.ke.capture.R
import com.nacare.ke.capture.databinding.FragmentCasesBinding
import com.nacare.ke.capture.databinding.FragmentFilterBottomSheetBinding

class FilterBottomSheetFragment : BottomSheetDialogFragment() {
    private lateinit var binding: FragmentFilterBottomSheetBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentFilterBottomSheetBinding.inflate(inflater, container, false)
        binding.apply {
            tvDate.setOnClickListener {
                // check if ln_dates is visible, if visible hide else show
                when (lnDates.visibility) {
                    View.VISIBLE -> {
                        // If visible, hide it
                        lnDates.visibility = View.GONE
                    }
                    else -> {
                        // If not visible, show it
                        lnDates.visibility = View.VISIBLE
                    }
                }
            }
        }
        return binding.root
    }
}