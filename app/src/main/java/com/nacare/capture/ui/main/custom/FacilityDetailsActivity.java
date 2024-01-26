package com.nacare.capture.ui.main.custom;

import static android.text.TextUtils.isEmpty;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.databinding.DataBindingUtil;

import android.content.Context;
import android.content.Intent;
import android.graphics.Path;
import android.os.Bundle;
import android.text.Editable;
import android.text.Html;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.nacare.capture.R;
import com.nacare.capture.data.Sdk;
import com.nacare.capture.data.model.FormatterClass;
import com.nacare.capture.data.model.HomeData;
import com.nacare.capture.data.service.forms.EventFormService;
import com.nacare.capture.data.service.forms.RuleEngineService;
import com.nacare.capture.databinding.ActivityEnrollmentFormBinding;
import com.nacare.capture.databinding.ActivityFacilityDetailsBinding;
import com.nacare.capture.ui.event_form.EventFormActivity;

import org.hisp.dhis.android.core.dataelement.DataElement;
import org.hisp.dhis.android.core.datavalue.DataValueObjectRepository;
import org.hisp.dhis.android.core.maintenance.D2Error;
import org.hisp.dhis.android.core.option.Option;
import org.hisp.dhis.android.core.program.ProgramStage;
import org.hisp.dhis.android.core.program.ProgramStageSection;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityDataValueObjectRepository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class FacilityDetailsActivity extends AppCompatActivity {
    private enum IntentExtra {
        EVENT_UID, PROGRAM_UID, OU_UID, TYPE
    }

    public enum FormType {
        CREATE, CHECK
    }

    public static Intent getIntent(Context context, String eventUid, String programUid,
                                   String orgUnitUid, FacilityDetailsActivity.FormType type) {
        Intent intent = new Intent(context, FacilityDetailsActivity.class);
        intent.putExtra(IntentExtra.EVENT_UID.name(), eventUid);
        intent.putExtra(IntentExtra.PROGRAM_UID.name(), programUid);
        intent.putExtra(IntentExtra.OU_UID.name(), orgUnitUid);
        intent.putExtra(IntentExtra.TYPE.name(), type.name());
        return intent;
    }

    private ActivityFacilityDetailsBinding binding;
    private String eventUid, programUid;
    private FormType formType;
    private RuleEngineService engineService;
    private ProgramStage programStage;
    private List<ProgramStageSection> programStageSections;
    private Map<String, View> inputFieldMap = new HashMap<>();
    private Map<String, String> optionFieldMap = new HashMap<>();

    private List<HomeData> collectedInputs = new ArrayList<>();
    private RadioGroup radioGroup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_facility_details);

        Toolbar toolbar = binding.toolbar;
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        eventUid = getIntent().getStringExtra(IntentExtra.EVENT_UID.name());
        programUid = getIntent().getStringExtra(IntentExtra.PROGRAM_UID.name());
        formType = FormType.valueOf(getIntent().getStringExtra(IntentExtra.TYPE.name()));
        binding.buttonEnd.setOnClickListener(this::submitEnrollment);
        String orgName = new FormatterClass().getSharedPref("orgName", this);
        String program = new FormatterClass().getSharedPref("program", this);
        if (!TextUtils.isEmpty(orgName)) {
            String formattedText = "Saving to <b>" + program + "</b> in <b>" + orgName + "</b>";
            binding.textViewNote.setText(Html.fromHtml(formattedText, Html.FROM_HTML_MODE_LEGACY));
        }
        String orgCode = new FormatterClass().getSharedPref("orgCode", this);
        if (!TextUtils.isEmpty(orgCode)) {
            if (EventFormService.getInstance().init(Sdk.d2(), eventUid, programUid, orgCode))
                this.engineService = new RuleEngineService();
        }
        prepareFormData();
    }

    private void submitEnrollment(View viewold) {
        collectedInputs.clear();
        for (Map.Entry<String, View> entry : inputFieldMap.entrySet()) {
            String id = entry.getKey();
            View view = entry.getValue();

            if (view instanceof TextInputEditText) {
                TextInputEditText textInputEditText = (TextInputEditText) view;
                String input = textInputEditText.getText().toString();

                if (!input.isEmpty()) {
                    HomeData dt = new HomeData(id, input);
                    collectedInputs.add(dt);
                }
            } else if (view instanceof AutoCompleteTextView) {
                AutoCompleteTextView textInputEditText = (AutoCompleteTextView) view;
                String input = textInputEditText.getText().toString();

                if (!input.isEmpty()) {
                    HomeData dt = new HomeData(id, generateAnswerOption(id, input));
                    if (dt.getName() != null) {
                        collectedInputs.add(dt);
                    }
                }
            } else if (view instanceof RadioGroup) {
                radioGroup.setOnCheckedChangeListener((radioGroup, i) -> {
                    String value;
                    switch (i) {
                        case R.id.radioButtonYes:
                            value = "true";
                            break;
                        case R.id.radioButtonNo:
                            value = "false";
                            break;
                        default:
                            value = null;
                            break;
                    }
                    if (!value.isEmpty()) {
                        HomeData dt = new HomeData(id, value);
                        collectedInputs.add(dt);
                    }

                });

            }
            // Handle other view types if needed
        }

        if (collectedInputs.size() > 0) {
            for (HomeData homeData : collectedInputs) {
                String orgCode = new FormatterClass().getSharedPref("orgCode", this);
                if (!TextUtils.isEmpty(orgCode)) {
                    TrackedEntityDataValueObjectRepository valueRepository =
                            Sdk.d2().trackedEntityModule().trackedEntityDataValues()
                                    .value(eventUid, homeData.getId());

                    String currentValue = valueRepository.blockingExists() ?
                            valueRepository.blockingGet().value() : "";

                    try {
                        valueRepository.blockingSet(homeData.getName());
                        Toast.makeText(this, "Saved Successfully", Toast.LENGTH_SHORT).show();
                    } catch (D2Error d2Error) {
                        Toast.makeText(this, "Failed to Save Values " + d2Error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }

//                new SaveDataAsyncTask(this, homeData).execute();
                Log.e("TAG", "Data Values *** " + homeData.getId());
                Log.e("TAG", "Data Values *** " + homeData.getName());
            }
        }

    }

    private String generateAnswerOption(String uid, String inputName) {

        String foundKey = null;
        for (Map.Entry<String, String> entry : optionFieldMap.entrySet()) {
            if (inputName.equals(entry.getValue())) {
                foundKey = entry.getKey();
                break; // Found the key, exit the loop
            }
        }

        // Check if the key was found
        if (foundKey != null) {
            System.out.println("Key for displayName '" + inputName + "': " + foundKey);
        } else {
            System.out.println("No key found for displayName '" + inputName + "'");
        }
        Option option = Sdk.d2().optionModule()
                .options()
                .byUid().eq(foundKey)
//                .byDisplayName().eq(inputName)
                .one()
                .blockingGet();
        Log.e("TAG", "Value we are looking for " + option);
        if (option != null) {
            return option.uid();
        }
        return null;
    }

    private void prepareFormData() {
        programStage = Sdk.d2().programModule()
                .programStages()
                .byProgramUid()
                .eq(programUid)
                .one().blockingGet();
        if (programStage != null) {
            programStageSections = Sdk.d2().programModule()
                    .programStageSections()
                    .withDataElements()
                    .byProgramStageUid().eq(programStage.uid())
                    .blockingGet();

            List<DataElement> flattenedList = programStageSections.stream()
                    .flatMap(section -> section.dataElements().stream()) // Replace getYourNestedList() with the actual method to retrieve nested list
                    .distinct()
                    .collect(Collectors.toList());
            List<String> exclusionIds = Arrays.asList("OeUTTmDXAye", "QHprEcvWSQV", "TUR7b6PuifD");

            for (DataElement dataElement : flattenedList) {
                if (!exclusionIds.contains(dataElement.uid())) {

                    createSearchFieldsDataElement(binding.formLinearLayout, dataElement, retrieveCurrentValue(dataElement));
                }
            }
        }
    }

    private String retrieveCurrentValue(DataElement dataElement) {
        TrackedEntityDataValueObjectRepository valueRepository =
                Sdk.d2().trackedEntityModule().trackedEntityDataValues()
                        .value(eventUid, dataElement.uid());

        return valueRepository.blockingExists() ?
                valueRepository.blockingGet().value() : "";
    }

    private void createSearchFieldsDataElement(LinearLayout linearLayout, DataElement item, String currentValue) {
        String valueType = item.valueType().toString();
        String label = item.displayName();
//        String attribute=item.attributeValues()
        LayoutInflater inflater = LayoutInflater.from(this);

        if ("TEXT".equals(valueType)) {
            if (item.optionSet() == null) {
                LinearLayout itemView = (LinearLayout) inflater.inflate(
                        R.layout.item_edittext,
                        linearLayout,
                        false
                );

                TextView tvName = itemView.findViewById(R.id.tv_name);
                TextView tvElement = itemView.findViewById(R.id.tv_element);
                TextInputLayout textInputLayout = itemView.findViewById(R.id.textInputLayout);
                TextInputEditText editText = itemView.findViewById(R.id.editText);

                tvName.setText(item.displayName());
                tvElement.setText(item.uid());
                inputFieldMap.put(item.uid(), editText);
                editText.setText(currentValue);
                linearLayout.addView(itemView);
            } else {
                LinearLayout itemView = (LinearLayout) inflater.inflate(
                        R.layout.item_autocomplete,
                        linearLayout,
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
                    optionFieldMap.put(option.uid(), option.displayName());
                    optionsStringList.add(option.displayName());
                }
                ArrayAdapter<String> adp = new ArrayAdapter<>(
                        this,
                        android.R.layout.simple_list_item_1,
                        optionsStringList
                );

                tvName.setText(item.displayName());
                autoCompleteTextView.setAdapter(adp);
                String currentData = generateOptionNameFromId(currentValue);
                if (currentData != null) {
                    autoCompleteTextView.setText(currentData, false);
                }
                autoCompleteTextView.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence charSequence, int start, int before, int count) {
                        // This method is called to notify you that somewhere within charSequence, the text is about to be changed.
                    }

                    @Override
                    public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                        // This method is called to notify you that somewhere within charSequence, the text has been changed.
                    }

                    @Override
                    public void afterTextChanged(Editable editable) {
                        // This method is called to notify you that the characters within editable have been changed.
                        // You can perform actions here based on the updated text.
                    }
                });
                adp.notifyDataSetChanged();
                inputFieldMap.put(item.uid(), autoCompleteTextView);
                linearLayout.addView(itemView);


                /* */
            }
        } else if ("DATE".equals(valueType)) {
            View itemView = inflater.inflate(
                    R.layout.item_date_edittext,
                    linearLayout,
                    false
            );

// Find views in the inflated layout
            TextView tvName = itemView.findViewById(R.id.tv_name);
            TextView tvElement = itemView.findViewById(R.id.tv_element);
            TextInputLayout textInputLayout = itemView.findViewById(R.id.textInputLayout);
            TextInputEditText editText = itemView.findViewById(R.id.editText);

// Set values to views
            tvName.setText(item.displayName());
            tvElement.setText(item.uid());
            inputFieldMap.put(item.uid(), editText);

// Get and set response if available
// Define keywords and check for maximum date restriction
            List<String> keywords = Arrays.asList("Birth", "Death");
//            boolean max = containsAnyKeyword(item.displayName(), keywords);
//            new FormatterClass().disableTextInputEditText(editText);

// Set click listener for opening date picker dialog
            editText.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
//                    new FormatterClass().showDatePickerDialog(
//                            ResponderActivity.this, editText, max, false
//                    );
                }
            });

