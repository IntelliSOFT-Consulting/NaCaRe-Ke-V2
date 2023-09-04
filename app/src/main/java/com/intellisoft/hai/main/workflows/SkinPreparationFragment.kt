package com.intellisoft.hai.main.workflows

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.intellisoft.hai.R
import com.intellisoft.hai.databinding.FragmentPeriBinding
import com.intellisoft.hai.databinding.FragmentSkinPreparationBinding
import com.intellisoft.hai.helper_class.FormatterClass
import com.intellisoft.hai.listeners.OnFragmentInteractionListener
import com.intellisoft.hai.room.MainViewModel
import com.intellisoft.hai.room.PeriData
import com.intellisoft.hai.room.SkinPreparationData
import com.intellisoft.hai.util.AppUtils
import com.intellisoft.hai.util.AppUtils.generateUuid

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [SkinPreparationFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class SkinPreparationFragment : Fragment() {
    private lateinit var formatterClass: FormatterClass
    private lateinit var mainViewModel: MainViewModel
    private lateinit var binding: FragmentSkinPreparationBinding
    private val risk_factors = HashSet<String>()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentSkinPreparationBinding.inflate(layoutInflater)
        mainViewModel = ViewModelProvider(this).get(MainViewModel::class.java)
        formatterClass = FormatterClass()
        binding.btnSubmit.apply {
            setOnClickListener {
                if (validate()) {
                    val user = formatterClass.getSharedPref("username", requireContext())
                    if (user != null) {
                        val patient = formatterClass.getSharedPref("patient", requireContext())

                        val chlorhexidine_alcohol =
                            if (binding.radioButtonChlorhexidineAlcoholNo.isChecked) "No" else "Yes"
                        val iodine_alcohol =
                            if (binding.radioButtonIodineAlcoholNo.isChecked) "No" else "Yes"

                        val chlorhexidine_aq =
                            if (binding.radioButtonChlorhexidineAqNo.isChecked) "No" else "Yes"
                        val iodine_aq =
                            if (binding.radioButtonIodineAqNo.isChecked) "No" else "Yes"
                        val skin_fully_dry =
                            if (binding.radioButtonSkinFullyDryNo.isChecked) "No" else "Yes"
                        val enc = formatterClass.getSharedPref("encounter", requireContext())
                        val data =
                            SkinPreparationData(
                                userId = user,
                                patientId = patient.toString(),
                                encounterId = enc.toString(),
                                chlorhexidine_alcohol = chlorhexidine_alcohol,
                                iodine_alcohol = iodine_alcohol,
                                chlorhexidine_aq = chlorhexidine_aq,
                                iodine_aq = iodine_aq,
                                skin_fully_dry = skin_fully_dry
                            )
                        val added = mainViewModel.addSkinPreparationData(data)
                        if (added) {
                            Toast.makeText(
                                requireContext(),
                                "Record Successfully saved",
                                Toast.LENGTH_SHORT
                            )
                                .show()
//                            mListener?.nextFragment(HandPreparationFragment())

                        }
                        else {
                            Toast.makeText(
                                requireContext(),
                                "Encountered problems saving data",
                                Toast.LENGTH_SHORT
                            )
                                .show()
                        }
                    } else {
                        Toast.makeText(
                            requireContext(),
                            "Please check user account",
                            Toast.LENGTH_SHORT
                        )
                            .show()
                    }
                }
            }
        }
        return binding.root
    }

    private fun validate(): Boolean {

        if (!binding.radioButtonChlorhexidineAlcoholNo.isChecked && !binding.radioButtonChlorhexidineAlcoholYes.isChecked) {
            Toast.makeText(
                requireContext(),
                "Please select Chlorhexidine+Alcohol",
                Toast.LENGTH_SHORT
            ).show()
            return false
        }
        if (!binding.radioButtonIodineAlcoholNo.isChecked && !binding.radioButtonIodineAlcoholYes.isChecked) {
            Toast.makeText(requireContext(), "Please select Iodine+Alcohol", Toast.LENGTH_SHORT)
                .show()
            return false
        }
        if (!binding.radioButtonChlorhexidineAqNo.isChecked && !binding.radioButtonChlorhexidineAqYes.isChecked) {
            Toast.makeText(requireContext(), "Please select Chlorhexidine-aq", Toast.LENGTH_SHORT)
                .show()
            return false
        }
        if (!binding.radioButtonIodineAqNo.isChecked && !binding.radioButtonIodineAqYes.isChecked) {
            Toast.makeText(requireContext(), "Please select Iodine-aq", Toast.LENGTH_SHORT).show()
            return false
        }
        if (!binding.radioButtonSkinFullyDryNo.isChecked && !binding.radioButtonSkinFullyDryYes.isChecked) {
            Toast.makeText(
                requireContext(),
                "Please select if skin allowed to fully dry",
                Toast.LENGTH_SHORT
            ).show()
            return false
        }

        return true
    }
}