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
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.intellisoft.nacare.adapter.TreeAdapter
import com.intellisoft.nacare.helper_class.CountyUnit
import com.intellisoft.nacare.helper_class.FormatterClass
import com.intellisoft.nacare.helper_class.OrgTreeNode
import com.intellisoft.nacare.room.Converters
import com.intellisoft.nacare.room.EventData
import com.intellisoft.nacare.room.MainViewModel
import com.intellisoft.nacare.room.OrganizationData
import com.intellisoft.nacare.util.AppUtils.disableTextInputEditText
import com.intellisoft.nacare.util.AppUtils.showDatePickerDialog
import com.nacare.ke.capture.R
import com.nacare.ke.capture.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {

    private lateinit var binding: FragmentHomeBinding
    private lateinit var viewModel: MainViewModel
    private lateinit var formatterClass: FormatterClass
    val hashMap1 = mutableMapOf<String, String>()
    private lateinit var dialog: AlertDialog


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewModel = ViewModelProvider(this).get(MainViewModel::class.java)

        binding = FragmentHomeBinding.inflate(inflater, container, false)
        formatterClass = FormatterClass()
        binding.apply {
            edtDate.setText(formatterClass.getFormattedDate())
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
                    showNoOrgUnits()
                }
            }
        }
        binding.btnNext.apply {
            setOnClickListener {
                val code = generateCode(binding.edtOrg.text.toString())
                val name = binding.edtOrg.text.toString()
                val date = binding.edtDate.text.toString()
                val data = EventData(
                    date = date,
                    orgUnitCode = code,
                    orgUnitName = name
                )
                viewModel.addEvent(requireContext(), data)
                formatterClass.saveSharedPref("date", date, requireContext())
                formatterClass.saveSharedPref("code", code, requireContext())
                formatterClass.saveSharedPref("name", name, requireContext())
                val hostNavController =
                    requireActivity().findNavController(R.id.nav_host_fragment_content_dashboard)
                hostNavController.navigate(R.id.nav_gallery)
            }
        }
        return binding.root
    }

    private fun showNoOrgUnits() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("No Organization Units")
            .setMessage("There are not organization units, pleas try again later!!")
            .setPositiveButton("Okay") { dialog, _ ->
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .show()

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
                    children = generateChild(converters.children)
                )
                treeNodes.add(orgNode)
            } catch (e: Exception) {
                e.printStackTrace()
                Log.e("TAG", "child units error:::: ${e.message}")
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

    private fun generateChild(children: List<CountyUnit>): List<OrgTreeNode> {
        val treeNodes = mutableListOf<OrgTreeNode>()
        for (ch in children) {
            val orgNode = OrgTreeNode(
                label = ch.name,
                code = ch.id,
                children = generateChild(ch.children)
            )
            treeNodes.add(orgNode)

        }

        return treeNodes
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
