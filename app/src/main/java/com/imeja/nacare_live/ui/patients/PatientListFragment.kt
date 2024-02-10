package com.imeja.nacare_live.ui.patients

import android.app.Application
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.NavHostFragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import com.imeja.nacare_live.R
import com.imeja.nacare_live.adapters.SearchResultsAdapter
import com.imeja.nacare_live.adapters.TrackedEntityAdapter
import com.imeja.nacare_live.databinding.FragmentPatientListBinding
import com.imeja.nacare_live.model.EntityData
import com.imeja.nacare_live.model.ProgramDetails
import com.imeja.nacare_live.model.SearchResult
import com.imeja.nacare_live.room.Converters
import com.imeja.nacare_live.room.MainViewModel

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [PatientListFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class PatientListFragment : Fragment() {
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

    private lateinit var binding: FragmentPatientListBinding
    private lateinit var viewModel: MainViewModel
    private val dataList = ArrayList<EntityData>()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentPatientListBinding.inflate(layoutInflater)

        viewModel = MainViewModel(requireContext().applicationContext as Application)
        loadTrackedEntities()
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        try {
            loadTrackedEntities()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun loadTrackedEntities() {
        val data = viewModel.loadAllTrackedEntities(requireContext())
        if (data != null) {
            dataList.clear()
            data.forEach {
                val single = EntityData(
                    date = it.enrollDate,
                    fName = extractValueFromAttributes("R1vaUuILrDy", it.attributes),
                    lName = extractValueFromAttributes("hzVijy6tEUF", it.attributes),
                    diagnosis = extractValueFromAttributes("BzhDnF5fG4x", it.attributes)

                )
                dataList.add(single)

            }
        }

        val adapterProgram = TrackedEntityAdapter(dataList, requireContext(), this::handleClick)

        binding.apply {
            val manager = LinearLayoutManager(requireContext())
            trackedEntityInstancesRecyclerView.apply {
                adapter = adapterProgram
                layoutManager = manager
                val divider = DividerItemDecoration(context, manager.orientation)
                divider.setDrawable(context.getDrawable(R.drawable.divider)!!)
//                addItemDecoration(divider)
            }
        }
    }

    private fun extractValueFromAttributes(s: String, attributes: String): String {
        var data = ""
        val converters = Converters().fromJsonAttribute(attributes)
        val single = converters.find { it.attribute == s }
        if (single != null) {
            data = single.value
        }

        return data

    }

    private fun handleClick(data: EntityData) {

    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.apply {
            textViewWithArrow.apply {
                setOnClickListener {
                    NavHostFragment.findNavController(this@PatientListFragment).navigateUp()
                }
            }
            enrollmentButton.apply {
                setOnClickListener {
                    val intent = Intent(requireContext(), PatientSearchActivity::class.java)
                    startActivity(intent)
                }
            }
        }
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment PatientListFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            PatientListFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}