package com.intellisoft.hai.main.ui.slideshow

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.intellisoft.hai.R
import com.intellisoft.hai.adapter.ParentAdapter
import com.intellisoft.hai.databinding.FragmentSlideshowBinding
import com.intellisoft.hai.helper_class.FormatterClass
import com.intellisoft.hai.helper_class.ParentItem
import com.intellisoft.hai.room.MainViewModel
import com.intellisoft.hai.room.PeriData
import com.intellisoft.hai.room.RegistrationData

class SlideshowFragment : Fragment() {
    private lateinit var binding: FragmentSlideshowBinding
    private lateinit var viewModel: MainViewModel
    private lateinit var formatterClass: FormatterClass
    private lateinit var detail: RegistrationData
    private lateinit var caseId: String
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewModel =
            ViewModelProvider(this).get(MainViewModel::class.java)
        binding = FragmentSlideshowBinding.inflate(inflater, container, false)
        val root: View = binding.root
        formatterClass = FormatterClass()
        val args = arguments
        if (args != null) {
            caseId = args.getString("caseId").toString()
            detail = viewModel.getCaseDetails(requireContext(), caseId)
            binding.apply {
                tvPatientId.text = detail.patientId
                tvGender.text = detail.gender
                tvSecondary.text = detail.secondaryId
                tvDob.text = detail.date_of_birth
                tvAdmission.text = detail.date_of_admission
                tvProcedure.text = detail.procedure
                tvLocation.text = detail.location
                tvDateSurgery.text = detail.date_of_surgery
                tvSchedule.text = detail.scheduling
            }

        }
        val recyclerView = root.findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        val parentItems = listOf(
            ParentItem("Peri-Operative", "0", listOf("Child 1", "Child 2")),
            ParentItem("Post Operative", "1", listOf("Child 3", "Child 4")),
            ParentItem("Surgical Site Infection Information", "2", listOf("Child 3", "Child 4")),
            ParentItem("Outcome", "3", listOf("Child 3", "Child 4"))
            // Add more parent items as needed
        )
        val hostNavController =
            requireActivity().findNavController(R.id.nav_host_fragment_content_dashboard)
        val parentAdapter =
            ParentAdapter(requireContext(), hostNavController, this::onclick, parentItems, caseId)
        recyclerView.adapter = parentAdapter
        return root
    }

    private fun onclick(data: ParentItem) {
        when (data.name) {
            "Peri-Operative" -> {
                openSelected(R.id.periFragment, caseId)
            }

            "Post Operative" -> {
                openPost(R.id.postDateFragment, caseId)
            }

            "Surgical Site Infection Information" -> {
                // Handle the case when position is 2
                // Add your code here
                Log.e("Position", "Position 2")
            }

            "Outcome" -> {
                // Handle the case when position is 3
                // Add your code here
                Log.e("Position", "Position 3")
            }

            else -> {
                // Handle other positions if needed
                Log.e("Position", "Unknown Position")
            }
        }
    }

    private fun openPost(fragment: Int, caseId: String) {
        formatterClass.deleteSharedPref("post", requireContext())
        formatterClass.saveSharedPref("caseId", caseId, requireContext())
        val bundle = Bundle()
        bundle.putString("caseId", caseId)
        val hostNavController =
            requireActivity().findNavController(R.id.nav_host_fragment_content_dashboard)
        hostNavController.navigate(fragment, bundle)
    }

    private fun openSelected(fragment: Int, caseId: String) {
        val user = formatterClass.getSharedPref("username", requireContext())
        if (user != null) {
            val patient = formatterClass.getSharedPref("patient", requireContext())
            formatterClass.saveSharedPref("encounter", caseId, requireContext())
            formatterClass.deleteSharedPref("peri", requireContext())
            val peri =
                PeriData(
                    userId = user,
                    patientId = patient.toString(),
                    encounterId = caseId,
                    risk_factors = "",
                    glucose_measured = "",
                    glucose_level = "",
                    intervention = "",
                )
            val added = viewModel.addPeriData(peri)
        }
        val bundle = Bundle()
        bundle.putString("caseId", caseId)
        val hostNavController =
            requireActivity().findNavController(R.id.nav_host_fragment_content_dashboard)
        hostNavController.navigate(fragment, bundle)
    }

}