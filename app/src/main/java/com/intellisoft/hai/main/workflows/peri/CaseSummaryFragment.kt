package com.intellisoft.hai.main.workflows.peri

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import com.intellisoft.hai.R
import com.intellisoft.hai.adapter.DividerItemDecoration
import com.intellisoft.hai.adapter.HandAdapter
import com.intellisoft.hai.adapter.PatientAdapter
import com.intellisoft.hai.databinding.FragmentCaseSummaryBinding
import com.intellisoft.hai.helper_class.FormatterClass
import com.intellisoft.hai.room.HandPreparationData
import com.intellisoft.hai.room.MainViewModel

/**
 * A simple [Fragment] subclass.
 * Use the [CaseSummaryFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class CaseSummaryFragment : Fragment() {
    private lateinit var formatterClass: FormatterClass
    private lateinit var mainViewModel: MainViewModel
    private lateinit var binding: FragmentCaseSummaryBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentCaseSummaryBinding.inflate(layoutInflater)
        mainViewModel = ViewModelProvider(this).get(MainViewModel::class.java)
        formatterClass = FormatterClass()
        binding.prevButton.apply {
            setOnClickListener {
                val caseId = formatterClass.getSharedPref("caseId", requireContext())
                val bundle = Bundle()
                bundle.putString("caseId", caseId)
                val hostNavController =
                    requireActivity().findNavController(R.id.nav_host_fragment_content_dashboard)
                hostNavController.navigateUp()
            }
        }
        binding.saveButton.apply {
            setOnClickListener {
                val caseId = formatterClass.getSharedPref("caseId", requireContext())
                val bundle = Bundle()
                bundle.putString("caseId", caseId)
                val hostNavController =
                    requireActivity().findNavController(R.id.nav_host_fragment_content_dashboard)
                hostNavController.navigate(R.id.nav_slideshow, bundle)
            }
        }
        val data = formatterClass.getSharedPref("patient", requireContext())
        if (data != null) {
            loadInitialData(data)
        }
        return binding.root
    }

    private fun loadInitialData(patient: String) {
        val caseId = formatterClass.getSharedPref("caseId", requireContext())
        val data = mainViewModel.loadPeriOperativeData(requireContext(), patient, caseId)
        binding.apply {
            if (data != null) {
                tvRiskFactors.text = data.risk_factors
                tvBloodGlucoseMeasured.text = data.glucose_measured
                tvBloodGlucoseLevels.text = data.glucose_level
                tvIntervention.text = data.intervention
            }
        }
        val prep = mainViewModel.loadPreparationData(requireContext(), patient, caseId)
        binding.apply {
            if (prep != null) {
                tvPreOpBathShower.text = prep.pre_bath
                tvAntibacterialSoapUsed.text = prep.soap_used
                tvHairRemoval.text = prep.hair_removal
                tvHairRemovalDate.text = prep.date_of_removal
            }
        }
        val skin = mainViewModel.loadSkinPreparationData(requireContext(), patient, caseId)
        binding.apply {
            if (skin != null) {
                tvChlorhexidineAlcohol.text = skin.chlorhexidine_alcohol
                tvIodineAlcohol.text = skin.iodine_alcohol
                tvChlorhexidineAq.text = skin.chlorhexidine_aq
                tvIodineAq.text = skin.iodine_aq
                tvSkinFullyDry.text = skin.skin_fully_dry
            }
        }
        val antibiotics =
            mainViewModel.loadPrePostPreparationData(requireContext(), patient, caseId)
        binding.apply {
            if (antibiotics != null) {
                tvPreOpAntibiotics.text = antibiotics.pre_antibiotic_prophylaxis
                tvOtherPreOpAntibiotics.text = antibiotics.pre_antibiotic_prophylaxis_other
                tvAntibioticsCeased.text = antibiotics.antibiotics_ceased
                tvPostOpAntibiotics.text = antibiotics.post_antibiotic_prophylaxis
                tvOtherPostOpAntibiotics.text = antibiotics.post_antibiotic_prophylaxis_other
                tvReasonPostOpAntibiotics.text = antibiotics.post_reason
                tvOtherReasonPostOpAntibiotics.text = antibiotics.post_reason_other
                tvDrainInserted.text = antibiotics.drain_inserted
                tvDrainLocation.text = antibiotics.drain_location
                tvAntibioticWithDrainNoInfection.text = antibiotics.drain_antibiotic
                tvImplantUsed.text = antibiotics.implant_used
                tvOtherImplantType.text = antibiotics.implant_other
            }
        }
        /**Hand preparation**/
        val hands =
            mainViewModel.loadAllHandPreparationData(requireContext(), patient, caseId)
        binding.apply {
            if (hands != null) {
                val adapter = HandAdapter(requireContext(), hands)
                recyclerView.adapter = adapter
                recyclerView.addItemDecoration(
                    DividerItemDecoration(
                        requireContext(),
                        R.drawable.divider
                    )
                )
            }
        }
    }

}