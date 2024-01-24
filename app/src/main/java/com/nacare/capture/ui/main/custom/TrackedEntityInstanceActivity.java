package com.nacare.capture.ui.main.custom;

import static com.nacare.capture.data.service.FlipperManager.setUp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.nacare.capture.R;
import com.nacare.capture.data.Sdk;
import com.nacare.capture.data.adapters.ExpandableListAdapter;
import com.nacare.capture.data.model.ExpandableItem;
import com.nacare.capture.data.model.FormatterClass;
import com.nacare.capture.ui.base.ListWithoutBindingsActivity;

import org.hisp.dhis.android.core.program.ProgramSection;
import org.hisp.dhis.android.core.program.ProgramStage;
import org.hisp.dhis.android.core.program.ProgramStageSection;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttribute;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class TrackedEntityInstanceActivity extends ListWithoutBindingsActivity {
    public static Intent getIntent(Context context) {
        return new Intent(context, TrackedEntityInstanceActivity.class);
    }

    private List<ProgramSection> programSectionList;
    private List<ProgramStageSection> programStageSections;
    private ProgramStage programStage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tracked_entity_instance);
        setUp(R.id.trackedEntityInstanceSearchToolbar, R.id.trackedEntityInstanceRecyclerView);
        RecyclerView recyclerView = findViewById(R.id.trackedEntityInstanceRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        List<ExpandableItem> itemList = generateSampleData();
        ExpandableListAdapter adapter = new ExpandableListAdapter(itemList, this);
        recyclerView.setAdapter(adapter);
    }

    private List<ExpandableItem> generateSampleData() {
        List<ExpandableItem> itemList = new ArrayList<>();
        List<TrackedEntityAttribute> trackedEntityAttributes = new ArrayList<>();
        String programUid = new FormatterClass().getSharedPref("programUid", this);
        if (programUid != null) {
            programSectionList = Sdk.d2().programModule()
                    .programSections()
                    .withAttributes()
                    .byProgramUid()
                    .eq(programUid)
                    .blockingGet();
            List<TrackedEntityAttribute> flattenedList = programSectionList.stream()
                    .flatMap(section -> section.attributes().stream()) // Replace getYourNestedList() with the actual method to retrieve nested list
                    .distinct()
                    .collect(Collectors.toList());

            for (TrackedEntityAttribute programSection : flattenedList) {
                trackedEntityAttributes.add(programSection);
            }
            itemList.add(new ExpandableItem("Patient Details and Cancer Information", trackedEntityAttributes, null));
            programStage = Sdk.d2().programModule()
                    .programStages()
                    .byProgramUid()
                    .eq(programUid)
                    .one()
                    .blockingGet();
            if (programStage != null) {
                programStageSections = Sdk.d2().programModule()
                        .programStageSections()
                        .withDataElements()
                        .byProgramStageUid().eq(programStage.uid())
                        .blockingGet();

                for (ProgramStageSection programStageSection : programStageSections) {
                    itemList.add(new ExpandableItem(programStageSection.displayName(), null, programStageSection.dataElements()));
                }
            }

        }

        return itemList;
    }
}