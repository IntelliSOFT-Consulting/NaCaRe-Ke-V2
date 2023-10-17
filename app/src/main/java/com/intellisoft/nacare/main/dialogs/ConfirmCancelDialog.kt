package com.intellisoft.nacare.main.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.nacare.capture.databinding.ConfirmCancelDialogBinding


class ConfirmCancelDialog : BottomSheetDialogFragment() {
    private lateinit var binding: ConfirmCancelDialogBinding
    private var listener: ConfirmCancelDialogListener? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = ConfirmCancelDialogBinding.inflate(inflater, container, false)
        val view = binding.root
        binding.apply {
            btnCancel.setOnClickListener {
                listener?.onCancelClick()
                dismiss()
            }
            btnComplete.setOnClickListener {
                listener?.onSubmitClick()
                dismiss()
            }
        }
        return view
    }

    fun setFilterBottomSheetListener(listener: ConfirmCancelDialogListener) {
        this.listener = listener
    }
}