package com.nacare.capture.data.adapters;


import static android.text.TextUtils.isEmpty;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.text.Editable;
import android.text.TextUtils;
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
import android.widget.Toast;

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
import com.nacare.capture.data.service.ActivityStarter;
import com.nacare.capture.data.service.DateFormatHelper;
import com.nacare.capture.ui.enrollment_form.EnrollmentFormActivity;
import com.nacare.capture.ui.main.custom.TrackedEntityInstanceActivity;
import com.nacare.capture.ui.tracked_entity_instances.search.TrackedEntityInstanceSearchActivity;

import org.hisp.dhis.android.core.dataelement.DataElement;
import org.hisp.dhis.android.core.option.Option;
import org.hisp.dhis.android.core.program.ProgramTrackedEntityAttribute;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttribute;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttributeValue;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttributeValueObjectRepository;
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
        ExpandableItem person = personList.get(position);
        holder.textViewName.setText(person.getGroupName());
        holder.linearLayout.setVisibility(View.GONE);
        if (currentPosition == position) {
            holder.lnLinearLayout.setBackgroundColor(ContextCompat.getColor(context, R.color.selected));
            holder.rotationImageView.setRotation(0);
            holder.linearLayout.setVisibility(View.VISIBLE);
        } else {
            holder.linearLayout.setVisibility(View.GONE);
        }
        holder.textViewName.setOnClickListener(view -> {
            currentPosition = position;
            notifyDataSetChanged();
        });
        if (person.getChildItems() != null) {
            holder.smallTextView.setText("0/" + person.getChildItems().size());
            for (TrackedEntityAttribute trackedEntityAttribute : person.getChildItems()) {
                createSearchFields(holder.linearLayout, trackedEntityAttribute, extractCurrentAttributeValue(trackedEntityAttribute));
            }
        }
        if (person.getDataElements() != null) {
            holder.smallTextView.setText("0/" + person.getDataElements().size());
            for (DataElement dataElement : person.getDataElements()) {
                createSearchFieldsDataElement(holder.linearLayout, dataElement, extractCurrentValue(dataElement));
            }
        }
        LayoutInflater inflater = LayoutInflater.from(context);
        LinearLayout itemView = (LinearLayout) inflater.inflate(R.layout.item_submit_cancel, holder.linearLayout, false);
//        holder.linearLayout.addView(itemView);
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

    private String extractCurrentAttributeValue(TrackedEntityAttribute trackedEntityAttribute) {
        String orgCode = new FormatterClass().getSharedPref("orgCode", context);
        String selectedTei = new FormatterClass().getSharedPref("selectedTei", context);
        String programUid = new FormatterClass().getSharedPref("programUid", context);

        TrackedEntityInstanceCollectionRepository teiRepository = Sdk.d2().trackedEntityModule().trackedEntityInstances().withTrackedEntityAttributeValues();

        List<String> programUids = new ArrayList<>();
        programUids.add(programUid);
        TrackedEntityInstance tei = teiRepository.byProgramUids(programUids)
//                .byOrganisationUnitUid().eq(orgCode)
                .byUid().eq(selectedTei).one().blockingGet();

        if (tei != null) {
            Log.e("TAG", "Current Value ****" + tei.trackedEntityAttributeValues());
            for (TrackedEntityAttributeValue attributeValue : tei.trackedEntityAttributeValues()) {

                if (attributeValue.trackedEntityAttribute().equals(trackedEntityAttribute.uid())) {
                    return attributeValue.value();
                }
            }

        }

        return null;
//        Stewring selectedTei = new FormatterClass().getSharedPref("selectedTei", context);
//        TrackedEntityDataValueObjectRepository valueRepository =
//                Sdk.d2().trackedEntityModule().trackedEntityDataValues()
//                        .value(trackedEntityAttribute.uid(), selectedTei);
//
//        return valueRepository.blockingExists() ?
//                valueRepository.blockingGet().value() : "";
    }

    private String valueAt(List<TrackedEntityAttributeValue> values, String attributeUid) {
        for (TrackedEntityAttributeValue attributeValue : values) {
            if (attributeValue.trackedEntityAttribute().equals(attributeUid)) {
                return attributeValue.value();
            }
        }

        return null;
    }

    private String extractCurrentValue(DataElement dataElement) {
        String selectedTei = new FormatterClass().getSharedPref("selectedTei", context);
        TrackedEntityDataValueObjectRepository valueRepository = Sdk.d2().trackedEntityModule().trackedEntityDataValues().value(dataElement.uid(), selectedTei);

        return valueRepository.blockingExists() ? valueRepository.blockingGet().value() : "";
    }

    private void createSearchFieldsDataElement(LinearLayout linearLayout, DataElement item, String value) {
        String valueType = item.valueType().toString();
        String label = item.displayName();
        LayoutInflater inflater = LayoutInflater.from(context);

        if ("TEXT".equals(valueType)) {
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
                        String selectedTei = new FormatterClass().getSharedPref("selectedTei", context);
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
                ArrayAdapter<String> adp = new ArrayAdapter<>(context, android.R.layout.simple_list_item_1, optionsStringList);

                tvName.setText(item.displayName());
                autoCompleteTextView.setAdapter(adp);
                adp.notifyDataSetChanged();
//                stringMap.put(item.uid(), autoCompleteTextView);
                linearLayout.addView(itemView);
            }
        }

    }

    private void createSearchFields(LinearLayout linearLayout, TrackedEntityAttribute item, String value) {
        String valueType = item.valueType().toString();
        String label = item.displayName();
        LayoutInflater inflater = LayoutInflater.from(context);

        if ("TEXT".equals(valueType)) {
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
                            String selectedTei = new FormatterClass().getSharedPref("selectedTei", context);
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
                            Log.e("TAG", "Details Here *** " + item.uid());
                            Log.e("TAG", "Details Here *** " + item.displayName());
                            Log.e("TAG", "Details Here *** " + item.optionSet().uid());
                            Log.e("TAG", "Details Here *** " + value);
                            new SaveValueTask(item.uid(), item.displayName(), item.optionSet().uid(), value).execute();
                        }
                    }

                    @Override
                    public void afterTextChanged(Editable editable) {

                    }
                });
                linearLayout.addView(itemView);
            }
        } else if ("DATE".equals(valueType)) {
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
            editText.setOnClickListener(v -> {
                Calendar calendar = Calendar.getInstance();
                new DatePickerDialog(context, (datePicker, year, month, day) -> {
                    String selectedTei = new FormatterClass().getSharedPref("selectedTei", context);
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
        return DateFormatHelper.formatSimpleDate(date);
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
            String selectedTei = new FormatterClass().getSharedPref("selectedTei", context);
            String message = "Data Saved";
            if (selectedTei != null) {
                TrackedEntityAttributeValueObjectRepository valueRepository = Sdk.d2().trackedEntityModule().
                        trackedEntityAttributeValues().value(itemuid, selectedTei);
                String currentValue = valueRepository.blockingExists() ? valueRepository.blockingGet().value() : "";
                if (currentValue == null) currentValue = "";

                try {
                    valueRepository.blockingSet(value);

                } catch (Exception d2Error) {
                    message = "Response Saved Successfully with Error " + d2Error.getMessage();
                    d2Error.printStackTrace();
                    Log.e("TAG", "Response Saved Successfully with Error " + d2Error.getMessage());


                } finally {
                    Log.e("TAG", "Response Saved Successfully *** " + value);
                    message = "Response Saved Successfully *** " + value;
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
                Log.e("TAG", result);
            }
        }
    }

// To execute the task


}