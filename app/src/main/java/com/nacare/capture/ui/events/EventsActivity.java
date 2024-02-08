package com.nacare.capture.ui.events;

import static android.text.TextUtils.isEmpty;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.nacare.capture.R;
import com.nacare.capture.data.Sdk;
import com.nacare.capture.data.model.FormatterClass;
import com.nacare.capture.data.service.ActivityStarter;
import com.nacare.capture.data.sync.FacilityEvent;
import com.nacare.capture.data.sync.FacilityTask;
import com.nacare.capture.ui.base.ListActivity;
import com.nacare.capture.ui.main.custom.FacilityDetailsActivity;

import org.hisp.dhis.android.core.enrollment.EnrollmentObjectRepository;
import org.hisp.dhis.android.core.event.EventCollectionRepository;
import org.hisp.dhis.android.core.event.EventCreateProjection;
import org.hisp.dhis.android.core.event.EventObjectRepository;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit;

import java.util.Collections;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class EventsActivity extends ListActivity {
    private String selectedProgram;
    private CompositeDisposable compositeDisposable;
    private EventAdapter adapter;
    private final int EVENT_RQ = 1210;

    private enum IntentExtra {
        PROGRAM
    }

    public static Intent getIntent(Context context, String programUid) {
        Bundle bundle = new Bundle();
        if (!isEmpty(programUid))
            bundle.putString(IntentExtra.PROGRAM.name(), programUid);
        Intent intent = new Intent(context, EventsActivity.class);
        intent.putExtras(bundle);
        return intent;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setUp(R.layout.activity_events, R.id.eventsToolbar, R.id.eventsRecyclerView);
        selectedProgram = getIntent().getStringExtra(IntentExtra.PROGRAM.name());
        compositeDisposable = new CompositeDisposable();
        observeEvents();

        if (isEmpty(selectedProgram))
            findViewById(R.id.eventButton).setVisibility(View.GONE);

        findViewById(R.id.textViewWithArrow).setOnClickListener(v -> {
            onBackPressed();
        });

        findViewById(R.id.eventButton).setOnClickListener(view -> {
                    findViewById(R.id.eventButton).setEnabled(false);

                    String orgCode = new FormatterClass().getSharedPref("orgCode", this);
                    if (TextUtils.isEmpty(orgCode)) {
                        Toast.makeText(this, "Please Select Organization Unit", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (TextUtils.isEmpty(selectedProgram)) {
                        Toast.makeText(this, "Please Select a Program to Proceed", Toast.LENGTH_SHORT).show();
                        return;
                    }


                    try {

//                        new FacilityEvent(EventsActivity.this,selectedProgram,orgCode).execute();
                        ProgressDialog progressDialog = new ProgressDialog(this);
                        progressDialog.setMessage("Please wait...");
                        progressDialog.setCancelable(false);
                        progressDialog.show();
                        compositeDisposable.add(
                                Sdk.d2().programModule().programs().uid(selectedProgram).get()
                                        .map(program -> {
                                            String stage = Sdk.d2().programModule().programStages()
                                                    .byProgramUid().eq(program.uid())
                                                    .one().blockingGet().uid();
                                            String attrOptionCombo = program.categoryCombo() != null ?
                                                    Sdk.d2().categoryModule().categoryOptionCombos()
                                                            .byCategoryComboUid().eq(program.categoryComboUid())
                                                            .one().blockingGet().uid() : null;

                                            return Sdk.d2().eventModule().events()
                                                    .blockingAdd(
                                                            EventCreateProjection.builder()
                                                                    .organisationUnit(orgCode)
                                                                    .program(program.uid())
                                                                    .programStage(stage)
                                                                    .attributeOptionCombo(attrOptionCombo)
                                                                    .build()
                                                    );
                                        })
                                        .map(eventUid ->
                                                FacilityDetailsActivity.getIntent(EventsActivity.this,
                                                        eventUid,
                                                        selectedProgram, orgCode, FacilityDetailsActivity.FormType.CREATE))
                                        .subscribeOn(Schedulers.io())
                                        .observeOn(AndroidSchedulers.mainThread())
                                        .subscribe(
                                                activityIntent -> {
                                                    if (progressDialog.isShowing()) {
                                                        progressDialog.dismiss();
                                                    }
                                                    ActivityStarter.startActivityForResult(
                                                            EventsActivity.this, activityIntent, EVENT_RQ);
                                                },
                                                error -> {
                                                    if (progressDialog.isShowing()) {
                                                        progressDialog.dismiss();
                                                    }
                                                    if (error != null) {
                                                        Toast.makeText(this, "Experienced problems " + error.getMessage(), Toast.LENGTH_SHORT).show();
                                                    }
                                                }
                                        ));
                    } catch (Exception e) {
//                       Toast.makeText(this, "Experienced problems " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
        );

    }

    private void observeEvents() {
        try {
            adapter = new EventAdapter(this);
            recyclerView.setAdapter(adapter);
            String orgUnit = new FormatterClass().getSharedPref("orgCode", this);
            if (!isEmpty(orgUnit)) {
                if (!isEmpty(selectedProgram)) {
                    getEventRepository(orgUnit).getPaged(20).observe(this, eventsPagedList -> {
                        adapter.setSource(eventsPagedList.getDataSource());
                        adapter.submitList(eventsPagedList);
                        findViewById(R.id.eventsNotificator).setVisibility(View.GONE);
                        findViewById(R.id.eventButton).setVisibility(
                                eventsPagedList.isEmpty() ? View.VISIBLE : View.GONE);
//                        findViewById(R.id.circularProgressBar).setVisibility(                                eventsPagedList.isEmpty() ? View.VISIBLE : View.GONE);
                    });
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onResume() {
        observeEvents();
        super.onResume();
    }

    private EventCollectionRepository getEventRepository(String orgUnit) {
        EventCollectionRepository eventRepository =
                Sdk.d2().eventModule().events().withTrackedEntityDataValues();
        return eventRepository.byProgramUid().eq(selectedProgram).byOrganisationUnitUid().eq(orgUnit);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (compositeDisposable != null) {
            compositeDisposable.clear();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == EVENT_RQ && resultCode == RESULT_OK) {
            adapter.invalidateSource();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
