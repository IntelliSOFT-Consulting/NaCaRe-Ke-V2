package com.nacare.capture.ui.events;

import static com.nacare.capture.data.service.StyleBinderHelper.setBackgroundColor;
import static com.nacare.capture.data.service.StyleBinderHelper.setState;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.paging.DataSource;
import androidx.paging.PagedListAdapter;

import com.nacare.capture.R;
import com.nacare.capture.data.Sdk;
import com.nacare.capture.data.service.ActivityStarter;
import com.nacare.capture.data.service.DateFormatHelper;
import com.nacare.capture.ui.base.DiffByIdItemCallback;
import com.nacare.capture.ui.base.ListItemWithSyncHolder;
import com.nacare.capture.ui.event_form.EventFormActivity;
import com.nacare.capture.ui.tracker_import_conflicts.TrackerImportConflictsAdapter;

import org.hisp.dhis.android.core.category.CategoryOptionCombo;
import org.hisp.dhis.android.core.event.Event;
import org.hisp.dhis.android.core.maintenance.D2Error;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit;
import org.hisp.dhis.android.core.program.ProgramStageDataElement;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityDataValue;

import java.util.ArrayList;
import java.util.List;

public class EventAdapter extends PagedListAdapter<Event, ListItemWithSyncHolder> {

    private final AppCompatActivity activity;
    private DataSource<?, Event> source;

    public EventAdapter(AppCompatActivity activity) {
        super(new DiffByIdItemCallback<>());
        this.activity = activity;
    }

    @NonNull
    @Override
    public ListItemWithSyncHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_update, parent, false);
        return new ListItemWithSyncHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ListItemWithSyncHolder holder, int position) {
        Event event = getItem(position);
        List<TrackedEntityDataValue> values = new ArrayList<>(event.trackedEntityDataValues());
        holder.title.setText(orgUnit(event.organisationUnit()).displayName());
        holder.subtitle1.setText(valueAt(values, event.programStage()));
        holder.subtitle2.setText(optionCombo(event.attributeOptionCombo()).displayName());
        holder.rightText.setText(DateFormatHelper.formatDate(event.eventDate()));


        holder.dateTextView.setText(DateFormatHelper.formatSimpleDate(event.eventDate()));
        holder.statusTextView.setText(event.status().name());
        holder.actionTextView.setText("Edit");
        int colorBlack = ContextCompat.getColor(holder.itemView.getContext(), R.color.black);

        holder.dateTextView.setTextColor(colorBlack);
        holder.statusTextView.setTextColor(colorBlack);
        holder.actionTextView.setTextColor(colorBlack);
        holder.icon.setImageResource(R.drawable.ic_programs_black_24dp);
        holder.delete.setVisibility(View.VISIBLE);
        holder.delete.setOnClickListener(view -> {
            try {
                Sdk.d2().eventModule().events().uid(event.uid()).blockingDelete();
                invalidateSource();
                notifyDataSetChanged();
            } catch (D2Error d2Error) {
                d2Error.printStackTrace();
            }
        });
        setBackgroundColor(R.color.colorAccentDark, holder.icon);
        setState(event.aggregatedSyncState(), holder.syncIcon);
        setConflicts(event.uid(), holder);

        holder.itemView.setOnClickListener(view -> ActivityStarter.startActivity(
                activity,
                EventFormActivity.getFormActivityIntent(
                        activity,
                        event.uid(),
                        event.program(),
                        event.organisationUnit(),
                        EventFormActivity.FormType.CHECK
                ), false
        ));
    }

    private OrganisationUnit orgUnit(String orgUnitUid) {
        return Sdk.d2().organisationUnitModule().organisationUnits().uid(orgUnitUid).blockingGet();
    }

    private String valueAt(List<TrackedEntityDataValue> values, String stageUid) {
        for (TrackedEntityDataValue dataValue : values) {
            ProgramStageDataElement programStageDataElement = Sdk.d2().programModule().programStageDataElements()
                    .byDataElement().eq(dataValue.dataElement())
                    .byProgramStage().eq(stageUid)
                    .one().blockingGet();
            if (programStageDataElement.displayInReports()) {
                return String.format("%s: %s", programStageDataElement.displayName(), dataValue.value());
            }
        }

        return null;
    }

    private CategoryOptionCombo optionCombo(String attrOptionCombo) {
        return Sdk.d2().categoryModule().categoryOptionCombos().uid(attrOptionCombo).blockingGet();
    }

    private void setConflicts(String trackedEntityInstanceUid, ListItemWithSyncHolder holder) {
        TrackerImportConflictsAdapter adapter = new TrackerImportConflictsAdapter();
        holder.recyclerView.setAdapter(adapter);
        adapter.setTrackerImportConflicts(Sdk.d2().importModule().trackerImportConflicts()
                .byTrackedEntityInstanceUid().eq(trackedEntityInstanceUid).blockingGet());
    }

    public void setSource(DataSource<?, Event> dataSource) {
        this.source = dataSource;
    }

    public void invalidateSource() {
        source.invalidate();
    }
}
