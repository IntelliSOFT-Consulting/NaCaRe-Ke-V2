package com.nacare.capture.ui.tracked_entity_instances.search;

import androidx.appcompat.app.AlertDialog;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.LiveData;
import androidx.paging.PagedList;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;
import com.nacare.capture.R;
import com.nacare.capture.data.Sdk;
import com.nacare.capture.data.adapters.TrackedResultsAdapter;
import com.nacare.capture.data.model.FormatterClass;
import com.nacare.capture.data.model.HomeData;
import com.nacare.capture.data.service.ActivityStarter;
import com.nacare.capture.data.sync.EnrollmentTask;
import com.nacare.capture.databinding.ActivitySearchResultsBinding;
import com.nacare.capture.ui.base.ListWithoutBindingsActivity;
import com.nacare.capture.ui.main.custom.TrackedEntityInstanceActivity;

import org.hisp.dhis.android.core.arch.repositories.scope.RepositoryScope;
import org.hisp.dhis.android.core.enrollment.Enrollment;
import org.hisp.dhis.android.core.enrollment.EnrollmentCollectionRepository;
import org.hisp.dhis.android.core.enrollment.EnrollmentCreateProjection;
import org.hisp.dhis.android.core.enrollment.EnrollmentObjectRepository;
import org.hisp.dhis.android.core.event.Event;
import org.hisp.dhis.android.core.event.EventCollectionRepository;
import org.hisp.dhis.android.core.event.EventCreateProjection;
import org.hisp.dhis.android.core.maintenance.D2Error;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnitMode;
import org.hisp.dhis.android.core.program.ProgramStage;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstance;
import org.hisp.dhis.android.core.trackedentity.search.TrackedEntityInstanceQueryCollectionRepository;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import io.reactivex.disposables.CompositeDisposable;

public class SearchResultsActivity extends ListWithoutBindingsActivity {
    private ActivitySearchResultsBinding binding;
    private CompositeDisposable disposable;

    private enum IntentExtra {
        PROGRAM, DATA, ORGANIZATION
    }

    public static Intent getIntent(Context context, String program, ArrayList<HomeData> homeData, String organization) {
        Intent intent = new Intent(context, SearchResultsActivity.class);
        intent.putExtra(IntentExtra.PROGRAM.name(), program);
        intent.putExtra(IntentExtra.DATA.name(), homeData);
        intent.putExtra(IntentExtra.ORGANIZATION.name(), organization);
        return intent;

    }

