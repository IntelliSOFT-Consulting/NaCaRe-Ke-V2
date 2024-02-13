package com.nacare.capture.data

import com.nacare.capture.model.CountyUnit
import com.nacare.capture.model.OrgTreeNode

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
}