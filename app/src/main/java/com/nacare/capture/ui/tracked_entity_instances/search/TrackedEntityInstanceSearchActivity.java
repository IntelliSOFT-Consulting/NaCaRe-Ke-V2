package com.nacare.capture.ui.tracked_entity_instances.search;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.LiveData;
import androidx.paging.PagedList;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.nacare.capture.R;
import com.nacare.capture.data.Sdk;
import com.nacare.capture.data.model.FormatterClass;
import com.nacare.capture.data.model.HomeData;
import com.nacare.capture.data.service.ActivityStarter;
import com.nacare.capture.databinding.ActivityTrackedEntityInstanceSearchBinding;
import com.nacare.capture.ui.base.ListWithoutBindingsActivity;
import com.nacare.capture.ui.main.custom.TrackedEntityRegistrationActivity;
import com.nacare.capture.ui.tracked_entity_instances.TrackedEntityInstanceAdapter;

import org.hisp.dhis.android.core.arch.helpers.UidsHelper;
import org.hisp.dhis.android.core.maintenance.D2Error;
import org.hisp.dhis.android.core.option.Option;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnitMode;
import org.hisp.dhis.android.core.program.ProgramSection;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttribute;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstance;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstanceCollectionRepository;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstanceCreateProjection;
import org.hisp.dhis.android.core.trackedentity.search.TrackedEntityInstanceQueryCollectionRepository;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

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
    private ArrayList<HomeData> collectedInputs = new ArrayList<>();

    public static Intent getIntent(Context context) {
        return new Intent(context, TrackedEntityInstanceSearchActivity.class);
    }

    private List<TrackedEntityInstance> trackedEntityInstances = new ArrayList<>();
    private CompositeDisposable compositeDisposable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this,
                R.layout.activity_tracked_entity_instance_search);
        setUp(R.id.trackedEntityInstanceSearchToolbar, R.id.trackedEntityInstanceRecyclerView);
        compositeDisposable = new CompositeDisposable();
        stringMap = new HashMap<>();
        disposable = new CompositeDisposable();
        lnParent = findViewById(R.id.lnParent);
        loadProgram();
        findViewById(R.id.btn_proceed).setOnClickListener(this::submitData);

    }

    private void submitData(View viewCurrent) {
        collectedInputs.clear();
        for (Map.Entry<String, View> entry : stringMap.entrySet()) {
            String id = entry.getKey();
            View view = entry.getValue();

            if (view instanceof TextInputEditText) {
                TextInputEditText textInputEditText = (TextInputEditText) view;
                String input = textInputEditText.getText().toString();

                if (!input.isEmpty()) {
                    HomeData dt = new HomeData(id, input.trim());
                    collectedInputs.add(dt);
                }

            } else if (view instanceof AutoCompleteTextView) {
                AutoCompleteTextView textInputEditText = (AutoCompleteTextView) view;
                String input = textInputEditText.getText().toString();

                if (!input.isEmpty()) {
                    HomeData dt = new HomeData(id, input);
                    if (dt.getName() != null) {
                        collectedInputs.add(dt);
                    }
                }
            }
        }
        if (collectedInputs.size() > 0) {
            String programUid = new FormatterClass().getSharedPref("programUid", this);
            if (TextUtils.isEmpty(programUid)) {
                Toast.makeText(this, "Please Select Program to Proceed", Toast.LENGTH_SHORT).show();
                return;
            }
            String orgCode = new FormatterClass().getSharedPref("orgCode", this);
            if (TextUtils.isEmpty(orgCode)) {
                Toast.makeText(this, "Please Select Organization to Proceed", Toast.LENGTH_SHORT).show();
                return;
            }
            searchTrackedEntityInstanceQuery(programUid, collectedInputs).observe(this, trackedEntityInstancePagedList -> {

                if (!trackedEntityInstancePagedList.isEmpty()) {
                    ActivityStarter.startActivity(this, SearchResultsActivity.getIntent(this, programUid, collectedInputs, orgCode), true);
                } else {
                    showCustomAlertDialog(this, collectedInputs);
                }
            });

        } else {
            Toast.makeText(this, "Please enter at least one search parameter", Toast.LENGTH_SHORT).show();
        }
    }

    private LiveData<PagedList<TrackedEntityInstance>> searchTrackedEntityInstanceQuery(String programUid, List<HomeData> data) {

        TrackedEntityInstanceQueryCollectionRepository collectionRepository = Sdk.d2().trackedEntityModule()
                .trackedEntityInstanceQuery()
                .byOrgUnitMode().eq(OrganisationUnitMode.DESCENDANTS)
                .byProgram().eq(programUid);
        for (HomeData hd : data) {
            collectionRepository = collectionRepository.byFilter(hd.getId()).like(hd.getName());
        }
        return collectionRepository.onlineFirst().getPaged(15);
    }

    private TrackedEntityInstanceCollectionRepository getTeiRepository(String programUid) {
        TrackedEntityInstanceCollectionRepository teiRepository =
                Sdk.d2().trackedEntityModule().trackedEntityInstances().withTrackedEntityAttributeValues();
        List<String> programUids = new ArrayList<>();
        programUids.add(programUid);
        return teiRepository.byProgramUids(programUids);
    }

    public void showCustomAlertDialog(Context context, ArrayList<HomeData> collectedInputs) {
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
        alertDialog.setCancelable(false);
        nextButton.setOnClickListener(v -> {
            alertDialog.dismiss();
            String orgCode = new FormatterClass().getSharedPref("orgCode", this);
            String programUid = new FormatterClass().getSharedPref("programUid", this);
            String trackedEntityType = new FormatterClass().getSharedPref("trackedEntityType", this);
            if (programUid != null && orgCode != null && trackedEntityType != null) {

                Toast.makeText(this, "Creating an Enrollment", Toast.LENGTH_SHORT).show();
                try {
                    ProgressDialog progressDialog = new ProgressDialog(this);
                    progressDialog.setMessage("Please wait...");
                    progressDialog.setCancelable(false);
                    progressDialog.show();

                    compositeDisposable.add(
                            Sdk.d2().programModule().programs().uid(programUid).get()
                                    .map(program -> Sdk.d2().trackedEntityModule().trackedEntityInstances()
                                            .blockingAdd(
                                                    TrackedEntityInstanceCreateProjection.builder()
                                                            .organisationUnit(orgCode)
                                                            .trackedEntityType(trackedEntityType)
                                                            .build()
                                            ))
                                    .map(teiUid -> TrackedEntityRegistrationActivity.getIntent(
                                            this,
                                            teiUid,
                                            programUid,
                                            orgCode, collectedInputs, "true"
                                    ))
                                    .subscribeOn(Schedulers.io())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe(
                                            activityIntent -> {
                                                if (progressDialog.isShowing()) {
                                                    progressDialog.dismiss();
                                                }
                                                ActivityStarter.startActivity(
                                                        this, activityIntent, true);
                                            },
                                            error -> {
                                                if (progressDialog.isShowing()) {
                                                    progressDialog.dismiss();
                                                }
                                                Toast.makeText(this, "Experienced problems " + error.getMessage(), Toast.LENGTH_SHORT).show();

                                            }


                                    ));
                } catch (Exception e) {
                    Toast.makeText(this, "Experienced problems " + e.getMessage(), Toast.LENGTH_SHORT).show();

                }


            } else {
                Toast.makeText(this, "Please select Organization Unit to proceed", Toast.LENGTH_SHORT).show();
            }


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

                if (item.uid().equals("AP13g7NcBOf")) {
                    LinearLayout itemView = (LinearLayout) inflater.inflate(
                            R.layout.item_edittext_custom,
                            findViewById(R.id.lnParent),
                            false
                    );
                    TextView tvName = itemView.findViewById(R.id.tv_name);
                    TextView tvElement = itemView.findViewById(R.id.tv_element);
                    TextInputEditText editTextOne = itemView.findViewById(R.id.editTextOne);
                    TextInputEditText editTextTwo = itemView.findViewById(R.id.editTextTwo);
                    TextInputEditText editTextThree = itemView.findViewById(R.id.editTextThree);
                    TextInputEditText editTextFour = itemView.findViewById(R.id.editTextFour);
                    TextInputEditText editText = itemView.findViewById(R.id.editText);

                    TextInputLayout textInputLayoutOne = itemView.findViewById(R.id.textInputLayoutOne);
                    TextInputLayout textInputLayoutTwo = itemView.findViewById(R.id.textInputLayoutTwo);
                    TextInputLayout textInputLayoutThree = itemView.findViewById(R.id.textInputLayoutThree);
                    TextInputLayout textInputLayoutFour = itemView.findViewById(R.id.textInputLayoutFour);

                    setupEditText(textInputLayoutOne, editTextOne, editTextTwo, 3, false, 0);
                    setupEditText(textInputLayoutTwo, editTextTwo, editTextThree, 3, false, 0);
                    setupEditText(textInputLayoutThree, editTextThree, editTextFour, 2, true, 12);

                    handleFinalTextView(editTextFour, textInputLayoutFour, 4);


                    tvName.setText(modifiedLabelName(item.displayName(), item.uid()));
                    tvElement.setText(item.uid());
                    stringMap.put(item.uid(), editText);
                    lnParent.addView(itemView);
                } else {
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
                }

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

    private void handleFinalTextView(TextInputEditText currentEditText, TextInputLayout textInputLayout, final int maxLength) {

        Date currentDate = new Date();
        SimpleDateFormat yearFormat = new SimpleDateFormat("yyyy", Locale.ENGLISH);
        String formattedYear = yearFormat.format(currentDate);
        System.out.println("Current Year: " + formattedYear);
        Integer maxValue = Integer.valueOf(formattedYear);
        currentEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                if (charSequence.length() == maxLength) {

                    if (!charSequence.toString().isEmpty() && Integer.parseInt(charSequence.toString()) > maxValue) {
                        currentEditText.setSelection(currentEditText.getText().length());  // Move the cursor to the end
                        textInputLayout.setError(".");

                    } else {
                        textInputLayout.setError(null);
                    }

                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });
    }

    private void setupEditText(final TextInputLayout textInputLayout, final EditText currentEditText,
                               final EditText nextEditText, final int maxLength, final Boolean isNumber, final Integer maxValue) {
        currentEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                if (charSequence.length() == maxLength) {
                    // Move the cursor to the next EditText when maxLength is reached
                    if (isNumber) {
                        if (!charSequence.toString().isEmpty() && Integer.parseInt(charSequence.toString()) > maxValue) {
//                            currentEditText.setText(String.valueOf(maxValue));  // Set to the maximum value
                            currentEditText.setSelection(currentEditText.getText().length());  // Move the cursor to the end
                            textInputLayout.setError(".");

                        } else {
                            nextEditText.requestFocus();
                            if (nextEditText instanceof TextInputEditText) {
                                ((TextInputEditText) nextEditText).setSelection(0);
                            }
                            textInputLayout.setError(null);
                        }
                    } else {
                        nextEditText.requestFocus();
                        if (nextEditText instanceof TextInputEditText) {
                            ((TextInputEditText) nextEditText).setSelection(0);
                        }
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });
    }

    private void updateEditText(EditText editTextOne, EditText editTextTwo, EditText editTextThree, EditText editTextFour, EditText editText) {
        String textOne = editTextOne.getText().toString();
        String textTwo = editTextTwo.getText().toString();
        String textThree = editTextThree.getText().toString();
        String textFour = editTextFour.getText().toString();
        String combinedText = textOne + "-" + textTwo + "-" + textThree + "-" + textFour;
        editText.setText(combinedText);
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

        TrackedEntityInstanceQueryCollectionRepository jeff = Sdk.d2().trackedEntityModule()
                .trackedEntityInstanceQuery()
                .byOrgUnits().in(organisationUids)
                .byOrgUnitMode().eq(OrganisationUnitMode.DESCENDANTS)
                .byProgram().eq(savedProgram);


        return jeff.byFilter(savedAttribute).like(savedFilter)
                .onlineFirst().getPaged(15);
    }
}
