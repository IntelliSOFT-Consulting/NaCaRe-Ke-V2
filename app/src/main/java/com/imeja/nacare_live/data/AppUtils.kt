package com.imeja.nacare_live.data

import com.imeja.nacare_live.model.CountyUnit
import com.imeja.nacare_live.model.OrgTreeNode

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