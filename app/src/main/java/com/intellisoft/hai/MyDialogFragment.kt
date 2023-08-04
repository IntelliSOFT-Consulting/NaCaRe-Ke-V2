package com.intellisoft.hai

import android.app.Dialog
import android.os.Bundle
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment

class MyDialogFragment : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireContext())
        val inflater = requireActivity().layoutInflater
        val view = inflater.inflate(R.layout.custom_comment_dialog, null)

        val editText = view.findViewById<EditText>(R.id.dialog_edittext)

        builder.setTitle("My Dialog")
            .setView(view)
            .setPositiveButton("OK") { _, _ ->
                // Do something with the EditText value
                val text = editText.text.toString()

            }
            .setNegativeButton("Cancel", null)

        return builder.create()
    }
}