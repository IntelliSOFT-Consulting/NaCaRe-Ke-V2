package com.intellisoft.hai.main.workflows.updated

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.intellisoft.hai.R
import com.intellisoft.hai.adapter.CaseAdapter
import com.intellisoft.hai.databinding.FragmentCaseBinding
import com.intellisoft.hai.databinding.FragmentCaseSectionBinding
import com.intellisoft.hai.helper_class.DataItems
import com.intellisoft.hai.helper_class.PeriItem

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [CaseFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class CaseFragment : Fragment() {
    private lateinit var binding: FragmentCaseBinding
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: CaseAdapter
    private lateinit var list: List<DataItems>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentCaseBinding.inflate(inflater, container, false)
        val root: View = binding.root
        recyclerView = binding.recyclerView
        recyclerView.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)

        // Load and parse the JSON file from assets
        try {
            val json = loadJSONFromAsset("peri.json")
            val itemResponse: PeriItem = Gson().fromJson(json, PeriItem::class.java)
            adapter = CaseAdapter(requireContext(),itemResponse.data)
            recyclerView.adapter = adapter
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return root
    }

    private fun loadJSONFromAsset(filename: String): String {
        return try {
            val inputStream = requireActivity().assets.open(filename)
            val size = inputStream.available()
            val buffer = ByteArray(size)
            inputStream.read(buffer)
            inputStream.close()
            String(buffer, Charsets.UTF_8)
        } catch (e: Exception) {
            e.printStackTrace()
            ""
        }
    }

}