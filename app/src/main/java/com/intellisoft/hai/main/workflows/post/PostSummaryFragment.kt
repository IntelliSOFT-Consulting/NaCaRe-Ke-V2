package com.intellisoft.hai.main.workflows.post

import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import com.intellisoft.hai.R
import com.intellisoft.hai.databinding.FragmentPostDateBinding
import com.intellisoft.hai.databinding.FragmentPostSummaryBinding
import com.intellisoft.hai.helper_class.FormatterClass
import com.intellisoft.hai.room.Converters
import com.intellisoft.hai.room.MainViewModel
import com.intellisoft.hai.room.PostOperativeData
import com.intellisoft.hai.util.AppUtils

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [PostSummaryFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class PostSummaryFragment : Fragment() {
    private lateinit var binding: FragmentPostSummaryBinding
    private lateinit var viewModel: MainViewModel
    private lateinit var formatterClass: FormatterClass

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        viewModel = ViewModelProvider(this).get(MainViewModel::class.java)
        binding = FragmentPostSummaryBinding.inflate(inflater, container, false)
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
        binding.nextButton.apply {
            setOnClickListener {
                val json = formatterClass.getSharedPref("post", requireContext())
                if (json != null) {
                    val converters = Converters()
                    val data: PostOperativeData = converters.postFromJson(json)
                    val dt = viewModel.getLatestPostData(requireContext(), data.encounterId)
                    if (dt != null) {
                        val converters = Converters()
                        val jeff = converters.toPostJson(dt)
                        formatterClass.saveSharedPref("post", jeff, requireContext())
                        val caseId = formatterClass.getSharedPref("caseId", requireContext())
                        val bundle = Bundle()
                        bundle.putString("caseId", caseId)
                        val hostNavController =
                            requireActivity().findNavController(R.id.nav_host_fragment_content_dashboard)
                        hostNavController.navigate(R.id.nav_slideshow, bundle)
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
                        "Invalid data, please try again",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
        val data = formatterClass.getSharedPref("post", requireContext())
        if (data != null) {
            loadData(data)
        }
        return binding.root
    }

    private fun loadData(json: String) {
        binding.apply {
            val converters = Converters()
            val data: PostOperativeData = converters.postFromJson(json)
            tvEventDate.text = data.check_up_date
            tvSignsPresent.text = data.infection_signs
            tvDate.text = data.event_date
            tvInfectionPresent.text = data.infection_surgery_time
            tvSsi.text = data.ssi
            tvDrainage.text = data.drainage
            tvPain.text = data.pain
            tvErythema.text = data.erythema
            tvHeat.text = data.heat
            tvFever.text = data.fever
            tvIncision.text = data.incision_opened
            tvDehisces.text = data.wound_dehisces
            tvAbscess.text = data.abscess
            tvSinusTract.text = data.sinus
            tvHypothermia.text = data.hypothermia
            tvApnea.text = data.apnea
            tvBradycardia.text = data.bradycardia
            tvLethargy.text = data.lethargy
            tvCough.text = data.cough
            tvNausea.text = data.nausea
            tvVomiting.text = data.vomiting
            tvSamplesForCulture.text = data.samples_sent
            tvOtherSymptoms.text = data.symptom_other

            /**Update text color**/
            updateTextColor(tvDrainage, data.drainage)
            updateTextColor(tvPain, data.pain)
            updateTextColor(tvErythema, data.erythema)
            updateTextColor(tvHeat, data.heat)
            updateTextColor(tvFever, data.fever)
            updateTextColor(tvIncision, data.incision_opened)
            updateTextColor(tvDehisces, data.wound_dehisces)
            updateTextColor(tvAbscess, data.abscess)
            updateTextColor(tvSinusTract, data.sinus)
            updateTextColor(tvHypothermia, data.hypothermia)
            updateTextColor(tvApnea, data.apnea)
            updateTextColor(tvBradycardia, data.bradycardia)
            updateTextColor(tvLethargy, data.lethargy)
            updateTextColor(tvCough, data.cough)
            updateTextColor(tvNausea, data.nausea)
            updateTextColor(tvVomiting, data.vomiting)
            updateTextColor(tvSamplesForCulture, data.samples_sent)
        }
    }

    private fun updateTextColor(tvDrainage: TextView, drainage: String) {
        if (drainage.equals("Yes", ignoreCase = true)) {
            tvDrainage.setTextColor(Color.GREEN)
        } else if (drainage.equals("No", ignoreCase = true)) {
            tvDrainage.setTextColor(Color.RED)
        }
    }

}