// Set text change listener for updating response
            editText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    // This method is called before the text is changed.
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (s != null) {
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {
                    // This method is called after the text has changed.
                    // You can perform actions here based on the updated text.
                }
            });

            linearLayout.addView(itemView);
        } else if ("BOOLEAN".equals(valueType)) {
            View itemView = inflater.inflate(
                    R.layout.item_radio,
                    linearLayout,
                    false
            );
            TextView tvName = itemView.findViewById(R.id.tv_name);
            radioGroup = itemView.findViewById(R.id.radioGroup);
            RadioButton radioButtonYes = itemView.findViewById(R.id.radioButtonYes);
            RadioButton radioButtonNo = itemView.findViewById(R.id.radioButtonNo);
            tvName.setText(item.displayName());

            if (currentValue != null && currentValue.equals("true")) {
                radioGroup.check(R.id.radioButtonYes);
            } else if (currentValue != null && currentValue.equals("false")) {
                radioGroup.check(R.id.radioButtonNo);
            } else {
                radioGroup.clearCheck();
            }
            inputFieldMap.put(item.uid(), radioGroup);
            linearLayout.addView(itemView);
        }

    }

    private String generateOptionNameFromId(String currentValue) {
        Option option = Sdk.d2().optionModule()
                .options()
                .byUid().eq(currentValue)
                .one()
                .blockingGet();
        if (option != null) {
            return option.displayName();
        } else return null;
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
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}