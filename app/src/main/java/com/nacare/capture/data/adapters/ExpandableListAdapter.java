package com.nacare.capture.data.adapters;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.nacare.capture.R;
import com.nacare.capture.data.Sdk;
import com.nacare.capture.data.model.ExpandableItem;

import org.hisp.dhis.android.core.dataelement.DataElement;
import org.hisp.dhis.android.core.option.Option;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttribute;

import java.util.ArrayList;
import java.util.List;

public class ExpandableListAdapter extends RecyclerView.Adapter<ExpandableListAdapter.PersonViewHolder> {


    private List<ExpandableItem> personList;
    private Context context;

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
            holder.linearLayout.setVisibility(View.VISIBLE);
        }
        holder.textViewName.setOnClickListener(view -> {
            currentPosition = position;
            notifyDataSetChanged();
        });
        if (person.getChildItems() != null) {
            holder.smallTextView.setText("0/" + person.getChildItems().size());
            for (TrackedEntityAttribute trackedEntityAttribute : person.getChildItems()) {
                createSearchFields(holder.linearLayout, trackedEntityAttribute);
            }
        }
        if (person.getDataElements() != null) {
            holder.smallTextView.setText("0/" + person.getDataElements().size());
            for (DataElement dataElement : person.getDataElements()) {
                createSearchFieldsDataElement(holder.linearLayout, dataElement);
            }
        }

    }

    private void createSearchFieldsDataElement(LinearLayout linearLayout, DataElement item) {
        String valueType = item.valueType().toString();
        String label = item.displayName();
        LayoutInflater inflater = LayoutInflater.from(context);

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

//                stringMap.put(item.uid(), editText);
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
                    optionsStringList.add(option.displayName());
                }
                ArrayAdapter<String> adp = new ArrayAdapter<>(
                        context,
                        android.R.layout.simple_list_item_1,
                        optionsStringList
                );

                tvName.setText(item.displayName());
                autoCompleteTextView.setAdapter(adp);
                adp.notifyDataSetChanged();
//                stringMap.put(item.uid(), autoCompleteTextView);
                linearLayout.addView(itemView);
            }
        }

    }

    private void createSearchFields(LinearLayout linearLayout, TrackedEntityAttribute item) {
        String valueType = item.valueType().toString();
        String label = item.displayName();
        LayoutInflater inflater = LayoutInflater.from(context);

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

//                stringMap.put(item.uid(), editText);
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
                    optionsStringList.add(option.displayName());
                }
                ArrayAdapter<String> adp = new ArrayAdapter<>(
                        context,
                        android.R.layout.simple_list_item_1,
                        optionsStringList
                );

                tvName.setText(item.displayName());
                autoCompleteTextView.setAdapter(adp);
                adp.notifyDataSetChanged();
//                stringMap.put(item.uid(), autoCompleteTextView);
                linearLayout.addView(itemView);
            }
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
    public int getItemCount() {
        return personList.size();
    }

    class PersonViewHolder extends RecyclerView.ViewHolder {
        TextView textViewFirstName, smallTextView, textViewName;
        LinearLayout linearLayout;

        PersonViewHolder(View itemView) {
            super(itemView);

            smallTextView = (TextView) itemView.findViewById(R.id.smallTextView);
            textViewName = (TextView) itemView.findViewById(R.id.textViewName);
            linearLayout = (LinearLayout) itemView.findViewById(R.id.linearLayout);
        }
    }
}