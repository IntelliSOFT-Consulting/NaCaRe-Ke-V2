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
import com.intellisoft.hai.databinding.FragmentHandPreparationBinding
import com.intellisoft.hai.databinding.FragmentPreBinding
import com.intellisoft.hai.helper_class.FormatterClass
import com.intellisoft.hai.listeners.OnFragmentInteractionListener
import com.intellisoft.hai.room.HandPreparationData
import com.intellisoft.hai.room.MainViewModel
import com.intellisoft.hai.util.AppUtils

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [PreFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class PreFragment : Fragment() {
  private lateinit var formatterClass: FormatterClass
  private lateinit var mainViewModel: MainViewModel
  private lateinit var binding: FragmentPreBinding
  private val pre_operation = HashSet<String>()
  private val post_operation = HashSet<String>()
  private var mListener: OnFragmentInteractionListener? = null
  private lateinit var time_spent: String
  private lateinit var plain_soap_water: String
  private lateinit var antimicrobial_soap_water: String
  private lateinit var hand_rub: String
  override fun onAttach(context: Context) {
    super.onAttach(context)
    if (context is OnFragmentInteractionListener) {
      mListener = context
    } else {
      throw RuntimeException("$context must implement OnFragmentInteractionListener")
    }
  }
  override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    binding = FragmentPreBinding.inflate(layoutInflater)
    mainViewModel = ViewModelProvider(this).get(MainViewModel::class.java)
    formatterClass = FormatterClass()
    binding.btnSubmit.apply {
      setOnClickListener {
        if (validate()) {
          val user = formatterClass.getSharedPref("username", requireContext())
          if (user != null) {
            val patient = formatterClass.getSharedPref("patient", requireContext())

            val data =
              HandPreparationData(
                userId = user,
                patientId = patient.toString(),
                encounterId = AppUtils.generateUuid(),
                practitioner= AppUtils.generateUuid(),
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
              mListener?.nextFragment(PreFragment())

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

  }
  private fun validate(): Boolean {


    return true
  }

}