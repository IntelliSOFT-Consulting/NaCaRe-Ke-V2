package com.nacare.capture.data.adapters;

import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.paging.DataSource;
import androidx.paging.PagedListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.nacare.capture.R;
import com.nacare.capture.data.Sdk;
import com.nacare.capture.data.service.ActivityStarter;
import com.nacare.capture.data.service.AttributeHelper;
import com.nacare.capture.data.service.DateFormatHelper;
import com.nacare.capture.data.service.ImageHelper;
import com.nacare.capture.data.service.StyleBinderHelper;
import com.nacare.capture.ui.base.DiffByIdItemCallback;
import com.nacare.capture.ui.base.ListItemWithSyncHolder;
import com.nacare.capture.ui.main.custom.TrackedEntityInstanceActivity;
import com.nacare.capture.ui.tracked_entity_instances.search.TrackedEntityInstanceSearchActivity;
import com.nacare.capture.ui.tracker_import_conflicts.TrackerImportConflictsAdapter;

import org.hisp.dhis.android.core.arch.call.D2Progress;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttributeValue;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstance;

import java.text.MessageFormat;
import java.util.List;

import io.reactivex.Observable;

public class TrackedResultsAdapter extends PagedListAdapter<TrackedEntityInstance, ListItemWithSyncHolder> {

    private DataSource<?, TrackedEntityInstance> source;

    private OnClickListener onClickListener;

    public TrackedResultsAdapter(OnClickListener onClickListener) {
        super(new DiffByIdItemCallback<>());
        this.onClickListener = onClickListener;
    }

    @NonNull
    @Override
    public ListItemWithSyncHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_search, parent, false);
        return new ListItemWithSyncHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ListItemWithSyncHolder holder, int position) {
        TrackedEntityInstance trackedEntityInstance = getItem(position);
        List<TrackedEntityAttributeValue> values = trackedEntityInstance.trackedEntityAttributeValues();
        /****
         * Updated Section
         * */
        holder.dateTextView.setText(valueAt(values, AttributeHelper.uniqueID(trackedEntityInstance)));
        holder.firstnameTextView.setText(valueAt(values, "MiXrdHDZ6Hw"));
        String first = valueAt(values, "R1vaUuILrDy");
        String last = valueAt(values, "hzVijy6tEUF");
        String name = first + " " + last;
        holder.nameTextView.setText(name);
        holder.statusTextView.setText(valueAt(values, "eFbT7iTnljR"));
        holder.actionTextView.setText(valueAt(values, AttributeHelper.teiSubtitle1(trackedEntityInstance)));

        int colorBlack = ContextCompat.getColor(holder.itemView.getContext(), R.color.black);
        holder.firstnameTextView.setTextColor(colorBlack);
        holder.dateTextView.setTextColor(colorBlack);
        holder.statusTextView.setTextColor(colorBlack);
        holder.actionTextView.setTextColor(colorBlack);
        holder.nameTextView.setTextColor(colorBlack);

        holder.itemView.setOnClickListener(v -> {
            onClickListener.onClick(trackedEntityInstance);

           /* AlertDialog.Builder builder = new AlertDialog.Builder(context);
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
            nextButton.setOnClickListener(c -> {
                alertDialog.dismiss();
                ActivityStarter.startActivity(
                        TrackedEntityInstanceSearchActivity.this, TrackedEntityInstanceActivity.getIntent(this), true);
            });

            alertDialog.show();*/
        });

       /* if (trackedEntityInstance.aggregatedSyncState() == State.TO_POST ||
                trackedEntityInstance.aggregatedSyncState() == State.TO_UPDATE) {
            holder.sync.setVisibility(View.VISIBLE);
            holder.sync.setOnClickListener(v -> {
                holder.sync.setVisibility(View.GONE);
                RotateAnimation rotateAnim = new RotateAnimation(0f, 359f,
                        Animation.RELATIVE_TO_SELF, 0.5f,
                        Animation.RELATIVE_TO_SELF, 0.5f);
                rotateAnim.setDuration(2500);
                rotateAnim.setRepeatMode(Animation.INFINITE);
                holder.syncIcon.startAnimation(rotateAnim);

                Disposable disposable = syncTei(trackedEntityInstance.uid())
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                data -> {
                                },
                                Throwable::printStackTrace,
                                () -> {
                                    holder.syncIcon.clearAnimation();
                                    invalidateSource();
                                }
                        );
            });
        } else {
            holder.sync.setVisibility(View.GONE);
            holder.sync.setOnClickListener(null);
        }*/
        StyleBinderHelper.setBackgroundColor(R.color.colorAccentDark, holder.icon);
        StyleBinderHelper.setState(trackedEntityInstance.aggregatedSyncState(), holder.syncIcon);
        setConflicts(trackedEntityInstance.uid(), holder);
    }

    private Observable<D2Progress> syncTei(String teiUid) {
        return Sdk.d2().trackedEntityModule().trackedEntityInstances()
                .byUid().eq(teiUid)
                .upload();
    }

    private String valueAt(List<TrackedEntityAttributeValue> values, String attributeUid) {
        for (TrackedEntityAttributeValue attributeValue : values) {
            if (attributeValue.trackedEntityAttribute().equals(attributeUid)) {
                return attributeValue.value();
            }
        }

        return null;
    }

    private String setSubtitle2(List<TrackedEntityAttributeValue> values, TrackedEntityInstance trackedEntityInstance) {
        String firstSubtitle = valueAt(values, AttributeHelper.teiSubtitle2First(trackedEntityInstance));
        String secondSubtitle = valueAt(values, AttributeHelper.teiSubtitle2Second(trackedEntityInstance));
        if (firstSubtitle != null) {
            if (secondSubtitle != null) {
                return MessageFormat.format("{0} - {1}", firstSubtitle, secondSubtitle);
            } else {
                return firstSubtitle;
            }
        } else {
            return secondSubtitle;
        }
    }

    private void setConflicts(String trackedEntityInstanceUid, ListItemWithSyncHolder holder) {
        TrackerImportConflictsAdapter adapter = new TrackerImportConflictsAdapter();
        holder.recyclerView.setAdapter(adapter);
        adapter.setTrackerImportConflicts(Sdk.d2().importModule().trackerImportConflicts()
                .byTrackedEntityInstanceUid().eq(trackedEntityInstanceUid).blockingGet());
    }

    private void setImage(TrackedEntityInstance trackedEntityInstance, ListItemWithSyncHolder holder) {
        Bitmap teiImage = ImageHelper.getBitmap(trackedEntityInstance);
        if (teiImage != null) {
            holder.icon.setVisibility(View.INVISIBLE);
            holder.bitmap.setImageBitmap(teiImage);
            holder.bitmap.setVisibility(View.VISIBLE);
        } else {
            holder.bitmap.setVisibility(View.GONE);
            holder.icon.setImageResource(R.drawable.ic_person_black_24dp);
            holder.icon.setVisibility(View.VISIBLE);
        }
    }

    public void setSource(DataSource<?, TrackedEntityInstance> dataSource) {
        this.source = dataSource;
    }

    public void invalidateSource() {
        source.invalidate();
    }

    public interface OnClickListener {
        void onClick(TrackedEntityInstance item);
    }
}
