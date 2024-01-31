package com.nacare.capture.data.adapters;


import static android.text.TextUtils.isEmpty;

import android.app.DatePickerDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.nacare.capture.R;
import com.nacare.capture.data.Sdk;
import com.nacare.capture.data.model.ExpandableItem;
import com.nacare.capture.data.model.FormatterClass;
import com.nacare.capture.data.service.DateFormatHelper;
import com.nacare.capture.data.sync.StringValueTask;
import com.nacare.capture.ui.main.custom.TrackedEntityInstanceActivity;

import org.hisp.dhis.android.core.dataelement.DataElement;
import org.hisp.dhis.android.core.event.Event;
import org.hisp.dhis.android.core.event.EventCollectionRepository;
import org.hisp.dhis.android.core.option.Option;
import org.hisp.dhis.android.core.program.ProgramTrackedEntityAttribute;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttribute;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttributeValue;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttributeValueObjectRepository;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityDataValue;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityDataValueObjectRepository;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstance;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstanceCollectionRepository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Optional;

public class ExpandableListAdapter extends RecyclerView.Adapter<ExpandableListAdapter.PersonViewHolder> {


    private List<ExpandableItem> personList;
    private Context context;
    private RadioGroup radioGroup;

    private static int currentPosition = 0;

    public ExpandableListAdapter(List<ExpandableItem> personList, Context context) {
        this.personList = personList;
        this.context = context;
    }

