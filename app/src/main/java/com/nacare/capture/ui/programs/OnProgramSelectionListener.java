package com.nacare.capture.ui.programs;

import org.hisp.dhis.android.core.program.ProgramType;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityType;

public interface OnProgramSelectionListener {
    void onProgramSelected(String programUid, ProgramType programType, String type, TrackedEntityType trackedEntityType);
}
