package com.intellisoft.hai.main.workflows

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.intellisoft.hai.R
import com.intellisoft.hai.databinding.FragmentHandPreparationBinding
import com.intellisoft.hai.databinding.FragmentSkinPreparationBinding
import com.intellisoft.hai.helper_class.FormatterClass
import com.intellisoft.hai.listeners.OnFragmentInteractionListener
import com.intellisoft.hai.room.HandPreparationData
import com.intellisoft.hai.room.MainViewModel
import com.intellisoft.hai.room.SkinPreparationData
import com.intellisoft.hai.util.AppUtils
import com.intellisoft.hai.util.AppUtils.generateUuid

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [HandPreparationFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class HandPreparationFragment : Fragment() {
    private lateinit var formatterClass: FormatterClass
    private lateinit var mainViewModel: MainViewModel
    private lateinit var binding: FragmentHandPreparationBinding
    private val risk_factors = HashSet<String>()
    private lateinit var time_spent: String
    private lateinit var plain_soap_water: String
    private lateinit var antimicrobial_soap_water: String
    private lateinit var hand_rub: String


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentHandPreparationBinding.inflate(layoutInflater)
        mainViewModel = ViewModelProvider(this).get(MainViewModel::class.java)
        formatterClass = FormatterClass()
        binding.btnSubmit.apply {
            setOnClickListener {
                if (validate()) {
                    val user = formatterClass.getSharedPref("username", requireContext())
                    if (user != null) {
                        val patient = formatterClass.getSharedPref("patient", requireContext())
                        val enc = formatterClass.getSharedPref("encounter", requireContext())
                        val data =
                            HandPreparationData(
                                userId = user,
                                patientId = patient.toString(),
                                encounterId = enc.toString(),
                                practitioner= generateUuid(),
                                time_spent = time_spent,
                                plain_soap_water = plain_soap_water,
                                antimicrobial_soap_water = antimicrobial_soap_water,
                                hand_rub = hand_rub
                            )
                        val added = mainViewModel.addHandPreparationData(data)
                        if (added) {
                            Toast.makeText(
                                requireContext(),
                                "Record Successfully saved",
                                Toast.LENGTH_SHORT
                            )
                                .show()
//                            mListener?.nextFragment(PreFragment())

                        } else {
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
        handleClicks()
        return binding.root
    }

    private fun handleClicks() {
        binding.estimatedTimeRadioGroup.setOnCheckedChangeListener { _, checkedId ->
            val selectedRadioButton = binding.root.findViewById<RadioButton>(checkedId)
            time_spent = selectedRadioButton.text.toString()
        }
        binding.plainSoapWaterRadioGroup.setOnCheckedChangeListener { _, checkedId ->
            val selectedRadioButton = binding.root.findViewById<RadioButton>(checkedId)
            plain_soap_water = selectedRadioButton.text.toString()
        }
        binding.antimicrobialSoapWaterRadioGroup.setOnCheckedChangeListener { _, checkedId ->
            val selectedRadioButton = binding.root.findViewById<RadioButton>(checkedId)
            antimicrobial_soap_water = selectedRadioButton.text.toString()
        }
        binding.alcoholBasedHandRubRadioGroup.setOnCheckedChangeListener { _, checkedId ->
            val selectedRadioButton = binding.root.findViewById<RadioButton>(checkedId)
            hand_rub = selectedRadioButton.text.toString()
        }
    }

    private fun validate(): Boolean {

        if (binding.estimatedTimeRadioGroup.checkedRadioButtonId == -1) {
            Toast.makeText(
                requireContext(),
                "Please select Chlorhexidine+Alcohol",
                Toast.LENGTH_SHORT
            ).show()
            return false
        }
        if (binding.plainSoapWaterRadioGroup.checkedRadioButtonId == -1) {
            Toast.makeText(
                requireContext(),
                "Please select Plain Soap + Water",
                Toast.LENGTH_SHORT
            ).show()
            return false
        }
        if (binding.antimicrobialSoapWaterRadioGroup.checkedRadioButtonId == -1) {
            Toast.makeText(
                requireContext(),
                "Please select Antimicrobial Soap + Water",
                Toast.LENGTH_SHORT
            ).show()
            return false
        }
        if (binding.alcoholBasedHandRubRadioGroup.checkedRadioButtonId == -1) {
            Toast.makeText(
                requireContext(),
                "Please select Alcohol-based Hand Rub",
                Toast.LENGTH_SHORT
            ).show()
            return false
        }


        return true
    }

}