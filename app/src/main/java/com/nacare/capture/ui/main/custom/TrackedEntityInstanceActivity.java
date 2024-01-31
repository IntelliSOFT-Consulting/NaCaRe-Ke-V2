package com.nacare.capture.ui.main.custom;

import static android.text.TextUtils.isEmpty;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.nacare.capture.data.sync.EventTask;
import com.nacare.capture.R;
import com.nacare.capture.data.Sdk;
import com.nacare.capture.data.adapters.ExpandableListAdapter;
import com.nacare.capture.data.model.ExpandableItem;
import com.nacare.capture.data.model.FormatterClass;
import com.nacare.capture.data.service.DateFormatHelper;
import com.nacare.capture.ui.base.ListWithoutBindingsActivity;

import org.hisp.dhis.android.core.arch.call.D2Progress;
import org.hisp.dhis.android.core.dataelement.DataElement;
import org.hisp.dhis.android.core.enrollment.Enrollment;
import org.hisp.dhis.android.core.enrollment.EnrollmentCollectionRepository;
import org.hisp.dhis.android.core.event.Event;
import org.hisp.dhis.android.core.event.EventCollectionRepository;
import org.hisp.dhis.android.core.imports.TrackerImportConflict;
import org.hisp.dhis.android.core.option.Option;
import org.hisp.dhis.android.core.program.ProgramSection;
import org.hisp.dhis.android.core.program.ProgramStage;
import org.hisp.dhis.android.core.program.ProgramStageSection;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttribute;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttributeValue;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttributeValueObjectRepository;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityDataValue;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstance;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstanceCollectionRepository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class TrackedEntityInstanceActivity extends ListWithoutBindingsActivity {

    private enum IntentExtra {
        TEI_UID, PROGRAM_UID, OU_UID, NEW_USER, EVENT_UID
    }


    public static Intent getIntent(Context context,
                                   String eventUid,
                                   String teiUid,
                                   String programUid,
                                   String orgUnitUid, boolean isNew) {
        Intent intent = new Intent(context, TrackedEntityInstanceActivity.class);
        intent.putExtra(IntentExtra.TEI_UID.name(), teiUid);
        intent.putExtra(IntentExtra.PROGRAM_UID.name(), programUid);
        intent.putExtra(IntentExtra.OU_UID.name(), orgUnitUid);
        intent.putExtra(IntentExtra.NEW_USER.name(), isNew);
        intent.putExtra(IntentExtra.EVENT_UID.name(), eventUid);
        return intent;
    }

    private List<ProgramSection> programSectionList;
    private List<ProgramStageSection> programStageSections;
    private ProgramStage programStage;

    private String selectedTei, selectedProgram, selectedOrgUnit;
    private LinearLayout linearLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tracked_entity_instance);
        setUp(R.id.trackedEntityInstanceSearchToolbar, R.id.trackedEntityInstanceRecyclerView);
        RecyclerView recyclerView = findViewById(R.id.trackedEntityInstanceRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        linearLayout = findViewById(R.id.parentLinearLayout);
        selectedTei = getIntent().getStringExtra(IntentExtra.TEI_UID.name());
        selectedProgram = getIntent().getStringExtra(IntentExtra.PROGRAM_UID.name());
        selectedOrgUnit = getIntent().getStringExtra(IntentExtra.OU_UID.name());
        new FormatterClass().saveSharedPref("selectedTei", selectedTei, this);
        List<ExpandableItem> itemList = generateSampleData();
        ExpandableListAdapter adapter = new ExpandableListAdapter(itemList, this);
        recyclerView.setAdapter(adapter);
//        if (itemList.size() > 0) {
//            for (ExpandableItem item : itemList) {
//                createSectionItem(linearLayout, item);
//            }
//        }

        loadCurrentTrackedEntity();
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
                String eventUid = new FormatterClass().getSharedPref("tracked_event", this);
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

    private void createSectionItem(LinearLayout parentLinearLayout, ExpandableItem data) {
        LayoutInflater inflater = LayoutInflater.from(this);
        LinearLayout itemView = (LinearLayout) inflater.inflate(R.layout.list_layout_tracked, parentLinearLayout, false);
        TextView smallTextView = itemView.findViewById(R.id.smallTextView);
        TextView textViewName = itemView.findViewById(R.id.textViewName);
        LinearLayout linearLayout = itemView.findViewById(R.id.linearLayout);
        LinearLayout lnLinearLayout = itemView.findViewById(R.id.lnLinearLayout);
        ImageView rotationImageView = itemView.findViewById(R.id.rotationImageView);
        textViewName.setText(data.getGroupName());
        linearLayout.setVisibility(View.GONE);
        if (data.getChildItems() != null) {
            smallTextView.setText(countResponded(data.getChildItems()) + "/" + data.getChildItems().size());
            linearLayout.removeAllViews();
            for (TrackedEntityAttribute trackedEntityAttribute : data.getChildItems()) {
                createSearchFields(linearLayout, trackedEntityAttribute, extractCurrentAttributeValue(trackedEntityAttribute));
            }
        }
        if (data.getDataElements() != null) {
            smallTextView.setText(countAlreadyResponded(data.getProgramUid(), data.getProgramStageUid(), data.getSelectedOrgUnit(), data.getSelectedTei(), data.getDataElements()) + "/" + data.getDataElements().size());
            linearLayout.removeAllViews();
            for (DataElement dataElement : data.getDataElements()) {
                createSearchFieldsDataElement(linearLayout,
                        dataElement,
                        extractCurrentValue(data.getProgramUid(), data.getProgramStageUid(), data.getSelectedOrgUnit(),
                                data.getSelectedTei(), dataElement)
                );
            }
        }
        parentLinearLayout.addView(itemView);
        parentLinearLayout.setOnClickListener(v -> {
            if (linearLayout.getVisibility() == View.VISIBLE) {
                linearLayout.setVisibility(View.GONE);
            } else {
                // Hide all other inner layouts and show the clicked one
//                for (int j = 1; j <= totalSections; j++) {
//                    if (j != i) {
//                         lnLinearLayout.setVisibility(View.GONE);
//                    }
//                }
                linearLayout.setVisibility(View.VISIBLE);
            }

        });

    }

    private void createSearchFieldsDataElement(LinearLayout linearLayout, DataElement item, String value) {
        String valueType = item.valueType().toString();
        String label = item.displayName();
        LayoutInflater inflater = LayoutInflater.from(this);

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

                        }

                        @Override
                        public void afterTextChanged(Editable editable) {
                            String value = editable.toString();
                            String selectedTei = new FormatterClass().getSharedPref("selectedTei", TrackedEntityInstanceActivity.this);
                            if (selectedTei != null) {
                                TrackedEntityAttributeValueObjectRepository valueRepository = Sdk.d2().trackedEntityModule().trackedEntityAttributeValues().value(item.uid(), selectedTei);
                                String currentValue = valueRepository.blockingExists() ? valueRepository.blockingGet().value() : "";
                                if (currentValue == null) currentValue = "";

                                Log.e("TAG", "Current Value" + currentValue);

                                try {
                                    if (!isEmpty(value)) {
                                        valueRepository.blockingSet(value);
                                    }
//                                else {
//                                    valueRepository.blockingDeleteIfExist();
//                                }
                                } catch (Exception d2Error) {
                                    d2Error.printStackTrace();
                                    Log.e("TAG", "Response Saved Successfully with Error " + d2Error.getMessage());
                                } finally {
                                    Log.e("TAG", "Response Saved Successfully");
                                }
                            } else {
                                Log.e("TAG", "Response Saved Successfully No tracked Entity");
                            }
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
                    ArrayAdapter<String> adp = new ArrayAdapter<>(TrackedEntityInstanceActivity.this, android.R.layout.simple_list_item_1, optionsStringList);

                    tvName.setText(item.displayName());
                    autoCompleteTextView.setAdapter(adp);
                    if (value != null) {
                        autoCompleteTextView.setText(value, false);
                    }
                    adp.notifyDataSetChanged();
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
                    new DatePickerDialog(TrackedEntityInstanceActivity.this, (datePicker, year, month, day) -> {
                        String selectedTei = new FormatterClass().getSharedPref("selectedTei", TrackedEntityInstanceActivity.this);
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
                break;
            }
            case "BOOLEAN": {
                View itemView = inflater.inflate(
                        R.layout.item_radio,
                        linearLayout,
                        false
                );
                TextView tvName = itemView.findViewById(R.id.tv_name);
                RadioGroup radioGroup = itemView.findViewById(R.id.radioGroup);
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
            case "NUMBER": {
                LinearLayout itemView = (LinearLayout) inflater.inflate(R.layout.item_edittext_phone, linearLayout, false);

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

                    }

                    @Override
                    public void afterTextChanged(Editable editable) {
                        String value = editable.toString();
                        String selectedTei = new FormatterClass().getSharedPref("selectedTei", TrackedEntityInstanceActivity.this);
                        if (selectedTei != null) {
                            TrackedEntityAttributeValueObjectRepository valueRepository = Sdk.d2().trackedEntityModule().trackedEntityAttributeValues().value(item.uid(), selectedTei);
                            String currentValue = valueRepository.blockingExists() ? valueRepository.blockingGet().value() : "";
                            if (currentValue == null) currentValue = "";

                            Log.e("TAG", "Current Value" + currentValue);

                            try {
                                if (!isEmpty(value)) {
                                    valueRepository.blockingSet(value);
                                }
//                                else {
//                                    valueRepository.blockingDeleteIfExist();
//                                }
                            } catch (Exception d2Error) {
                                d2Error.printStackTrace();
                                Log.e("TAG", "Response Saved Successfully with Error " + d2Error.getMessage());
                            } finally {
                                Log.e("TAG", "Response Saved Successfully");
                            }
                        } else {
                            Log.e("TAG", "Response Saved Successfully No tracked Entity");
                        }
                    }
                });
                linearLayout.addView(itemView);
                break;
            }
        }


    }

    private void createSearchFields(LinearLayout linearLayout, TrackedEntityAttribute item, String value) {
        String valueType = item.valueType().toString();
        String label = item.displayName();
        LayoutInflater inflater = LayoutInflater.from(this);

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
                                String selectedTei = new FormatterClass().getSharedPref("selectedTei", TrackedEntityInstanceActivity.this);
                                if (selectedTei != null) {
                                    TrackedEntityAttributeValueObjectRepository valueRepository = Sdk.d2().trackedEntityModule().
                                            trackedEntityAttributeValues().value(item.uid(), selectedTei);
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
                                } else {
                                    Log.e("TAG", "Response Saved Successfully No tracked Entity");
                                }
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
                    ArrayAdapter<String> adp = new ArrayAdapter<>(TrackedEntityInstanceActivity.this, android.R.layout.simple_list_item_1, optionsStringList);

                    tvName.setText(item.displayName());
                    autoCompleteTextView.setAdapter(adp);
                    adp.notifyDataSetChanged();
//                    String currentData = generateOptionNameFromId(value);
//                    if (currentData != null) {
////                    autoCompleteTextView.setText(currentData, false);
//                    }
                    autoCompleteTextView.setText(value, false);
                    autoCompleteTextView.addTextChangedListener(new TextWatcher() {
                        @Override
                        public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {
                        }

                        @Override
                        public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                            String value = charSequence.toString();
                            if (!value.isEmpty()) {
                                Log.e("TAG", "Details Here *** " + item.uid());
                                Log.e("TAG", "Details Here *** " + item.displayName());
                                Log.e("TAG", "Details Here *** " + item.optionSet().uid());
                                Log.e("TAG", "Details Here *** " + value);
//                               SaveValueTask(item.uid(), item.displayName(), item.optionSet().uid(), value).execute();
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
                    new DatePickerDialog(TrackedEntityInstanceActivity.this, (datePicker, year, month, day) -> {
                        String selectedTei = new FormatterClass().getSharedPref("selectedTei", TrackedEntityInstanceActivity.this);
                        String valueCurrent = getDate(year, month, day);
                        editText.setText(valueCurrent);
//                    TrackedEntityAttributeValueObjectRepository valueRepository = Sdk.d2().trackedEntityModule().trackedEntityAttributeValues().value(item.uid(), selectedTei);
//                    String currentValue = valueRepository.blockingExists() ? valueRepository.blockingGet().value() : "";
//                    if (currentValue == null) currentValue = "";
//
//                    Log.e("TAG", "Current Value" + currentValue);
//                    String valueCurrent = getDate(year, month, day);
//
//                    try {
//                        if (!isEmpty(valueCurrent)) {
//                            valueRepository.blockingSet(valueCurrent);
//                        } else {
//                            valueRepository.blockingDeleteIfExist();
//                        }
//                    } catch (Exception d2Error) {
//                        d2Error.printStackTrace();
//                        Log.e("TAG", "Response Saved Successfully with Error " + d2Error.getMessage());
//                    } finally {
//                        Log.e("TAG", "Response Saved Successfully");
//                    }

                    }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
                });
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
                break;
            }
            case "BOOLEAN": {
                View itemView = inflater.inflate(
                        R.layout.item_radio,
                        linearLayout,
                        false
                );
                TextView tvName = itemView.findViewById(R.id.tv_name);
                RadioGroup radioGroup = itemView.findViewById(R.id.radioGroup);
                RadioButton radioButtonYes = itemView.findViewById(R.id.radioButtonYes);
                RadioButton radioButtonNo = itemView.findViewById(R.id.radioButtonNo);
                tvName.setText(item.displayName());
//
//            if (currentValue != null && currentValue.equals("true")) {
//                radioGroup.check(R.id.radioButtonYes);
//            } else if (currentValue != null && currentValue.equals("false")) {
//                radioGroup.check(R.id.radioButtonNo);
//            } else {
//                radioGroup.clearCheck();
//            }
//            inputFieldMap.put(item.uid(), radioGroup);
                linearLayout.addView(itemView);
                break;
            }
        }

    }

    private List<Option> generateOptionSets(String uid) {
        List<Option> optionsList;
        optionsList = Sdk.d2().optionModule().options().byOptionSetUid().eq(uid).blockingGet();
        return optionsList;
    }

    private String getSharedPref(String key) {
        return new FormatterClass().getSharedPref(key, this);
    }

    private String getDate(int year, int month, int day) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, day);
        Date date = calendar.getTime();
        return DateFormatHelper.formatSimpleDate(date);
    }

    private String extractCurrentAttributeValue(TrackedEntityAttribute trackedEntityAttribute) {
        String orgCode = getSharedPref("orgCode");
        String selectedTei = getSharedPref("selectedTei");
        String programUid = getSharedPref("programUid");
        try {
            TrackedEntityInstanceCollectionRepository teiRepository = Sdk.d2().trackedEntityModule().trackedEntityInstances().withTrackedEntityAttributeValues();

            List<String> programUids = new ArrayList<>();
            programUids.add(programUid);
            TrackedEntityInstance tei = teiRepository.byProgramUids(programUids)
                    .byUid().eq(selectedTei).one().blockingGet();
            if (tei != null) {
                return tei.trackedEntityAttributeValues().stream()
                        .filter(attributeValue -> attributeValue.trackedEntityAttribute().equals(trackedEntityAttribute.uid()))
                        .map(TrackedEntityAttributeValue::value)
                        .findFirst()
                        .orElse(null);
            }
        } catch (Exception e) {
            return null;
        }

        return null;
    }

    private String countResponded(List<TrackedEntityAttribute> trackedEntityAttributes) {
        Integer counter = 0;

        try {
            for (TrackedEntityAttribute trackedEntityAttribute : trackedEntityAttributes) {
                String value = extractCurrentAttributeValue(trackedEntityAttribute);
                if (value != null) {
                    counter++;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "" + counter;
    }

    private String countAlreadyResponded(String programUid, String programStageUid, String selectedOrgUnit, String selectedTei, List<DataElement> dataElements) {
        Integer counter = 0;
        try {
            for (DataElement dt : dataElements) {
                String value = extractCurrentValue(programUid, programStageUid, selectedOrgUnit,
                        selectedTei, dt);
                if (value != null) {
                    counter++;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "" + counter;
    }

    private String extractCurrentValue(String programUid, String programStageUid, String orgUnit, String selectedTei, DataElement dataElement) {
        /**
         * Get the event based on the organization and program stage**/
        List<String> uids = new ArrayList<>();
        uids.add(selectedTei);

        EventCollectionRepository eventRepository =
                Sdk.d2().eventModule().events().withTrackedEntityDataValues();
        Event event = eventRepository
                .withTrackedEntityDataValues()
                .byProgramUid().eq(programUid)
                .byProgramStageUid().eq(programStageUid)
                .byOrganisationUnitUid().eq(orgUnit)
                .byTrackedEntityInstanceUids(uids)
                .one()
                .blockingGet();

        if (event != null) {
            if (event.trackedEntityDataValues().size() > 0) {
                Optional<String> result = event.trackedEntityDataValues()
                        .stream()
                        .filter(dataValue -> dataValue.dataElement().equalsIgnoreCase(dataElement.uid()))
                        .map(TrackedEntityDataValue::value)
                        .findFirst();
                String value = result.orElse(null);

                Log.e("TAG", "Value for the Current Data Event " + value);

                return value;

            }
            return "";
        }
        return "";
    }

    private void loadCurrentTrackedEntity() {
        Log.e("TAG", "Selected Program " + selectedProgram);
        Log.e("TAG", "Selected Org Unit " + selectedOrgUnit);
        Log.e("TAG", "Selected Tei " + selectedTei);

        /**
         * Get the current enrollemr linked to the tracked entity**/

        EnrollmentCollectionRepository enrollmentCollectionRepository = Sdk.d2().enrollmentModule().enrollments();
        Enrollment enrollment = enrollmentCollectionRepository
                .byProgram().eq(selectedProgram)
                .byOrganisationUnit().eq(selectedOrgUnit)
                .byTrackedEntityInstance().eq(selectedTei)
                .one()
                .blockingGet();

        Log.e("TAG", "Tracked Entity Enrollment" + enrollment);

        if (enrollment != null) {
            new EventTask(this, enrollment, selectedProgram, selectedOrgUnit).execute();
        } else {
            System.out.println("No enrollments found for the specified criteria.");
            Log.e("TAG", "");
        }

    }

    private List<ExpandableItem> generateSampleData() {
        try {

            List<ExpandableItem> itemList = new ArrayList<>();

            List<TrackedEntityAttribute> trackedEntityAttributes = new ArrayList<>();
            String programUid = new FormatterClass().getSharedPref("programUid", this);
            if (programUid != null) {
                programSectionList = Sdk.d2().programModule()
                        .programSections()
                        .withAttributes()
                        .byProgramUid()
                        .eq(programUid)
                        .blockingGet();
                List<TrackedEntityAttribute> flattenedList = programSectionList.stream()
                        .flatMap(section -> section.attributes().stream()) // Replace getYourNestedList() with the actual method to retrieve nested list
                        .distinct()
                        .collect(Collectors.toList());

                for (TrackedEntityAttribute programSection : flattenedList) {
                    if (shouldAddAttribute(programSection)) {
                        trackedEntityAttributes.add(programSection);
                    }
                }
                itemList.add(new ExpandableItem(programUid, selectedOrgUnit, selectedTei, "registration", "Patient Details and Cancer Information", trackedEntityAttributes, null));
                programStage = Sdk.d2().programModule()
                        .programStages()
                        .byProgramUid()
                        .eq(programUid)
                        .one()
                        .blockingGet();
                if (programStage != null) {
                    programStageSections = Sdk.d2().programModule()
                            .programStageSections()
                            .withDataElements()
                            .byProgramStageUid().eq(programStage.uid())
                            .blockingGet();

                    for (ProgramStageSection programStageSection : programStageSections) {
                        itemList.add(new ExpandableItem(programUid, selectedOrgUnit, selectedTei, programStage.uid(), programStageSection.displayName(), null, programStageSection.dataElements()));
                    }
                }

            }

            return itemList;
        } catch (Exception e) {
            return null;
        }
    }

    private boolean shouldAddAttribute(TrackedEntityAttribute programSection) {
        List<String> keywords = Arrays.asList("MiXrdHDZ6Hw", "yIp9UZ1Bex6", "RhplKXZoKsC", "wzHl7HdsSlO", "OSs8D8u1El7", "HEoJiJqgPh1",
                "k5cjujLd0nd", "ghOKiyhlPX0", "BzhDnF5fG4x", "Lhoe9ecBhZi", "AyuVgasCLyM", "vPICBz6JEmK", "xxEsZFtua8N");
        Optional<String> matchingKeyword = keywords.stream()
                .filter(keyword -> programSection.uid().contains(keyword))
                .findFirst();
        return matchingKeyword.isPresent();

    }
}