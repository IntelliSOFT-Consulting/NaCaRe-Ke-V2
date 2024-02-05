package com.nacare.capture.data.model;

import java.util.List;

public class OrgTreeNode {
    private final String label;
    private final String code;
    private final String level;
    private final List<OrgTreeNode> children;
    private boolean isExpanded;

    public OrgTreeNode(String label, String code, String level, List<OrgTreeNode> children, boolean isExpanded) {
        this.label = label;
        this.code = code;
        this.level = level;
        this.children = children;
        this.isExpanded = isExpanded;
    }

    public String getLabel() {
        return label;
    }

    public String getCode() {
        return code;
    }

    public String getLevel() {
        return level;
    }

    public List<OrgTreeNode> getChildren() {
        return children;
    }

    public boolean isExpanded() {
        return isExpanded;
    }

    public void setExpanded(boolean expanded) {
        isExpanded = expanded;
    }
}
