package com.intellisoft.nacare.main.ui.cases

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.GridLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.nacare.ke.capture.R
import com.nacare.ke.capture.databinding.FragmentCasesBinding
import com.nacare.ke.capture.databinding.FragmentFilterBottomSheetBinding

class FilterBottomSheetFragment : BottomSheetDialogFragment() {
    private lateinit var binding: FragmentFilterBottomSheetBinding
    private var listener: FilterBottomSheetListener? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentFilterBottomSheetBinding.inflate(inflater, container, false)
        val view = binding.root
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
            lnNotSynced.setOnClickListener {
                listener?.onStatusClicked("not synced")
                dismiss()
            }
            lnDraft.setOnClickListener {
                listener?.onStatusClicked("draft")
                dismiss()
            }
            lnCompleted.setOnClickListener {
                listener?.onStatusClicked("completed")
                dismiss()
            }
            lnDuplicate.setOnClickListener {
                listener?.onStatusClicked("duplicates")
                dismiss()
            }

        }



        return view
    }

    fun setFilterBottomSheetListener(listener: FilterBottomSheetListener) {
        this.listener = listener
    }
}