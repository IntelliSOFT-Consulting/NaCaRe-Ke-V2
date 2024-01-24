package com.nacare.capture.data.model;


import org.hisp.dhis.android.core.dataelement.DataElement;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttribute;

import java.util.Date;
import java.util.List;

public class ExpandableItem {
    private String groupName;
    private List<TrackedEntityAttribute> childItems;
    private List<DataElement> dataElements;
    private boolean isExpanded;

    public ExpandableItem(String groupName, List<TrackedEntityAttribute> childItems, List<DataElement> dataElements) {
        this.groupName = groupName;
        this.childItems = childItems;
        this.dataElements = dataElements;
        this.isExpanded = false;
    }

    public String getGroupName() {
        return groupName;
    }

    public List<TrackedEntityAttribute> getChildItems() {
        return childItems;
    }

    public List<DataElement> getDataElements() {
        return dataElements;
    }

    public boolean isExpanded() {
        return isExpanded;
    }

    public void setExpanded(boolean expanded) {
        isExpanded = expanded;
    }
}
