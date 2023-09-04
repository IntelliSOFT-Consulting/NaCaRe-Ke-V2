package com.intellisoft.hai.main.ui.slideshow

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.intellisoft.hai.R
import com.intellisoft.hai.adapter.ParentAdapter
import com.intellisoft.hai.databinding.FragmentCasesBinding
import com.intellisoft.hai.databinding.FragmentSlideshowBinding
import com.intellisoft.hai.helper_class.FormatterClass
import com.intellisoft.hai.helper_class.ParentItem
import com.intellisoft.hai.room.MainViewModel
import com.intellisoft.hai.room.PatientData
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
            ParentItem("Peri-Operative", listOf("Child 1", "Child 2")),
            ParentItem("Post Operative", listOf("Child 3", "Child 4")),
            ParentItem("Surgical Site Infection Information", listOf("Child 3", "Child 4")),
            ParentItem("Outcome", listOf("Child 3", "Child 4"))
            // Add more parent items as needed
        )

        val parentAdapter = ParentAdapter(this::onclick, parentItems, caseId)
        recyclerView.adapter = parentAdapter
        return root
    }

    private fun onclick(data: ParentItem) {
        when (data.name) {
            "Peri-Operative" -> {
                openSelected(R.id.caseSectionFragment, caseId)
            }

            "Post Operative" -> {
                // Handle the case when position is 1
                // Add your code here
                Log.e("Position", "Position 1")
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

    private fun openSelected(fragment: Int, caseId: String) {
        val bundle = Bundle()
        bundle.putString("caseId", caseId)
        val hostNavController =
            requireActivity().findNavController(R.id.nav_host_fragment_content_dashboard)
        hostNavController.navigate(fragment, bundle)
    }

}