    @Override
    public PersonViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_layout_tracked, parent, false);
        return new PersonViewHolder(v);
    }


    @Override
    public void onBindViewHolder(final PersonViewHolder holder, final int position) {
        ExpandableItem data = personList.get(position);
        holder.textViewName.setText(data.getGroupName());
        holder.linearLayout.setVisibility(View.GONE);
        if (currentPosition == position) {
            holder.lnLinearLayout.setBackgroundColor(ContextCompat.getColor(context, R.color.selected));
            holder.rotationImageView.setRotation(0);
            holder.linearLayout.setVisibility(View.VISIBLE);
        } else {
            holder.linearLayout.setVisibility(View.GONE);
        }
        holder.itemView.setOnClickListener(view -> {
            currentPosition = position;
            notifyDataSetChanged();
        });
        if (data.getChildItems() != null) {
            holder.smallTextView.setText(countResponded(data.getChildItems()) + "/" + data.getChildItems().size());
            holder.linearLayout.removeAllViews();
            for (TrackedEntityAttribute trackedEntityAttribute : data.getChildItems()) {
                createSearchFields(holder.linearLayout, trackedEntityAttribute, extractCurrentAttributeValue(trackedEntityAttribute));
            }
        }
        if (data.getDataElements() != null) {
            holder.smallTextView.setText(countAlreadyResponded(data.getProgramUid(), data.getProgramStageUid(), data.getSelectedOrgUnit(), data.getSelectedTei(), data.getDataElements()) + "/" + data.getDataElements().size());
            holder.linearLayout.removeAllViews();
            for (DataElement dataElement : data.getDataElements()) {
                createSearchFieldsDataElement(holder.linearLayout,
                        dataElement,
                        extractCurrentValue(data.getProgramUid(), data.getProgramStageUid(), data.getSelectedOrgUnit(),
                                data.getSelectedTei(), dataElement)
                );
            }
        }
        LayoutInflater inflater = LayoutInflater.from(context);
        LinearLayout itemView = (LinearLayout) inflater.inflate(R.layout.item_submit_cancel, holder.linearLayout, false);
        holder.linearLayout.addView(itemView);
        MaterialButton submitButton = itemView.findViewById(R.id.btn_proceed);
        submitButton.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            LayoutInflater inflater2 = LayoutInflater.from(context);
            View customView = inflater2.inflate(R.layout.custom_layout_confirm, null);
            builder.setView(customView);
            AlertDialog alertDialog = builder.create();
            TextView tvTitle = customView.findViewById(R.id.tv_title);
            TextView tvMessage = customView.findViewById(R.id.tv_message);
            MaterialButton noButton = customView.findViewById(R.id.no_button);
            MaterialButton yesButton = customView.findViewById(R.id.yes_button);
            tvTitle.setText(R.string.submit_form);
            tvMessage.setText("Are you sure you want to save?\nYou will npt be able to edit this patient info once saved");

            yesButton.setOnClickListener(c -> {
                alertDialog.dismiss();
            });
            noButton.setOnClickListener(c -> {
                alertDialog.dismiss();
            });

            alertDialog.show();
        });
        MaterialButton nextButton = itemView.findViewById(R.id.btn_cancel);
        nextButton.setOnClickListener(v -> {

        });


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

    private String getSharedPref(String key) {
        return new FormatterClass().getSharedPref(key, context);
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

    private String valueAt(List<TrackedEntityAttributeValue> values, String attributeUid) {
        for (TrackedEntityAttributeValue attributeValue : values) {
            if (attributeValue.trackedEntityAttribute().equals(attributeUid)) {
                return attributeValue.value();
            }
        }

        return null;
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

    private void createSearchFieldsDataElement(LinearLayout linearLayout, DataElement item, String value) {
        String valueType = item.valueType().toString();
        String label = item.displayName();
        LayoutInflater inflater = LayoutInflater.from(context);

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
                        public void onTextChanged(CharSequence s, int start, int before, int count) {
                            String value = s.toString();
                            if (!value.isEmpty()) {
                                new StringValueTask(context, item.uid(), value,false).execute();
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
                    ArrayAdapter<String> adp = new ArrayAdapter<>(context, android.R.layout.simple_list_item_1, optionsStringList);

                    tvName.setText(item.displayName());
                    autoCompleteTextView.setAdapter(adp);
                    adp.notifyDataSetChanged();
                    String currentData = generateOptionNameFromId(value);
                    if (currentData != null) {
//                    autoCompleteTextView.setText(currentData, false);
                    }
                    autoCompleteTextView.setText(value, false);
                    autoCompleteTextView.addTextChangedListener(new TextWatcher() {
                        @Override
                        public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {
                        }

                        @Override
                        public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                            String value = charSequence.toString();
                            if (!value.isEmpty()) {
                                new SaveValueTask(item.uid(), item.displayName(), item.optionSet().uid(), value).execute();
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
                    new DatePickerDialog(context, (datePicker, year, month, day) -> {
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
                        String value = s.toString();
                        if (!value.isEmpty()) {
                            new StringValueTask(context, item.uid(), value,false).execute();
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
                radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(RadioGroup group, int checkedId) {
                        String value = null;
                        switch (checkedId) {
                            case R.id.radioButtonYes:
                                value = "true";
                                break;
                            case R.id.radioButtonNo:
                                value = "false";
                                break;
                        }
                        if (value != null) {
                            new StringValueTask(context, item.uid(), value,false).execute();
                        }
                    }
                });


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
                        String value = charSequence.toString();
                        if (!value.isEmpty()) {
                            new StringValueTask(context, item.uid(), value,false).execute();
                        }
                    }

                    @Override
                    public void afterTextChanged(Editable editable) {

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
        LayoutInflater inflater = LayoutInflater.from(context);

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
//                stringMap.put(item.uid(), editText);
                    controlInputAppearance(item.uid(), editText);
                    editText.setText(value);
                    editText.addTextChangedListener(new TextWatcher() {
                        @Override
                        public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {
                        }

                        @Override
                        public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                            String value = charSequence.toString();
                            if (!value.isEmpty()) {
                                new StringValueTask(context, item.uid(), value, true).execute();
                            }
                        }

                        @Override
                        public void afterTextChanged(Editable editable) {

                        }
                    });
                    linearLayout.addView(itemView);
                }
                else {
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
                    ArrayAdapter<String> adp = new ArrayAdapter<>(context, android.R.layout.simple_list_item_1, optionsStringList);

                    tvName.setText(item.displayName());
                    autoCompleteTextView.setAdapter(adp);
                    adp.notifyDataSetChanged();
                    String currentData = generateOptionNameFromId(value);
                    if (currentData != null) {
//                    autoCompleteTextView.setText(currentData, false);
                    }
                    autoCompleteTextView.setText(value, false);
                    autoCompleteTextView.addTextChangedListener(new TextWatcher() {
                        @Override
                        public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {
                        }

                        @Override
                        public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                            String value = charSequence.toString();
                            if (!value.isEmpty()) {
                                new SaveValueTask(item.uid(), item.displayName(), item.optionSet().uid(), value).execute();
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
                    new DatePickerDialog(context, (datePicker, year, month, day) -> {
                        String selectedTei = new FormatterClass().getSharedPref("selectedTei", context);
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
                        String value = s.toString();
                        if (!value.isEmpty()) {
                            new StringValueTask(context, item.uid(), value, true).execute();
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
//
                if (value != null && value.equals("true")) {
                    radioGroup.check(R.id.radioButtonYes);
                } else if (value != null && value.equals("false")) {
                    radioGroup.check(R.id.radioButtonNo);
                } else {
                    radioGroup.clearCheck();
                }
                radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(RadioGroup group, int checkedId) {
                        // checkedId is the RadioButton ID that is checked in the RadioGroup
                        String value = null;
                        switch (checkedId) {
                            case R.id.radioButtonYes:
                                value = "true";
                                break;
                            case R.id.radioButtonNo:
                                value = "false";
                                break;
                        }
                        if (value != null) {
                            new StringValueTask(context, item.uid(), value, true).execute();
                        }
                    }
                });

                linearLayout.addView(itemView);
                break;
            }
        }

    }

    private void controlInputAppearance(String uid, TextInputEditText editText) {
        try {
            ProgramTrackedEntityAttribute tei = Sdk.d2().programModule().programTrackedEntityAttributes()
                    .byUid().eq(uid)
                    .one().blockingGet();
          /*  if (tei != null) {
                tei.id();
                tei.
                t
            }*/
        } catch (Exception e) {

        }
    }

    private String extractOptionUid(String name, String uid, String value) {
        List<String> keywords = Arrays.asList("Sex", "Site", "Type of facility", "County of Usual Residence", "Insurance Cover");
        Option option = Sdk.d2().optionModule().options().byOptionSetUid().eq(uid).byDisplayName().eq(value).one().blockingGet();
        if (option != null) {
            Optional<String> matchingKeyword = keywords.stream()
                    .filter(keyword -> name.contains(keyword))
                    .findFirst();

            return matchingKeyword.map(keyword -> option.code()).orElseGet(() -> option.code());

        } else return null;
    }

    private String getDate(int year, int month, int day) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, day);
        Date date = calendar.getTime();
        return DateFormatHelper.formatCurrentDate(date);
    }

    private String generateOptionNameFromId(String currentValue) {
        Option option = Sdk.d2().optionModule().options().byUid().eq(currentValue).one().blockingGet();
        if (option != null) {
            return option.displayName();
        } else return currentValue;

    }

    private List<Option> generateOptionSets(String uid) {
        List<Option> optionsList;
        optionsList = Sdk.d2().optionModule().options().byOptionSetUid().eq(uid).blockingGet();
        return optionsList;
    }


    @Override
    public int getItemCount() {
        return personList.size();
    }

    class PersonViewHolder extends RecyclerView.ViewHolder {
        TextView textViewFirstName, smallTextView, textViewName;
        LinearLayout linearLayout, lnLinearLayout;
        ImageView rotationImageView;

        PersonViewHolder(View itemView) {
            super(itemView);

            smallTextView = (TextView) itemView.findViewById(R.id.smallTextView);
            textViewName = (TextView) itemView.findViewById(R.id.textViewName);
            linearLayout = (LinearLayout) itemView.findViewById(R.id.linearLayout);
            lnLinearLayout = (LinearLayout) itemView.findViewById(R.id.lnLinearLayout);
            rotationImageView = (ImageView) itemView.findViewById(R.id.rotationImageView);
        }
    }

    private class SaveValueTask extends AsyncTask<Void, Void, String> {
        private String value, displayName, uid, itemuid;

        SaveValueTask(String itemuid, String displayName, String uid, String value) {
            this.displayName = displayName;
            this.itemuid = itemuid;
            this.uid = uid;
            this.value = value;
        }

        @Override
        protected String doInBackground(Void... params) {
            value = extractOptionUid(displayName, uid, value);
            String eventUid = new FormatterClass().getSharedPref("tracked_event", context);
            Log.e("TAG", "Details Here *** Event Tracked " + eventUid);
            String message = "Data Saved";
            if (eventUid != null) {

                try {
                    TrackedEntityDataValueObjectRepository valueRepository =
                            Sdk.d2().trackedEntityModule().trackedEntityDataValues()
                                    .value(eventUid, itemuid);

                    valueRepository.blockingSet(value);
                    String currentValue = valueRepository.blockingExists() ?
                            valueRepository.blockingGet().value() : "";

                    Log.e("TAG", "Details Here *** Saved successfully " + currentValue);

                } catch (Exception d2Error) {
                    message = "Response Saved Successfully with Error " + d2Error.getMessage();
                    d2Error.printStackTrace();
                    Log.e("TAG", "Response Saved Successfully with Error " + d2Error.getMessage());
                }
            } else {
                Log.e("TAG", "Response Saved Successfully No tracked Entity");
                message = "Response Saved Successfully No tracked Entity";
            }
            return message;
        }

        @Override
        protected void onPostExecute(String result) {
            if (result != null) {
                Log.e("TAG", "Post Results Data **** " + result);
            }
        }
    }

// To execute the task


}