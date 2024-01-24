package com.nacare.capture.ui.tracked_entity_instances.search;

import android.content.Context;
import android.content.Intent;
import android.graphics.Path;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.LiveData;
import androidx.paging.PagedList;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.nacare.capture.R;
import com.nacare.capture.data.Sdk;
import com.nacare.capture.data.model.FormatterClass;
import com.nacare.capture.data.service.ActivityStarter;
import com.nacare.capture.data.service.forms.EventFormService;
import com.nacare.capture.data.service.forms.FormField;
import com.nacare.capture.databinding.ActivityTrackedEntityInstanceSearchBinding;
import com.nacare.capture.ui.base.ListWithoutBindingsActivity;
import com.nacare.capture.ui.main.custom.TrackedEntityInstanceActivity;
import com.nacare.capture.ui.tracked_entity_instances.TrackedEntityInstanceAdapter;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.nacare.capture.ui.tracked_entity_instances.TrackedEntityInstancesActivity;

import org.hisp.dhis.android.core.arch.helpers.UidsHelper;
import org.hisp.dhis.android.core.arch.repositories.scope.RepositoryScope;
import org.hisp.dhis.android.core.common.ValueType;
import org.hisp.dhis.android.core.option.Option;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnitMode;
import org.hisp.dhis.android.core.program.Program;
import org.hisp.dhis.android.core.program.ProgramSection;
import org.hisp.dhis.android.core.program.ProgramType;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttribute;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstance;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.Flowable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class TrackedEntityInstanceSearchActivity extends ListWithoutBindingsActivity {

    private ProgressBar progressBar;
    private TextView notificator;
    private TrackedEntityInstanceAdapter adapter;
    private SearchFormAdapter searchFormAdapter;
    private ActivityTrackedEntityInstanceSearchBinding binding;
    private CompositeDisposable disposable;

    private String savedAttribute;
    private String savedProgram;
    private String savedFilter;
    private LinearLayout lnParent;
    private Map<String, View> stringMap;

    private List<TrackedEntityAttribute> attributeList;
    private ProgramSection programSectionList;

    public static Intent getIntent(Context context) {
        return new Intent(context, TrackedEntityInstanceSearchActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this,
                R.layout.activity_tracked_entity_instance_search);
        setUp(R.id.trackedEntityInstanceSearchToolbar, R.id.trackedEntityInstanceRecyclerView);

        stringMap = new HashMap<>();
        disposable = new CompositeDisposable();
        lnParent = findViewById(R.id.lnParent);
        loadProgram();
        findViewById(R.id.btn_proceed).setOnClickListener(v -> showCustomAlertDialog(this));

    }

    public void showCustomAlertDialog(Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        LayoutInflater inflater = LayoutInflater.from(context);
        View customView = inflater.inflate(R.layout.custom_layout, null);
        builder.setView(customView);
        AlertDialog alertDialog = builder.create();
        TextView tvTitle = customView.findViewById(R.id.tv_title);
        TextView tvMessage = customView.findViewById(R.id.tv_message);
        MaterialButton nextButton = customView.findViewById(R.id.next_button);
        tvTitle.setText(R.string.search_results);
        tvMessage.setText(R.string.no_record_found_for_the_patient_with_the_details_provided);
        nextButton.setText(R.string.register_new_patient);
        nextButton.setOnClickListener(v -> {
            alertDialog.dismiss();
            ActivityStarter.startActivity(
                    TrackedEntityInstanceSearchActivity.this, TrackedEntityInstanceActivity.getIntent(this), true);
        });

        alertDialog.show();
    }

    private void loadProgram() {
        String programUid = new FormatterClass().getSharedPref("programUid", this);
        if (programUid != null) {
            programSectionList = Sdk.d2().programModule()
                    .programSections()
                    .withAttributes()
                    .byProgramUid()
                    .eq(programUid)
                    .byName().like("SEARCH PATIENT")
                    .one().blockingGet();
            if (programSectionList != null) {
                List<String> exclusionIds = Arrays.asList("R1vaUuILrDy", "hn8hJsBAKrh", "hzVijy6tEUF");
                List<TrackedEntityAttribute> remainingList = new ArrayList<>();

                TrackedEntityAttribute hzVijy6tEUF = null;
                if (programSectionList.attributes() != null) {
                    for (TrackedEntityAttribute programSection : programSectionList.attributes()) {
                        if ("R1vaUuILrDy".equals(programSection.uid())) {
                            hzVijy6tEUF = programSection;
                        } else if (!exclusionIds.contains(programSection.uid())) {
                            remainingList.add(programSection);
                        }
                    }
                }
                if (hzVijy6tEUF != null) {
                    remainingList.add(0, hzVijy6tEUF);
                }

                if (!remainingList.isEmpty()) {
                    for (TrackedEntityAttribute trackedEntityAttribute : remainingList) {
                        createSearchFields(trackedEntityAttribute);
                    }

                }

            }
        }

    }

    private void createSearchFields(TrackedEntityAttribute item) {
        String valueType = item.valueType().toString();
        String label = item.displayName();
        LayoutInflater inflater = LayoutInflater.from(this);

        if ("TEXT".equals(valueType)) {
            if (item.optionSet() == null) {
                LinearLayout itemView = (LinearLayout) inflater.inflate(
                        R.layout.item_edittext,
                        findViewById(R.id.lnParent),
                        false
                );

                TextView tvName = itemView.findViewById(R.id.tv_name);
                TextView tvElement = itemView.findViewById(R.id.tv_element);
                TextInputLayout textInputLayout = itemView.findViewById(R.id.textInputLayout);
                TextInputEditText editText = itemView.findViewById(R.id.editText);

                tvName.setText(modifiedLabelName(item.displayName(), item.uid()));
                tvElement.setText(item.uid());

                stringMap.put(item.uid(), editText);
                lnParent.addView(itemView);
            } else {
                LinearLayout itemView = (LinearLayout) inflater.inflate(
                        R.layout.item_autocomplete,
                        findViewById(R.id.lnParent),
                        false
                );


                TextView tvName = itemView.findViewById(R.id.tv_name);
                TextView tvElement = itemView.findViewById(R.id.tv_element);
                TextInputLayout textInputLayout = itemView.findViewById(R.id.textInputLayout);
                AutoCompleteTextView autoCompleteTextView = itemView.findViewById(R.id.autoCompleteTextView);

                tvElement.setText(item.uid());

                List<String> optionsStringList = new ArrayList<>();

                List<Option> optionsList = generateOptionSets(item.optionSet().uid());
                for (Option option : optionsList) {
                    optionsStringList.add(option.displayName());
                }
                ArrayAdapter<String> adp = new ArrayAdapter<>(
                        this,
                        android.R.layout.simple_list_item_1,
                        optionsStringList
                );

                tvName.setText(item.displayName());
                autoCompleteTextView.setAdapter(adp);
                adp.notifyDataSetChanged();
                stringMap.put(item.uid(), autoCompleteTextView);
                lnParent.addView(itemView);
            }
        }

    }

    private String modifiedLabelName(String s, String uid) {
        if (uid.equals("R1vaUuILrDy")) {
            return "Patient Name";
        } else {
            return s;
        }

    }

    private List<Option> generateOptionSets(String uid) {
        List<Option> optionsList;
        optionsList = Sdk.d2().optionModule()
                .options()
                .byOptionSetUid().eq(uid)
                .blockingGet();
        return optionsList;
    }


    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }


    private LiveData<PagedList<TrackedEntityInstance>> getTrackedEntityInstanceQuery() {
        List<OrganisationUnit> organisationUnits = Sdk.d2().organisationUnitModule().organisationUnits()
                .byOrganisationUnitScope(OrganisationUnit.Scope.SCOPE_DATA_CAPTURE)
                .byRootOrganisationUnit(true)
                .blockingGet();

        List<String> organisationUids = new ArrayList<>();
        if (!organisationUnits.isEmpty()) {
            organisationUids = UidsHelper.getUidsList(organisationUnits);
        }

        return Sdk.d2().trackedEntityModule()
                .trackedEntityInstanceQuery()
                .byOrgUnits().in(organisationUids)
                .byOrgUnitMode().eq(OrganisationUnitMode.DESCENDANTS)
                .byProgram().eq(savedProgram)
                .byFilter(savedAttribute).like(savedFilter)
                .onlineFirst().getPaged(15);
    }
}
