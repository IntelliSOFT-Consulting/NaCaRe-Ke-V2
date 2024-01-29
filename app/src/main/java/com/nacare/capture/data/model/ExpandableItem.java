package com.nacare.capture.data.model;


import org.hisp.dhis.android.core.dataelement.DataElement;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttribute;

import java.util.Date;
import java.util.List;

public class ExpandableItem {
    private String groupName;
    private List<TrackedEntityAttribute> childItems;
    private List<DataElement> dataElements;
    private String programUid;
    private String programStageUid;

    private String selectedOrgUnit;
    private String selectedTei;
    private boolean isExpanded;

    public ExpandableItem(String programUid,
                          String selectedOrgUnit,
                          String selectedTei,
                          String programStageUid,
                          String groupName,
                          List<TrackedEntityAttribute> childItems, List<DataElement> dataElements) {
        this.programUid = programUid;
        this.selectedOrgUnit = selectedOrgUnit;
        this.selectedTei = selectedTei;
        this.programStageUid = programStageUid;
        this.groupName = groupName;
        this.childItems = childItems;
        this.dataElements = dataElements;
        this.isExpanded = false;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public void setChildItems(List<TrackedEntityAttribute> childItems) {
        this.childItems = childItems;
    }

    public void setDataElements(List<DataElement> dataElements) {
        this.dataElements = dataElements;
    }

    public String getProgramUid() {
        return programUid;
    }

    public void setProgramUid(String programUid) {
        this.programUid = programUid;
    }

    public String getProgramStageUid() {
        return programStageUid;
    }

    public void setProgramStageUid(String programStageUid) {
        this.programStageUid = programStageUid;
    }

    public String getSelectedOrgUnit() {
        return selectedOrgUnit;
    }

    public void setSelectedOrgUnit(String selectedOrgUnit) {
        this.selectedOrgUnit = selectedOrgUnit;
    }

    public String getSelectedTei() {
        return selectedTei;
    }

    public void setSelectedTei(String selectedTei) {
        this.selectedTei = selectedTei;
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