    private String selectedProgram, selectedOrganization;
    private ArrayList<HomeData> homeData;
    private TrackedResultsAdapter adapter;
    private final int ENROLLMENT_RQ = 1210;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = DataBindingUtil.setContentView(this,
                R.layout.activity_search_results);
        setUp(R.id.trackedEntityInstanceSearchToolbar, R.id.trackedEntityInstanceRecyclerView);
        disposable = new CompositeDisposable();
        selectedProgram = getIntent().getStringExtra(IntentExtra.PROGRAM.name());
        selectedOrganization = getIntent().getStringExtra(IntentExtra.ORGANIZATION.name());
        homeData = (ArrayList<HomeData>) getIntent().getSerializableExtra(IntentExtra.DATA.name());
        adapter = new TrackedResultsAdapter(this::handleResultsClick);
        recyclerView.setAdapter(adapter);
        searchTrackedEntityInstanceQuery(selectedProgram, homeData).observe(this, trackedEntityInstancePagedList -> {
            adapter.setSource(trackedEntityInstancePagedList.getDataSource());
            adapter.submitList(trackedEntityInstancePagedList);

        });

    }


    private void handleResultsClick(TrackedEntityInstance data) {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = LayoutInflater.from(this);
        View customView = inflater.inflate(R.layout.custom_layout_cases, null);
        builder.setView(customView);
        AlertDialog alertDialog = builder.create();
        TextView tvTitle = customView.findViewById(R.id.tv_title);
        TextView tvMessage = customView.findViewById(R.id.tv_message);
        MaterialButton noButton = customView.findViewById(R.id.no_button);
        MaterialButton yesButton = customView.findViewById(R.id.yes_button);

        String htmlText = "Please select an action for the selected record:<br><br>1." +
                "<b>Add new primary cancer information for an existing patient:</b> " +
                "Choose this option if this is new primary cancer information for an existing patient.<br><br> " +
                "2.<b>Update an Existing Cancer Case:</b> Choose this option if you want to update any other additional information " +
                "relating to an existing cancer case.";

        tvTitle.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_START);
        tvTitle.setText(R.string.alert);
        tvMessage.setText(Html.fromHtml(htmlText));
        tvMessage.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_START);
        noButton.setText(R.string.add_new_primary_cancer_info);
        noButton.setOnClickListener(v -> {
            alertDialog.dismiss();
            String enrollmentUid = null;
            try {
                new EnrollmentTask(SearchResultsActivity.this, data.uid(), selectedProgram, selectedOrganization).execute();
            } catch (Exception e) {
                e.printStackTrace();
            }
         /*   try {

                EnrollmentCollectionRepository enrollmentCollectionRepository = Sdk.d2().enrollmentModule().enrollments();
                Enrollment enrollment = enrollmentCollectionRepository
                        .byProgram().eq(selectedProgram)
                        .byOrganisationUnit().eq(selectedOrganization)
                        .byTrackedEntityInstance().eq(data.uid())
                        .one()
                        .blockingGet();

                if (enrollment == null) {

                    enrollmentUid = Sdk.d2().enrollmentModule().enrollments().blockingAdd(
                            EnrollmentCreateProjection.builder()
                                    .organisationUnit(selectedOrganization)
                                    .program(selectedProgram)
                                    .trackedEntityInstance(data.uid())
                                    .build()
                    );
                    EnrollmentObjectRepository enrollmentRepository = Sdk.d2().enrollmentModule().enrollments().uid(enrollmentUid);
                    enrollmentRepository.setEnrollmentDate(new FormatterClass().getNowWithoutTime());
                    enrollmentRepository.setIncidentDate(new FormatterClass().getNowWithoutTime());
                } else {
                    enrollmentUid = enrollment.uid();
                }

                // Create an Empty Event to hold the responses
                ProgramStage stage = Sdk.d2().programModule().programStages()
                        .byProgramUid().eq(selectedProgram)
                        .byName().like("ALL")
                        .one().blockingGet();
                if (stage != null) {
                    String eventUid = Sdk.d2().eventModule().events()
                            .blockingAdd(
                                    EventCreateProjection.builder()
                                            .organisationUnit(selectedOrganization)
                                            .program(selectedProgram)
                                            .programStage(stage.uid())
                                            .enrollment(enrollmentUid)
                                            .build()
                            );

                    Log.e("TAG", "Created Event **** " + eventUid);

                    new FormatterClass().saveSharedPref("tracked_event", eventUid, this);
                    ActivityStarter.startActivity(this,
                            TrackedEntityInstanceActivity.getIntent(this, eventUid, data.uid(), selectedProgram, selectedOrganization, false), true);
                } else {
                    Toast.makeText(this, "Please select a program stage to proceed", Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                Log.e("Error", "Error creating enrollment" + e.getMessage());
            }*/

        });
        yesButton.setText(R.string.update_an_existing_cancer_case);
        yesButton.setOnClickListener(v -> {
            alertDialog.dismiss();

            try {
                new EnrollmentTask(SearchResultsActivity.this, data.uid(), selectedProgram, selectedOrganization).execute();
            } catch (Exception e) {
                e.printStackTrace();
            }
          /*  String eventUid = null;
            try {
                EnrollmentCollectionRepository enrollmentCollectionRepository = Sdk.d2().enrollmentModule().enrollments();
                Enrollment enrollment = enrollmentCollectionRepository
                        .byProgram().eq(selectedProgram)
                        .byOrganisationUnit().eq(selectedOrganization)
                        .byTrackedEntityInstance().eq(data.uid())
                        .one()
                        .blockingGet();

                Log.e("TAG", " Current Enrollment **** " + enrollment);
                if (enrollment != null) {
                    // Create an Empty Event to hold the responses
                    ProgramStage stage = Sdk.d2().programModule().programStages()
                            .byProgramUid().eq(selectedProgram)
                            .byName().like("ALL")
                            .one()
                            .blockingGet();
                    Log.e("TAG", " Current Enrollment **** Stage" + stage);
                    if (stage != null) {
                        *//***
             * Get the newest event
             * ***//*
                        EventCollectionRepository eventCollectionRepository = Sdk.d2().eventModule().events().withTrackedEntityDataValues();
                        Event event = eventCollectionRepository
                                .byEnrollmentUid().eq(enrollment.uid())
                                .orderByCreated(RepositoryScope.OrderByDirection.DESC)
                                .one()
                                .blockingGet();
                        Log.e("TAG", " Current Enrollment **** Event" + event);
                        if (event != null) {
                            eventUid = event.uid();
                        } else {
                            *//*
             *  Create a new event linked to the enrollment
             *//*
                            eventUid = Sdk.d2().eventModule().events()
                                    .blockingAdd(
                                            EventCreateProjection.builder()
                                                    .organisationUnit(selectedOrganization)
                                                    .program(selectedProgram)
                                                    .programStage(stage.uid())
                                                    .enrollment(enrollment.uid())
                                                    .build()
                                    );

                            Log.e("TAG", " Current Enrollment **** Event Created" + eventUid);
                        }

                        new FormatterClass().saveSharedPref("tracked_event", eventUid, this);

                        ActivityStarter.startActivity(this,
                                TrackedEntityInstanceActivity.getIntent(this, eventUid, data.uid(), selectedProgram, selectedOrganization, false), true);
                    } else {
                        Toast.makeText(this, "Please select a program stage to proceed", Toast.LENGTH_SHORT).show();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                Log.e("TAG", " Current Enrollment **** Error " + e.getMessage());
            }*/

        });

        alertDialog.show();
    }


    private LiveData<PagedList<TrackedEntityInstance>> searchTrackedEntityInstanceQuery(String programUid, ArrayList<HomeData> data) {

        TrackedEntityInstanceQueryCollectionRepository collectionRepository = Sdk.d2().trackedEntityModule()
                .trackedEntityInstanceQuery()
                .byOrgUnitMode().eq(OrganisationUnitMode.DESCENDANTS)
                .byProgram().eq(programUid);
        for (HomeData hd : data) {
            collectionRepository = collectionRepository.byFilter(hd.getId()).like(hd.getName());
        }
        return collectionRepository.onlineFirst().getPaged(15);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (disposable != null) {
            disposable.dispose();
        }
    }
}