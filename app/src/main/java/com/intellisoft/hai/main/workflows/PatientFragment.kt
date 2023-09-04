package com.intellisoft.hai.main.workflows

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.intellisoft.hai.adapter.EncounterAdapter
import com.intellisoft.hai.databinding.FragmentPatientBinding
import com.intellisoft.hai.helper_class.FormatterClass
import com.intellisoft.hai.listeners.OnFragmentInteractionListener
import com.intellisoft.hai.main.workflows.peri.PeriFragment
import com.intellisoft.hai.room.EncounterData
import com.intellisoft.hai.room.MainViewModel

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
    private lateinit var mRecyclerView: RecyclerView
    private lateinit var formatterClass: FormatterClass
    private lateinit var mainViewModel: MainViewModel
    private var encounterList: List<EncounterData>? = null
    private var mListener: OnFragmentInteractionListener? = null
    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnFragmentInteractionListener) {
            mListener = context
        } else {
            throw RuntimeException("$context must implement OnFragmentInteractionListener")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentPatientBinding.inflate(layoutInflater)

        mainViewModel = ViewModelProvider(this).get(MainViewModel::class.java)
        formatterClass = FormatterClass()
        mRecyclerView = binding.recyclerView
        mRecyclerView.setHasFixedSize(true)
        mRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.fab.apply {
            setOnClickListener {
                mListener?.nextFragment(PeriFragment())
            }
        }
        return binding.root
    }

    override fun onStart() {
        super.onStart()
        loadData()
    }

    private fun loadData() {
        try {
            mainViewModel = ViewModelProvider(this).get(MainViewModel::class.java)
            encounterList = mainViewModel.getEncounters(requireContext())
            if (encounterList != null) {
                if (encounterList!!.isNotEmpty()) {
                    val adapter = EncounterAdapter(encounterList!!, requireContext(), this::onclick)
                    mRecyclerView.adapter = adapter
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun onclick(data: EncounterData) {
        formatterClass.saveSharedPref("patient", data.patientId, requireContext())
        formatterClass.saveSharedPref("encounter", data.type, requireContext())
        val intent = Intent(requireContext(), VisitsActivity::class.java)
        intent.putExtra("data", data)
        startActivity(intent)
    }

    override fun onResume() {
        super.onResume()
        loadData()
    }

}
