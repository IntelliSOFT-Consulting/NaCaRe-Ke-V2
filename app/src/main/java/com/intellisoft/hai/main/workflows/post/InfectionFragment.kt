package com.intellisoft.hai.main.workflows.post

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import com.intellisoft.hai.R
import com.intellisoft.hai.databinding.FragmentInfectionBinding
import com.intellisoft.hai.databinding.FragmentPostDateBinding
import com.intellisoft.hai.helper_class.FormatterClass
import com.intellisoft.hai.room.MainViewModel

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [InfectionFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class InfectionFragment : Fragment() {
    private lateinit var binding: FragmentInfectionBinding
    private lateinit var viewModel: MainViewModel
    private lateinit var formatterClass: FormatterClass
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewModel = ViewModelProvider(this).get(MainViewModel::class.java)

        binding = FragmentInfectionBinding.inflate(inflater, container, false)
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
            }
        }
        return binding.root
    }

}