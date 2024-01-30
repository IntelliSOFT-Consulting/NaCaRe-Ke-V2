package com.nacare.capture.ui.main.custom;

import static com.nacare.capture.data.service.FlipperManager.setUp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.nacare.capture.R;
import com.nacare.capture.data.Sdk;
import com.nacare.capture.data.adapters.ExpandableListAdapter;
import com.nacare.capture.data.model.ExpandableItem;
import com.nacare.capture.data.model.FormatterClass;
import com.nacare.capture.ui.base.ListWithoutBindingsActivity;
import com.nacare.capture.ui.enrollment_form.EnrollmentFormActivity;

import org.hisp.dhis.android.core.enrollment.Enrollment;
import org.hisp.dhis.android.core.enrollment.EnrollmentCollectionRepository;
import org.hisp.dhis.android.core.event.Event;
import org.hisp.dhis.android.core.event.EventCollectionRepository;
import org.hisp.dhis.android.core.option.Option;
import org.hisp.dhis.android.core.program.ProgramSection;
import org.hisp.dhis.android.core.program.ProgramStage;
import org.hisp.dhis.android.core.program.ProgramStageSection;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttribute;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttributeValue;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstance;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstanceCollectionRepository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class TrackedEntityInstanceActivity extends ListWithoutBindingsActivity {

    private enum IntentExtra {
        TEI_UID, PROGRAM_UID, OU_UID, NEW_USER, EVENT_UID
    }


    public static Intent getIntent(Context context,
                                   String eventUid,
                                   String teiUid,
                                   String programUid,
                                   String orgUnitUid, boolean isNew) {
        Intent intent = new Intent(context, TrackedEntityInstanceActivity.class);
        intent.putExtra(IntentExtra.TEI_UID.name(), teiUid);
        intent.putExtra(IntentExtra.PROGRAM_UID.name(), programUid);
        intent.putExtra(IntentExtra.OU_UID.name(), orgUnitUid);
        intent.putExtra(IntentExtra.NEW_USER.name(), isNew);
        intent.putExtra(IntentExtra.EVENT_UID.name(), eventUid);
        return intent;
    }

    private List<ProgramSection> programSectionList;
    private List<ProgramStageSection> programStageSections;
    private ProgramStage programStage;

    private String selectedTei, selectedProgram, selectedOrgUnit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tracked_entity_instance);
        setUp(R.id.trackedEntityInstanceSearchToolbar, R.id.trackedEntityInstanceRecyclerView);
        RecyclerView recyclerView = findViewById(R.id.trackedEntityInstanceRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        selectedTei = getIntent().getStringExtra(IntentExtra.TEI_UID.name());
        selectedProgram = getIntent().getStringExtra(IntentExtra.PROGRAM_UID.name());
        selectedOrgUnit = getIntent().getStringExtra(IntentExtra.OU_UID.name());
        new FormatterClass().saveSharedPref("selectedTei", selectedTei, this);
        List<ExpandableItem> itemList = generateSampleData();
        ExpandableListAdapter adapter = new ExpandableListAdapter(itemList, this);
        recyclerView.setAdapter(adapter);

        loadCurrentTrackedEntity();
    }

    private void loadCurrentTrackedEntity() {
        Log.e("TAG", "Selected Program " + selectedProgram);
        Log.e("TAG", "Selected Org Unit " + selectedOrgUnit);
        Log.e("TAG", "Selected Tei " + selectedTei);

        /**
         * Get the current enrollemr linked to the tracked entity**/

        EnrollmentCollectionRepository enrollmentCollectionRepository = Sdk.d2().enrollmentModule().enrollments();
        List<Enrollment> enrollments = enrollmentCollectionRepository
                .byProgram().eq(selectedProgram)
                .byOrganisationUnit().eq(selectedOrgUnit)
                .byTrackedEntityInstance().eq(selectedTei)
                .blockingGet();


        List<String> trackedEntities = new ArrayList<>();
        for (Enrollment enrollment : enrollments) {
//            String trackedEntityInstance = enrollment.;
//            trackedEntities.add(trackedEntityInstance);
        }

        EventCollectionRepository eventRepository =
                Sdk.d2().eventModule().events().withTrackedEntityDataValues();
        List<String> instanceUids = new ArrayList<>();
        instanceUids.add(selectedTei);

        List<Event> eventList = eventRepository.
                byProgramUid().eq(selectedProgram)
                .byOrganisationUnitUid().eq(selectedOrgUnit)
//                .byEnrollmentUid().eq
                .byTrackedEntityInstanceUids(instanceUids).blockingGet();

        for (Event ev : eventList) {
            Log.e("TAG", "Event Data *** " + ev.programStage());
        }


        TrackedEntityInstanceCollectionRepository teiRepository =
                Sdk.d2().trackedEntityModule().trackedEntityInstances().withTrackedEntityAttributeValues();

        List<String> programUids = new ArrayList<>();
        programUids.add(selectedProgram);
        TrackedEntityInstance trackedEntityInstance = teiRepository
                .byProgramUids(programUids)
                .byUid().eq(selectedTei)
                .one()
                .blockingGet();
        Log.e("TAG", "Selected TrackedEntityInstance **** " + trackedEntityInstance.trackedEntityAttributeValues());
        for (TrackedEntityAttributeValue teav : trackedEntityInstance.trackedEntityAttributeValues()) {


            Log.e("TAG", "Selected " + teav.value() + " Attribute " + teav.trackedEntityAttribute());
        }

    }

    private List<ExpandableItem> generateSampleData() {
        try {

            /**
             * Get Current Enrollment
             *
             * Confirm matches
             */
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
                    if (shouldAddAttribute(programSection)) {
                        trackedEntityAttributes.add(programSection);
                    }
                }
                itemList.add(new ExpandableItem(programUid, selectedOrgUnit, selectedTei, "registration", "Patient Details and Cancer Information", trackedEntityAttributes, null));
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
                        itemList.add(new ExpandableItem(programUid, selectedOrgUnit, selectedTei, programStage.uid(), programStageSection.displayName(), null, programStageSection.dataElements()));
                    }
                }

            }

            return itemList;
        } catch (Exception e) {
            return null;
        }
    }

    private boolean shouldAddAttribute(TrackedEntityAttribute programSection) {
        List<String> keywords = Arrays.asList("MiXrdHDZ6Hw", "yIp9UZ1Bex6", "RhplKXZoKsC", "wzHl7HdsSlO", "OSs8D8u1El7", "HEoJiJqgPh1",
                "k5cjujLd0nd", "ghOKiyhlPX0", "BzhDnF5fG4x", "Lhoe9ecBhZi", "AyuVgasCLyM", "vPICBz6JEmK", "xxEsZFtua8N");
        Optional<String> matchingKeyword = keywords.stream()
                .filter(keyword -> programSection.uid().contains(keyword))
                .findFirst();
        return matchingKeyword.isPresent();

    }
}