package com.intellisoft.hai.main.workflows

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.intellisoft.hai.databinding.FragmentPatientBinding
import com.intellisoft.hai.helper_class.FormatterClass
import com.intellisoft.hai.room.MainViewModel
import com.intellisoft.hai.room.PreparationData
import com.intellisoft.hai.util.AppUtils
import com.intellisoft.hai.util.AppUtils.controlData
import com.intellisoft.hai.util.AppUtils.disableTextInputEditText
import com.intellisoft.hai.util.AppUtils.generateUuid

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass. Use the [PatientFragment.newInstance] factory method to create an
 * instance of this fragment.
 */
class PatientFragment : Fragment() {
  private lateinit var binding: FragmentPatientBinding
  private lateinit var formatterClass: FormatterClass
  private lateinit var mainViewModel: MainViewModel
  override fun onCreateView(
      inflater: LayoutInflater,
      container: ViewGroup?,
      savedInstanceState: Bundle?
  ): View? {
    // Inflate the layout for this fragment
    binding = FragmentPatientBinding.inflate(layoutInflater)

    mainViewModel = ViewModelProvider(this).get(MainViewModel::class.java)
    formatterClass = FormatterClass()

    return binding.root
    //    return inflater.inflate(R.layout.fragment_patient, container, false)
  }


}
