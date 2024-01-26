package com.nacare.capture.ui.tracked_entity_instances.search;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
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

import com.google.android.material.button.MaterialButton;
import com.nacare.capture.R;
import com.nacare.capture.data.Sdk;
import com.nacare.capture.data.adapters.TrackedResultsAdapter;
import com.nacare.capture.data.model.HomeData;
import com.nacare.capture.data.service.ActivityStarter;
import com.nacare.capture.databinding.ActivitySearchResultsBinding;
import com.nacare.capture.ui.base.ListWithoutBindingsActivity;
import com.nacare.capture.ui.enrollment_form.EnrollmentFormActivity;
import com.nacare.capture.ui.main.custom.TrackedEntityInstanceActivity;
import com.nacare.capture.ui.tracked_entity_instances.TrackedEntityInstanceAdapter;

import org.hisp.dhis.android.core.organisationunit.OrganisationUnitMode;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstance;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstanceCreateProjection;

import java.util.ArrayList;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

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
        for (HomeData data : homeData) {
            searchTrackedEntityInstanceQuery(selectedProgram, data).observe(this, trackedEntityInstancePagedList -> {
                adapter.setSource(trackedEntityInstancePagedList.getDataSource());
                adapter.submitList(trackedEntityInstancePagedList);

            });

        }
    }

    private void handleResultsClick(TrackedEntityInstance trackedEntityInstance) {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = LayoutInflater.from(this);
        View customView = inflater.inflate(R.layout.custom_layout_confirm, null);
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
        tvMessage.setText(Html.fromHtml(htmlText, Html.FROM_HTML_MODE_LEGACY));
        tvMessage.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_START);
        noButton.setText(R.string.add_new_primary_cancer_info);
        noButton.setOnClickListener(v -> {
            alertDialog.dismiss();
            disposable.add(
                    Sdk.d2().programModule().programs().uid(selectedProgram).get()
                            .map(program -> Sdk.d2().trackedEntityModule().trackedEntityInstances()
                                    .blockingAdd(
                                            TrackedEntityInstanceCreateProjection.builder()
                                                    .organisationUnit(selectedOrganization)
                                                    .trackedEntityType(program.trackedEntityType().uid())
                                                    .build()
                                    ))
                            .map(teiUid -> TrackedEntityInstanceActivity.getIntent(
                                    SearchResultsActivity.this
                            ))
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(
                                    activityIntent ->
                                            ActivityStarter.startActivity(
                                                    SearchResultsActivity.this, activityIntent, true),
                                    Throwable::printStackTrace
                            )

            );

        });
        yesButton.setText(R.string.update_an_existing_cancer_case);
        yesButton.setOnClickListener(v -> {
            alertDialog.dismiss();

        });

        alertDialog.show();
    }


    private LiveData<PagedList<TrackedEntityInstance>> searchTrackedEntityInstanceQuery(String programUid, HomeData data) {

        return Sdk.d2().trackedEntityModule()
                .trackedEntityInstanceQuery()
                .byOrgUnitMode().eq(OrganisationUnitMode.DESCENDANTS)
                .byProgram().eq(programUid)
                .byFilter(data.getId()).like(data.getName())
                .onlineFirst().getPaged(15);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (disposable != null) {
            disposable.dispose();
        }
    }
}