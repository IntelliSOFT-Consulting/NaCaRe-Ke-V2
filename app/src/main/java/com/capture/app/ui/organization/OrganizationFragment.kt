package com.capture.app.ui.organization

import android.app.Application
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.NavHostFragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.capture.app.R
import com.capture.app.adapters.TreeAdapter
import com.capture.app.data.AppUtils
import com.capture.app.data.FormatterClass

import com.capture.app.databinding.FragmentGalleryBinding
import com.capture.app.model.CodeValuePair
import com.capture.app.model.OrgTreeNode
import com.capture.app.room.Converters
import com.capture.app.room.MainViewModel

class OrganizationFragment : Fragment() {
    val hashMap1 = mutableMapOf<String, String>()
    private var _binding: FragmentGalleryBinding? = null
    private val formatter = FormatterClass()
    private var searchParameters = ArrayList<CodeValuePair>()

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private lateinit var viewModel: MainViewModel
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentGalleryBinding.inflate(inflater, container, false)
        val root: View = binding.root
        viewModel = MainViewModel(requireContext().applicationContext as Application)

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.apply {
            btnProceed.apply {
                setOnClickListener {
                    val organization = binding.autoCompleteTextView.text.toString()
                    if (organization.isNullOrEmpty()) {
                        Toast.makeText(
                            requireContext(),
                            "Select Org Unit to Proceed",
                            Toast.LENGTH_SHORT
                        ).show()
                        return@setOnClickListener
                    }
                    try {
                        val found = searchParameters.firstOrNull { it.value == organization }
                        if (found != null) {
                            formatter.saveSharedPref("orgCode", found.code, requireContext())
                            formatter.saveSharedPref("orgName", found.value, requireContext())
                        }
                        NavHostFragment.findNavController(this@OrganizationFragment)
                            .navigate(R.id.programsFragment)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }

        }
        val username = formatter.getSharedPref("username", requireContext())
        if (username != null) {
            if (username == "admin") {
                loadOrganization()
            }
        }

    }


    private fun loadOrganization() {
        val organization = viewModel.loadOrganization(requireContext())
        if (organization != null) {

            val treeNodes = mutableListOf<OrgTreeNode>()
            try {
                organization.forEach {
                    Log.e("TAG", "Retrieved Organization Data $it")
                    val converters = Converters().fromJsonOrgUnit(it.jsonData)

                    val orgNode = OrgTreeNode(
                        label = converters.name,
                        code = converters.id,
                        level = converters.level,
                        children = AppUtils().generateChild(converters.children)
                    )
                    treeNodes.add(orgNode)
                }

            } catch (e: Exception) {
                e.printStackTrace()
            }

            val adapter = TreeAdapter(requireContext(), treeNodes, this::selectOrganization)
            binding.programsRecyclerView.adapter = adapter
            binding.programsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
            val resultList = mutableListOf<OrgTreeNode>()
            treeNodes.forEach {
                if (it.children.isNotEmpty()) {
                    manipulateChildren(it, it.level.toInt(), resultList)
                }
            }
            Log.e("TAG", "Collected Organizations **** $resultList")
            prepareSearchData(resultList)
        }
    }

    private fun prepareSearchData(resultList: MutableList<OrgTreeNode>) {

        try {
            val optionsStringList: MutableList<String> = ArrayList()
            resultList.forEach {
                optionsStringList.add(it.label)
                saveValue(it.code, it.label)
            }
            val adp = ArrayAdapter(
                requireContext(),
                android.R.layout.simple_list_item_1,
                optionsStringList
            )
            binding.autoCompleteTextView.setAdapter(adp)
            adp.notifyDataSetChanged()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun saveValue(id: String, value: String) {
        val existingIndex = searchParameters.indexOfFirst { it.code == id }
        if (existingIndex != -1) {
            // Update the existing entry if the code is found
            searchParameters[existingIndex] = CodeValuePair(code = id, value = value)
        } else {
            // Add a new entry if the code is not found
            val data = CodeValuePair(code = id, value = value)
            searchParameters.add(data)
        }
    }

    private fun manipulateChildren(
        data: OrgTreeNode,
        level: Int,
        resultList: MutableList<OrgTreeNode>
    ) {
        if (level == 5) {
            // Add current node to the result list
            resultList.add(data)
        } else {
            // Recursively process children nodes
            data.children.forEach { child ->
                manipulateChildren(child, level + 1, resultList)
            }
        }
    }

    private fun selectOrganization(data: OrgTreeNode) {
        binding.apply {
            autoCompleteTextView.setText(data.label)
            hashMap1[data.label] = data.code
            formatter.saveSharedPref("orgCode", data.code, requireContext())
            formatter.saveSharedPref("orgName", data.label, requireContext())
            loadOrganization()
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}