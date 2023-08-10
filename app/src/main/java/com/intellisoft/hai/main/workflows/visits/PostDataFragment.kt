package com.intellisoft.hai.main.workflows.visits

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.intellisoft.hai.R
import com.intellisoft.hai.adapter.OutcomeAdapter
import com.intellisoft.hai.databinding.FragmentOutcomeDataBinding
import com.intellisoft.hai.databinding.FragmentPostDataBinding
import com.intellisoft.hai.helper_class.FormatterClass
import com.intellisoft.hai.room.MainViewModel
import com.intellisoft.hai.room.OutcomeData

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [PostDataFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class PostDataFragment : Fragment() {
    private lateinit var binding: FragmentPostDataBinding
    private lateinit var mRecyclerView: RecyclerView
    private lateinit var formatterClass: FormatterClass
    private lateinit var mainViewModel: MainViewModel
    private var encounterList: List<OutcomeData>? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentPostDataBinding.inflate(layoutInflater)
        mainViewModel = ViewModelProvider(this).get(MainViewModel::class.java)
        formatterClass = FormatterClass()
        mRecyclerView = binding.recyclerView
        mRecyclerView.setHasFixedSize(true)
        mRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        return binding.root
    }

    override fun onStart() {
        super.onStart()
        loadData()
    }

    private fun loadData() {
        try {
            val encounterId = formatterClass.getSharedPref("encounter", requireContext())
            mainViewModel = ViewModelProvider(this).get(MainViewModel::class.java)
            encounterList = mainViewModel.getOutcomes(requireContext(), encounterId.toString())
            if (encounterList != null) {
                if (encounterList!!.isNotEmpty()) {
                    val adapter = OutcomeAdapter(encounterList!!, requireContext(), this::onclick)
                    mRecyclerView.adapter = adapter
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun onclick(data: OutcomeData) {
        formatterClass.saveSharedPref("patient", data.patientId, requireContext())

    }

    override fun onResume() {
        super.onResume()
        loadData()
    }
}