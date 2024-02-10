package com.imeja.nacare_live.ui.organization

import android.app.Application
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.NavHostFragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.imeja.nacare_live.R
import com.imeja.nacare_live.adapters.TreeAdapter
import com.imeja.nacare_live.data.AppUtils
import com.imeja.nacare_live.data.FormatterClass

import com.imeja.nacare_live.databinding.FragmentGalleryBinding
import com.imeja.nacare_live.model.OrgTreeNode
import com.imeja.nacare_live.room.Converters
import com.imeja.nacare_live.room.MainViewModel

class OrganizationFragment : Fragment() {
    val hashMap1 = mutableMapOf<String, String>()
    private var _binding: FragmentGalleryBinding? = null
    private val formatter = FormatterClass()

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

//                    val organization = binding.autoCompleteTextView.text
//                    if (organization.isNullOrEmpty()) {
//                        Toast.makeText(
//                            requireContext(),
//                            "Select Org Unit to Proceed",
//                            Toast.LENGTH_SHORT
//                        ).show()
//                        return@setOnClickListener
//                    }

                    NavHostFragment.findNavController(this@OrganizationFragment)
                        .navigate(R.id.programsFragment)
                }
            }
        }
        loadOrganization()
    }

    private fun loadOrganization() {
        val organization = viewModel.loadOrganization(requireContext())
        if (organization != null) {
            Log.e("TAG", "Retrieved Organization Data $organization")
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