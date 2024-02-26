package com.capture.app.ui.programs

import android.app.Application
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.NavHostFragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.capture.app.adapters.ProgramAdapter
import com.capture.app.databinding.FragmentProgramsBinding
import com.capture.app.model.ProgramDetails
import com.capture.app.room.Converters
import com.capture.app.room.MainViewModel


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [ProgramsFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ProgramsFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    private lateinit var binding: FragmentProgramsBinding
    private lateinit var viewModel: MainViewModel
    private val programList = ArrayList<ProgramDetails>()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentProgramsBinding.inflate(layoutInflater)
        viewModel = MainViewModel(requireContext().applicationContext as Application)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val programs = viewModel.loadPrograms(requireContext())
        binding.apply { btnProceed.apply { setOnClickListener {
            NavHostFragment.findNavController(this@ProgramsFragment).navigateUp()
        } } }
        if (!programs.isEmpty()) {
            programList.clear()
            programs.forEach {
                if (it.userId.contains("notification") || it.userId.contains("facility")) {
                    val converters = Converters().fromJson(it.jsonData)
                    if (!converters.programs.isEmpty()) {
                        converters.programs.forEach {
                            val id = it.id
                            val name = it.name
                            val progSection = it.programSections
                            val progStage = it.programStages
                            val tei = it.trackedEntityType
                            val prog =
                                ProgramDetails(id = id, name = name, progStage, progSection,tei)
                            programList.add(prog)
                        }

                    }
                }
            }
            val adapterProgram =
                ProgramAdapter(this@ProgramsFragment, programList, requireContext())
            binding.apply {
                val manager = LinearLayoutManager(requireContext())
                programsRecyclerView.apply {
                    adapter = adapterProgram
                    layoutManager = manager
                }
            }
        } else {

        }
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment ProgramsFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            ProgramsFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}