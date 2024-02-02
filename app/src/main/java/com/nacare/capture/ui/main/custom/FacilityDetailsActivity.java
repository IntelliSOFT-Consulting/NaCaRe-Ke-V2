package com.nacare.capture.ui.main.custom;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.databinding.DataBindingUtil;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
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
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.nacare.capture.R;
import com.nacare.capture.data.Sdk;
import com.nacare.capture.data.model.FormatterClass;
import com.nacare.capture.data.model.HomeData;
import com.nacare.capture.data.response.ProgramResponse;
import com.nacare.capture.data.service.forms.EventFormService;
import com.nacare.capture.data.service.forms.RuleEngineService;
import com.nacare.capture.databinding.ActivityFacilityDetailsBinding;

import org.hisp.dhis.android.core.arch.call.D2Progress;
import org.hisp.dhis.android.core.attribute.AttributeValue;
import org.hisp.dhis.android.core.dataelement.DataElement;
import org.hisp.dhis.android.core.event.Event;
import org.hisp.dhis.android.core.event.EventObjectRepository;
import org.hisp.dhis.android.core.imports.TrackerImportConflict;
import org.hisp.dhis.android.core.maintenance.D2Error;
import org.hisp.dhis.android.core.option.Option;
import org.hisp.dhis.android.core.program.ProgramStage;
import org.hisp.dhis.android.core.program.ProgramStageDataElement;
import org.hisp.dhis.android.core.program.ProgramStageSection;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityDataValueObjectRepository;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

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

        try {
            EventObjectRepository ev = Sdk.d2().eventModule().events().uid(eventUid);
            Event event = Sdk.d2().eventModule().events().uid(eventUid).blockingGet();
            if (event != null) {
                if (event.eventDate() == null) {
                    ev.setEventDate(new FormatterClass().getNowWithoutTime());
                }
            }
        } catch (Exception e) {
            Log.e("TAG", "Error Updating Event Date ***" + e.getMessage());
        }
        prepareFormData();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.sync_menu, menu);
        return true;
    }

    private Observable<D2Progress> syncEvent(String teiUid) {
        return Sdk.d2().eventModule().events()
                .byUid().eq(teiUid)
                .upload();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle menu item clicks here
        switch (item.getItemId()) {
            case R.id.menu_sync:
                Disposable disposable = syncEvent(eventUid)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                data -> {

                                    Log.e("TAG", "Data Upload *** " + data);
                                },
                                Throwable::printStackTrace,
                                () -> {

                                }
                        );

                List<TrackerImportConflict> trackerImportConflicts = Sdk.d2().importModule().trackerImportConflicts()
                        .byEventUid().eq(eventUid).blockingGet();

                Log.e("TAG", "Data Upload *** Conflicts" + trackerImportConflicts);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
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

                RadioGroup radioGroup = (RadioGroup) view;
                int checkedId = radioGroup.getCheckedRadioButtonId();

                if (checkedId != -1) {
                    RadioButton selectedRadioButton = findViewById(checkedId);
                    if (selectedRadioButton != null) {
                        String value = selectedRadioButton.getText().toString();
                        Log.e("TAG", "Selected Value **** " + value);
                        if (value.equalsIgnoreCase("Yes")) {
                            value = "true";
                        } else {
                            value = "false";
                        }
                        HomeData dt = new HomeData(id, value);
                        collectedInputs.add(dt);
                    }
                }

            }
            // Handle other view types if needed
        }

        if (collectedInputs.size() > 0) {
            String orgCode = new FormatterClass().getSharedPref("orgCode", this);
            if (!TextUtils.isEmpty(orgCode)) {
                try {
                    SaveDataAsyncTask saveDataAsyncTask = new SaveDataAsyncTask(this, eventUid, collectedInputs);
                    saveDataAsyncTask.execute();
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.e("TAG", "Form Field ***** Async Error " + e.getMessage());
                }
            }
        } else {
            Toast.makeText(this, "Please enter at least a value", Toast.LENGTH_SHORT).show();
        }

    }

    private class SaveDataAsyncTask extends AsyncTask<Void, Void, Boolean> {
        private final Context context;
        private final String eventUid;
        private final List<HomeData> homeData;

        public SaveDataAsyncTask(Context context, String eventUid, List<HomeData> homeData) {
            this.context = context;
            this.eventUid = eventUid;
            this.homeData = homeData;
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            try {
                for (HomeData hm : homeData) {
                    TrackedEntityDataValueObjectRepository valueRepository =
                            Sdk.d2().trackedEntityModule().trackedEntityDataValues()
                                    .value(eventUid, hm.getId());

                    String currentValue = valueRepository.blockingExists() ?
                            valueRepository.blockingGet().value() : "";

                    valueRepository.blockingSet(hm.getName());
                    Log.e("TAG", "Form Field ***** Event Data Saved " + hm.getId() + " Value " + hm.getName());
                }


                return true;
            } catch (D2Error d2Error) {
                // Handle the error appropriately

                Log.e("TAG", "Form Field ***** Event Data Saved  Error" + d2Error.getMessage());
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean success) {
            super.onPostExecute(success);

            if (success) {
                Toast.makeText(context, "Saved Successfully", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(context, "Failed to Save Values", Toast.LENGTH_SHORT).show();
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
                .one()
                .blockingGet();
        Log.e("TAG", "Value we are looking for " + option);
        if (option != null) {
            return option.code();
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
//        new DataAttributeTask("notification").execute();
//        new DataAttributeTask("facility").execute();
//        new DataAttributeTask("notification").execute();
//        new DataAttributeTask("facility").execute();

            List<DataElement> flattenedList = programStageSections.stream()
                    .flatMap(section -> section.dataElements().stream())
                    .distinct()
                    .collect(Collectors.toList());
//            List<String> exclusionIds = Arrays.asList("OeUTTmDXAye", "QHprEcvWSQV", "TUR7b6PuifD");

            for (DataElement dataElement : flattenedList) {
//                if (!exclusionIds.contains(dataElement.uid())) {
                createSearchFieldsDataElement(binding.formLinearLayout, dataElement, retrieveCurrentValue(dataElement), extractElementAttributes(dataElement));
//                }
            }
        }
    }


    private List<ProgramResponse.AttributeValue> extractElementAttributes(DataElement currentElement) {
        List<ProgramResponse.AttributeValue> attributeValues = new ArrayList<>();
        String serverProgram = new FormatterClass().getSharedPref("facility_attribute_data", this);
        if (serverProgram != null) {
            try {
                JSONObject jsonObject = new JSONObject(serverProgram);
                JSONArray programsArray = jsonObject.getJSONArray("programs");
                for (int i = 0; i < programsArray.length(); i++) {
                    JSONObject program = programsArray.getJSONObject(i);
                    JSONArray programStages = program.getJSONArray("programStages");
                    for (int j = 0; j < programStages.length(); j++) {
                        JSONObject programStage = programStages.getJSONObject(j);
                        JSONArray programStageSections = programStage.getJSONArray("programStageSections");
                        for (int k = 0; k < programStageSections.length(); k++) {
                            JSONObject programStageSection = programStageSections.getJSONObject(k);
                            JSONArray dataElements = programStageSection.getJSONArray("dataElements");
                            for (int l = 0; l < dataElements.length(); l++) {
                                JSONObject dataElement = dataElements.getJSONObject(l);
                                String dataElementId = dataElement.getString("id");
                                JSONArray here = dataElement.getJSONArray("attributeValues");
                                if (dataElementId.equalsIgnoreCase(currentElement.uid())) {
                                    // You can add additional logic here based on your needs
                                    for (int q = 0; q < here.length(); q++) {
                                        JSONObject childHere = here.getJSONObject(q);
                                        String value = childHere.getString("value");
                                        JSONObject attribute = childHere.getJSONObject("attribute");
                                        String attributeId = attribute.getString("id");
                                        String attributeName = attribute.getString("name");
                                        ProgramResponse.Attribute at = new ProgramResponse.Attribute();
                                        at.id = attributeId;
                                        at.name = attributeName;
                                        ProgramResponse.AttributeValue atv = new ProgramResponse.AttributeValue();
                                        atv.value = value;
                                        atv.attribute = at;
                                        attributeValues.add(atv);
                                    }
                                }
                            }
                        }
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
                Log.e("TAG", "Server Attributes *** Error " + e.getMessage());
            }
        }
        return attributeValues;
    }

    private String retrieveCurrentValue(DataElement dataElement) {
        TrackedEntityDataValueObjectRepository valueRepository =
                Sdk.d2().trackedEntityModule().trackedEntityDataValues()
                        .value(eventUid, dataElement.uid());

        return valueRepository.blockingExists() ?
                valueRepository.blockingGet().value() : "";
    }

    private void createSearchFieldsDataElement(LinearLayout linearLayout, DataElement item, String currentValue,
                                               List<ProgramResponse.AttributeValue> attributeValueList) {
        String valueType = item.valueType().toString();
        String label = item.displayName();
        LayoutInflater inflater = LayoutInflater.from(this);
        boolean isHidden = confirmHiddenValues("Hidden", attributeValueList);
        boolean isDisabled = confirmHiddenValues("Disabled", attributeValueList);
        boolean isRequired = confirmHiddenValues("Required", attributeValueList);
        //exclude hidden values

        switch (valueType) {
            case "TEXT":
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

                    String name = item.displayName();
                    tvName.setText(name);
                    tvElement.setText(item.uid());
                    inputFieldMap.put(item.uid(), editText);
                    editText.setText(currentValue);
                    if (!isHidden) {
                        linearLayout.addView(itemView);
                    }
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

                    String name = item.displayName();
                    tvName.setText(name);
                    autoCompleteTextView.setAdapter(adp);
//                    String currentData = generateOptionNameFromId(currentValue);
//                    if (currentData != null) {
//                        autoCompleteTextView.setText(currentData, false);
//                    }
                    autoCompleteTextView.setText(currentValue, false);
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
                    if (!isHidden) {
                        linearLayout.addView(itemView);
                    }


                    /* */
                }
                break;
            case "DATE": {
                View itemView = inflater.inflate(
                        R.layout.item_date_edittext,
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

                if (!isHidden) {
                    linearLayout.addView(itemView);
                }
                break;
            }
            case "BOOLEAN": {
                View itemView = inflater.inflate(
                        R.layout.item_radio,
                        linearLayout,
                        false
                );
                TextView tvName = itemView.findViewById(R.id.tv_name);
                radioGroup = itemView.findViewById(R.id.radioGroup);
                RadioButton radioButtonYes = itemView.findViewById(R.id.radioButtonYes);
                RadioButton radioButtonNo = itemView.findViewById(R.id.radioButtonNo);
                String name = item.displayName();
                tvName.setText(name);

                if (currentValue != null && currentValue.equals("true")) {
                    radioGroup.check(R.id.radioButtonYes);
                } else if (currentValue != null && currentValue.equals("false")) {
                    radioGroup.check(R.id.radioButtonNo);
                } else {
                    radioGroup.clearCheck();
                }
                inputFieldMap.put(item.uid(), radioGroup);
                if (!isHidden) {
                    linearLayout.addView(itemView);
                }
                break;
            }
        }

    }

    private boolean confirmHiddenValues(String target, List<ProgramResponse.AttributeValue> attributeValueList) {

        boolean isHidden = false;
        if (attributeValueList.isEmpty()) isHidden = false;
        else {
            for (ProgramResponse.AttributeValue patr : attributeValueList) {
                ProgramResponse.Attribute data = patr.attribute;
                if (data.name.equalsIgnoreCase(target)) {
                    isHidden = patr.value.equalsIgnoreCase("true");
                }
            }
        }
        return isHidden;
    }

    private boolean hasRequiredAttribute(List<AttributeValue> attributeValues) {
        return attributeValues.stream()
                .anyMatch(attributeValue ->
                        attributeValue.attribute().name().equalsIgnoreCase("Required")
                                && attributeValue.value().equalsIgnoreCase("true"));
    }


    private String generateOptionNameFromId(String currentValue) {
        Option option = Sdk.d2().optionModule()
                .options()
                .byCode().eq(currentValue)
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