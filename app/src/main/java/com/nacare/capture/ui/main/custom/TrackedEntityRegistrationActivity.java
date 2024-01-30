package com.nacare.capture.ui.main.custom;

import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.nacare.capture.R;
import com.nacare.capture.data.Sdk;
import com.nacare.capture.data.adapters.ExpandableListAdapter;
import com.nacare.capture.data.model.FormatterClass;
import com.nacare.capture.data.model.HomeData;
import com.nacare.capture.data.service.ActivityStarter;
import com.nacare.capture.data.service.DateFormatHelper;
import com.nacare.capture.ui.base.ListWithoutBindingsActivity;

import org.hisp.dhis.android.core.enrollment.EnrollmentCreateProjection;
import org.hisp.dhis.android.core.enrollment.EnrollmentObjectRepository;
import org.hisp.dhis.android.core.event.EventCreateProjection;
import org.hisp.dhis.android.core.event.EventObjectRepository;
import org.hisp.dhis.android.core.maintenance.D2Error;
import org.hisp.dhis.android.core.option.Option;
import org.hisp.dhis.android.core.program.ProgramSection;
import org.hisp.dhis.android.core.program.ProgramStage;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttribute;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttributeValue;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttributeValueObjectRepository;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityDataValue;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityDataValueCollectionRepository;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityDataValueObjectRepository;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstance;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstanceCollectionRepository;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class TrackedEntityRegistrationActivity extends ListWithoutBindingsActivity {
    private enum IntentExtra {
        SEARCH_FIELDS, TEI_UID, PROGRAM_UID, OU_UID, NEW
    }

    List<TrackedEntityAttribute> trackedEntityAttributes = new ArrayList<>();
    private RadioGroup radioGroup;
    private ProgressBar syncProgressBar;

    public static Intent getIntent(Context context, String teiUid, String programUid, String orgUid,
                                   ArrayList<HomeData> collectedInputs, String isNew) {
        Intent intent = new Intent(context, TrackedEntityRegistrationActivity.class);
        intent.putExtra(IntentExtra.TEI_UID.name(), teiUid);
        intent.putExtra(IntentExtra.PROGRAM_UID.name(), programUid);
        intent.putExtra(IntentExtra.OU_UID.name(), orgUid);
        intent.putExtra(IntentExtra.SEARCH_FIELDS.name(), collectedInputs);
        intent.putExtra(IntentExtra.NEW.name(), isNew);
        return intent;
    }

    private String selectedProgram, selectedEntity, selectedOrganization;
    private List<ProgramSection> programSectionList;
    String isNew;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tracked_entity_registration);
        setUp(R.id.trackedEntityInstanceSearchToolbar, R.id.trackedEntityInstanceRecyclerView);
        RecyclerView recyclerView = findViewById(R.id.trackedEntityInstanceRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        syncProgressBar = findViewById(R.id.syncProgressBar);
        syncProgressBar.setVisibility(View.GONE);
        isNew = getIntent().getStringExtra(IntentExtra.NEW.name());
        selectedEntity = getIntent().getStringExtra(IntentExtra.TEI_UID.name());
        selectedProgram = getIntent().getStringExtra(IntentExtra.PROGRAM_UID.name());
        selectedOrganization = getIntent().getStringExtra(IntentExtra.OU_UID.name());
        List<HomeData> homeData = (ArrayList<HomeData>) getIntent().getSerializableExtra(IntentExtra.SEARCH_FIELDS.name());

        if (isNew != null) {
            if (isNew.equalsIgnoreCase("true")) {
                /**
                 * Create a new Enrollment Here
                 * */

                String enrollmentUid = null;
                try {
                    enrollmentUid = Sdk.d2().enrollmentModule().enrollments().blockingAdd(
                            EnrollmentCreateProjection.builder()
                                    .organisationUnit(selectedOrganization)
                                    .program(selectedProgram)
                                    .trackedEntityInstance(selectedEntity)
                                    .build()
                    );
                    EnrollmentObjectRepository enrollmentRepository = Sdk.d2().enrollmentModule()
                            .enrollments()
                            .uid(enrollmentUid);
                    enrollmentRepository.setEnrollmentDate(new FormatterClass().getNowWithoutTime());
                    enrollmentRepository.setIncidentDate(new FormatterClass().getNowWithoutTime());


                    // Create an Empty Event to hold the responses
                    ProgramStage stage = Sdk.d2().programModule().programStages()
                            .byProgramUid().eq(selectedProgram)
                            .byName().like("ALL")
                            .one().blockingGet();
                    if (stage != null) {
//                        String eventUid = Sdk.d2().eventModule().events()
//                                .blockingAdd(
//                                        EventCreateProjection.builder()
//                                                .organisationUnit(selectedOrganization)
//                                                .program(selectedProgram)
//                                                .programStage(stage.uid())
//                                                .enrollment(enrollmentUid)
//                                                .build()
//                                );
//
//
//                        EventObjectRepository eventObjectRepository = Sdk.d2().eventModule().events().uid(eventUid);
//                        eventObjectRepository.setEventDate(new FormatterClass().getNowWithoutTime());

                        for (HomeData hm : homeData) {
                            createAttributes(selectedEntity, hm);
                        }

                        programSectionList = Sdk.d2().programModule()
                                .programSections()
                                .withAttributes()
                                .byProgramUid()
                                .eq(selectedProgram)
                                .blockingGet();
                        List<TrackedEntityAttribute> flattenedList = programSectionList.stream()
                                .flatMap(section -> section.attributes().stream()) // Replace getYourNestedList() with the actual method to retrieve nested list
                                .distinct()
                                .collect(Collectors.toList());
                        List<String> exclusionIds = Arrays.asList("AP13g7NcBOf", "hQSBZptmMp2", "xj0DQtTqeJc", "o2i9nPNWdjV");

                        for (TrackedEntityAttribute tr : flattenedList) {
                            if (!exclusionIds.contains(tr.uid())) {
                                trackedEntityAttributes.add(tr);
                            }
                        }
                        for (TrackedEntityAttribute trackedEntityAttribute : trackedEntityAttributes) {
                            createInputField(trackedEntityAttribute, extractCurrentAttributeValue(trackedEntityAttribute.uid()));
                        }
                    } else {
                        Toast.makeText(this, "Please select a program stage to proceed", Toast.LENGTH_SHORT).show();
                    }
                } catch (D2Error e) {
                    Log.e("Error", "Error creating enrollment" + e.getMessage());
                }

            } else {

                programSectionList = Sdk.d2().programModule()
                        .programSections()
                        .withAttributes()
                        .byProgramUid()
                        .eq(selectedProgram)
                        .blockingGet();
                List<TrackedEntityAttribute> flattenedList = programSectionList.stream()
                        .flatMap(section -> section.attributes().stream()) // Replace getYourNestedList() with the actual method to retrieve nested list
                        .distinct()
                        .collect(Collectors.toList());
                List<String> exclusionIds = Arrays.asList("AP13g7NcBOf", "hQSBZptmMp2", "xj0DQtTqeJc", "o2i9nPNWdjV");

                for (TrackedEntityAttribute tr : flattenedList) {
                    if (!exclusionIds.contains(tr.uid())) {
                        trackedEntityAttributes.add(tr);
                    }
                }
                for (TrackedEntityAttribute trackedEntityAttribute : trackedEntityAttributes) {
                    createInputField(trackedEntityAttribute, extractCurrentAttributeValue(trackedEntityAttribute.uid()));
                }
            }
        }


    }

    private String extractCurrentAttributeValue(String teiUid) {

        try {
            TrackedEntityInstanceCollectionRepository teiRepository = Sdk.d2().trackedEntityModule()
                    .trackedEntityInstances()
                    .withTrackedEntityAttributeValues();

            List<String> programUids = new ArrayList<>();
            programUids.add(selectedProgram);
            TrackedEntityInstance tei = teiRepository.byProgramUids(programUids)
                    .byUid().eq(selectedEntity).one().blockingGet();
            if (tei != null) {
                return tei.trackedEntityAttributeValues().stream()
                        .filter(attributeValue -> attributeValue.trackedEntityAttribute().equals(teiUid))
                        .map(TrackedEntityAttributeValue::value)
                        .findFirst()
                        .orElse(null);
            }
        } catch (Exception e) {
            return null;
        }

        return null;
    }

    private String getDate(int year, int month, int day) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, day);
        Date date = calendar.getTime();
        return DateFormatHelper.formatCurrentDate(date);
    }

    private List<Option> generateOptionSets(String uid) {
        List<Option> optionsList;
        optionsList = Sdk.d2().optionModule().options().byOptionSetUid().eq(uid).blockingGet();
        return optionsList;
    }

    private String getCodeForItem(String uid, String displayName) {
        Option option = Sdk.d2().optionModule().
                options().
                byOptionSetUid().eq(uid)
                .byDisplayName().eq(displayName)
                .one()
                .blockingGet();
        if (option != null) {
            return option.code();
        }
        return null;
    }

    private void createInputField(TrackedEntityAttribute item, String value) {
        String valueType = item.valueType().toString();
        String label = item.displayName();
        LayoutInflater inflater = LayoutInflater.from(this);
        LinearLayout linearLayout = findViewById(R.id.linearLayout);


        switch (valueType) {
            case "TEXT":
                if (item.optionSet() == null) {
                    LinearLayout itemView = (LinearLayout) inflater.inflate(R.layout.item_edittext, linearLayout, false);

                    TextView tvName = itemView.findViewById(R.id.tv_name);
                    TextView tvElement = itemView.findViewById(R.id.tv_element);
                    TextInputLayout textInputLayout = itemView.findViewById(R.id.textInputLayout);
                    TextInputEditText editText = itemView.findViewById(R.id.editText);

                    tvName.setText(item.displayName());
                    tvElement.setText(item.uid());
                    editText.setText(value);
                    editText.addTextChangedListener(new TextWatcher() {
                        @Override
                        public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {
                        }

                        @Override
                        public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                            String value = charSequence.toString();
                            if (!value.isEmpty()) {
                                saveFormField(item.uid(), value);
                            }
                        }

                        @Override
                        public void afterTextChanged(Editable editable) {

                        }
                    });
                    linearLayout.addView(itemView);
                } else {
                    LinearLayout itemView = (LinearLayout) inflater.inflate(R.layout.item_autocomplete, linearLayout, false);


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
                    ArrayAdapter<String> adp = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, optionsStringList);

                    tvName.setText(item.displayName());
                    autoCompleteTextView.setAdapter(adp);
                    adp.notifyDataSetChanged();

                    autoCompleteTextView.setText(value, false);
                    autoCompleteTextView.addTextChangedListener(new TextWatcher() {
                        @Override
                        public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {
                        }

                        @Override
                        public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                            String value = charSequence.toString();
                            if (!value.isEmpty()) {
                                new saveOptionTask(value, item.optionSet().uid(), item.uid()).execute();

                            }
                        }

                        @Override
                        public void afterTextChanged(Editable editable) {

                        }
                    });
                    linearLayout.addView(itemView);
                }
                break;
            case "DATE": {
                View itemView = inflater.inflate(R.layout.item_date_edittext, linearLayout, false);
                TextView tvName = itemView.findViewById(R.id.tv_name);
                TextView tvElement = itemView.findViewById(R.id.tv_element);
                TextInputLayout textInputLayout = itemView.findViewById(R.id.textInputLayout);
                TextInputEditText editText = itemView.findViewById(R.id.editText);
                tvName.setText(item.displayName());
                tvElement.setText(item.uid());
                List<String> keywords = Arrays.asList("Birth", "Death");
                editText.setKeyListener(null);
                editText.setCursorVisible(false);
                editText.setFocusable(false);
                if (value != null) {
                    value = new FormatterClass().extractValid(value);
                    editText.setText(value);
                }
                editText.setOnClickListener(v -> {
                    Calendar calendar = Calendar.getInstance();
                    new DatePickerDialog(this, (datePicker, year, month, day) -> {
                        String valueCurrent = getDate(year, month, day);
                        editText.setText(valueCurrent);

                    }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
                });
                editText.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                        // This method is called before the text is changed.
                    }

                    @Override
                    public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                        String value = charSequence.toString();
                        if (!value.isEmpty()) {
                            saveFormField(item.uid(), value);
                        }
                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                        // This method is called after the text has changed.
                        // You can perform actions here based on the updated text.
                    }
                });
                linearLayout.addView(itemView);
                break;
            }
            case "INTEGER":
            case "NUMBER": {
                View itemView = inflater.inflate(R.layout.item_edittext_number, linearLayout, false);
                TextView tvName = itemView.findViewById(R.id.tv_name);
                TextView tvElement = itemView.findViewById(R.id.tv_element);
                TextInputLayout textInputLayout = itemView.findViewById(R.id.textInputLayout);
                TextInputEditText editText = itemView.findViewById(R.id.editText);
                tvName.setText(item.displayName());
                tvElement.setText(item.uid());
                if (value != null) {
                    editText.setText(value);
                }

                editText.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                        // This method is called before the text is changed.
                    }

                    @Override
                    public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                        String value = charSequence.toString();
                        if (!value.isEmpty()) {
                            saveFormField(item.uid(), value);
                        }
                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                        // This method is called after the text has changed.
                        // You can perform actions here based on the updated text.
                    }
                });
                linearLayout.addView(itemView);
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
                tvName.setText(item.displayName());
                if (value != null && value.equals("true")) {
                    radioGroup.check(R.id.radioButtonYes);
                } else if (value != null && value.equals("false")) {
                    radioGroup.check(R.id.radioButtonNo);
                } else {
                    radioGroup.clearCheck();
                }
                linearLayout.addView(itemView);
                break;
            }
        }
    }

    private void saveFormField(String attributeUid, String value) {
        TrackedEntityAttributeValueObjectRepository valueRepository = Sdk.d2().trackedEntityModule().
                trackedEntityAttributeValues().value(attributeUid, selectedEntity);
        String currentValue = valueRepository.blockingExists() ? valueRepository.blockingGet().value() : "";
        if (currentValue == null) currentValue = "";

        try {
            valueRepository.blockingSet(value);

        } catch (Exception d2Error) {
            d2Error.printStackTrace();
            Log.e("TAG", "Response Saved Successfully with Error " + d2Error.getMessage());
        } finally {
            Log.e("TAG", "Response Saved Successfully");
        }
    }

    private void createAttributes(String selectedEntity, HomeData homeData) {
        try {
            TrackedEntityAttributeValueObjectRepository valueRepository =
                    Sdk.d2().trackedEntityModule().trackedEntityAttributeValues()
                            .value(homeData.getId(), selectedEntity);
            valueRepository.blockingSet(homeData.getName());
            Log.e("TAG", "Saving Attribute **** Success");
        } catch (D2Error d2Error) {
            d2Error.printStackTrace();
            Log.e("TAG", "Saving Attribute **** Error" + d2Error.getMessage());
        }
    }

    @Override
    public void onBackPressed() {
        // Create an AlertDialog.Builder
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Please Confirm");
        builder.setMessage("Are you sure you want to exit the registration process?\nYour progress will be saved");
        builder.setPositiveButton("Yes", (dialog, which) -> {

            if (isNew != null) {
                if (isNew.equalsIgnoreCase("true")) {
                    manipulateUserInputForUnique();
                } else {
                    TrackedEntityRegistrationActivity.super.onBackPressed();
                }
            } else {
                TrackedEntityRegistrationActivity.super.onBackPressed();
            }
        });

        builder.setNegativeButton("No", (dialog, which) -> {
            dialog.dismiss();
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void manipulateUserInputForUnique() {

        try {
            String firstname = extractCurrentAttributeValue("R1vaUuILrDy");
            String lastname = extractCurrentAttributeValue("hzVijy6tEUF");
            String dob = extractCurrentAttributeValue("mPpjmOxwsEZ");
            String month = extractDesiredValue(dob, "MM");
            String year = extractDesiredValue(dob, "yyyy");
            if (firstname != null && firstname.length() > 3) {
                firstname = firstname.substring(0, 3).toUpperCase();
            } else {
                firstname = firstname.toUpperCase();
            }
            if (lastname != null && lastname.length() > 3) {
                lastname = lastname.substring(0, 3).toUpperCase();
            } else {
                lastname = lastname.toUpperCase();
            }

            if (month != null && year != null) {

                String patient_identification = firstname + "-" + lastname + "-" + month + "-" + year;
                saveFormField("AP13g7NcBOf", patient_identification);
                TrackedEntityRegistrationActivity.super.onBackPressed();

            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("TAG", "Error Generating the Unique ID ****" + e.getMessage());
        }
    }

    private String extractDesiredValue(String dateString, String format) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
        String data = null;
        try {
            Date date = dateFormat.parse(dateString);
            SimpleDateFormat desireFormat = new SimpleDateFormat(format);
            data = desireFormat.format(date);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return data;
    }

    private class saveOptionTask extends AsyncTask<Void, Void, String> {
        private String value, optionUid, attributeUid;


        saveOptionTask(String value, String optionUid, String attributeUid) {
            this.value = value;
            this.optionUid = optionUid;
            this.attributeUid = attributeUid;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            syncProgressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected String doInBackground(Void... params) {

            String dataValue = null;
            Option option = Sdk.d2().optionModule().
                    options().
                    byOptionSetUid().eq(optionUid)
                    .byDisplayName().eq(value)
                    .one()
                    .blockingGet();
            if (option != null) {
                dataValue = option.code();
            }
            Log.e("TAG", "Response Saved ** Data Value Obtained " + dataValue);
            String message = "Data Saved";
            if (dataValue != null) {
                TrackedEntityAttributeValueObjectRepository valueRepository = Sdk.d2().trackedEntityModule().
                        trackedEntityAttributeValues().value(attributeUid, selectedEntity);
                try {
                    valueRepository.blockingSet(dataValue);
                    Log.e("TAG", "Response Saved ** Data Value Obtained " + dataValue);
                } catch (Exception e) {
                    Log.e("TAG", "Response Saved ** Data Value Error " + e.getMessage());
                }


            }
            return message;
        }

        @Override
        protected void onPostExecute(String result) {
            syncProgressBar.setVisibility(View.GONE);
            if (result != null) {
                Log.e("TAG", "Response Saved ****  " + result);
            }
        }
    }
}