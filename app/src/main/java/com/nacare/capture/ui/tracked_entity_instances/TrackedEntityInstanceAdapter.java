package com.nacare.capture.ui.tracked_entity_instances;

import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.paging.DataSource;
import androidx.paging.PagedListAdapter;

import com.nacare.capture.R;
import com.nacare.capture.data.Sdk;
import com.nacare.capture.data.service.DateFormatHelper;
import com.nacare.capture.ui.base.DiffByIdItemCallback;
import com.nacare.capture.ui.base.ListItemWithSyncHolder;
import com.nacare.capture.ui.tracker_import_conflicts.TrackerImportConflictsAdapter;
import com.nacare.capture.data.service.AttributeHelper;
import com.nacare.capture.data.service.ImageHelper;
import com.nacare.capture.data.service.StyleBinderHelper;

import org.hisp.dhis.android.core.arch.call.D2Progress;
import org.hisp.dhis.android.core.common.State;
import org.hisp.dhis.android.core.maintenance.D2Error;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttributeValue;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstance;

import java.text.MessageFormat;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class TrackedEntityInstanceAdapter extends PagedListAdapter<TrackedEntityInstance, ListItemWithSyncHolder> {

    private DataSource<?, TrackedEntityInstance> source;

    public TrackedEntityInstanceAdapter() {
        super(new DiffByIdItemCallback<>());
    }

    @NonNull
    @Override
    public ListItemWithSyncHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_tracked, parent, false);
        return new ListItemWithSyncHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ListItemWithSyncHolder holder, int position) {
        TrackedEntityInstance trackedEntityInstance = getItem(position);
        List<TrackedEntityAttributeValue> values = trackedEntityInstance.trackedEntityAttributeValues();
        holder.title.setText(valueAt(values, AttributeHelper.teiTitle(trackedEntityInstance)));
        holder.subtitle1.setText(valueAt(values, AttributeHelper.teiSubtitle1(trackedEntityInstance)));
        holder.subtitle2.setText(setSubtitle2(values, trackedEntityInstance));
        holder.rightText.setText(DateFormatHelper.formatDate(trackedEntityInstance.created()));

        /****
         * Updated Section
         * */
        holder.dateTextView.setText(DateFormatHelper.formatSimpleDate(trackedEntityInstance.created()));
        holder.firstnameTextView.setText(valueAt(values, AttributeHelper.teiTitle(trackedEntityInstance)));
        holder.actionTextView.setText(valueAt(values, AttributeHelper.teiSubtitle1(trackedEntityInstance)));
        holder.statusTextView.setText(valueAt(values, AttributeHelper.teiTitle(trackedEntityInstance)));
        int colorBlack = ContextCompat.getColor(holder.itemView.getContext(), R.color.black);
        holder.firstnameTextView.setTextColor(colorBlack);
        holder.dateTextView.setTextColor(colorBlack);
        holder.statusTextView.setTextColor(colorBlack);
        holder.actionTextView.setTextColor(colorBlack);

        setImage(trackedEntityInstance, holder);
        holder.delete.setVisibility(View.VISIBLE);
        holder.delete.setOnClickListener(view -> {
            try {
                Sdk.d2().trackedEntityModule().trackedEntityInstances().uid(trackedEntityInstance.uid()).blockingDelete();
                invalidateSource();
                notifyDataSetChanged();
            } catch (D2Error d2Error) {
                d2Error.printStackTrace();
            }
        });
        if (trackedEntityInstance.aggregatedSyncState() == State.TO_POST ||
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
        }
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
}
