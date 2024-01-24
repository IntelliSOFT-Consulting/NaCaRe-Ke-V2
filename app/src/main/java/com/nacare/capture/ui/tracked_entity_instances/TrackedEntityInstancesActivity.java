package com.nacare.capture.ui.tracked_entity_instances;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.nacare.capture.R;
import com.nacare.capture.data.Sdk;
import com.nacare.capture.data.model.FormatterClass;
import com.nacare.capture.data.service.ActivityStarter;
import com.nacare.capture.ui.base.ListActivity;
import com.nacare.capture.ui.enrollment_form.EnrollmentFormActivity;
import com.nacare.capture.ui.tracked_entity_instances.search.TrackedEntityInstanceSearchActivity;

import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstanceCollectionRepository;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstanceCreateProjection;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

import static android.text.TextUtils.isEmpty;

public class TrackedEntityInstancesActivity extends ListActivity {

    private CompositeDisposable compositeDisposable;
    private String selectedProgram;
    private final int ENROLLMENT_RQ = 1210;
    private TrackedEntityInstanceAdapter adapter;

    private enum IntentExtra {
        PROGRAM
    }

    public static Intent getTrackedEntityInstancesActivityIntent(Context context, String program) {
        Intent intent = new Intent(context, TrackedEntityInstancesActivity.class);
        intent.putExtra(IntentExtra.PROGRAM.name(), program);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setUp(R.layout.activity_tracked_entity_instances, R.id.trackedEntityInstancesToolbar,
                R.id.trackedEntityInstancesRecyclerView);
        selectedProgram = getIntent().getStringExtra(IntentExtra.PROGRAM.name());
        compositeDisposable = new CompositeDisposable();
        observeTrackedEntityInstances();

        if (isEmpty(selectedProgram))
            findViewById(R.id.enrollmentButton).setVisibility(View.GONE);

        findViewById(R.id.textViewWithArrow).setOnClickListener(v -> {
            onBackPressed();
        });
        findViewById(R.id.enrollmentButton).setOnClickListener(view -> {
            String orgCode = new FormatterClass().getSharedPref("orgCode", this);
            if (TextUtils.isEmpty(orgCode)) {
                Toast.makeText(this, "Please Select Organization Unit", Toast.LENGTH_SHORT).show();
                return;
            }
            ActivityStarter.startActivity(
                    TrackedEntityInstancesActivity.this, TrackedEntityInstanceSearchActivity.getIntent(this), false);
//            compositeDisposable.add(
//                    Sdk.d2().programModule().programs().uid(selectedProgram).get()
//                            .map(program -> Sdk.d2().trackedEntityModule().trackedEntityInstances()
//                                    .blockingAdd(
//                                            TrackedEntityInstanceCreateProjection.builder()
//                                                    .organisationUnit(orgCode)
//                                                    .trackedEntityType(program.trackedEntityType().uid())
//                                                    .build()
//                                    ))
//                            .map(teiUid -> EnrollmentFormActivity.getFormActivityIntent(
//                                    TrackedEntityInstancesActivity.this,
//                                    teiUid,
//                                    selectedProgram,
//                                    orgCode
//                            ))
//                            .subscribeOn(Schedulers.io())
//                            .observeOn(AndroidSchedulers.mainThread())
//
//                            .subscribe(
//                                    activityIntent ->
//                                            ActivityStarter.startActivityForResult(
//                                                    TrackedEntityInstancesActivity.this, activityIntent, ENROLLMENT_RQ),
//                                    Throwable::printStackTrace
//                            )
//
//            );

        });
    }

    private void observeTrackedEntityInstances() {
        adapter = new TrackedEntityInstanceAdapter();
        recyclerView.setAdapter(adapter);

        getTeiRepository().getPaged(20).observe(this, trackedEntityInstancePagedList -> {
            adapter.setSource(trackedEntityInstancePagedList.getDataSource());
            adapter.submitList(trackedEntityInstancePagedList);
            findViewById(R.id.trackedEntityInstancesNotificator).setVisibility(
                    trackedEntityInstancePagedList.isEmpty() ? View.VISIBLE : View.GONE);
            findViewById(R.id.circularProgressBar).setVisibility(
                    trackedEntityInstancePagedList.isEmpty() ? View.VISIBLE : View.GONE);
        });
    }

    private TrackedEntityInstanceCollectionRepository getTeiRepository() {
        TrackedEntityInstanceCollectionRepository teiRepository =
                Sdk.d2().trackedEntityModule().trackedEntityInstances().withTrackedEntityAttributeValues();
        if (!isEmpty(selectedProgram)) {
            List<String> programUids = new ArrayList<>();
            programUids.add(selectedProgram);
            String orgCode = new FormatterClass().getSharedPref("orgCode", this);
            if (TextUtils.isEmpty(orgCode)) {
                return teiRepository.byProgramUids(programUids).byOrganisationUnitUid().eq(orgCode);
            } else return teiRepository;
        } else {
            return teiRepository;
        }
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
        if (requestCode == ENROLLMENT_RQ && resultCode == RESULT_OK) {
            adapter.invalidateSource();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
