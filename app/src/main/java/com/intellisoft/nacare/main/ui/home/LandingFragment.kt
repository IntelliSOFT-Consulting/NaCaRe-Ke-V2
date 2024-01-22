package com.intellisoft.nacare.main.ui.home

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.NavHostFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.intellisoft.nacare.adapter.TreeAdapter
import com.intellisoft.nacare.helper_class.FormatterClass
import com.intellisoft.nacare.helper_class.OrgTreeNode
import com.intellisoft.nacare.models.Constants
import com.intellisoft.nacare.room.Converters
import com.intellisoft.nacare.room.MainViewModel
import com.intellisoft.nacare.room.OrganizationData
import com.intellisoft.nacare.util.AppUtils
import com.intellisoft.nacare.viewmodels.NetworkViewModel
import com.nacare.capture.R
import com.nacare.capture.databinding.FragmentHomeBinding
import com.nacare.capture.databinding.FragmentLandingBinding

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [LandingFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class LandingFragment : Fragment() {
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

    private lateinit var binding: FragmentLandingBinding
    private lateinit var viewModel: MainViewModel
    private lateinit var networkViewModel: NetworkViewModel
    private lateinit var formatterClass: FormatterClass
    val hashMap1 = mutableMapOf<String, String>()
    private lateinit var dialog: AlertDialog
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        viewModel = ViewModelProvider(this).get(MainViewModel::class.java)
        networkViewModel = ViewModelProvider(this).get(NetworkViewModel::class.java)

        binding = FragmentLandingBinding.inflate(inflater, container, false)
        formatterClass = FormatterClass()
        displayInitialData()
        val org = viewModel.loadOrganizations(requireActivity())
        if (!org.isNullOrEmpty()) {
            showOrgUnitDialog(org)
        } else {
//            AppUtils.showNoOrgUnits(requireContext())
        }

        AppUtils.controlData(
            binding.organizationEdittext,
            binding.organizationLayout,
            "Please select organization unit",
            hasMin = false,
            hasMax = false,
            min = 0,
            max = 0
        )
        binding.organizationEdittext.apply {

        }
        binding.btnProceed.apply {

            setOnClickListener {
                val organization = binding.organizationEdittext.text
                if (organization.isNullOrEmpty()) {
                    binding.organizationLayout.error = "Please select organization unit"
                    binding.organizationEdittext.requestFocus()
                    return@setOnClickListener
                }
                NavHostFragment.findNavController(this@LandingFragment)
                    .navigate(R.id.action_landingFragment_to_programsFragment)
            }
        }
        return binding.root
    }

    private fun showOrgUnitDialog(org: List<OrganizationData>) {
        val or = org.firstOrNull()
        if (or != null) {
            val treeNodes = mutableListOf<OrgTreeNode>()
            try {
                val converters = Converters().fromJsonOrgUnit(or.children)
                val orgNode = OrgTreeNode(
                    label = converters.name,
                    code = converters.id,
                    level = converters.level,
                    children = AppUtils.generateChild(converters.children)
                )
                treeNodes.add(orgNode)

            } catch (e: Exception) {
                e.printStackTrace()
            }
            val adapter = TreeAdapter(requireContext(), treeNodes, this::selectOrganization)
            binding.recyclerView.adapter = adapter
            binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        }

    }

    private fun selectOrganization(data: OrgTreeNode) {
        binding.apply {
            organizationEdittext.setText(data.label)
            hashMap1[data.label] = data.code
            if (::dialog.isInitialized && dialog.isShowing) {
                dialog.dismiss()
            }
        }
    }

    private fun displayInitialData() {
        val org = viewModel.loadOrganizations(requireActivity())
        if (!org.isNullOrEmpty()) {
            val co = formatterClass.getSharedPref(Constants.CURRENT_ORG, requireContext())
            if (co != null) {
                val matchingOrg = org.firstOrNull { it.code == co }
                if (matchingOrg != null) {
                    val children = matchingOrg.children
                    if (children.isEmpty()) {
                        binding.organizationEdittext.setText(matchingOrg.name)
                    }
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
         * @return A new instance of fragment LandingFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            LandingFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}