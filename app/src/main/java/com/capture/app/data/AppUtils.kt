package com.capture.app.data

import android.app.Application
import android.content.Context
import android.util.Log
import com.capture.app.data.Constants.FACILITY_STATUS
import com.capture.app.model.CountyUnit
import com.capture.app.model.DataValue
import com.capture.app.model.OrgTreeNode
import com.capture.app.room.Converters
import com.capture.app.room.MainViewModel

class AppUtils {

    fun generateChild(children: List<CountyUnit>): List<OrgTreeNode> {
        val treeNodes = mutableListOf<OrgTreeNode>()
        for (ch in children) {
            val orgNode = OrgTreeNode(
                label = ch.name,
                code = ch.id,
                level = ch.level,
                children = generateChild(ch.children)
            )
            treeNodes.add(orgNode)

        }

        return treeNodes.sortedBy { it.label }
    }

    fun checkIfFacilityIsFunctional(context: Context): Boolean {
        val searchParameters = ArrayList<DataValue>()
        val viewModel = MainViewModel(context.applicationContext as Application)
        val orgUnit = FormatterClass().getSharedPref("orgCode", context)
        if (orgUnit != null) {
            val data = viewModel.loadEvents(orgUnit, context)
            Log.e("TAG", "Facility Data ***** $data")
            if (data != null) {
                if (data.isNotEmpty()) {
                    data.forEach {
                        FormatterClass().saveSharedPref(
                            "facility_checker_data",
                            it.dataValues,
                            context
                        )
                    }
                    val facilityData =
                        FormatterClass().getSharedPref("facility_checker_data", context)
                    Log.e("TAG", "Facility Data *****  datas $facilityData")
                    if (facilityData != null) {
                        val attributes = Converters().fromJsonDataAttribute(facilityData)
                        if (attributes.isNotEmpty()) {
                            searchParameters.clear()
                            attributes.forEachIndexed { index, attribute ->
                                saveValued(
                                    index,
                                    attribute.dataElement,
                                    attribute.value,
                                    searchParameters
                                )
                            }
                            Log.e("TAG", "Facility Data *****  params $searchParameters")
                            val facilityCodeValue =
                                searchParameters.find { it.dataElement == FACILITY_STATUS }
                            if (facilityCodeValue != null) {
                                Log.e(
                                    "TAG",
                                    "Facility Data *****  value ${facilityCodeValue.value}"
                                )
                                if (facilityCodeValue.value=="Functional"){
                                    return true
                                }
                            }
                            Log.e("TAG", "Facility Data *****  value null $facilityCodeValue")
                        }
                    }
                }
            }
        }
        Log.e("TAG", "Facility Data ***** $orgUnit")
        return false
    }

    private fun saveValued(
        index: Int,
        id: String,
        value: String,
        searchParameters: ArrayList<DataValue>
    ) {
        val existingIndex = searchParameters.indexOfFirst { it.dataElement == id }
        if (existingIndex != -1) {
            // Update the existing entry if the code is found
            searchParameters[existingIndex] = DataValue(dataElement = id, value = value)
        } else {
            // Add a new entry if the code is not found
            val data = DataValue(dataElement = id, value = value)
            searchParameters.add(data)
        }
    }
}