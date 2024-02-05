package com.nacare.capture.ui.main.custom;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.LiveData;
import androidx.paging.PagedList;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.nacare.capture.R;
import com.nacare.capture.data.Sdk;
import com.nacare.capture.data.adapters.TreeAdapter;
import com.nacare.capture.data.model.FormatterClass;
import com.nacare.capture.data.model.OrgTreeNode;
import com.nacare.capture.data.service.ActivityStarter;
import com.nacare.capture.data.sync.OrganizationTask;
import com.nacare.capture.ui.programs.ProgramsActivity;
import com.nacare.capture.ui.tracked_entity_instances.TrackedEntityInstanceAdapter;

import org.hisp.dhis.android.core.arch.repositories.scope.RepositoryScope;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit;
import org.hisp.dhis.android.core.program.Program;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class OrganizationActivity extends AppCompatActivity implements OrganizationTask.OnTaskCompletedListener {
    private Disposable disposable;
    private MaterialButton nextButton;
    private AutoCompleteTextView autoCompleteTextView;
    private List<String> stringList;

    private Map<String, String> stringMap;
    private ArrayAdapter adapter;
    private RecyclerView programsRecyclerView;
    private ProgressBar syncProgressBar;

    public static Intent getOrganizationActivityIntent(Context context) {
        return new Intent(context, OrganizationActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_organization);
        Toolbar toolbar = findViewById(R.id.toolBar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Cancer Notification Tool");
        syncProgressBar = findViewById(R.id.syncProgressBar);
        programsRecyclerView = findViewById(R.id.programsRecyclerView);
        autoCompleteTextView = findViewById(R.id.autoCompleteTextView);
        nextButton = findViewById(R.id.btn_proceed);
        String orgName = new FormatterClass().getSharedPref("orgName", this);
        autoCompleteTextView.setText(orgName, false);
        nextButton.setOnClickListener(v -> {
//                    String data = autoCompleteTextView.getText().toString();
//                    if (data.isEmpty()) {
//                        Toast.makeText(this, "Select Organization Unit to Proceed", Toast.LENGTH_SHORT).show();
//                        autoCompleteTextView.setError("Select Organization Unit to Proceed");
//                        autoCompleteTextView.requestFocus();
//                        return;
//                    }
//                    String orgCode = getCodeFromHash(data);
//                    if (TextUtils.isEmpty(orgCode)) {
//                        autoCompleteTextView.setError("Select Organization Unit to Proceed");
//                        autoCompleteTextView.requestFocus();
//                        return;
//                    }
//                    new FormatterClass().saveSharedPref("orgCode", orgCode, this);
//                    new FormatterClass().saveSharedPref("orgName", data, this);
                    ActivityStarter.startActivity(OrganizationActivity.this,
                            ProgramsActivity.getProgramActivityIntent(OrganizationActivity.this), false);
                }
        );

        stringMap = new HashMap<>();
        stringList = new ArrayList<>();

        autoCompleteTextView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int before, int count) {
                // This method is called to notify you that characters within `charSequence` are about to be replaced with new text.
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                // This method is called to notify you that somewhere within `charSequence`, text has been added or removed.
            }

            @Override
            public void afterTextChanged(Editable editable) {
                // This method is called to notify you that the characters within `editable` have changed.
                String newText = editable.toString();
                // Do something with the new text.
                if (newText.length() >= 3) {
                    loadOrganizationsByName(newText);

                }
            }
        });

        loadOrganizations();


    }

    private String getCodeFromHash(String data) {
        return stringMap.get(data);
    }

    private void loadOrganizationsByName(String newText) {
        Log.e("TAG", "Organization Units ***** " + newText);
        try {
            disposable = Sdk.d2().organisationUnitModule().organisationUnits()
//                .byRootOrganisationUnit(true)
                    .byDisplayName().like(newText)
                    .byLevel().eq(5)
                    .get()
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())

                    .subscribe(
                            organisationUnits -> {
                                stringList.clear();
                                // Handle the list of organizations here
                                for (OrganisationUnit organisationUnit : organisationUnits) {
                                    // Access information about each organisationUnit
                                    String name = organisationUnit.displayName();
                                    String uid = organisationUnit.uid();
                                    stringList.add(name);
                                    stringMap.put(name, uid);
                                }
                                adapter = new ArrayAdapter(this,
                                        android.R.layout.simple_list_item_1, stringList);
                                autoCompleteTextView.setAdapter(adapter);
                            },

                            error -> {
                                Log.e("TAG", "Error Encountered **** " + error.getMessage());
                            }

                    );
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadOrganizations() {
        try {
            // Handle errors here
            disposable = Sdk.d2().organisationUnitModule().organisationUnits()
                    .byRootOrganisationUnit(true)
                    .get()
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            organisationUnits -> {
                                // Handle the list of organizations here
                                for (OrganisationUnit organisationUnit : organisationUnits) {
                                    Log.e("TAG", "Organization Data ***** " + organisationUnit);
//                                    fetchOrganisationUnits(organisationUnit.uid());
                                    new OrganizationTask(OrganizationActivity.this, organisationUnit.uid(), this).execute();
                                }
                            },
                            error -> {
                                Log.e("TAG", "Error Encountered **** " + error.getMessage());
                            }
                    );
        } catch (Exception e) {
            Log.e("TAG", "Organization Error **** " + e.getMessage());
        }
    }

    private void fetchOrganisationUnits(String parentUid) {
        try {
            // Handle errors here
            disposable = Sdk.d2().organisationUnitModule().organisationUnits()
                    .byParentUid().eq(parentUid)
                    .get()
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            organisationUnits -> {
                                // Handle the list of organizations here
                                for (OrganisationUnit organisationUnit : organisationUnits) {
                                    Log.e("TAG", "Organization Data ***** " + organisationUnit);
                                    if (organisationUnit.level() != null) {
                                        // Check if the level is less than 5 before fetching children
                                        if (organisationUnit.level() < 5) {
                                            // Recursive call to fetch children for the current organization unit
                                            fetchOrganisationUnits(organisationUnit.uid());
                                        }
                                    }
                                }
                            },
                            error -> {
                                Log.e("TAG", "Error Encountered **** " + error.getMessage());
                            }
                    );

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    public void onTaskCompleted(ArrayList<OrgTreeNode> orgTreeNodes) {
        if (!orgTreeNodes.isEmpty()) {
            Log.e("TAG", "Data Retrieved **** " + orgTreeNodes);
            TreeAdapter adapter = new TreeAdapter(this, orgTreeNodes, this::handleClick);
            programsRecyclerView.setAdapter(adapter);
            programsRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        }
    }

    @Override
    public void startStopProgress(boolean show) {
        if (show) {
            syncProgressBar.setVisibility(View.VISIBLE);
        } else {
            syncProgressBar.setVisibility(View.GONE);
        }
    }

    private void handleClick(OrgTreeNode otn) {
        Log.e("TAG", "Data Retrieved **** " + otn.getLabel());
        new FormatterClass().saveSharedPref("orgCode", otn.getCode(), this);
        new FormatterClass().saveSharedPref("orgName", otn.getLabel(), this);
        autoCompleteTextView.setText(otn.getLabel(), false);
        ActivityStarter.startActivity(OrganizationActivity.this,
                ProgramsActivity.getProgramActivityIntent(OrganizationActivity.this), false);
    }

}