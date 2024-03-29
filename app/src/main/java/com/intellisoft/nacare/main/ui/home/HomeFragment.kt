package com.intellisoft.nacare.main.ui.home

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.intellisoft.nacare.adapter.TreeAdapter
import com.intellisoft.nacare.helper_class.FormatterClass
import com.intellisoft.nacare.helper_class.OrgTreeNode
import com.intellisoft.nacare.models.Constants
import com.intellisoft.nacare.models.Constants.CURRENT_ORG
import com.intellisoft.nacare.room.Converters
import com.intellisoft.nacare.room.EventData
import com.intellisoft.nacare.room.MainViewModel
import com.intellisoft.nacare.room.OrganizationData
import com.intellisoft.nacare.util.AppUtils.disableTextInputEditText
import com.intellisoft.nacare.util.AppUtils.generateChild
import com.intellisoft.nacare.util.AppUtils.showDatePickerDialog
import com.intellisoft.nacare.util.AppUtils.showNoOrgUnits
import com.intellisoft.nacare.viewmodels.NetworkViewModel
import com.nacare.capture.R
import com.nacare.capture.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {

    private lateinit var binding: FragmentHomeBinding
    private lateinit var viewModel: MainViewModel
    private lateinit var networkViewModel: NetworkViewModel
    private lateinit var formatterClass: FormatterClass
    val hashMap1 = mutableMapOf<String, String>()
    private lateinit var dialog: AlertDialog


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewModel = ViewModelProvider(this).get(MainViewModel::class.java)
        networkViewModel = ViewModelProvider(this).get(NetworkViewModel::class.java)

        binding = FragmentHomeBinding.inflate(inflater, container, false)
        formatterClass = FormatterClass()
        binding.apply {
            edtDate.setText(formatterClass.getFormattedDateMonth())
            disableTextInputEditText(edtDate)
            disableTextInputEditText(edtOrg)
        }
        initOrganizations()
        binding.edtDate.apply {
            setOnClickListener {
                showDatePickerDialog(
                    requireContext(), binding.edtDate, setMaxNow = true, setMinNow = false
                )
            }
        }
        binding.edtOrg.apply {
            setOnClickListener {
                val org = viewModel.loadOrganizations(requireActivity())
                if (!org.isNullOrEmpty()) {
                    showOrgUnitDialog(org)
                } else {
                    showNoOrgUnits(requireContext())
                }
            }
        }
        binding.orgHolder.apply {

        }
        binding.btnNext.apply {
            setOnClickListener {
                val code = generateCode(binding.edtOrg.text.toString())
                val name = binding.edtOrg.text.toString()
                val date = binding.edtDate.text.toString()

                if (name.isEmpty()) {
                    binding.orgHolder.error = "Please select unit"
                    binding.edtOrg.requestFocus()
                    return@setOnClickListener
                }
                val data = EventData(
                    userId = "",
                    date = date,
                    orgUnitCode = code,
                    orgUnitName = name,
                    patientId = "", serverId = "", entityId = ""
                )
                viewModel.addEvent(requireContext(), data)
                formatterClass.saveSharedPref("date", date, requireContext())
                formatterClass.saveSharedPref("code", code, requireContext())
                formatterClass.saveSharedPref("name", name, requireContext())
                networkViewModel.updateData(data)
                val hostNavController =
                    requireActivity().findNavController(R.id.nav_host_fragment_content_dashboard)
                hostNavController.navigate(R.id.nav_gallery)
            }
        }

        displayInitialData()
        return binding.root
    }

    private fun displayInitialData() {
        val org = viewModel.loadOrganizations(requireActivity())
        if (!org.isNullOrEmpty()) {
            val co = formatterClass.getSharedPref(CURRENT_ORG, requireContext())
            if (co != null) {
                val matchingOrg = org.firstOrNull { it.code == co }
                if (matchingOrg != null) {
                    val children = matchingOrg.children
                    Log.e("Data", "Org Children $children")
                    if (children.isEmpty()) {
                        /*children.forEach {

                        }*/
                        binding.edtOrg.setText(matchingOrg.name)
                    }
                }

            }

        }
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
                    children = generateChild(converters.children)
                )
                treeNodes.add(orgNode)

            } catch (e: Exception) {
                e.printStackTrace()
            }

            val dialogBuilder = AlertDialog.Builder(requireActivity())
            val inflater = layoutInflater
            val dialogView = inflater.inflate(R.layout.dialog_tree, null)
            dialogBuilder.setView(dialogView)

            val recyclerView: RecyclerView = dialogView.findViewById(R.id.recyclerView)
            val adapter = TreeAdapter(requireContext(), treeNodes, this::selectOrganization)
            recyclerView.adapter = adapter
            recyclerView.layoutManager = LinearLayoutManager(requireContext())

            dialogBuilder.setPositiveButton("OK") { dialog, which ->
                // Handle positive button click if needed
            }

            dialog = dialogBuilder.create()
            dialog.show()
        }
    }

    private fun selectOrganization(data: OrgTreeNode) {
        binding.apply {
            edtOrg.setText(data.label)
            hashMap1[data.label] = data.code
            if (::dialog.isInitialized && dialog.isShowing) {
                dialog.dismiss()
            }
        }
    }


    private fun generateCode(value: String): String {
        return hashMap1[value].toString()
    }

    private fun initOrganizations() {
        val organizationList = mutableListOf<String>()
        val org = viewModel.loadOrganizations(requireActivity())
        if (!org.isNullOrEmpty()) {
            org.forEach {
                hashMap1[it.name] = it.code
                organizationList.add(it.name)
            }
            val adapter = ArrayAdapter(
                requireContext(),
                android.R.layout.simple_list_item_1,
                organizationList
            )
            binding.apply {
//                actOrganization.setAdapter(adapter)
//                actOrganization.setText(organizationList[0], false)
            }

        }
    }
}
